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
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.JBIG2ImageReader;

/**
 * This abstract class is the base class for all types of huffman tables.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
 */
public abstract class HuffmanTable {

  /**
   * This static class represents a code for use in huffman tables.
   * 
   * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
   * 
   */
  public static class Code {
    final int prefixLength;
    final int rangeLength;
    final int rangeLow;
    final boolean isLowerRange;
    int code = -1;

    public Code(int prefixLength, int rangeLength, int rangeLow, boolean isLowerRange) {
      this.prefixLength = prefixLength;
      this.rangeLength = rangeLength;
      this.rangeLow = rangeLow;
      this.isLowerRange = isLowerRange;
    }

    @Override
    public String toString() {
      return (code != -1 ? ValueNode.bitPattern(code, prefixLength) : "?") + "/" + prefixLength + "/" + rangeLength
          + "/" + rangeLow;
    }
  }

  private InternalNode rootNode = new InternalNode();

  public void initTree(List<Code> codeTable) {
    preprocessCodes(codeTable);

    for (Code c : codeTable) {
      rootNode.append(c);
    }
    System.out.println("");
  }

  public long decode(ImageInputStream iis) throws IOException {
    return rootNode.decode(iis);
  }

  @Override
  public String toString() {
    return rootNode + "\n";
  }

  public static String codeTableToString(List<Code> codeTable) {
    StringBuilder sb = new StringBuilder();

    for (Code c : codeTable) {
      sb.append(c.toString()).append("\n");
    }

    return sb.toString();
  }

  private void preprocessCodes(List<Code> codeTable) {
    /* Annex B.3 1) - build the histogram */
    int maxPrefixLength = 0;

    for (Code c : codeTable) {
      maxPrefixLength = Math.max(maxPrefixLength, c.prefixLength);
    }

    int lenCount[] = new int[maxPrefixLength + 1];
    for (Code c : codeTable) {
      lenCount[c.prefixLength]++;
    }

    int curCode, curTemp;
    int firstCode[] = new int[lenCount.length + 1];
    lenCount[0] = 0;

    /* Annex B.3 3) */
    for (int curLen = 1; curLen <= lenCount.length; curLen++) {
      firstCode[curLen] = (firstCode[curLen - 1] + (lenCount[curLen - 1]) << 1);
      curCode = firstCode[curLen];
      for (Code code : codeTable) {
        if (code.prefixLength == curLen) {
          code.code = curCode;
          curCode++;
        }
      }
    }

    if (JBIG2ImageReader.DEBUG)
      System.out.println(codeTableToString(codeTable));
  }
}
