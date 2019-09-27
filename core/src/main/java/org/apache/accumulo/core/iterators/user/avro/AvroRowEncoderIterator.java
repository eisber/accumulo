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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.io.Text;

public class AvroRowEncoderIterator extends RowBuilderIterator {
  public static final String FILTER = "filter";
  public static final String MLEAP_BUNDLE = "mleap.bundle";

  // root schema holding fields for each column family
  private Schema schema;

  private AvroRowRecordBuilder recordBuilder;

  private AvroRowSerializer serializer;

  private AvroRowComputedColumns computedColumns;

  private AvroRowFilter filter;

  private AvroRowMLeap mleap;

  @Override
  protected void startRow(Text rowKey) throws IOException {
    this.serializer.startRow();

    this.recordBuilder.startRow();
  }

  @Override
  protected void processCell(Key key, Value value, RowBuilderType type) throws IOException {
    this.recordBuilder.processCell(key, value, type);
  }

  @Override
  protected byte[] endRow(Text rowKey) throws IOException {
    // finalize the GenericRecord
    GenericRecordBuilder genericRecordBuilder = this.recordBuilder.endRow();

    // compute expression based columns
    Record record = this.computedColumns.endRow(rowKey, genericRecordBuilder);

    // check if filter matches
    if (this.filter.endRow(rowKey, record))
      return null;

    this.mleap.endRow(record);

    // serialize
    return this.serializer.endRow(record);
  }

  @Override
  public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options,
      IteratorEnvironment env) throws IOException {

    super.init(source, options, env);

    // initialize compute columns (only definitions are initialized, need to wait
    // for schema)
    this.computedColumns = new AvroRowComputedColumns(options);

    // union( user-supplied fields + computed fields )
    Collection<RowBuilderField> allFields = this.computedColumns.getComputedSchemaFields();
    allFields.addAll(Arrays.asList(super.schemaFields));

    // build the AVRO schema
    this.schema = AvroSchemaBuilder.buildSchema(allFields);

    // initialize the record builder
    this.recordBuilder = new AvroRowRecordBuilder(this.schema);

    // finalize the computed columns
    this.computedColumns.initialize(this.schema, this.schemaFields);

    // setup binary serializer
    this.serializer = new AvroRowSerializer(schema);

    // setup JUEL based filter
    this.filter = new AvroRowFilter(schema, schemaFields, options.get(FILTER));

    // setup ML bundle
    this.mleap = new AvroRowMLeap(schema, options.get(MLEAP_BUNDLE));
  }
}
