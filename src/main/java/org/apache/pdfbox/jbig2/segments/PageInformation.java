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

package org.apache.pdfbox.jbig2.segments;

import java.io.IOException;

import org.apache.pdfbox.jbig2.SegmentData;
import org.apache.pdfbox.jbig2.SegmentHeader;
import org.apache.pdfbox.jbig2.err.InvalidHeaderValueException;
import org.apache.pdfbox.jbig2.io.SubInputStream;
import org.apache.pdfbox.jbig2.util.CombinationOperator;
import org.apache.pdfbox.jbig2.util.log.Logger;
import org.apache.pdfbox.jbig2.util.log.LoggerFactory;

/**
 * This class represents the segment type "Page information", 7.4.8 (page 73).
 */
public class PageInformation implements SegmentData {

  private final Logger log = LoggerFactory.getLogger(PageInformation.class);

  private SubInputStream subInputStream;

  /** Page bitmap width, four byte, 7.4.8.1 */
  private int bitmapWidth;

  /** Page bitmap height, four byte, 7.4.8.2 */
  private int bitmapHeight;

  /** Page X resolution, four byte, 7.4.8.3 */
  private int resolutionX;

  /** Page Y resolution, four byte, 7.4.8.4 */
  private int resolutionY;

  /** Page segment flags, one byte, 7.4.8.5 */
  private boolean combinationOperatorOverrideAllowed;
  private CombinationOperator combinationOperator;
  private boolean requiresAuxiliaryBuffer;
  private short defaultPixelValue;
  private boolean mightContainRefinements;
  private boolean isLossless;

  /** Page striping information, two byte, 7.4.8.6 */
  private boolean isStriped;
  private short maxStripeSize;

  private void parseHeader() throws IOException, InvalidHeaderValueException {

    readWidthAndHeight();
    readResolution();

    /* Bit 7 */
    subInputStream.readBit(); // dirty read

    /* Bit 6 */
    readCombinationOperatorOverrideAllowed();

    /* Bit 5 */
    readRequiresAuxiliaryBuffer();

    /* Bit 3-4 */
    readCombinationOperator();

    /* Bit 2 */
    readDefaultPixelvalue();

    /* Bit 1 */
    readContainsRefinement();

    /* Bit 0 */
    readIsLossless();

    /* Bit 15 */
    readIsStriped();

    /* Bit 0-14 */
    readMaxStripeSize();

    this.checkInput();

  }

  private void readResolution() throws IOException {
    resolutionX = (int) subInputStream.readBits(32) & 0xffffffff;
    resolutionY = (int) subInputStream.readBits(32) & 0xffffffff;
  }

  private void checkInput() throws InvalidHeaderValueException {
    if (bitmapHeight == 0xffffffffL)
      if (!isStriped)
        log.info("isStriped should contaion the value true");
  }

  private void readCombinationOperatorOverrideAllowed() throws IOException {
    /* Bit 6 */
    if (subInputStream.readBit() == 1) {
      combinationOperatorOverrideAllowed = true;
    }
  }

  private void readRequiresAuxiliaryBuffer() throws IOException {
    /* Bit 5 */
    if (subInputStream.readBit() == 1) {
      requiresAuxiliaryBuffer = true;
    }
  }

  private void readCombinationOperator() throws IOException {
    /* Bit 3-4 */
    combinationOperator = CombinationOperator.translateOperatorCodeToEnum((short) (subInputStream.readBits(2) & 0xf));
  }

  private void readDefaultPixelvalue() throws IOException {
    /* Bit 2 */
    defaultPixelValue = (short) subInputStream.readBit();
  }

  private void readContainsRefinement() throws IOException {
    /* Bit 1 */
    if (subInputStream.readBit() == 1) {
      mightContainRefinements = true;
    }
  }

  private void readIsLossless() throws IOException {
    /* Bit 0 */
    if (subInputStream.readBit() == 1) {
      isLossless = true;
    }
  }

  private void readIsStriped() throws IOException {
    /* Bit 15 */
    if (subInputStream.readBit() == 1) {
      isStriped = true;
    }
  }

  private void readMaxStripeSize() throws IOException {
    /* Bit 0-14 */
    maxStripeSize = (short) (subInputStream.readBits(15) & 0xffff);
  }

  private void readWidthAndHeight() throws IOException {
    bitmapWidth = (int) subInputStream.readBits(32); // & 0xffffffff;
    bitmapHeight = (int) subInputStream.readBits(32); // & 0xffffffff;
  }

  public void init(final SegmentHeader header, final SubInputStream sis) throws InvalidHeaderValueException, IOException {
    subInputStream = sis;

    parseHeader();
  }

  public int getWidth() {
    return bitmapWidth;
  }

  public int getHeight() {
    return bitmapHeight;
  }

  public int getResolutionX() {
    return resolutionX;
  }

  public int getResolutionY() {
    return resolutionY;
  }

  public short getDefaultPixelValue() {
    return defaultPixelValue;
  }

  public boolean isCombinationOperatorOverrideAllowed() {
    return combinationOperatorOverrideAllowed;
  }

  public CombinationOperator getCombinationOperator() {
    return combinationOperator;
  }

  public boolean isStriped() {
    return isStriped;
  }

  public short getMaxStripeSize() {
    return maxStripeSize;
  }

  public boolean isAuxiliaryBufferRequired() {
    return requiresAuxiliaryBuffer;
  }

  public boolean mightContainRefinements() {
    return mightContainRefinements;
  }

  public boolean isLossless() {
    return isLossless;
  }

  protected int getBitmapWidth() {
    return bitmapWidth;
  }

  protected int getBitmapHeight() {
    return bitmapHeight;
  }
}
