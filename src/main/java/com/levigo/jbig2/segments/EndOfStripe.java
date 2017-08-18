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
 * This segment flags an end of stripe (see JBIG2 ISO standard, 7.4.9).
 */
public class EndOfStripe implements SegmentData {

  private SubInputStream subInputStream;
  private int lineNumber;

  private void parseHeader() throws IOException, IntegerMaxValueException, InvalidHeaderValueException {
    lineNumber = (int) (subInputStream.readBits(32) & 0xffffffff);
  }

  public void init(SegmentHeader header, SubInputStream sis) throws IntegerMaxValueException,
      InvalidHeaderValueException, IOException {
    this.subInputStream = sis;
    parseHeader();
  }

  public int getLineNumber() {
    return lineNumber;
  }
}
