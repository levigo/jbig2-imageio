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

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.Region;
import com.levigo.jbig2.SegmentHeader;
import com.levigo.jbig2.decoder.arithmetic.ArithmeticDecoder;
import com.levigo.jbig2.decoder.arithmetic.CX;
import com.levigo.jbig2.decoder.mmr.MMRDecompressor;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * This class represents a generic region segment.<br>
 * Parsing is done as described in 7.4.5.<br>
 * Decoding procedure is done as described in 6.2.5.7 and 7.4.6.4.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
public class GenericRegion implements Region {

  private final Logger log = LoggerFactory.getLogger(GenericRegion.class);

  private SubInputStream subInputStream;
  private long dataHeaderOffset;
  private long dataHeaderLength;
  private long dataOffset;
  private long dataLength;

  /** Region segment information field, 7.4.1 */
  private RegionSegmentInformation regionInfo;

  /** Generic region segment flags, 7.4.6.2 */
  private boolean useExtTemplates;
  private boolean isTPGDon;
  private byte gbTemplate;
  private boolean isMMREncoded;

  /** Generic region segment AT flags, 7.4.6.3 */
  private short[] gbAtX;
  private short[] gbAtY;
  private boolean[] gbAtOverride;

  /**
   * If true, AT pixels are not on their nominal location and have to be overridden
   */
  private boolean override;

  /** Decoded data as pixel values (use row stride/width to wrap line) */
  private Bitmap regionBitmap;

  private ArithmeticDecoder arithDecoder;
  private CX cx;

  private MMRDecompressor mmrDecompressor;

  public GenericRegion() {
  }

  public GenericRegion(final SubInputStream subInputStream) {
    this.subInputStream = subInputStream;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
  }

  private void parseHeader() throws IOException, InvalidHeaderValueException {
    regionInfo.parseHeader();

    /* Bit 5-7 */
    subInputStream.readBits(3); // Dirty read...

    /* Bit 4 */
    if (subInputStream.readBit() == 1) {
      useExtTemplates = true;
    }

    /* Bit 3 */
    if (subInputStream.readBit() == 1) {
      isTPGDon = true;
    }

    /* Bit 1-2 */
    gbTemplate = (byte) (subInputStream.readBits(2) & 0xf);

    /* Bit 0 */
    if (subInputStream.readBit() == 1) {
      isMMREncoded = true;
    }

    if (!isMMREncoded) {
      final int amountOfGbAt;
      if (gbTemplate == 0) {
        if (useExtTemplates) {
          amountOfGbAt = 12;
        } else {
          amountOfGbAt = 4;
        }
      } else {
        amountOfGbAt = 1;
      }

      readGbAtPixels(amountOfGbAt);
    }

    /* Segment data structure */
    computeSegmentDataStructure();

    this.checkInput();
  }

  private void readGbAtPixels(final int amountOfGbAt) throws IOException {
    gbAtX = new short[amountOfGbAt];
    gbAtY = new short[amountOfGbAt];

    for (int i = 0; i < amountOfGbAt; i++) {
      gbAtX[i] = subInputStream.readByte();
      gbAtY[i] = subInputStream.readByte();
    }
  }

  private void computeSegmentDataStructure() throws IOException {
    dataOffset = subInputStream.getStreamPosition();
    dataHeaderLength = dataOffset - dataHeaderOffset;
    dataLength = subInputStream.length() - dataHeaderLength;
  }

  private void checkInput() throws InvalidHeaderValueException {
    if (isMMREncoded) {
      if (gbTemplate != 0) {
        log.info("gbTemplate should contain the value 0");
      }
    }
  }

