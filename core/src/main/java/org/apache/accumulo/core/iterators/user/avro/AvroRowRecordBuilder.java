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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.util.Utf8;
import org.apache.hadoop.io.Text;

/**
 * This class collects all cells of interested into an AVRO Generic Record.
 * 
 * Cells with non-empty column family and column qualifier are stored in nested AVRO records. Cells
 * with empty column qualifier are stored in the top-level record.
 * 
 * Example:
 * 
 * <pre>
 * cf1, cq1,  abc
 * cf1, cq2,  3.2
 * cf2, null, 6
 * cf3, cq3,  def
 * </pre>
 * 
 * Avro Record:
 * 
 * <pre>
 * { 
 * 	 cf1: { cq1: "abc", cq2: 3.2 }, 
 * 	 cf2: 6, 
 *   cf3: { cq3: "def " }
 * }
 * </pre>
 */
public class AvroRowRecordBuilder {
  // record builder for the root
  private GenericRecordBuilder rootRecordBuilder;

  // fast lookup by using Text as key type
  private Map<Text,SubGenericRecordBuilder> columnFamilyRecordBuilder = new HashMap<>();

  // allocate once to re-use memory
  private Text columnFamilyText = new Text();
  private Text columnQualifierText = new Text();

  /**
   * Base class to allow common dispatching to both TopLevel and Nested fields
   */
  abstract class SubGenericRecordBuilder {
    public void set(Text columnQualifier, Object value) {}

    public void endRow() {}

    public void startRow() {}
  }

  /**
   * Ignores the columnQualifier and just stores the value
   */
  class TopLevelGenericRecordBuilder extends SubGenericRecordBuilder {

    private Field field;

    public TopLevelGenericRecordBuilder(Field field) {
      this.field = field;
    }

    @Override
    public void set(Text columnQualifier, Object value) {
      rootRecordBuilder.set(this.field, value);
    }
  }

  /**
   * Uses the columnQualifier to find the nested AVRO field.
   */
  class NestedGenericRecordBuilder extends SubGenericRecordBuilder {
    // used for fast clear
    private Field[] nestedFields;
    // wrapped builder
    private GenericRecordBuilder recordBuilder;
    // used for fast value setting
    private Map<Text,Field> fieldLookup;
    // co-relate back to parent record
    private Field parentField;

    public NestedGenericRecordBuilder(Schema schema, Field parentField) {
      this.parentField = parentField;

      nestedFields = schema.getFields().toArray(new Field[0]);

      recordBuilder = new GenericRecordBuilder(schema);

      // make sure we made from Text (not string) to field
      // a) this avoids memory allocation for the string object
      // b) this allows use to get the field index in the avro record directly (no
      // field index lookup required)
      fieldLookup = Stream.of(nestedFields)
          .collect(Collectors.toMap(f -> new Text(f.name()), Function.identity()));
    }

    public Field getParentField() {
      return parentField;
    }

    @Override
    public void set(Text columnQualifer, Object value) {
      // from hadoop text to field pos
      recordBuilder.set(fieldLookup.get(columnQualifer), value);
    }

    @Override
    public void startRow() {
      for (Field field : nestedFields)
        recordBuilder.clear(field);
    }

    @Override
    public void endRow() {
      rootRecordBuilder.set(parentField, recordBuilder.build());
    }
  }

  public AvroRowRecordBuilder(Schema schema) {
    // separate record builder for the root record holding the nested schemas
    this.rootRecordBuilder = new GenericRecordBuilder(schema);

    // setup GenericRecordBuilder for each column family
    for (Field field : schema.getFields()) {
      Schema nestedSchema = field.schema();

      // field is a data field that needs collection from cells
      // nested vs top level element
      SubGenericRecordBuilder builder = nestedSchema.getType() == Type.RECORD
          ? new NestedGenericRecordBuilder(nestedSchema, field)
          : new TopLevelGenericRecordBuilder(field);

      // store in map for fast lookup
      columnFamilyRecordBuilder.put(new Text(field.name()), builder);
    }
  }

  public void startRow() {
    // clear all fields
    for (SubGenericRecordBuilder builder : columnFamilyRecordBuilder.values())
      builder.startRow();
  }

  public Object decodeValue(Value value, RowBuilderType type) {
    switch (type) {
      case String:
        // avoid byte array copying
        return new Utf8(value.get());
      default:
        // use existing accumulo encoders to decode values
        return type.getEncoder().decode(value.get());
    }
  }

  public void processCell(Key key, Value value, RowBuilderType type) {
    // passing columnFamilyText to re-use memory
    SubGenericRecordBuilder builder =
        columnFamilyRecordBuilder.get(key.getColumnFamily(columnFamilyText));

    // passing columnQualifierText to re-use memory
    Object decodedValue = decodeValue(value, type);

    // pass decoded value into AVRO generic record
    builder.set(key.getColumnQualifier(columnQualifierText), decodedValue);
  }

  public GenericRecordBuilder endRow() {
    // populate root record
    for (SubGenericRecordBuilder nestedRecordBuilder : columnFamilyRecordBuilder.values())
      nestedRecordBuilder.endRow();

    return rootRecordBuilder;
  }
}
