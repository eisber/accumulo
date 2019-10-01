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

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.accumulo.core.iterators.user.avro.juel.AvroELContext;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.hadoop.io.Text;

/**
 * Evaluates the user-supplied filter (JUEL syntax) against the constructed AVRO record.
 * 
 * @implNote filter operates on AVRO Record object, not on the serialized version.
 */
public class AvroRowFilter implements AvroRowConsumer {

  public static AvroRowFilter create(Schema schema, String filter) {
    return filter == null || filter.trim().length() == 0 ? null : new AvroRowFilter(schema, filter);
  }

  /**
   * JUEL expression context exposing AVRO GenericRecord
   */
  private AvroELContext expressionContext;

  /**
   * JUEL filter expression
   */
  private ValueExpression filterExpression;

  private AvroRowFilter(Schema schema, String filter) {
    this.expressionContext = new AvroELContext(schema);

    ExpressionFactory factory = ExpressionFactory.newInstance();

    this.filterExpression = factory.createValueExpression(expressionContext, filter, boolean.class);
  }

  @Override
  public IndexedRecord consume(Text rowKey, IndexedRecord record) throws IOException {
    // link AVRO record with JUEL expression context
    this.expressionContext.setCurrent(rowKey, record);

    return (boolean) filterExpression.getValue(this.expressionContext) ? record : null;
  }
}
