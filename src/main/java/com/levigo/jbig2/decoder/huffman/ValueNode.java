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

import com.levigo.jbig2.decoder.huffman.HuffmanTable.Code;

/**
 * Represents a value node in a huffman tree. It is a leaf of a tree.
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
