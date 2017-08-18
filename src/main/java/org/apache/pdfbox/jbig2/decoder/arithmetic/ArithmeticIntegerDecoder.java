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

package org.apache.pdfbox.jbig2.decoder.arithmetic;

import java.io.IOException;

/**
 * This class represents the arithmetic integer decoder, described in ISO/IEC 14492:2001 (Annex A).
 */
public class ArithmeticIntegerDecoder {

  private final ArithmeticDecoder decoder;

  private int prev;

  public ArithmeticIntegerDecoder(ArithmeticDecoder decoder) {
    this.decoder = decoder;
  }

  /**
   * Arithmetic Integer Decoding Procedure, Annex A.2.
   * 
   * @return Decoded value.
   * @throws IOException
   */
  public long decode(CX cxIAx) throws IOException {
    int v = 0;
    int d, s;

    int bitsToRead;
    int offset;

    if (cxIAx == null) {
      cxIAx = new CX(512, 1);
    }

    prev = 1;

    cxIAx.setIndex(prev);
    s = decoder.decode(cxIAx);
    setPrev(s);

    cxIAx.setIndex(prev);
    d = decoder.decode(cxIAx);
    setPrev(d);

    if (d == 1) {
      cxIAx.setIndex(prev);
      d = decoder.decode(cxIAx);
      setPrev(d);

      if (d == 1) {
        cxIAx.setIndex(prev);
        d = decoder.decode(cxIAx);
        setPrev(d);

        if (d == 1) {
          cxIAx.setIndex(prev);
          d = decoder.decode(cxIAx);
          setPrev(d);

          if (d == 1) {
            cxIAx.setIndex(prev);
            d = decoder.decode(cxIAx);
            setPrev(d);

            if (d == 1) {
              bitsToRead = 32;
              offset = 4436;
            } else {
              bitsToRead = 12;
              offset = 340;
            }
          } else {
            bitsToRead = 8;
            offset = 84;
          }
        } else {
          bitsToRead = 6;
          offset = 20;
        }
      } else {
        bitsToRead = 4;
        offset = 4;
      }
    } else {
      bitsToRead = 2;
      offset = 0;
    }

    for (int i = 0; i < bitsToRead; i++) {
      cxIAx.setIndex(prev);
      d = decoder.decode(cxIAx);
      setPrev(d);
      v = (v << 1) | d;
    }

    v += offset;

    if (s == 0) {
      return v;
    } else if (s == 1 && v > 0) {
      return -v;
    }

    return Long.MAX_VALUE;
  }

  private void setPrev(int bit) {
    if (prev < 256) {
      prev = ((prev << 1) | bit) & 0x1ff;
    } else {
      prev = ((((prev << 1) | bit) & 511) | 256) & 0x1ff;
    }
  }

  /**
   * The IAID decoding procedure, Annex A.3.
   * 
   * @param cxIAID - The contexts and statistics for decoding procedure.
   * @param symCodeLen - Symbol code length.
   * 
   * @return The decoded value.
   * 
   * @throws IOException
   */
  public int decodeIAID(CX cxIAID, long symCodeLen) throws IOException {
    // A.3 1)
    prev = 1;

    // A.3 2)
    for (int i = 0; i < symCodeLen; i++) {
      cxIAID.setIndex(prev);
      prev = (prev << 1) | decoder.decode(cxIAID);
    }

    // A.3 3) & 4)
    return (prev - (1 << symCodeLen));
  }
}
