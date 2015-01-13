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

/**
 * This class represents a "Table" segment. It handles custom tables, see Annex B.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
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
