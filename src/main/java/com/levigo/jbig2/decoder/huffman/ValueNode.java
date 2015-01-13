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

package com.levigo.jbig2.decoder.huffman;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.decoder.huffman.HuffmanTable.Code;

/**
 * Represents a value node in a huffman tree. It is a leaf of a tree.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
 */
class ValueNode extends Node {
  private int rangeLen;
  private int rangeLow;
  private boolean isLowerRange;

  protected ValueNode(Code c) {
    rangeLen = c.rangeLength;
    rangeLow = c.rangeLow;
    isLowerRange = c.isLowerRange;
  }

  @Override
  protected long decode(ImageInputStream iis) throws IOException {

    if (isLowerRange) {
      /* B.4 4) */
      return (rangeLow - iis.readBits(rangeLen));
    } else {
      /* B.4 5) */
      return rangeLow + iis.readBits(rangeLen);
    }
  }

  static String bitPattern(int v, int len) {
    char result[] = new char[len];
    for (int i = 1; i <= len; i++)
      result[i - 1] = (v >> (len - i) & 1) != 0 ? '1' : '0';

    return new String(result);
  }
}
