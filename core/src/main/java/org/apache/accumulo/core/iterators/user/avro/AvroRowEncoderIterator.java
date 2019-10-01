/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.accumulo.core.iterators.user.avro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.PartialKey;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.OptionDescriber;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.iterators.user.avro.processors.AvroRowComputedColumns;
import org.apache.accumulo.core.iterators.user.avro.processors.AvroRowConsumer;
import org.apache.accumulo.core.iterators.user.avro.processors.AvroRowFilter;
import org.apache.accumulo.core.iterators.user.avro.processors.AvroRowSerializer;
import org.apache.accumulo.core.iterators.user.avro.record.AvroFastRecord;
import org.apache.accumulo.core.iterators.user.avro.record.AvroSchemaBuilder;
import org.apache.accumulo.core.iterators.user.avro.record.RowBuilderCellConsumer;
import org.apache.accumulo.core.iterators.user.avro.record.RowBuilderField;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.hadoop.io.Text;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Backend iterator for Accumulo Connector for Apache Spark.
 * 
 * Features:
 * 
 * <ul>
 * <li>Combines selected key/value pairs into a single AVRO encoded row</li>
 * <li>Output schema convention: column family are top-level keys, column qualifiers are nested
 * record fields.</li>
 * <li>Row-level filtering through user-supplied Java Unified Expression Language (JUEL)-encoded
 * filter constraint.</li>
 * <li>Compute columns based on other row-level columns.</li>
 * <li>Schema less serialization performed to safe bandwidth.</li>
 * </ul>
 */
public class AvroRowEncoderIterator implements SortedKeyValueIterator<Key,Value>, OptionDescriber {
  /**
   * Key for the schema input option.
   */
  public static final String SCHEMA = "schema";

  /**
   * Key for filter option.
   */
  public static final String FILTER = "filter";

  /**
   * Key for mleap bundle option.
   */
  public static final String MLEAP_BUNDLE = "mleap.bundle";

  /**
   * A custom and fast implementation of an Avro record.
   */
  private AvroFastRecord rootRecord;

  /**
   * The final serializer creating the binary array.
   */
  private AvroRowSerializer serializer;

  /**
   * List of processors executed when the row was build up.
   */
  private List<AvroRowConsumer> processors;

  /**
   * Fast lookup table from "column family" to "column qualifier" to "type". If it's not in this
   * mapping we can skip the cell. Using this order as the cells are sorted by family, qualifier
   */
  protected Map<ByteSequence,Map<ByteSequence,RowBuilderCellConsumer>> cellToColumnMap;

  /**
   * The source iterator;
   */
  protected SortedKeyValueIterator<Key,Value> sourceIter;

  /**
   * The current key.
   */
  private Key topKey = null;

  /**
   * The current value.
   */
  private Value topValue = null;