  /**
   * The procedure is described in 6.2.5.7, page 17.
   * 
   * @returns The decoded {@link Bitmap} of this region.
   */
  public Bitmap getRegionBitmap() throws IOException {
    if (null == regionBitmap) {

      if (isMMREncoded) {

        /*
         * MMR DECODER CALL
         */
        if (null == mmrDecompressor) {
          mmrDecompressor = new MMRDecompressor(regionInfo.getBitmapWidth(), regionInfo.getBitmapHeight(),
              new SubInputStream(subInputStream, dataOffset, dataLength));
        }

        /* 6.2.6 */
        regionBitmap = mmrDecompressor.uncompress();

      } else {

        /*
         * ARITHMETIC DECODER PROCEDURE for generic region segments
         */

        updateOverrideFlags();

        /* 6.2.5.7 - 1) */
        int ltp = 0;

        if (arithDecoder == null) {
          arithDecoder = new ArithmeticDecoder(subInputStream);
        }
        if (cx == null) {
          cx = new CX(65536, 1);
        }

        /* 6.2.5.7 - 2) */
        regionBitmap = new Bitmap(regionInfo.getBitmapWidth(), regionInfo.getBitmapHeight());

        final int paddedWidth = (regionBitmap.getWidth() + 7) & -8;

        /* 6.2.5.7 - 3 */
        for (int line = 0; line < regionBitmap.getHeight(); line++) {

          /* 6.2.5.7 - 3 b) */
          if (isTPGDon) {
            ltp ^= decodeSLTP();
          }

          /* 6.2.5.7 - 3 c) */
          if (ltp == 1) {
            if (line > 0) {
              copyLineAbove(line);
            }
          } else {
            /* 3 d) */
            // NOT USED ATM - If corresponding pixel of SKIP bitmap is 0, set
            // current pixel to 0. Something like that:
            // if (useSkip) {
            // for (int i = 1; i < rowstride; i++) {
            // if (skip[pixel] == 1) {
            // gbReg[pixel] = 0;
            // }
            // pixel++;
            // }
            // } else {
            decodeLine(line, regionBitmap.getWidth(), regionBitmap.getRowStride(), paddedWidth);
            // }
          }
        }
      }
    }

    // if (JBIG2ImageReader.DEBUG)
    // if (header != null && header.getSegmentNr() == 3)
    // new Testbild(gbReg.getByteArray(), (int) gbReg.getWidth(), (int) gbReg.getHeight(),
    // gbReg.getRowStride());

    /* 4 */
    return regionBitmap;
  }

  private int decodeSLTP() throws IOException {
    switch (gbTemplate){
      case 0 :
        cx.setIndex(0x9b25);
        break;
      case 1 :
        cx.setIndex(0x795);
        break;
      case 2 :
        cx.setIndex(0xe5);
        break;
      case 3 :
        cx.setIndex(0x195);
        break;
    }
    return arithDecoder.decode(cx);
  }

  private void decodeLine(final int lineNumber, final int width, final int rowStride, final int paddedWidth)
      throws IOException {
    final int byteIndex = regionBitmap.getByteIndex(0, lineNumber);
    final int idx = byteIndex - rowStride;

    switch (gbTemplate){
      case 0 :
        if (!useExtTemplates) {
          decodeTemplate0a(lineNumber, width, rowStride, paddedWidth, byteIndex, idx);
        } else {
          decodeTemplate0b(lineNumber, width, rowStride, paddedWidth, byteIndex, idx);
        }
        break;
      case 1 :
        decodeTemplate1(lineNumber, width, rowStride, paddedWidth, byteIndex, idx);
        break;
      case 2 :
        decodeTemplate2(lineNumber, width, rowStride, paddedWidth, byteIndex, idx);
        break;
      case 3 :
        decodeTemplate3(lineNumber, width, rowStride, paddedWidth, byteIndex, idx);
        break;
    }
  }

  /**
   * Each pixel gets the value from the corresponding pixel of the row above. Line 0 cannot get
   * copied values (source will be -1, doesn't exist).
   * 
   * @param lineNumber - Coordinate of the row that should be set.
   */
  private void copyLineAbove(final int lineNumber) {
    int targetByteIndex = lineNumber * regionBitmap.getRowStride();
    int sourceByteIndex = targetByteIndex - regionBitmap.getRowStride();

    for (int i = 0; i < regionBitmap.getRowStride(); i++) {
      // Get the byte that should be copied and put it into Bitmap
      regionBitmap.setByte(targetByteIndex++, regionBitmap.getByte(sourceByteIndex++));
    }
  }

