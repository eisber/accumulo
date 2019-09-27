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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.accumulo.core.data.ArrayByteSequence;
import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.PartialKey;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.OptionDescriber;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.hadoop.io.Text;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract base iterator used to build rows based on user-supplied schema information. Furthermore
 * individual cell values are decoded based on schema supplied type information.
 */
public abstract class RowBuilderIterator
    implements SortedKeyValueIterator<Key,Value>, OptionDescriber {
  /**
   * Key for the schema input option.
   */
  public static final String SCHEMA = "schema";

  /**
   * Fast lookup table from "column family" to "column qualifier" to "type". If it's not in this
   * mapping we can skip the cell. Using this order as the cells are sorted by family, qualifier
   */
  private HashMap<ByteSequence,HashMap<ByteSequence,RowBuilderType>> cellToColumnMap;

  /**
   * The source iterator;
   */
  protected SortedKeyValueIterator<Key,Value> sourceIter;

  /**
   * The user-supplied schema.
   */
  protected RowBuilderField[] schemaFields;

  /**
   * The current key.
   */
  private Key topKey = null;

  /**
   * The current value.
   */
  private Value topValue = null;

  /**
   * Signal start of a new row.
   * 
   * @param rowKey
   *          the row key.
   */
  protected abstract void startRow(Text rowKey) throws IOException;

  /**
   * Process the indiviual cell.
   * 
   * @param key
   *          The key of the current row.
   * @param value
   *          The value of the current row.
   * @param type
   *          The type of the current row.
   * @throws IOException
   */
  protected abstract void processCell(Key key, Value value, RowBuilderType type) throws IOException;

  /**
   * Signal the end of the row.
   * 
   * @param rowKey
   *          the row key.
   * @return the value to be output for the row key or null if it should be skipped.
   */
  protected abstract byte[] endRow(Text rowKey) throws IOException;

  private void encodeRow() throws IOException {
    byte[] rowValue = null;
    Text currentRow;

    do {
      boolean foundFeature = false;
      do {
        // no more input row?
        if (!sourceIter.hasTop())
          return;

        currentRow = new Text(sourceIter.getTopKey().getRow());

        ByteSequence currentFamily = null;
        Map<ByteSequence,RowBuilderType> currentQualifierMapping = null;

        // dispatch
        startRow(currentRow);

        while (sourceIter.hasTop() && sourceIter.getTopKey().getRow().equals(currentRow)) {
          Key sourceTopKey = sourceIter.getTopKey();

          // different column family?
          if (currentFamily == null || !sourceTopKey.getColumnFamilyData().equals(currentFamily)) {
            currentFamily = sourceTopKey.getColumnFamilyData();
            currentQualifierMapping = cellToColumnMap.get(currentFamily);
          }

          // skip if no mapping found
          if (currentQualifierMapping != null) {

            RowBuilderType type =
                currentQualifierMapping.get(sourceTopKey.getColumnQualifierData());
            if (type != null) {
              foundFeature = true;

              Value value = sourceIter.getTopValue();

              processCell(sourceTopKey, value, type);
            }
          }

          sourceIter.next();
        }
      } while (!foundFeature); // skip rows until we found a single feature

      // produce final row
      rowValue = endRow(currentRow);
      // skip if null
    } while (rowValue == null);

    // null doesn't seem to be allowed for cf/cq...
    topKey = new Key(currentRow, new Text("v"), new Text(""));
    topValue = new Value(rowValue);
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
    RowBuilderIterator copy;
    try {
      copy = this.getClass().getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // it's immutable, thus shallow = deep copy
    copy.cellToColumnMap = cellToColumnMap;
    copy.sourceIter = sourceIter.deepCopy(env);

    return copy;
  }

  @Override
  public IteratorOptions describeOptions() {
    IteratorOptions io = new IteratorOptions("RowBuilderIterator",
        "RowBuilderIterator assists in building rows based on user-supplied schema.", null, null);

    io.addNamedOption("schema", "Schema selected cells of interest along with type information.");

    return io;
  }

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
    sourceIter = source;

    // build the lookup table for the cells we care for
    schemaFields = new ObjectMapper().readValue(options.get(SCHEMA), RowBuilderField[].class);

    cellToColumnMap = new HashMap<>();
    for (RowBuilderField schemaMappingField : schemaFields) {
      ByteSequence columnFamily = new ArrayByteSequence(schemaMappingField.getColumnFamily());
      HashMap<ByteSequence,RowBuilderType> qualifierMap = cellToColumnMap.get(columnFamily);

      if (qualifierMap == null) {
        qualifierMap = new HashMap<>();
        cellToColumnMap.put(columnFamily, qualifierMap);
      }

      // find the decoder for the respective type
      RowBuilderType type = schemaMappingField.getRowBuilderType();

      String columnQualifier = schemaMappingField.getColumnQualifier();
      ByteSequence columnQualifierByteArraySequence = columnQualifier != null
          ? new ArrayByteSequence(columnQualifier) : new ArrayByteSequence(new byte[0]);

      qualifierMap.put(columnQualifierByteArraySequence, type);
    }
  }
}
