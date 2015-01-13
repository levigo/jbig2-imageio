/**
 * Copyright (C) 1995-2015 levigo holding gmbh.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.levigo.jbig2.segments;

import java.io.IOException;

import com.levigo.jbig2.SegmentHeader;
import com.levigo.jbig2.SegmentData;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.CombinationOperator;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * This class represents the segment type "Page information", 7.4.8 (page 73).
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
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
