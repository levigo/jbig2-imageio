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

package com.levigo.jbig2.decoder.huffman;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.JBIG2ImageReader;
import com.levigo.jbig2.decoder.huffman.HuffmanTable.Code;

/**
 * This class represents an internal node of a huffman tree. It contains two child nodes.
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
