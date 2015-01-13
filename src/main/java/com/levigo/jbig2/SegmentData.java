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

package com.levigo.jbig2;

import java.io.IOException;

import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.SubInputStream;

/**
 * Interface for all data parts of segments.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
 */
public interface SegmentData {

  /**
   * Parse the stream and read information of header.
   * 
   * @param header - The segments' header (to make referred-to segments available in data part).
   * @param sis - Wrapped {@code ImageInputStream} into {@code SubInputStream}.
   * 
   * @throws InvalidHeaderValueException
   * @throws IntegerMaxValueException
   * @throws IOException
   */
  public void init(SegmentHeader header, SubInputStream sis) throws InvalidHeaderValueException, IntegerMaxValueException,
      IOException;
}
