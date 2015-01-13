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

import com.levigo.jbig2.JBIG2ImageReader;
import com.levigo.jbig2.decoder.huffman.HuffmanTable.Code;

/**
 * This class represents an internal node of a huffman tree. It contains two child nodes.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
 */
class InternalNode extends Node {
  private final int depth;

  private Node zero;
  private Node one;

  protected InternalNode() {
    depth = 0;
  }

  protected InternalNode(int depth) {
    this.depth = depth;
  }

  protected void append(Code c) {
    if (JBIG2ImageReader.DEBUG)
      System.out.println("I'm working on " + c.toString());

    // ignore unused codes
    if (c.prefixLength == 0)
      return;

    int shift = c.prefixLength - 1 - depth;

    if (shift < 0)
      throw new IllegalArgumentException("Negative shifting is not possible.");

    int bit = (c.code >> shift) & 1;
    if (shift == 0) {
      if (c.rangeLength == -1) {
        // the child will be a OutOfBand
        if (bit == 1) {
          if (one != null)
            throw new IllegalStateException("already have a OOB for " + c);
          one = new OutOfBandNode(c);
        } else {
          if (zero != null)
            throw new IllegalStateException("already have a OOB for " + c);
          zero = new OutOfBandNode(c);
        }
      } else {
        // the child will be a ValueNode
        if (bit == 1) {
          if (one != null)
            throw new IllegalStateException("already have a ValueNode for " + c);
          one = new ValueNode(c);
        } else {
          if (zero != null)
            throw new IllegalStateException("already have a ValueNode for " + c);
          zero = new ValueNode(c);
        }
      }
    } else {
      // the child will be an InternalNode
      if (bit == 1) {
        if (one == null)
          one = new InternalNode(depth + 1);
        ((InternalNode) one).append(c);
      } else {
        if (zero == null)
          zero = new InternalNode(depth + 1);
        ((InternalNode) zero).append(c);
      }
    }
  }

  @Override
  protected long decode(ImageInputStream iis) throws IOException {
    int b = iis.readBit();
    Node n = b == 0 ? zero : one;
    return n.decode(iis);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("\n");

    pad(sb);
    sb.append("0: ").append(zero).append("\n");
    pad(sb);
    sb.append("1: ").append(one).append("\n");

    return sb.toString();
  }

  private void pad(StringBuilder sb) {
    for (int i = 0; i < depth; i++)
      sb.append("   ");
  }
}