  private void decodeTemplate0a(final int lineNumber, final int width, final int rowStride, final int paddedWidth,
      int byteIndex, int idx) throws IOException {
    int context;
    int overriddenContext = 0;

    int line1 = 0;
    int line2 = 0;

    if (lineNumber >= 1) {
      line1 = regionBitmap.getByteAsInteger(idx);
    }

    if (lineNumber >= 2) {
      line2 = regionBitmap.getByteAsInteger(idx - rowStride) << 6;
    }

    context = (line1 & 0xf0) | (line2 & 0x3800);

    int nextByte;
    for (int x = 0; x < paddedWidth; x = nextByte) {
      /* 6.2.5.7 3d */
      byte result = 0;
      nextByte = x + 8;
      final int minorWidth = width - x > 8 ? 8 : width - x;

      if (lineNumber > 0) {
        line1 = (line1 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx + 1) : 0);
      }

      if (lineNumber > 1) {
        line2 = (line2 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx - rowStride + 1) << 6 : 0);
      }

      for (int minorX = 0; minorX < minorWidth; minorX++) {
        final int toShift = 7 - minorX;
        if (override) {
          overriddenContext = overrideAtTemplate0a(context, (x + minorX), lineNumber, result, minorX, toShift);
          cx.setIndex(overriddenContext);
        } else {
          cx.setIndex(context);
        }

        int bit = arithDecoder.decode(cx);

        result |= bit << toShift;

        context = ((context & 0x7bf7) << 1) | bit | ((line1 >> toShift) & 0x10) | ((line2 >> toShift) & 0x800);
      }

