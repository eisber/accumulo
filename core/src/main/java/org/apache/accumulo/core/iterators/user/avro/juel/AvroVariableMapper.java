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

package org.apache.accumulo.core.iterators.user.avro.juel;

import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.accumulo.core.iterators.user.avro.RowBuilderField;
import org.apache.accumulo.core.iterators.user.avro.juel.expressions.AvroObjectExpression;
import org.apache.accumulo.core.iterators.user.avro.juel.expressions.AvroVariableExpression;
import org.apache.accumulo.core.iterators.user.avro.juel.expressions.RowKeyVariableExpression;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;

public class AvroVariableMapper extends VariableMapper {

  private static final String ROWKEY_VARIABLE_NAME = "rowKey";

  private Schema schema;
  private RowBuilderField[] schemaFields;

  public AvroVariableMapper(Schema schema, RowBuilderField[] schemaFields) {
    this.schema = schema;
    this.schemaFields = schemaFields;
  }

  private RowBuilderField findRowBuilderFieldByVariable(String variable) {
    for (RowBuilderField smf : schemaFields)
      if (variable.equals(smf.getFilterVariableName()))
        return smf;

    return null;
  }

  /**
   * Resolve variables in this order: rowKey, mapped variables (e.g. v2 = cf1.cq1) and finally using
   * variable expressions.
   */
  @Override
  public ValueExpression resolveVariable(String variable) {
    if (variable.equals(ROWKEY_VARIABLE_NAME))
      return RowKeyVariableExpression.INSTANCE;

    // check if this is a statically resolved variable (e.g. v2 = cf1.cq1)
    RowBuilderField field = findRowBuilderFieldByVariable(variable);
    if (field == null)
      return new AvroObjectExpression(schema.getField(variable));

    // resolve using variable expressions.
    Field columnFamilyField = schema.getField(field.getColumnFamily());

    if (field.getColumnQualifier() == null || field.getColumnQualifier().isEmpty())
      return new AvroVariableExpression(field.getRowBuilderType().getJavaClass(),
          columnFamilyField.pos());

    Field columnQualifierField = columnFamilyField.schema().getField(field.getColumnQualifier());
    return new AvroVariableExpression(field.getRowBuilderType().getJavaClass(),
        columnFamilyField.pos(), columnQualifierField.pos());
  }

  @Override
  public ValueExpression setVariable(String variable, ValueExpression expression) {
    return null;
  }
}
