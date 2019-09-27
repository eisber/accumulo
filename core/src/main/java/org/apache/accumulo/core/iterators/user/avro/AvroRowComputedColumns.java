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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.accumulo.core.iterators.user.avro.juel.AvroELContext;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.io.Text;

/**
 * Holds all computed columns.
 * 
 * Note: it's a bit convoluted as we first have to parse the options to figure which additional
 * columns we have, return to the caller so we can setup the AVRO schema and then continue the setup
 * here.
 */
public class AvroRowComputedColumns {
  public static final String COLUMN_PREFIX = "column.";

  /**
   * JUEL expression context exposing AVRO GenericRecord
   */
  private AvroELContext expressionContext;

  /**
   * Definitions created from user-supplied options.
   */
  private List<ExpressionColumnDefinition> expressionColumnDefinitions;

  /**
   * The executable column expressions.
   */
  private List<ExpressionColumn> expressionColumns;

  /**
   * Just the definition of the expression. Need to collect them all first so the AVRO schema can be
   * build.
   */
  class ExpressionColumnDefinition {
    private RowBuilderField schemaField;

    private String expression;

    public ExpressionColumnDefinition(RowBuilderField schemaField, String expression) {
      this.schemaField = schemaField;
      this.expression = expression;
    }

    public RowBuilderField getSchemaField() {
      return schemaField;
    }

    public String getExpression() {
      return expression;
    }
  }

  /**
   * The fully initialized expression ready to be computed.
   */
  class ExpressionColumn {
    private ValueExpression columnExpression;

    private Field field;

    public ExpressionColumn(ValueExpression columnExpression, Field field) {
      this.columnExpression = columnExpression;
      this.field = field;
    }

    public void populateField(GenericRecordBuilder recordBuilder) {
      Object value = this.columnExpression.getValue(AvroRowComputedColumns.this.expressionContext);
      recordBuilder.set(this.field, value);
    }
  }

  public AvroRowComputedColumns(Map<String,String> options) {
    // expression setup
    // options: column.<name>.<type>, JUEL expression
    this.expressionColumns = new ArrayList<>();

    for (Map.Entry<String,String> entry : options.entrySet()) {
      if (!entry.getKey().startsWith(COLUMN_PREFIX))
        continue;

      String[] arr = entry.getKey().split("\\.");
      if (arr.length != 3)
        throw new IllegalArgumentException(
            "Unable to parse column specification. column.<name>.<type>: " + entry.getKey());

      String column = arr[1];
      String type = arr[2];
      String expression = entry.getValue();
      RowBuilderField schemaField = new RowBuilderField(column, null, type, null);

      this.expressionColumnDefinitions.add(new ExpressionColumnDefinition(schemaField, expression));
    }
  }

  /**
   * 
   * @return a collection of RowBuilderFields based on the column expression definitions.
   */
  public Collection<RowBuilderField> getComputedSchemaFields() {
    return this.expressionColumnDefinitions.stream().map(ExpressionColumnDefinition::getSchemaField)
        .collect(Collectors.toList());
  }

  /**
   * Initialize the columns expression. Can't be done in the constructor as the schema wasn't ready.
   * 
   * @param schema
   *          the AVRO input schema.
   * @param schemaFields
   *          the user supplied schema.
   */
  public void initialize(Schema schema, RowBuilderField[] schemaFields) {
    this.expressionContext = new AvroELContext(schema, schemaFields);

    ExpressionFactory factory = ExpressionFactory.newInstance();

    this.expressionColumns = this.expressionColumnDefinitions.stream().map(expr -> {
      Field field = schema.getField(expr.getSchemaField().getColumnFamily());

      RowBuilderType type = expr.getSchemaField().getRowBuilderType();
      ValueExpression columnExpression = factory.createValueExpression(expressionContext,
          expr.getExpression(), type.getJavaClass());

      return new ExpressionColumn(columnExpression, field);
    }).collect(Collectors.toList());
  }

  /**
   * Compute each expression and assign the output to the corresponding field.
   * 
   * @param rowKey
   *          the row key.
   * @param recordBuilder
   *          the recordBuilder to be filled.
   * @return the AVRO record contain the input data and the additionally compute fields.
   */
  public Record endRow(Text rowKey, GenericRecordBuilder recordBuilder) {

    if (!this.expressionColumns.isEmpty()) {
      // need to build it now, so that we can access the fields from within the
      // expressions
      Record record = recordBuilder.build();
      this.expressionContext.setCurrent(rowKey, record);

      // compute each exporession
      for (ExpressionColumn expr : this.expressionColumns)
        expr.populateField(recordBuilder);
    }

    return recordBuilder.build();
  }
}
