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

import java.util.Arrays;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.accumulo.core.iterators.user.avro.juel.AvroELContext;
import org.apache.accumulo.core.iterators.user.avro.record.AvroSchemaBuilder;
import org.apache.accumulo.core.iterators.user.avro.record.RowBuilderField;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.io.Text;
import org.junit.Test;

import junit.framework.TestCase;

public class AvroJuelTest extends TestCase {

  private AvroELContext context;
  private ExpressionFactory factory;
  private Schema schema;

  @Override
  public void setUp() throws Exception {
    factory = ExpressionFactory.newInstance();

    RowBuilderField[] schemaMappingFields =
        new RowBuilderField[] {new RowBuilderField("cf1", "cq1", "long", "v0"),
            new RowBuilderField("cf2", "cq2", "double", "v1"),
            new RowBuilderField("cf2", "cq3", "string", "v2")};

    schema = AvroSchemaBuilder.buildSchema(Arrays.asList(schemaMappingFields));

    context = new AvroELContext(schema);
  }

  private void setRecordValues(String rowKey, long cq1, double cq2, String cq3) {
    GenericRecordBuilder cf1RecordBuilder =
        new GenericRecordBuilder(schema.getField("cf1").schema());
    GenericRecordBuilder cf2RecordBuilder =
        new GenericRecordBuilder(schema.getField("cf2").schema());

    cf1RecordBuilder.set("cq1", cq1);
    cf2RecordBuilder.set("cq2", cq2);
    cf2RecordBuilder.set("cq3", cq3);

    GenericRecordBuilder rootRecordBuilder = new GenericRecordBuilder(schema);
    rootRecordBuilder.set("cf1", cf1RecordBuilder.build());
    rootRecordBuilder.set("cf2", cf2RecordBuilder.build());

    context.setCurrent(new Text(rowKey), rootRecordBuilder.build());
  }

  @Test
  public void testVariableExpressions() {
    ValueExpression exprV0 = factory.createValueExpression(context, "${v0}", long.class);

    // set the values after the expression is created
    setRecordValues("key1", 3L, 2.0, "");
    assertEquals(3L, exprV0.getValue(context));

    // test if we can reset it
    setRecordValues("key1", 4L, 2.5, "");
    assertEquals(4L, exprV0.getValue(context));

    // check for the second variable
    ValueExpression exprV1 = factory.createValueExpression(context, "${v1}", double.class);
    assertEquals(2.5, exprV1.getValue(context));
  }

  @Test
  public void testVariableConditions() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v0 > 2.1 && v1 < 3}", boolean.class);

    setRecordValues("key1", 3L, 2.0, "");

    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testStringEndsWith() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v2.endsWith('test')}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "This is a test");
    assertTrue((boolean) expr.getValue(context));

    expr = factory.createValueExpression(context, "${!v2.endsWith('foo')}", boolean.class);
    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testStringStartsWith() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v2.startsWith('This')}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "This is a test");
    assertTrue((boolean) expr.getValue(context));

    expr = factory.createValueExpression(context, "${!v2.startsWith('this')}", boolean.class);
    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testStringContains() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v2.contains('is')}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "This is a test");
    assertTrue((boolean) expr.getValue(context));

    expr = factory.createValueExpression(context, "${!v2.contains('IS')}", boolean.class);
    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testStringIn() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v2.in('a','b','c')}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "b");
    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testIntIn() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v0.in(0, 1, 3)}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "b");
    assertTrue((boolean) expr.getValue(context));

    expr = factory.createValueExpression(context, "${v0.in(0, 1)}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "b");
    assertFalse((boolean) expr.getValue(context));
  }

  @Test
  public void testStringQuoteEscape() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v2 == 'a\\'bc'}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "a'bc");
    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testStringDoubleQuoteEscape() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v2 == 'a\"bc'}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "a\"bc");
    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testStringBackslash() {
    ValueExpression expr =
        factory.createValueExpression(context, "${v2 == 'a\\\\bc'}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "a\\bc");
    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testRowKey() {
    ValueExpression expr =
        factory.createValueExpression(context, "${rowKey == 'key1'}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "abc");
    assertTrue((boolean) expr.getValue(context));

    setRecordValues("key2", 3L, 2.0, "abc");
    assertFalse((boolean) expr.getValue(context));
  }

  @Test
  public void testObjectPropertyBased() {
    ValueExpression expr = factory.createValueExpression(context, "${cf1.cq1 == 3}", boolean.class);
    setRecordValues("key1", 3L, 2.0, "abc");
    assertTrue((boolean) expr.getValue(context));
  }

  @Test
  public void testColumnRemapping() {
    ValueExpression expr =
        factory.createValueExpression(context, "${(cf1.cq1 + 1)/2.0}", Object.class);

    setRecordValues("key1", 3L, 2.0, "abc");

    assertEquals((3 + 1) / 2.0, expr.getValue(context));
  }
}
