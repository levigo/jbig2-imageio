/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.levigo.jbig2.segments;

import java.io.IOException;

import com.levigo.jbig2.SegmentHeader;
import com.levigo.jbig2.SegmentData;
import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.SubInputStream;

/**
 * This class represents a "Table" segment. It handles custom tables, see Annex B.
 */
public class Table implements SegmentData {

  private SubInputStream subInputStream;

  /** Code table flags, B.2.1, page 87 */
  private int htOutOfBand;
  private int htPS;
  private int htRS;

  /** Code table lowest value, B.2.2, page 87 */
  private int htLow;

  /** Code table highest value, B.2.3, page 87 */
  private int htHigh;

  private void parseHeader() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    int bit;

    /* Bit 7 */
    if ((bit = subInputStream.readBit()) == 1) {
      throw new InvalidHeaderValueException("B.2.1 Code table flags: Bit 7 must be zero, but was " + bit);
    }

    /* Bit 4-6 */
    htRS = (int) ((subInputStream.readBits(3) + 1) & 0xf);

    /* Bit 1-3 */
    htPS = (int) ((subInputStream.readBits(3) + 1) & 0xf);

    /* Bit 0 */
    htOutOfBand = (int) subInputStream.readBit();

    htLow = (int) subInputStream.readBits(32); // & 0xffffffff);
    htHigh = (int) subInputStream.readBits(32); // & 0xffffffff);
  }

  public void init(SegmentHeader header, SubInputStream sis) throws InvalidHeaderValueException, IOException,
      IntegerMaxValueException {
    subInputStream = sis;

    parseHeader();
  }

  public int getHtOOB() {
    return htOutOfBand;
  }

  public int getHtPS() {
    return htPS;
  }

  public int getHtRS() {
    return htRS;
  }

  public int getHtLow() {
    return htLow;
  }

  public int getHtHigh() {
    return htHigh;
  }

  public SubInputStream getSubInputStream() {
    return subInputStream;
  }
}
