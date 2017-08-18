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
import com.levigo.jbig2.util.CombinationOperator;

/**
 * This class represents the "Region segment information" field, 7.4.1 (page 50). <br>
 * Every region segment data starts with this part.
 */
public class RegionSegmentInformation implements SegmentData {

  private SubInputStream subInputStream;

  /** Region segment bitmap width, 7.4.1.1 */
  private int bitmapWidth;

  /** Region segment bitmap height, 7.4.1.2 */
  private int bitmapHeight;

  /** Region segment bitmap X location, 7.4.1.3 */
  private int xLocation;

  /** Region segment bitmap Y location, 7.4.1.4 */
  private int yLocation;

  /** Region segment flags, 7.4.1.5 */
  private CombinationOperator combinationOperator;

  public RegionSegmentInformation(SubInputStream subInputStream) {
    this.subInputStream = subInputStream;
  }

  public RegionSegmentInformation() {
  }

  public void parseHeader() throws IOException {
    this.bitmapWidth = ((int) (subInputStream.readBits(32) & 0xffffffff));
    this.bitmapHeight = ((int) (subInputStream.readBits(32) & 0xffffffff));
    this.xLocation = ((int) (subInputStream.readBits(32) & 0xffffffff));
    this.yLocation = ((int) (subInputStream.readBits(32) & 0xffffffff));

    /* Bit 3-7 */
    subInputStream.readBits(5); // Dirty read... reserved bits are 0

    /* Bit 0-2 */
    readCombinationOperator();
  }

  private void readCombinationOperator() throws IOException {
    this.combinationOperator = (CombinationOperator.translateOperatorCodeToEnum((short) (subInputStream.readBits(3) & 0xf)));
  }

  public void init(SegmentHeader header, SubInputStream sis) throws InvalidHeaderValueException,
      IntegerMaxValueException, IOException {
  }

  public void setBitmapWidth(final int bitmapWidth) {
    this.bitmapWidth = bitmapWidth;
  }

  public int getBitmapWidth() {
    return bitmapWidth;
  }

  public void setBitmapHeight(final int bitmapHeight) {
    this.bitmapHeight = bitmapHeight;
  }

  public int getBitmapHeight() {
    return bitmapHeight;
  }

  public int getXLocation() {
    return xLocation;
  }

  public int getYLocation() {
    return yLocation;
  }

  public CombinationOperator getCombinationOperator() {
    return combinationOperator;
  }
}
