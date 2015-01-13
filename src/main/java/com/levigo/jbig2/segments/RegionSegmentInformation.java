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
import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.CombinationOperator;

/**
 * This class represents the "Region segment information" field, 7.4.1 (page 50). <br>
 * Every region segment data starts with this part.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
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
