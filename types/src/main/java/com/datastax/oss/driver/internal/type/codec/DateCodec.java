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
import com.datastax.oss.driver.api.type.codec.TypeCodec;
import com.datastax.oss.driver.api.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.util.Strings;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static java.lang.Long.parseLong;

public class DateCodec implements TypeCodec<LocalDate> {

  private static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);

  @Override
  public GenericType<LocalDate> getJavaType() {
    return GenericType.LOCAL_DATE;
  }

  @Override
  public DataType getCqlType() {
    return DataTypes.DATE;
  }

  @Override
  public ByteBuffer encode(LocalDate value, ProtocolVersion protocolVersion) {
    if (value == null) {
      return null;
    }
    long days = ChronoUnit.DAYS.between(EPOCH, value);
    int unsigned = signedToUnsigned((int) days);
    return TypeCodecs.INT.encodePrimitive(unsigned, protocolVersion);
  }

  @Override
  public LocalDate decode(ByteBuffer bytes, ProtocolVersion protocolVersion) {
    if (bytes == null || bytes.remaining() == 0) {
      return null;
    }
    int unsigned = TypeCodecs.INT.decodePrimitive(bytes, protocolVersion);
    int signed = unsignedToSigned(unsigned);
    return EPOCH.plusDays(signed);
  }

  @Override
  public String format(LocalDate value) {
    return (value == null) ? "NULL" : Strings.quote(DateTimeFormatter.ISO_LOCAL_DATE.format(value));
  }

  @Override
  public LocalDate parse(String value) {
    if (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL")) {
      return null;
    }

    // single quotes are optional for long literals, mandatory for date patterns
    // strip enclosing single quotes, if any
    if (Strings.isQuoted(value)) {
      value = Strings.unquote(value);
    }

    if (Strings.isLongLiteral(value)) {
      long raw;
      try {
        raw = parseLong(value);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            String.format("Cannot parse date value from \"%s\"", value));
      }
      int days;
      try {
        days = cqlDateToDaysSinceEpoch(raw);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Cannot parse date value from \"%s\"", value));
      }
      return EPOCH.plusDays(days);
    }

    try {
      return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException(
          String.format("Cannot parse date value from \"%s\"", value));
    }
  }

  private static int signedToUnsigned(int signed) {
    return signed - Integer.MIN_VALUE;
  }

  private static int unsignedToSigned(int unsigned) {
    return unsigned + Integer.MIN_VALUE; // this relies on overflow for "negative" values
  }

  /**
   * Converts a raw CQL long representing a numeric DATE literal to the number of days since the
   * Epoch. In CQL, numeric DATE literals are longs (unsigned integers actually) between 0 and 2^32
   * - 1, with the epoch in the middle; this method re-centers the epoch at 0.
   */
  private static int cqlDateToDaysSinceEpoch(long raw) {
    if (raw < 0 || raw > MAX_CQL_LONG_VALUE)
      throw new IllegalArgumentException(
          String.format(
              "Numeric literals for DATE must be between 0 and %d (got %d)",
              MAX_CQL_LONG_VALUE, raw));
    return (int) (raw - EPOCH_AS_CQL_LONG);
  }

  private static final long MAX_CQL_LONG_VALUE = ((1L << 32) - 1);
  private static final long EPOCH_AS_CQL_LONG = (1L << 31);
}
