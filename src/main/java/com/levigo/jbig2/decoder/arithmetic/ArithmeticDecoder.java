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

package com.levigo.jbig2.decoder.arithmetic;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

/**
 * This class represents the arithmetic decoder, described in ISO/IEC 14492:2001 in E.3
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
 */
public class ArithmeticDecoder {

  private static final int QE[][] = {
      {
          0x5601, 1, 1, 1
      }, {
          0x3401, 2, 6, 0
      }, {
          0x1801, 3, 9, 0
      }, {
          0x0AC1, 4, 12, 0
      }, {
          0x0521, 5, 29, 0
      }, {
          0x0221, 38, 33, 0
      }, {
          0x5601, 7, 6, 1
      }, {
          0x5401, 8, 14, 0
      }, {
          0x4801, 9, 14, 0
      }, {
          0x3801, 10, 14, 0
      }, {
          0x3001, 11, 17, 0
      }, {
          0x2401, 12, 18, 0
      }, {
          0x1C01, 13, 20, 0
      }, {
          0x1601, 29, 21, 0
      }, {
          0x5601, 15, 14, 1
      }, {
          0x5401, 16, 14, 0
      }, {
          0x5101, 17, 15, 0
      }, {
          0x4801, 18, 16, 0
      }, {
          0x3801, 19, 17, 0
      }, {
          0x3401, 20, 18, 0
      }, {
          0x3001, 21, 19, 0
      }, {
          0x2801, 22, 19, 0
      }, {
          0x2401, 23, 20, 0
      }, {
          0x2201, 24, 21, 0
      }, {
          0x1C01, 25, 22, 0
      }, {
          0x1801, 26, 23, 0
      }, {
          0x1601, 27, 24, 0
      }, {
          0x1401, 28, 25, 0
      }, {
          0x1201, 29, 26, 0
      }, {
          0x1101, 30, 27, 0
      }, {
          0x0AC1, 31, 28, 0
      }, {
          0x09C1, 32, 29, 0
      }, {
          0x08A1, 33, 30, 0
      }, {
          0x0521, 34, 31, 0
      }, {
          0x0441, 35, 32, 0
      }, {
          0x02A1, 36, 33, 0
      }, {
          0x0221, 37, 34, 0
      }, {
          0x0141, 38, 35, 0
      }, {
          0x0111, 39, 36, 0
      }, {
          0x0085, 40, 37, 0
      }, {
          0x0049, 41, 38, 0
      }, {
          0x0025, 42, 39, 0
      }, {
          0x0015, 43, 40, 0
      }, {
          0x0009, 44, 41, 0
      }, {
          0x0005, 45, 42, 0
      }, {
          0x0001, 45, 43, 0
      }, {
          0x5601, 46, 46, 0
      }
  };

  private int a;
  private long c;
  private int ct;

  private int b;

  private long streamPos0;

  private final ImageInputStream iis;

  public ArithmeticDecoder(ImageInputStream iis) throws IOException {
    this.iis = iis;
    init();
  }

  private void init() throws IOException {
    this.streamPos0 = iis.getStreamPosition();
    b = this.iis.read();

    c = b << 16;

    byteIn();

    c <<= 7;
    ct -= 7;
    a = 0x8000;
  }

  public int decode(CX cx) throws IOException {
    int d;
    final int qeValue = QE[cx.cx()][0];
    final int icx = cx.cx();

    a -= qeValue;

    if ((c >> 16) < qeValue) {
      d = lpsExchange(cx, icx, qeValue);
      renormalize();
    } else {
      c -= (qeValue << 16);
      if ((a & 0x8000) == 0) {
        d = mpsExchange(cx, icx);
        renormalize();
      } else {
        return cx.mps();
      }
    }

    return d;
  }

  private void byteIn() throws IOException {
    if (iis.getStreamPosition() > streamPos0) {
      iis.seek(iis.getStreamPosition() - 1);
    }

    b = iis.read();

    if (b == 0xFF) {
      final int b1 = iis.read();
      if (b1 > 0x8f) {
        c += 0xff00;
        ct = 8;
        iis.seek(iis.getStreamPosition() - 2);
      } else {
        c += b1 << 9;
        ct = 7;
      }
    } else {
      b = iis.read();
      c += b << 8;
      ct = 8;
    }

    c &= 0xffffffffL;
  }

  private void renormalize() throws IOException {
    do {
      if (ct == 0) {
        byteIn();
      }

      a <<= 1;
      c <<= 1;
      ct--;

    } while ((a & 0x8000) == 0);

    c &= 0xffffffffL;
  }

  private int mpsExchange(CX cx, int icx) {
    final int mps = cx.mps();

    if (a < QE[icx][0]) {

      if (QE[icx][3] == 1) {
        cx.toggleMps();
      }

      cx.setCx(QE[icx][2]);
      return 1 - mps;
    } else {
      cx.setCx(QE[icx][1]);
      return mps;
    }
  }

  private int lpsExchange(CX cx, int icx, int qeValue) {
    final int mps = cx.mps();

    if (a < qeValue) {
      cx.setCx(QE[icx][1]);
      a = qeValue;

      return mps;
    } else {
      if (QE[icx][3] == 1) {
        cx.toggleMps();
      }

      cx.setCx(QE[icx][2]);
      a = qeValue;
      return 1 - mps;
    }
  }

  int getA() {
    return a;
  }

  long getC() {
    return c;
  }
}