      regionBitmap.setByte(byteIndex++, result);
      idx++;
    }
  }

  private void decodeTemplate0b(final int lineNumber, final int width, final int rowStride, final int paddedWidth,
      int byteIndex, int idx) throws IOException {
    int context;
    int overriddenContext = 0;

    int line1 = 0;
    int line2 = 0;

    if (lineNumber >= 1) {
      line1 = regionBitmap.getByteAsInteger(idx);
    }

    if (lineNumber >= 2) {
      line2 = regionBitmap.getByteAsInteger(idx - rowStride) << 6;
    }

    context = (line1 & 0xf0) | (line2 & 0x3800);

    int nextByte;
    for (int x = 0; x < paddedWidth; x = nextByte) {
      /* 6.2.5.7 3d */
      byte result = 0;
      nextByte = x + 8;
      final int minorWidth = width - x > 8 ? 8 : width - x;

      if (lineNumber > 0) {
        line1 = (line1 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx + 1) : 0);
      }

      if (lineNumber > 1) {
        line2 = (line2 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx - rowStride + 1) << 6 : 0);
      }

      for (int minorX = 0; minorX < minorWidth; minorX++) {
        final int toShift = 7 - minorX;
        if (override) {
          overriddenContext = overrideAtTemplate0b(context, (x + minorX), lineNumber, result, minorX, toShift);
          cx.setIndex(overriddenContext);
        } else {
          cx.setIndex(context);
        }

        final int bit = arithDecoder.decode(cx);

        result |= bit << toShift;

        context = ((context & 0x7bf7) << 1) | bit | ((line1 >> toShift) & 0x10) | ((line2 >> toShift) & 0x800);
      }

      regionBitmap.setByte(byteIndex++, result);
      idx++;
    }
  }

  private void decodeTemplate1(final int lineNumber, int width, final int rowStride, final int paddedWidth,
      int byteIndex, int idx) throws IOException {
    int context;
    int overriddenContext;

    int line1 = 0;
    int line2 = 0;

    if (lineNumber >= 1) {
      line1 = regionBitmap.getByteAsInteger(idx);
    }

    if (lineNumber >= 2) {
      line2 = regionBitmap.getByteAsInteger(idx - rowStride) << 5;
    }

    context = ((line1 >> 1) & 0x1f8) | ((line2 >> 1) & 0x1e00);

    int nextByte;
    for (int x = 0; x < paddedWidth; x = nextByte) {
      /* 6.2.5.7 3d */
      byte result = 0;
      nextByte = x + 8;
      final int minorWidth = width - x > 8 ? 8 : width - x;

      if (lineNumber >= 1) {
        line1 = (line1 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx + 1) : 0);
      }

      if (lineNumber >= 2) {
        line2 = (line2 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx - rowStride + 1) << 5 : 0);
      }

      for (int minorX = 0; minorX < minorWidth; minorX++) {
        if (override) {
          overriddenContext = overrideAtTemplate1(context, x + minorX, lineNumber, result, minorX);
          cx.setIndex(overriddenContext);
        } else {
          cx.setIndex(context);
        }

        final int bit = arithDecoder.decode(cx);

        result |= bit << 7 - minorX;

        final int toShift = 8 - minorX;
        context = ((context & 0xefb) << 1) | bit | ((line1 >> toShift) & 0x8) | ((line2 >> toShift) & 0x200);
      }

      regionBitmap.setByte(byteIndex++, result);
      idx++;
    }
  }

  private void decodeTemplate2(final int lineNumber, final int width, final int rowStride, final int paddedWidth,
      int byteIndex, int idx) throws IOException {
    int context;
    int overriddenContext;

    int line1 = 0;
    int line2 = 0;

    if (lineNumber >= 1) {
      line1 = regionBitmap.getByteAsInteger(idx);
    }

    if (lineNumber >= 2) {
      line2 = regionBitmap.getByteAsInteger(idx - rowStride) << 4;
    }

    context = ((line1 >> 3) & 0x7c) | ((line2 >> 3) & 0x380);

    int nextByte;
    for (int x = 0; x < paddedWidth; x = nextByte) {
      /* 6.2.5.7 3d */
      byte result = 0;
      nextByte = x + 8;
      final int minorWidth = width - x > 8 ? 8 : width - x;

      if (lineNumber >= 1) {
        line1 = (line1 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx + 1) : 0);
      }

      if (lineNumber >= 2) {
        line2 = (line2 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx - rowStride + 1) << 4 : 0);
      }

      for (int minorX = 0; minorX < minorWidth; minorX++) {

        if (override) {
          overriddenContext = overrideAtTemplate2(context, x + minorX, lineNumber, result, minorX);
          cx.setIndex(overriddenContext);
        } else {
          cx.setIndex(context);
        }

        final int bit = arithDecoder.decode(cx);

        result |= bit << (7 - minorX);

        final int toShift = 10 - minorX;
        context = ((context & 0x1bd) << 1) | bit | ((line1 >> toShift) & 0x4) | ((line2 >> toShift) & 0x80);
      }

      regionBitmap.setByte(byteIndex++, result);
      idx++;
    }
  }

  private void decodeTemplate3(final int lineNumber, final int width, final int rowStride, final int paddedWidth,
      int byteIndex, int idx) throws IOException {
    int context;
    int overriddenContext;

    int line1 = 0;

    if (lineNumber >= 1) {
      line1 = regionBitmap.getByteAsInteger(idx);
    }

    context = (line1 >> 1) & 0x70;

    int nextByte;
    for (int x = 0; x < paddedWidth; x = nextByte) {
      /* 6.2.5.7 3d */
      byte result = 0;
      nextByte = x + 8;
      final int minorWidth = width - x > 8 ? 8 : width - x;

      if (lineNumber >= 1) {
        line1 = (line1 << 8) | (nextByte < width ? regionBitmap.getByteAsInteger(idx + 1) : 0);
      }

      for (int minorX = 0; minorX < minorWidth; minorX++) {

        if (override) {
          overriddenContext = overrideAtTemplate3(context, x + minorX, lineNumber, result, minorX);
          cx.setIndex(overriddenContext);
        } else {
          cx.setIndex(context);
        }

        final int bit = arithDecoder.decode(cx);

        result |= bit << (7 - minorX);
        context = ((context & 0x1f7) << 1) | bit | ((line1 >> (8 - minorX)) & 0x010);
      }

      regionBitmap.setByte(byteIndex++, result);
      idx++;
    }
  }

  private void updateOverrideFlags() {
    if (gbAtX == null || gbAtY == null) {
      log.info("AT pixels not set");
      return;
    }

    if (gbAtX.length != gbAtY.length) {
      log.info("AT pixel inconsistent, amount of x pixels: " + gbAtX.length + ", amount of y pixels:" + gbAtY.length);
      return;
    }

    gbAtOverride = new boolean[gbAtX.length];

    switch (gbTemplate){
      case 0 :
        if (!useExtTemplates) {
          if (gbAtX[0] != 3 || gbAtY[0] != -1)
            setOverrideFlag(0);

          if (gbAtX[1] != -3 || gbAtY[1] != -1)
            setOverrideFlag(1);

          if (gbAtX[2] != 2 || gbAtY[2] != -2)
            setOverrideFlag(2);

          if (gbAtX[3] != -2 || gbAtY[3] != -2)
            setOverrideFlag(3);

        } else {
          if (gbAtX[0] != -2 || gbAtY[0] != 0)
            setOverrideFlag(0);

          if (gbAtX[1] != 0 || gbAtY[1] != -2)
            setOverrideFlag(1);

          if (gbAtX[2] != -2 || gbAtY[2] != -1)
            setOverrideFlag(2);

          if (gbAtX[3] != -1 || gbAtY[3] != -2)
            setOverrideFlag(3);

          if (gbAtX[4] != 1 || gbAtY[4] != -2)
            setOverrideFlag(4);

          if (gbAtX[5] != 2 || gbAtY[5] != -1)
            setOverrideFlag(5);

          if (gbAtX[6] != -3 || gbAtY[6] != 0)
            setOverrideFlag(6);

          if (gbAtX[7] != -4 || gbAtY[7] != 0)
            setOverrideFlag(7);

          if (gbAtX[8] != 2 || gbAtY[8] != -2)
            setOverrideFlag(8);

          if (gbAtX[9] != 3 || gbAtY[9] != -1)
            setOverrideFlag(9);

          if (gbAtX[10] != -2 || gbAtY[10] != -2)
            setOverrideFlag(10);

          if (gbAtX[11] != -3 || gbAtY[11] != -1)
            setOverrideFlag(11);
        }
        break;
      case 1 :
        if (gbAtX[0] != 3 || gbAtY[0] != -1)
          setOverrideFlag(0);
        break;
      case 2 :
        if (gbAtX[0] != 2 || gbAtY[0] != -1)
          setOverrideFlag(0);
        break;
      case 3 :
        if (gbAtX[0] != 2 || gbAtY[0] != -1)
          setOverrideFlag(0);
        break;
    }

  }

  private void setOverrideFlag(final int index) {
    gbAtOverride[index] = true;
    override = true;
  }

  private int overrideAtTemplate0a(int context, final int x, final int y, final int result, final int minorX,
      final int toShift) throws IOException {
    if (gbAtOverride[0]) {
      context &= 0xffef;
      if (gbAtY[0] == 0 && gbAtX[0] >= -minorX)
        context |= (result >> (toShift - gbAtX[0]) & 0x1) << 4;
      else
        context |= getPixel(x + gbAtX[0], y + gbAtY[0]) << 4;
    }

    if (gbAtOverride[1]) {
      context &= 0xfbff;
      if (gbAtY[1] == 0 && gbAtX[1] >= -minorX)
        context |= (result >> (toShift - gbAtX[1]) & 0x1) << 10;
      else
        context |= getPixel(x + gbAtX[1], y + gbAtY[1]) << 10;
    }

    if (gbAtOverride[2]) {
      context &= 0xf7ff;
      if (gbAtY[2] == 0 && gbAtX[2] >= -minorX)
        context |= (result >> (toShift - gbAtX[2]) & 0x1) << 11;
      else
        context |= getPixel(x + gbAtX[2], y + gbAtY[2]) << 11;
    }

    if (gbAtOverride[3]) {
      context &= 0x7fff;
      if (gbAtY[3] == 0 && gbAtX[3] >= -minorX)
        context |= (result >> (toShift - gbAtX[3]) & 0x1) << 15;
      else
        context |= getPixel(x + gbAtX[3], y + gbAtY[3]) << 15;
    }
    return context;
  }

  private int overrideAtTemplate0b(int context, final int x, final int y, final int result, final int minorX,
      final int toShift) throws IOException {
    if (gbAtOverride[0]) {
      context &= 0xfffd;
      if (gbAtY[0] == 0 && gbAtX[0] >= -minorX)
        context |= (result >> (toShift - gbAtX[0]) & 0x1) << 1;
      else
        context |= getPixel(x + gbAtX[0], y + gbAtY[0]) << 1;
    }

    if (gbAtOverride[1]) {
      context &= 0xdfff;
      if (gbAtY[1] == 0 && gbAtX[1] >= -minorX)
        context |= (result >> (toShift - gbAtX[1]) & 0x1) << 13;
      else
        context |= getPixel(x + gbAtX[1], y + gbAtY[1]) << 13;
    }
    if (gbAtOverride[2]) {
      context &= 0xfdff;
      if (gbAtY[2] == 0 && gbAtX[2] >= -minorX)
        context |= (result >> (toShift - gbAtX[2]) & 0x1) << 9;
      else
        context |= getPixel(x + gbAtX[2], y + gbAtY[2]) << 9;
    }
    if (gbAtOverride[3]) {
      context &= 0xbfff;
      if (gbAtY[3] == 0 && gbAtX[3] >= -minorX)
        context |= (result >> (toShift - gbAtX[3]) & 0x1) << 14;
      else
        context |= getPixel(x + gbAtX[3], y + gbAtY[3]) << 14;
    }
    if (gbAtOverride[4]) {
      context &= 0xefff;
      if (gbAtY[4] == 0 && gbAtX[4] >= -minorX)
        context |= (result >> (toShift - gbAtX[4]) & 0x1) << 12;
      else
        context |= getPixel(x + gbAtX[4], y + gbAtY[4]) << 12;
    }
    if (gbAtOverride[5]) {
      context &= 0xffdf;
      if (gbAtY[5] == 0 && gbAtX[5] >= -minorX)
        context |= (result >> (toShift - gbAtX[5]) & 0x1) << 5;
      else
        context |= getPixel(x + gbAtX[5], y + gbAtY[5]) << 5;
    }
    if (gbAtOverride[6]) {
      context &= 0xfffb;
      if (gbAtY[6] == 0 && gbAtX[6] >= -minorX)
        context |= (result >> (toShift - gbAtX[6]) & 0x1) << 2;
      else
        context |= getPixel(x + gbAtX[6], y + gbAtY[6]) << 2;
    }
    if (gbAtOverride[7]) {
      context &= 0xfff7;
      if (gbAtY[7] == 0 && gbAtX[7] >= -minorX)
        context |= (result >> (toShift - gbAtX[7]) & 0x1) << 3;
      else
        context |= getPixel(x + gbAtX[7], y + gbAtY[7]) << 3;
    }
    if (gbAtOverride[8]) {
      context &= 0xf7ff;
      if (gbAtY[8] == 0 && gbAtX[8] >= -minorX)
        context |= (result >> (toShift - gbAtX[8]) & 0x1) << 11;
      else
        context |= getPixel(x + gbAtX[8], y + gbAtY[8]) << 11;
    }
    if (gbAtOverride[9]) {
      context &= 0xffef;
      if (gbAtY[9] == 0 && gbAtX[9] >= -minorX)
        context |= (result >> (toShift - gbAtX[9]) & 0x1) << 4;
      else
        context |= getPixel(x + gbAtX[9], y + gbAtY[9]) << 4;
    }
    if (gbAtOverride[10]) {
      context &= 0x7fff;
      if (gbAtY[10] == 0 && gbAtX[10] >= -minorX)
        context |= (result >> (toShift - gbAtX[10]) & 0x1) << 15;
      else
        context |= getPixel(x + gbAtX[10], y + gbAtY[10]) << 15;
    }
    if (gbAtOverride[11]) {
      context &= 0xfdff;
      if (gbAtY[11] == 0 && gbAtX[11] >= -minorX)
        context |= (result >> (toShift - gbAtX[11]) & 0x1) << 10;
      else
        context |= getPixel(x + gbAtX[11], y + gbAtY[11]) << 10;
    }

    return context;
  }

  private int overrideAtTemplate1(int context, final int x, final int y, final int result, final int minorX)
      throws IOException {
    context &= 0x1ff7;
    if (gbAtY[0] == 0 && gbAtX[0] >= -minorX)
      return (context | (result >> (7 - (minorX + gbAtX[0])) & 0x1) << 3);
    else
      return (context | getPixel(x + gbAtX[0], y + gbAtY[0]) << 3);
  }

  private int overrideAtTemplate2(int context, final int x, final int y, final int result, final int minorX)
      throws IOException {
    context &= 0x3fb;
    if (gbAtY[0] == 0 && gbAtX[0] >= -minorX)
      return (context | (result >> (7 - (minorX + gbAtX[0])) & 0x1) << 2);
    else
      return (context | getPixel(x + gbAtX[0], y + gbAtY[0]) << 2);
  }

  private int overrideAtTemplate3(int context, final int x, final int y, final int result, final int minorX)
      throws IOException {
    context &= 0x3ef;
    if (gbAtY[0] == 0 && gbAtX[0] >= -minorX)
      return (context | (result >> (7 - (minorX + gbAtX[0])) & 0x1) << 4);
    else
      return (context | getPixel(x + gbAtX[0], y + gbAtY[0]) << 4);
  }

  private byte getPixel(final int x, final int y) throws IOException {
    if (x < 0 || x >= regionBitmap.getWidth())
      return 0;

    if (y < 0 || y >= regionBitmap.getHeight())
      return 0;

    return regionBitmap.getPixel(x, y);
  }

  /**
   * Used by {@link SymbolDictionary}.
   */
  protected void setParameters(final boolean isMMREncoded, final long dataOffset, final long dataLength, final int gbh,
      final int gbw) {
    this.isMMREncoded = isMMREncoded;
    this.dataOffset = dataOffset;
    this.dataLength = dataLength;
    this.regionInfo.setBitmapHeight(gbh);
    this.regionInfo.setBitmapWidth(gbw);

    this.mmrDecompressor = null;
    resetBitmap();
  }

  /**
   * Used by {@link SymbolDictionary}.
   */
  protected void setParameters(final boolean isMMREncoded, final byte sdTemplate, final boolean isTPGDon,
      final boolean useSkip, final short[] sdATX, final short[] sdATY, final int symWidth, final int hcHeight,
      final CX cx, final ArithmeticDecoder arithmeticDecoder) {
    this.isMMREncoded = isMMREncoded;
    this.gbTemplate = sdTemplate;
    this.isTPGDon = isTPGDon;
    this.gbAtX = sdATX;
    this.gbAtY = sdATY;
    this.regionInfo.setBitmapWidth(symWidth);
    this.regionInfo.setBitmapHeight(hcHeight);
    if (null != cx)
      this.cx = cx;
    if (null != arithmeticDecoder)
      this.arithDecoder = arithmeticDecoder;

    this.mmrDecompressor = null;
    resetBitmap();
  }

  /**
   * Used by {@link PatternDictionary} and {@link HalftoneRegion}.
   */
  protected void setParameters(final boolean isMMREncoded, final long dataOffset, final long dataLength, final int gbh,
      final int gbw, final byte gbTemplate, final boolean isTPGDon, final boolean useSkip, final short[] gbAtX,
      final short[] gbAtY) {
    this.dataOffset = dataOffset;
    this.dataLength = dataLength;

    this.regionInfo = new RegionSegmentInformation();
    this.regionInfo.setBitmapHeight(gbh);
    this.regionInfo.setBitmapWidth(gbw);
    this.gbTemplate = gbTemplate;

    this.isMMREncoded = isMMREncoded;
    this.isTPGDon = isTPGDon;
    this.gbAtX = gbAtX;
    this.gbAtY = gbAtY;
  }

  /**
   * Simply sets the memory-critical bitmap of this region to {@code null}.
   */
  protected void resetBitmap() {
    this.regionBitmap = null;
  }

  public void init(final SegmentHeader header, final SubInputStream sis) throws InvalidHeaderValueException,
      IOException {
    this.subInputStream = sis;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
    parseHeader();
  }

  public RegionSegmentInformation getRegionInfo() {
    return regionInfo;
  }

  protected boolean useExtTemplates() {
    return useExtTemplates;
  }

  protected boolean isTPGDon() {
    return isTPGDon;
  }

  protected byte getGbTemplate() {
    return gbTemplate;
  }

  protected boolean isMMREncoded() {
    return isMMREncoded;
  }

  protected short[] getGbAtX() {
    return gbAtX;
  }

  protected short[] getGbAtY() {
    return gbAtY;
  }
}
