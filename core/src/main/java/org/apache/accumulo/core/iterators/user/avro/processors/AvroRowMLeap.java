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

package org.apache.accumulo.core.iterators.user.avro.processors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.IndexedRecord;
import org.apache.hadoop.io.Text;

import ml.combust.mleap.avro.SchemaConverter;
import ml.combust.mleap.core.types.StructField;
import ml.combust.mleap.core.types.StructType;
import ml.combust.mleap.runtime.frame.ArrayRow;
import ml.combust.mleap.runtime.frame.DefaultLeapFrame;
import ml.combust.mleap.runtime.frame.Row;
import scala.collection.JavaConverters;
import scala.collection.mutable.WrappedArray;

/**
 * Maps AVRO Generic row to MLeap data frame enabling server-side inference.
 */
public class AvroRowMLeap implements AvroRowConsumer {
  private DefaultLeapFrame mleapDataFrame;
  private Object[] mleapValues;
  private Field[] mleapAvroFields;
  private StructType mleapSchema;

  public AvroRowMLeap(Schema schema, String mleapBundle) {

    if (mleapBundle == null || mleapBundle.trim().length() == 0)
      return;

    // mapping the Avro schema to MLeap schema
    List<Field> avroFields = new ArrayList<>();

    List<StructField> mleapFields = new ArrayList<>();
    for (Field field : schema.getFields()) {
      if (field.schema().getType() == Type.RECORD)
        continue;

      avroFields.add(field);
      mleapFields.add(SchemaConverter.avroToMleapField(field, null));
    }

    this.mleapAvroFields = avroFields.toArray(new Field[avroFields.size()]);
    this.mleapSchema = StructType.apply(mleapFields).get();
    this.mleapValues = new Object[this.mleapAvroFields.length];

    this.mleapDataFrame = new DefaultLeapFrame(this.mleapSchema, JavaConverters
        .asScalaIteratorConverter(
            Arrays.stream(new Row[] {new ArrayRow(WrappedArray.make(this.mleapValues))}).iterator())
        .asScala().toSeq());
  }

  @Override
  public IndexedRecord consume(Text rowKey, IndexedRecord record) throws IOException {
    // surface data to MLeap dataframe
    for (int i = 0; i < this.mleapAvroFields.length; i++)
      this.mleapValues[i] = record.get(this.mleapAvroFields[i].pos());

    // TODO: execute
    mleapDataFrame.printSchema();
    mleapDataFrame.show(System.out);

    return record;
  }
}
