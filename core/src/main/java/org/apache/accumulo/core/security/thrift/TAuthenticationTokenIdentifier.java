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
/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.accumulo.core.security.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2015-12-16")
public class TAuthenticationTokenIdentifier implements org.apache.thrift.TBase<TAuthenticationTokenIdentifier, TAuthenticationTokenIdentifier._Fields>, java.io.Serializable, Cloneable, Comparable<TAuthenticationTokenIdentifier> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TAuthenticationTokenIdentifier");

  private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField KEY_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("keyId", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField ISSUE_DATE_FIELD_DESC = new org.apache.thrift.protocol.TField("issueDate", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField EXPIRATION_DATE_FIELD_DESC = new org.apache.thrift.protocol.TField("expirationDate", org.apache.thrift.protocol.TType.I64, (short)4);
  private static final org.apache.thrift.protocol.TField INSTANCE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("instanceId", org.apache.thrift.protocol.TType.STRING, (short)5);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TAuthenticationTokenIdentifierStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TAuthenticationTokenIdentifierTupleSchemeFactory());
  }

  public String principal; // required
  public int keyId; // optional
  public long issueDate; // optional
  public long expirationDate; // optional
  public String instanceId; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    PRINCIPAL((short)1, "principal"),
    KEY_ID((short)2, "keyId"),
    ISSUE_DATE((short)3, "issueDate"),
    EXPIRATION_DATE((short)4, "expirationDate"),
    INSTANCE_ID((short)5, "instanceId");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // PRINCIPAL
          return PRINCIPAL;
        case 2: // KEY_ID
          return KEY_ID;
        case 3: // ISSUE_DATE
          return ISSUE_DATE;
        case 4: // EXPIRATION_DATE
          return EXPIRATION_DATE;
        case 5: // INSTANCE_ID
          return INSTANCE_ID;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __KEYID_ISSET_ID = 0;
  private static final int __ISSUEDATE_ISSET_ID = 1;
  private static final int __EXPIRATIONDATE_ISSET_ID = 2;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.KEY_ID,_Fields.ISSUE_DATE,_Fields.EXPIRATION_DATE,_Fields.INSTANCE_ID};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.KEY_ID, new org.apache.thrift.meta_data.FieldMetaData("keyId", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.ISSUE_DATE, new org.apache.thrift.meta_data.FieldMetaData("issueDate", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.EXPIRATION_DATE, new org.apache.thrift.meta_data.FieldMetaData("expirationDate", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.INSTANCE_ID, new org.apache.thrift.meta_data.FieldMetaData("instanceId", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TAuthenticationTokenIdentifier.class, metaDataMap);
  }

  public TAuthenticationTokenIdentifier() {
  }

  public TAuthenticationTokenIdentifier(
    String principal)
  {
    this();
    this.principal = principal;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TAuthenticationTokenIdentifier(TAuthenticationTokenIdentifier other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetPrincipal()) {
      this.principal = other.principal;
    }
    this.keyId = other.keyId;
    this.issueDate = other.issueDate;
    this.expirationDate = other.expirationDate;
    if (other.isSetInstanceId()) {
      this.instanceId = other.instanceId;
    }
  }

  public TAuthenticationTokenIdentifier deepCopy() {
    return new TAuthenticationTokenIdentifier(this);
  }

  @Override
  public void clear() {
    this.principal = null;
    setKeyIdIsSet(false);
    this.keyId = 0;
    setIssueDateIsSet(false);
    this.issueDate = 0;
    setExpirationDateIsSet(false);
    this.expirationDate = 0;
    this.instanceId = null;
  }

  public String getPrincipal() {
    return this.principal;
  }

  public TAuthenticationTokenIdentifier setPrincipal(String principal) {
    this.principal = principal;
    return this;
  }

  public void unsetPrincipal() {
    this.principal = null;
  }

  /** Returns true if field principal is set (has been assigned a value) and false otherwise */
  public boolean isSetPrincipal() {
    return this.principal != null;
  }

  public void setPrincipalIsSet(boolean value) {
    if (!value) {
      this.principal = null;
    }
  }

  public int getKeyId() {
    return this.keyId;
  }

  public TAuthenticationTokenIdentifier setKeyId(int keyId) {
    this.keyId = keyId;
    setKeyIdIsSet(true);
    return this;
  }

  public void unsetKeyId() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __KEYID_ISSET_ID);
  }

  /** Returns true if field keyId is set (has been assigned a value) and false otherwise */
  public boolean isSetKeyId() {
    return EncodingUtils.testBit(__isset_bitfield, __KEYID_ISSET_ID);
  }

  public void setKeyIdIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __KEYID_ISSET_ID, value);
  }

  public long getIssueDate() {
    return this.issueDate;
  }

  public TAuthenticationTokenIdentifier setIssueDate(long issueDate) {
    this.issueDate = issueDate;
    setIssueDateIsSet(true);
    return this;
  }

  public void unsetIssueDate() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ISSUEDATE_ISSET_ID);
  }

  /** Returns true if field issueDate is set (has been assigned a value) and false otherwise */
  public boolean isSetIssueDate() {
    return EncodingUtils.testBit(__isset_bitfield, __ISSUEDATE_ISSET_ID);
  }

  public void setIssueDateIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ISSUEDATE_ISSET_ID, value);
  }

  public long getExpirationDate() {
    return this.expirationDate;
  }

  public TAuthenticationTokenIdentifier setExpirationDate(long expirationDate) {
    this.expirationDate = expirationDate;
    setExpirationDateIsSet(true);
    return this;
  }

  public void unsetExpirationDate() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __EXPIRATIONDATE_ISSET_ID);
  }

  /** Returns true if field expirationDate is set (has been assigned a value) and false otherwise */
  public boolean isSetExpirationDate() {
    return EncodingUtils.testBit(__isset_bitfield, __EXPIRATIONDATE_ISSET_ID);
  }

  public void setExpirationDateIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __EXPIRATIONDATE_ISSET_ID, value);
  }

  public String getInstanceId() {
    return this.instanceId;
  }

  public TAuthenticationTokenIdentifier setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  public void unsetInstanceId() {
    this.instanceId = null;
  }

  /** Returns true if field instanceId is set (has been assigned a value) and false otherwise */
  public boolean isSetInstanceId() {
    return this.instanceId != null;
  }

  public void setInstanceIdIsSet(boolean value) {
    if (!value) {
      this.instanceId = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case PRINCIPAL:
      if (value == null) {
        unsetPrincipal();
      } else {
        setPrincipal((String)value);
      }
      break;

    case KEY_ID:
      if (value == null) {
        unsetKeyId();
      } else {
        setKeyId((Integer)value);
      }
      break;

    case ISSUE_DATE:
      if (value == null) {
        unsetIssueDate();
      } else {
        setIssueDate((Long)value);
      }
      break;

    case EXPIRATION_DATE:
      if (value == null) {
        unsetExpirationDate();
      } else {
        setExpirationDate((Long)value);
      }
      break;

    case INSTANCE_ID:
      if (value == null) {
        unsetInstanceId();
      } else {
        setInstanceId((String)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case PRINCIPAL:
      return getPrincipal();

    case KEY_ID:
      return getKeyId();

    case ISSUE_DATE:
      return getIssueDate();

    case EXPIRATION_DATE:
      return getExpirationDate();

    case INSTANCE_ID:
      return getInstanceId();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case PRINCIPAL:
      return isSetPrincipal();
    case KEY_ID:
      return isSetKeyId();
    case ISSUE_DATE:
      return isSetIssueDate();
    case EXPIRATION_DATE:
      return isSetExpirationDate();
    case INSTANCE_ID:
      return isSetInstanceId();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TAuthenticationTokenIdentifier)
      return this.equals((TAuthenticationTokenIdentifier)that);
    return false;
  }

  public boolean equals(TAuthenticationTokenIdentifier that) {
    if (that == null)
      return false;

    boolean this_present_principal = true && this.isSetPrincipal();
    boolean that_present_principal = true && that.isSetPrincipal();
    if (this_present_principal || that_present_principal) {
      if (!(this_present_principal && that_present_principal))
        return false;
      if (!this.principal.equals(that.principal))
        return false;
    }

    boolean this_present_keyId = true && this.isSetKeyId();
    boolean that_present_keyId = true && that.isSetKeyId();
    if (this_present_keyId || that_present_keyId) {
      if (!(this_present_keyId && that_present_keyId))
        return false;
      if (this.keyId != that.keyId)
        return false;
    }

    boolean this_present_issueDate = true && this.isSetIssueDate();
    boolean that_present_issueDate = true && that.isSetIssueDate();
    if (this_present_issueDate || that_present_issueDate) {
      if (!(this_present_issueDate && that_present_issueDate))
        return false;
      if (this.issueDate != that.issueDate)
        return false;
    }

    boolean this_present_expirationDate = true && this.isSetExpirationDate();
    boolean that_present_expirationDate = true && that.isSetExpirationDate();
    if (this_present_expirationDate || that_present_expirationDate) {
      if (!(this_present_expirationDate && that_present_expirationDate))
        return false;
      if (this.expirationDate != that.expirationDate)
        return false;
    }

    boolean this_present_instanceId = true && this.isSetInstanceId();
    boolean that_present_instanceId = true && that.isSetInstanceId();
    if (this_present_instanceId || that_present_instanceId) {
      if (!(this_present_instanceId && that_present_instanceId))
        return false;
      if (!this.instanceId.equals(that.instanceId))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_principal = true && (isSetPrincipal());
    list.add(present_principal);
    if (present_principal)
      list.add(principal);

    boolean present_keyId = true && (isSetKeyId());
    list.add(present_keyId);
    if (present_keyId)
      list.add(keyId);

    boolean present_issueDate = true && (isSetIssueDate());
    list.add(present_issueDate);
    if (present_issueDate)
      list.add(issueDate);

    boolean present_expirationDate = true && (isSetExpirationDate());
    list.add(present_expirationDate);
    if (present_expirationDate)
      list.add(expirationDate);

    boolean present_instanceId = true && (isSetInstanceId());
    list.add(present_instanceId);
    if (present_instanceId)
      list.add(instanceId);

    return list.hashCode();
  }

  @Override
  public int compareTo(TAuthenticationTokenIdentifier other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(other.isSetPrincipal());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPrincipal()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, other.principal);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetKeyId()).compareTo(other.isSetKeyId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetKeyId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.keyId, other.keyId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIssueDate()).compareTo(other.isSetIssueDate());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIssueDate()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.issueDate, other.issueDate);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetExpirationDate()).compareTo(other.isSetExpirationDate());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExpirationDate()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.expirationDate, other.expirationDate);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetInstanceId()).compareTo(other.isSetInstanceId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetInstanceId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.instanceId, other.instanceId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TAuthenticationTokenIdentifier(");
    boolean first = true;

    sb.append("principal:");
    if (this.principal == null) {
      sb.append("null");
    } else {
      sb.append(this.principal);
    }
    first = false;
    if (isSetKeyId()) {
      if (!first) sb.append(", ");
      sb.append("keyId:");
      sb.append(this.keyId);
      first = false;
    }
    if (isSetIssueDate()) {
      if (!first) sb.append(", ");
      sb.append("issueDate:");
      sb.append(this.issueDate);
      first = false;
    }
    if (isSetExpirationDate()) {
      if (!first) sb.append(", ");
      sb.append("expirationDate:");
      sb.append(this.expirationDate);
      first = false;
    }
    if (isSetInstanceId()) {
      if (!first) sb.append(", ");
      sb.append("instanceId:");
      if (this.instanceId == null) {
        sb.append("null");
      } else {
        sb.append(this.instanceId);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TAuthenticationTokenIdentifierStandardSchemeFactory implements SchemeFactory {
    public TAuthenticationTokenIdentifierStandardScheme getScheme() {
      return new TAuthenticationTokenIdentifierStandardScheme();
    }
  }

  private static class TAuthenticationTokenIdentifierStandardScheme extends StandardScheme<TAuthenticationTokenIdentifier> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TAuthenticationTokenIdentifier struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // PRINCIPAL
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.principal = iprot.readString();
              struct.setPrincipalIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // KEY_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.keyId = iprot.readI32();
              struct.setKeyIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // ISSUE_DATE
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.issueDate = iprot.readI64();
              struct.setIssueDateIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // EXPIRATION_DATE
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.expirationDate = iprot.readI64();
              struct.setExpirationDateIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // INSTANCE_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.instanceId = iprot.readString();
              struct.setInstanceIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TAuthenticationTokenIdentifier struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.principal != null) {
        oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
        oprot.writeString(struct.principal);
        oprot.writeFieldEnd();
      }
      if (struct.isSetKeyId()) {
        oprot.writeFieldBegin(KEY_ID_FIELD_DESC);
        oprot.writeI32(struct.keyId);
        oprot.writeFieldEnd();
      }
      if (struct.isSetIssueDate()) {
        oprot.writeFieldBegin(ISSUE_DATE_FIELD_DESC);
        oprot.writeI64(struct.issueDate);
        oprot.writeFieldEnd();
      }
      if (struct.isSetExpirationDate()) {
        oprot.writeFieldBegin(EXPIRATION_DATE_FIELD_DESC);
        oprot.writeI64(struct.expirationDate);
        oprot.writeFieldEnd();
      }
      if (struct.instanceId != null) {
        if (struct.isSetInstanceId()) {
          oprot.writeFieldBegin(INSTANCE_ID_FIELD_DESC);
          oprot.writeString(struct.instanceId);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TAuthenticationTokenIdentifierTupleSchemeFactory implements SchemeFactory {
    public TAuthenticationTokenIdentifierTupleScheme getScheme() {
      return new TAuthenticationTokenIdentifierTupleScheme();
    }
  }

  private static class TAuthenticationTokenIdentifierTupleScheme extends TupleScheme<TAuthenticationTokenIdentifier> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TAuthenticationTokenIdentifier struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetPrincipal()) {
        optionals.set(0);
      }
      if (struct.isSetKeyId()) {
        optionals.set(1);
      }
      if (struct.isSetIssueDate()) {
        optionals.set(2);
      }
      if (struct.isSetExpirationDate()) {
        optionals.set(3);
      }
      if (struct.isSetInstanceId()) {
        optionals.set(4);
      }
      oprot.writeBitSet(optionals, 5);
      if (struct.isSetPrincipal()) {
        oprot.writeString(struct.principal);
      }
      if (struct.isSetKeyId()) {
        oprot.writeI32(struct.keyId);
      }
      if (struct.isSetIssueDate()) {
        oprot.writeI64(struct.issueDate);
      }
      if (struct.isSetExpirationDate()) {
        oprot.writeI64(struct.expirationDate);
      }
      if (struct.isSetInstanceId()) {
        oprot.writeString(struct.instanceId);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TAuthenticationTokenIdentifier struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(5);
      if (incoming.get(0)) {
        struct.principal = iprot.readString();
        struct.setPrincipalIsSet(true);
      }
      if (incoming.get(1)) {
        struct.keyId = iprot.readI32();
        struct.setKeyIdIsSet(true);
      }
      if (incoming.get(2)) {
        struct.issueDate = iprot.readI64();
        struct.setIssueDateIsSet(true);
      }
      if (incoming.get(3)) {
        struct.expirationDate = iprot.readI64();
        struct.setExpirationDateIsSet(true);
      }
      if (incoming.get(4)) {
        struct.instanceId = iprot.readString();
        struct.setInstanceIdIsSet(true);
      }
    }
  }

}

