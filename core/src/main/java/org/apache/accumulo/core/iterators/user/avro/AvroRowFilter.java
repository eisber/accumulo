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

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.accumulo.core.iterators.user.avro.juel.AvroELContext;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.hadoop.io.Text;

public class AvroRowFilter {

  /**
   * JUEL expression context exposing AVRO GenericRecord
   */
  private AvroELContext expressionContext;

  /**
   * JUEL filter expression
   */
  private ValueExpression filterExpression;

  public AvroRowFilter(Schema schema, RowBuilderField[] schemaFields, String filter) {
    this.expressionContext = new AvroELContext(schema, schemaFields);

    ExpressionFactory factory = ExpressionFactory.newInstance();

    this.filterExpression = factory.createValueExpression(expressionContext, filter, boolean.class);
  }

  public boolean endRow(Text rowKey, Record record) {
    if (this.filterExpression != null) {
      this.expressionContext.setCurrent(rowKey, record);

      if (!(boolean) filterExpression.getValue(this.expressionContext))
        return true;
    }

    return false;
  }
}
