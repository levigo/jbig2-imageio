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
import java.util.ArrayList;

import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;

/**
 * Interface for all JBIG2 dictionaries segments.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
 */
public interface Dictionary extends SegmentData {

  /**
   * Decodes a dictionary segment and returns the result.
   * 
   * @return A list of {@link Bitmap}s as a result of the decoding process of dictionary segments.
   * 
   * @throws IOException
   * @throws InvalidHeaderValueException
   * @throws IntegerMaxValueException
   */
  public ArrayList<Bitmap> getDictionary() throws IOException, InvalidHeaderValueException, IntegerMaxValueException;
}