  @Override
  public boolean validateOptions(Map<String,String> options) {
    try {
      new ObjectMapper().readValue(options.get(SCHEMA), RowBuilderField[].class);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options,
      IteratorEnvironment env) throws IOException {

    this.sourceIter = source;

    // build the lookup table for the cells we care for from the user-supplied JSON
    RowBuilderField[] schemaFields =
        new ObjectMapper().readValue(options.get(SCHEMA), RowBuilderField[].class);

    // union( user-supplied fields + computed fields )
    ArrayList<RowBuilderField> allFields = new ArrayList<>(Arrays.asList(schemaFields));

    // initialize compute columns (only definitions are initialized, need to wait
    // for schema)
    AvroRowComputedColumns computedColumns = AvroRowComputedColumns.create(options);
    if (computedColumns != null)
      allFields.addAll(computedColumns.getComputedSchemaFields());

    // build the AVRO schema
    Schema schema = AvroSchemaBuilder.buildSchema(allFields);

    // initialize the record builder
    this.rootRecord = new AvroFastRecord(schema);

    // provide fast lookup map
    this.cellToColumnMap = AvroFastRecord.createCellToFieldMap(rootRecord);

    // feed the final schema bag
    if (computedColumns != null)
      computedColumns.initialize(schema);

    // setup binary serializer
    this.serializer = new AvroRowSerializer(schema);

    // setup ML bundle
    // this.mleap = new AvroRowMLeap(schema, options.get(MLEAP_BUNDLE));

    this.processors = Arrays.stream(new AvroRowConsumer[] {
        // compute additional columns
        computedColumns,
        // filter row
        AvroRowFilter.create(schema, options.get(FILTER))})
        // compute & filter are optional depending on input
        .filter(x -> x != null).collect(Collectors.toList());
  }

  @Override
  public IteratorOptions describeOptions() {
    IteratorOptions io = new IteratorOptions("AvroRowEncodingIterator",
        "AvroRowEncodingIterator assists in building rows based on user-supplied schema.", null,
        null);

    io.addNamedOption("schema", "Schema selected cells of interest along with type information.");

    return io;
  }

  private void encodeRow() throws IOException {
    byte[] rowValue = null;
    Text currentRow;

    do {
      boolean foundConsumer = false;
      do {
        // no more input row?
        if (!sourceIter.hasTop())
          return;

        currentRow = new Text(sourceIter.getTopKey().getRow());

        ByteSequence currentFamily = null;
        Map<ByteSequence,RowBuilderCellConsumer> currentQualifierMapping = null;

        // start of new record
        this.rootRecord.clear();

        while (sourceIter.hasTop() && sourceIter.getTopKey().getRow().equals(currentRow)) {
          Key sourceTopKey = sourceIter.getTopKey();

          // different column family?
          if (currentFamily == null || !sourceTopKey.getColumnFamilyData().equals(currentFamily)) {
            currentFamily = sourceTopKey.getColumnFamilyData();
            currentQualifierMapping = cellToColumnMap.get(currentFamily);
          }

          // skip if no mapping found
          if (currentQualifierMapping != null) {

            RowBuilderCellConsumer consumer =
                currentQualifierMapping.get(sourceTopKey.getColumnQualifierData());
            if (consumer != null) {
              foundConsumer = true;

              Value value = sourceIter.getTopValue();

              consumer.consume(sourceTopKey, value);
            }
          }

          sourceIter.next();
        }
      } while (!foundConsumer); // skip rows until we found a single feature

      // produce final row
      rowValue = endRow(currentRow);
      // skip if null
    } while (rowValue == null);

    // null doesn't seem to be allowed for cf/cq...
    topKey = new Key(currentRow, new Text("v"), new Text(""));
    topValue = new Value(rowValue);
  }

  private byte[] endRow(Text rowKey) throws IOException {
    // let's start the processing pipeline
    IndexedRecord record = this.rootRecord;

    for (AvroRowConsumer processor : this.processors) {
      record = processor.consume(rowKey, record);

      // stop early
      if (record == null)
        return null;
    }

    // serialize the record
    return this.serializer.serialize(record);
  }

  @Override
  public Key getTopKey() {
    return topKey;
  }

  @Override
  public Value getTopValue() {
    return topValue;
  }

  @Override
  public boolean hasTop() {
    return topKey != null;
  }

  @Override
  public void next() throws IOException {
    topKey = null;
    topValue = null;
    encodeRow();
  }

  @Override
  public void seek(Range range, Collection<ByteSequence> columnFamilies, boolean inclusive)
      throws IOException {
    topKey = null;
    topValue = null;

    // from RowEncodingIterator
    Key sk = range.getStartKey();

    if (sk != null && sk.getColumnFamilyData().length() == 0
        && sk.getColumnQualifierData().length() == 0 && sk.getColumnVisibilityData().length() == 0
        && sk.getTimestamp() == Long.MAX_VALUE && !range.isStartKeyInclusive()) {
      // assuming that we are seeking using a key previously returned by this iterator
      // therefore go to the next row
      Key followingRowKey = sk.followingKey(PartialKey.ROW);
      if (range.getEndKey() != null && followingRowKey.compareTo(range.getEndKey()) > 0)
        return;

      range = new Range(sk.followingKey(PartialKey.ROW), true, range.getEndKey(),
          range.isEndKeyInclusive());
    }

    sourceIter.seek(range, columnFamilies, inclusive);
    encodeRow();
  }

  @Override
  public SortedKeyValueIterator<Key,Value> deepCopy(IteratorEnvironment env) {
    AvroRowEncoderIterator copy = new AvroRowEncoderIterator();

    copy.serializer = serializer;
    copy.rootRecord = new AvroFastRecord(copy.rootRecord.getSchema());
    copy.cellToColumnMap = AvroFastRecord.createCellToFieldMap(copy.rootRecord);
    copy.processors = copy.processors;
    copy.sourceIter = sourceIter.deepCopy(env);

    return copy;
  }
}
