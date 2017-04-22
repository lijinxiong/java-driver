/*
 * Copyright (C) 2017-2017 DataStax Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.type.codec;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.type.DataType;
import com.datastax.oss.driver.api.type.DataTypes;
import com.datastax.oss.driver.api.type.codec.PrimitiveDoubleCodec;
import com.datastax.oss.driver.api.type.reflect.GenericType;
import java.nio.ByteBuffer;

public class DoubleCodec implements PrimitiveDoubleCodec {
  @Override
  public GenericType<Double> getJavaType() {
    return GenericType.DOUBLE;
  }

  @Override
  public DataType getCqlType() {
    return DataTypes.DOUBLE;
  }

  @Override
  public ByteBuffer encodePrimitive(double value, ProtocolVersion protocolVersion) {
    ByteBuffer bytes = ByteBuffer.allocate(8);
    bytes.putDouble(0, value);
    return bytes;
  }

  @Override
  public double decodePrimitive(ByteBuffer bytes, ProtocolVersion protocolVersion) {
    if (bytes == null || bytes.remaining() == 0) {
      return 0;
    } else if (bytes.remaining() != 8) {
      throw new IllegalArgumentException(
          "Invalid 64-bits double value, expecting 8 bytes but got " + bytes.remaining());
    } else {
      return bytes.getDouble(bytes.position());
    }
  }

  @Override
  public String format(Double value) {
    return (value == null) ? "NULL" : Double.toString(value);
  }

  @Override
  public Double parse(String value) {
    try {
      return (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL"))
          ? null
          : Double.parseDouble(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          String.format("Cannot parse 64-bits double value from \"%s\"", value));
    }
  }
}
