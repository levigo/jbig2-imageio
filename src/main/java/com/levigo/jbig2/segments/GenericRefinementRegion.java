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
import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * This class represents a generic refinement region and implements the procedure described in JBIG2
 * ISO standard, 6.3 and 7.4.7.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
public class GenericRefinementRegion implements Region {
  private static final Logger log = LoggerFactory.getLogger(GenericRefinementRegion.class);

  public static abstract class Template {
    protected abstract short form(short c1, short c2, short c3, short c4, short c5);

    protected abstract void setIndex(CX cx);
  }

  private static class Template0 extends Template {

    @Override
    protected short form(short c1, short c2, short c3, short c4, short c5) {
      return (short) ((c1 << 10) | (c2 << 7) | (c3 << 4) | (c4 << 1) | c5);
    }

    @Override
    protected void setIndex(CX cx) {
      // Figure 14, page 22
      cx.setIndex(0x100);
    }

  }

  private static class Template1 extends Template {

    @Override
    protected short form(short c1, short c2, short c3, short c4, short c5) {
      return (short) (((c1 & 0x02) << 8) | (c2 << 6) | ((c3 & 0x03) << 4) | (c4 << 1) | c5);
    }

    @Override
    protected void setIndex(CX cx) {
      // Figure 15, page 22
      cx.setIndex(0x080);
    }

  }

  private static final Template T0 = new Template0();
  private static final Template T1 = new Template1();

  private SubInputStream subInputStream;

  private SegmentHeader segmentHeader;

  /** Region segment information flags, 7.4.1 */
  private RegionSegmentInformation regionInfo;

  /** Generic refinement region segment flags, 7.4.7.2 */
  private boolean isTPGROn;
  private short templateID;

  private Template template;
  /** Generic refinement region segment AT flags, 7.4.7.3 */
  private short grAtX[];
  private short grAtY[];

  /** Decoded data as pixel values (use row stride/width to wrap line) */
  private Bitmap regionBitmap;

  /** Variables for decoding */
  private Bitmap referenceBitmap;
  private int referenceDX;
  private int referenceDY;

  private ArithmeticDecoder arithDecoder;
  private CX cx;

  /**
   * If true, AT pixels are not on their nominal location and have to be overridden.
   */
  private boolean override;
  private boolean[] grAtOverride;
  public GenericRefinementRegion() {
  }

  public GenericRefinementRegion(final SubInputStream subInputStream) {
    this.subInputStream = subInputStream;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
  }

  public GenericRefinementRegion(final SubInputStream subInputStream, final SegmentHeader segmentHeader) {
    this.subInputStream = subInputStream;
    this.segmentHeader = segmentHeader;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
  }

  /**
   * Parses the flags described in JBIG2 ISO standard:
   * <ul>
   * <li>7.4.7.2 Generic refinement region segment flags</li>
   * <li>7.4.7.3 Generic refinement refion segment AT flags</li>
   * </ul>
   * 
   * @throws IOException
   */
  private void parseHeader() throws IOException {
    regionInfo.parseHeader();

    /* Bit 2-7 */
    subInputStream.readBits(6); // Dirty read...

    /* Bit 1 */
    if (subInputStream.readBit() == 1) {
      isTPGROn = true;
    }

    /* Bit 0 */
    templateID = (short) subInputStream.readBit();

    switch (templateID){
      case 0 :
        template = T0;
        readAtPixels();
        break;
      case 1 :
        template = T1;
        break;
    }
  }

  private void readAtPixels() throws IOException {
    grAtX = new short[2];
    grAtY = new short[2];

    /* Byte 0 */
    grAtX[0] = subInputStream.readByte();
    /* Byte 1 */
    grAtY[0] = subInputStream.readByte();
    /* Byte 2 */
    grAtX[1] = subInputStream.readByte();
    /* Byte 3 */
    grAtY[1] = subInputStream.readByte();
  }

  /**
   * Decode using a template and arithmetic coding, as described in 6.3.5.6
   * 
   * @throws IOException
   * @throws InvalidHeaderValueException
   * @throws IntegerMaxValueException
   */
  public Bitmap getRegionBitmap() throws IOException, IntegerMaxValueException, InvalidHeaderValueException {
    if (null == regionBitmap) {
      /* 6.3.5.6 - 1) */
      int isLineTypicalPredicted = 0;

      if (referenceBitmap == null) {
        // Get the reference bitmap, which is the base of refinement process
        referenceBitmap = getGrReference();
      }

      if (arithDecoder == null) {
        arithDecoder = new ArithmeticDecoder(subInputStream);
      }

      if (cx == null) {
        cx = new CX(8192, 1);
      }

      /* 6.3.5.6 - 2) */
      regionBitmap = new Bitmap(regionInfo.getBitmapWidth(), regionInfo.getBitmapHeight());

      if (templateID == 0) {
        // AT pixel may only occur in template 0
        updateOverride();
      }

      final int paddedWidth = (regionBitmap.getWidth() + 7) & -8;
      final int deltaRefStride = isTPGROn ? -referenceDY * referenceBitmap.getRowStride() : 0;
      final int yOffset = deltaRefStride + 1;

      /* 6.3.5.6 - 3 */
      for (int y = 0; y < regionBitmap.getHeight(); y++) {
        /* 6.3.5.6 - 3 b) */
        if (isTPGROn) {
          isLineTypicalPredicted ^= decodeSLTP();
        }

        if (isLineTypicalPredicted == 0) {
          /* 6.3.5.6 - 3 c) */
          decodeOptimized(y, regionBitmap.getWidth(), regionBitmap.getRowStride(), referenceBitmap.getRowStride(),
              paddedWidth, deltaRefStride, yOffset);
        } else {
          /* 6.3.5.6 - 3 d) */
          decodeTypicalPredictedLine(y, regionBitmap.getWidth(), regionBitmap.getRowStride(),
              referenceBitmap.getRowStride(), paddedWidth, deltaRefStride);
        }
      }
    }
    /* 6.3.5.6 - 4) */
    return regionBitmap;
  }

  private int decodeSLTP() throws IOException {
    template.setIndex(cx);
    return arithDecoder.decode(cx);
  }

  private Bitmap getGrReference() throws IntegerMaxValueException, InvalidHeaderValueException, IOException {
    final SegmentHeader[] segments = segmentHeader.getRtSegments();
    final Region region = (Region) segments[0].getSegmentData();

    return region.getRegionBitmap();
  }

  private void decodeOptimized(final int lineNumber, final int width, final int rowStride, final int refRowStride,
      final int paddedWidth, final int deltaRefStride, final int lineOffset) throws IOException {

    // Offset of the reference bitmap with respect to the bitmap being decoded
    // For example: if referenceDY = -1, y is 1 HIGHER that currY
    final int currentLine = lineNumber - referenceDY;
    final int referenceByteIndex = referenceBitmap.getByteIndex(Math.max(0, -referenceDX), currentLine);

    final int byteIndex = regionBitmap.getByteIndex(Math.max(0, referenceDX), lineNumber);

    switch (templateID){
      case 0 :
        decodeTemplate(lineNumber, width, rowStride, refRowStride, paddedWidth, deltaRefStride, lineOffset, byteIndex,
            currentLine, referenceByteIndex, T0);
        break;
      case 1 :
        decodeTemplate(lineNumber, width, rowStride, refRowStride, paddedWidth, deltaRefStride, lineOffset, byteIndex,
            currentLine, referenceByteIndex, T1);
        break;
    }

  }

  private void decodeTemplate(final int lineNumber, final int width, final int rowStride, final int refRowStride,
      final int paddedWidth, final int deltaRefStride, final int lineOffset, int byteIndex, final int currentLine,
      int refByteIndex, Template templateFormation) throws IOException {
    short c1, c2, c3, c4, c5;

    int w1, w2, w3, w4;
    w1 = w2 = w3 = w4 = 0;

    if (currentLine >= 1 && (currentLine - 1) < referenceBitmap.getHeight())
      w1 = referenceBitmap.getByteAsInteger(refByteIndex - refRowStride);
    if (currentLine >= 0 && currentLine < referenceBitmap.getHeight())
      w2 = referenceBitmap.getByteAsInteger(refByteIndex);
    if (currentLine >= -1 && currentLine + 1 < referenceBitmap.getHeight())
      w3 = referenceBitmap.getByteAsInteger(refByteIndex + refRowStride);
    refByteIndex++;

    if (lineNumber >= 1) {
      w4 = regionBitmap.getByteAsInteger(byteIndex - rowStride);
    }
    byteIndex++;

    final int modReferenceDX = referenceDX % 8;
    final int shiftOffset = 6 + modReferenceDX;
    final int modRefByteIdx = refByteIndex % refRowStride;

    if (shiftOffset >= 0) {
      c1 = (short) ((shiftOffset >= 8 ? 0 : w1 >>> shiftOffset) & 0x07);
      c2 = (short) ((shiftOffset >= 8 ? 0 : w2 >>> shiftOffset) & 0x07);
      c3 = (short) ((shiftOffset >= 8 ? 0 : w3 >>> shiftOffset) & 0x07);
      if (shiftOffset == 6 && modRefByteIdx > 1) {
        if (currentLine >= 1 && (currentLine - 1) < referenceBitmap.getHeight()) {
          c1 |= referenceBitmap.getByteAsInteger(refByteIndex - refRowStride - 2) << 2 & 0x04;
        }
        if (currentLine >= 0 && currentLine < referenceBitmap.getHeight()) {
          c2 |= referenceBitmap.getByteAsInteger(refByteIndex - 2) << 2 & 0x04;
        }
        if (currentLine >= -1 && currentLine + 1 < referenceBitmap.getHeight()) {
          c3 |= referenceBitmap.getByteAsInteger(refByteIndex + refRowStride - 2) << 2 & 0x04;
        }
      }
      if (shiftOffset == 0) {
        w1 = w2 = w3 = 0;
        if (modRefByteIdx < refRowStride - 1) {
          if (currentLine >= 1 && (currentLine - 1) < referenceBitmap.getHeight())
            w1 = referenceBitmap.getByteAsInteger(refByteIndex - refRowStride);
          if (currentLine >= 0 && currentLine < referenceBitmap.getHeight())
            w2 = referenceBitmap.getByteAsInteger(refByteIndex);
          if (currentLine >= -1 && currentLine + 1 < referenceBitmap.getHeight())
            w3 = referenceBitmap.getByteAsInteger(refByteIndex + refRowStride);
        }
        refByteIndex++;
      }
    } else {
      c1 = (short) ((w1 << 1) & 0x07);
      c2 = (short) ((w2 << 1) & 0x07);
      c3 = (short) ((w3 << 1) & 0x07);
      w1 = w2 = w3 = 0;
      if (modRefByteIdx < refRowStride - 1) {
        if (currentLine >= 1 && (currentLine - 1) < referenceBitmap.getHeight())
          w1 = referenceBitmap.getByteAsInteger(refByteIndex - refRowStride);
        if (currentLine >= 0 && currentLine < referenceBitmap.getHeight())
          w2 = referenceBitmap.getByteAsInteger(refByteIndex);
        if (currentLine >= -1 && currentLine + 1 < referenceBitmap.getHeight())
          w3 = referenceBitmap.getByteAsInteger(refByteIndex + refRowStride);
        refByteIndex++;
      }
      c1 |= (short) ((w1 >>> 7) & 0x07);
      c2 |= (short) ((w2 >>> 7) & 0x07);
      c3 |= (short) ((w3 >>> 7) & 0x07);
    }

    c4 = (short) (w4 >>> 6);
    c5 = 0;

    final int modBitsToTrim = (2 - modReferenceDX) % 8;
    w1 <<= modBitsToTrim;
    w2 <<= modBitsToTrim;
    w3 <<= modBitsToTrim;

    w4 <<= 2;

    for (int x = 0; x < width; x++) {
      final int minorX = x & 0x07;

      final short tval = templateFormation.form(c1, c2, c3, c4, c5);

      if (override) {
        cx.setIndex(overrideAtTemplate0(tval, x, lineNumber,
            regionBitmap.getByte(regionBitmap.getByteIndex(x, lineNumber)), minorX));
      } else {
        cx.setIndex(tval);
      }
      final int bit = arithDecoder.decode(cx);
      regionBitmap.setPixel(x, lineNumber, (byte) bit);

      c1 = (short) (((c1 << 1) | 0x01 & (w1 >>> 7)) & 0x07);
      c2 = (short) (((c2 << 1) | 0x01 & (w2 >>> 7)) & 0x07);
      c3 = (short) (((c3 << 1) | 0x01 & (w3 >>> 7)) & 0x07);
      c4 = (short) (((c4 << 1) | 0x01 & (w4 >>> 7)) & 0x07);
      c5 = (short) bit;

      if ((x - referenceDX) % 8 == 5) {
        if (((x - referenceDX) / 8) + 1 >= referenceBitmap.getRowStride()) {
          w1 = w2 = w3 = 0;
        } else {
          if (currentLine >= 1 && (currentLine - 1 < referenceBitmap.getHeight())) {
            w1 = referenceBitmap.getByteAsInteger(refByteIndex - refRowStride);
          } else {
            w1 = 0;
          }
          if (currentLine >= 0 && currentLine < referenceBitmap.getHeight()) {
            w2 = referenceBitmap.getByteAsInteger(refByteIndex);
          } else {
            w2 = 0;
          }
          if (currentLine >= -1 && (currentLine + 1) < referenceBitmap.getHeight()) {
            w3 = referenceBitmap.getByteAsInteger(refByteIndex + refRowStride);
          } else {
            w3 = 0;
          }
        }
        refByteIndex++;
      } else {
        w1 <<= 1;
        w2 <<= 1;
        w3 <<= 1;
      }

      if (minorX == 5 && lineNumber >= 1) {
        if ((x >> 3) + 1 >= regionBitmap.getRowStride()) {
          w4 = 0;
        } else {
          w4 = regionBitmap.getByteAsInteger(byteIndex - rowStride);
        }
        byteIndex++;
      } else {
        w4 <<= 1;
      }

    }
  }

  private void updateOverride() {
    if (grAtX == null || grAtY == null) {
      log.info("AT pixels not set");
      return;
    }

    if (grAtX.length != grAtY.length) {
      log.info("AT pixel inconsistent");
      return;
    }

    grAtOverride = new boolean[grAtX.length];

    switch (templateID){
      case 0 :
        if (grAtX[0] != -1 && grAtY[0] != -1) {
          grAtOverride[0] = true;
          override = true;
        }

        if (grAtX[1] != -1 && grAtY[1] != -1) {
          grAtOverride[1] = true;
          override = true;
        }
        break;
      case 1 :
        override = false;
        break;
    }
  }

  private void decodeTypicalPredictedLine(final int lineNumber, final int width, final int rowStride,
      final int refRowStride, final int paddedWidth, final int deltaRefStride) throws IOException {

    // Offset of the reference bitmap with respect to the bitmap being
    // decoded
    // For example: if grReferenceDY = -1, y is 1 HIGHER that currY
    final int currentLine = lineNumber - referenceDY;
    final int refByteIndex = referenceBitmap.getByteIndex(0, currentLine);

    final int byteIndex = regionBitmap.getByteIndex(0, lineNumber);

    switch (templateID){
      case 0 :
        decodeTypicalPredictedLineTemplate0(lineNumber, width, rowStride, refRowStride, paddedWidth, deltaRefStride,
            byteIndex, currentLine, refByteIndex);
        break;
      case 1 :
        decodeTypicalPredictedLineTemplate1(lineNumber, width, rowStride, refRowStride, paddedWidth, deltaRefStride,
            byteIndex, currentLine, refByteIndex);
        break;
    }
  }

  private void decodeTypicalPredictedLineTemplate0(final int lineNumber, final int width, final int rowStride,
      final int refRowStride, final int paddedWidth, final int deltaRefStride, int byteIndex, final int currentLine,
      int refByteIndex) throws IOException {
    int context;
    int overriddenContext;

    int previousLine;
    int previousReferenceLine;
    int currentReferenceLine;
    int nextReferenceLine;

    if (lineNumber > 0) {
      previousLine = regionBitmap.getByteAsInteger(byteIndex - rowStride);
    } else {
      previousLine = 0;
    }

    if (currentLine > 0 && currentLine <= referenceBitmap.getHeight()) {
      previousReferenceLine = referenceBitmap.getByteAsInteger(refByteIndex - refRowStride + deltaRefStride) << 4;
    } else {
      previousReferenceLine = 0;
    }

    if (currentLine >= 0 && currentLine < referenceBitmap.getHeight()) {
      currentReferenceLine = referenceBitmap.getByteAsInteger(refByteIndex + deltaRefStride) << 1;
    } else {
      currentReferenceLine = 0;
    }

    if (currentLine > -2 && currentLine < (referenceBitmap.getHeight() - 1)) {
      nextReferenceLine = referenceBitmap.getByteAsInteger(refByteIndex + refRowStride + deltaRefStride);
    } else {
      nextReferenceLine = 0;
    }

    context = ((previousLine >> 5) & 0x6) | ((nextReferenceLine >> 2) & 0x30) | (currentReferenceLine & 0x180)
        | (previousReferenceLine & 0xc00);

    int nextByte;
    for (int x = 0; x < paddedWidth; x = nextByte) {
      byte result = 0;
      nextByte = x + 8;
      final int minorWidth = width - x > 8 ? 8 : width - x;
      final boolean readNextByte = nextByte < width;
      final boolean refReadNextByte = nextByte < referenceBitmap.getWidth();

      final int yOffset = deltaRefStride + 1;

      if (lineNumber > 0) {
        previousLine = (previousLine << 8)
            | (readNextByte ? regionBitmap.getByteAsInteger(byteIndex - rowStride + 1) : 0);
      }

      if (currentLine > 0 && currentLine <= referenceBitmap.getHeight()) {
        previousReferenceLine = (previousReferenceLine << 8)
            | (refReadNextByte ? referenceBitmap.getByteAsInteger(refByteIndex - refRowStride + yOffset) << 4 : 0);
      }

      if (currentLine >= 0 && currentLine < referenceBitmap.getHeight()) {
        currentReferenceLine = (currentReferenceLine << 8)
            | (refReadNextByte ? referenceBitmap.getByteAsInteger(refByteIndex + yOffset) << 1 : 0);
      }

      if (currentLine > -2 && currentLine < (referenceBitmap.getHeight() - 1)) {
        nextReferenceLine = (nextReferenceLine << 8)
            | (refReadNextByte ? referenceBitmap.getByteAsInteger(refByteIndex + refRowStride + yOffset) : 0);
      }

      for (int minorX = 0; minorX < minorWidth; minorX++) {
        boolean isPixelTypicalPredicted = false;
        int bit = 0;

        // i)
        final int bitmapValue = (context >> 4) & 0x1FF;

        if (bitmapValue == 0x1ff) {
          isPixelTypicalPredicted = true;
          bit = 1;
        } else if (bitmapValue == 0x00) {
          isPixelTypicalPredicted = true;
          bit = 0;
        }

        if (!isPixelTypicalPredicted) {
          // iii) - is like 3 c) but for one pixel only

          if (override) {
            overriddenContext = overrideAtTemplate0(context, x + minorX, lineNumber, result, minorX);
            cx.setIndex(overriddenContext);
          } else {
            cx.setIndex(context);
          }
          bit = arithDecoder.decode(cx);
        }

        final int toShift = 7 - minorX;
        result |= bit << toShift;

        context = ((context & 0xdb6) << 1) | bit | ((previousLine >> toShift + 5) & 0x002)
            | ((nextReferenceLine >> toShift + 2) & 0x010) | ((currentReferenceLine >> toShift) & 0x080)
            | ((previousReferenceLine >> toShift) & 0x400);
      }
      regionBitmap.setByte(byteIndex++, result);
      refByteIndex++;
    }
  }

  private void decodeTypicalPredictedLineTemplate1(final int lineNumber, final int width, final int rowStride,
      final int refRowStride, final int paddedWidth, final int deltaRefStride, int byteIndex, final int currentLine,
      int refByteIndex) throws IOException {
    int context;
    int grReferenceValue;

    int previousLine;
    int previousReferenceLine;
    int currentReferenceLine;
    int nextReferenceLine;

    if (lineNumber > 0) {
      previousLine = regionBitmap.getByteAsInteger(byteIndex - rowStride);
    } else {
      previousLine = 0;
    }

    if (currentLine > 0 && currentLine <= referenceBitmap.getHeight()) {
      previousReferenceLine = referenceBitmap.getByteAsInteger(byteIndex - refRowStride + deltaRefStride) << 2;
    } else {
      previousReferenceLine = 0;
    }

    if (currentLine >= 0 && currentLine < referenceBitmap.getHeight()) {
      currentReferenceLine = referenceBitmap.getByteAsInteger(byteIndex + deltaRefStride);
    } else {
      currentReferenceLine = 0;
    }

    if (currentLine > -2 && currentLine < (referenceBitmap.getHeight() - 1)) {
      nextReferenceLine = referenceBitmap.getByteAsInteger(byteIndex + refRowStride + deltaRefStride);
    } else {
      nextReferenceLine = 0;
    }

    context = ((previousLine >> 5) & 0x6) | ((nextReferenceLine >> 2) & 0x30) | (currentReferenceLine & 0xc0)
        | (previousReferenceLine & 0x200);

    grReferenceValue = ((nextReferenceLine >> 2) & 0x70) | (currentReferenceLine & 0xc0)
        | (previousReferenceLine & 0x700);

    int nextByte;
    for (int x = 0; x < paddedWidth; x = nextByte) {
      byte result = 0;
      nextByte = x + 8;
      final int minorWidth = width - x > 8 ? 8 : width - x;
      final boolean readNextByte = nextByte < width;
      final boolean refReadNextByte = nextByte < referenceBitmap.getWidth();

      final int yOffset = deltaRefStride + 1;

      if (lineNumber > 0) {
        previousLine = (previousLine << 8)
            | (readNextByte ? regionBitmap.getByteAsInteger(byteIndex - rowStride + 1) : 0);
      }

      if (currentLine > 0 && currentLine <= referenceBitmap.getHeight()) {
        previousReferenceLine = (previousReferenceLine << 8)
            | (refReadNextByte ? referenceBitmap.getByteAsInteger(refByteIndex - refRowStride + yOffset) << 2 : 0);
      }

      if (currentLine >= 0 && currentLine < referenceBitmap.getHeight()) {
        currentReferenceLine = (currentReferenceLine << 8)
            | (refReadNextByte ? referenceBitmap.getByteAsInteger(refByteIndex + yOffset) : 0);
      }

      if (currentLine > -2 && currentLine < (referenceBitmap.getHeight() - 1)) {
        nextReferenceLine = (nextReferenceLine << 8)
            | (refReadNextByte ? referenceBitmap.getByteAsInteger(refByteIndex + refRowStride + yOffset) : 0);
      }

      for (int minorX = 0; minorX < minorWidth; minorX++) {
        int bit = 0;

        // i)
        final int bitmapValue = (grReferenceValue >> 4) & 0x1ff;

        if (bitmapValue == 0x1ff) {
          bit = 1;
        } else if (bitmapValue == 0x00) {
          bit = 0;
        } else {
          cx.setIndex(context);
          bit = arithDecoder.decode(cx);
        }

        final int toShift = 7 - minorX;
        result |= bit << toShift;

        context = ((context & 0x0d6) << 1) | bit | ((previousLine >> toShift + 5) & 0x002)
            | ((nextReferenceLine >> toShift + 2) & 0x010) | ((currentReferenceLine >> toShift) & 0x040)
            | ((previousReferenceLine >> toShift) & 0x200);

        grReferenceValue = ((grReferenceValue & 0x0db) << 1) | ((nextReferenceLine >> toShift + 2) & 0x010)
            | ((currentReferenceLine >> toShift) & 0x080) | ((previousReferenceLine >> toShift) & 0x400);
      }
      regionBitmap.setByte(byteIndex++, result);
      refByteIndex++;
    }
  }

  private int overrideAtTemplate0(int context, final int x, final int y, final int result, final int minorX)
      throws IOException {
    if (grAtOverride[0]) {
      context &= 0xfff7;
      if (grAtY[0] == 0 && grAtX[0] >= -minorX) {
        context |= (result >> (7 - (minorX + grAtX[0])) & 0x1) << 3;
      } else {
        context |= getPixel(regionBitmap, x + grAtX[0], y + grAtY[0]) << 3;
      }
    }

    if (grAtOverride[1]) {
      context &= 0xefff;
      if (grAtY[1] == 0 && grAtX[1] >= -minorX) {
        context |= (result >> (7 - (minorX + grAtX[1])) & 0x1) << 12;
      } else {
        context |= getPixel(referenceBitmap, x + grAtX[1] + referenceDX, y + grAtY[1] + referenceDY) << 12;
      }
    }
    return context;
  }

  private byte getPixel(final Bitmap b, final int x, final int y) throws IOException {
    if (x < 0 || x >= b.getWidth()) {
      return 0;
    }
    if (y < 0 || y >= b.getHeight()) {
      return 0;
    }

    return b.getPixel(x, y);
  }

  public void init(final SegmentHeader header, final SubInputStream sis) throws IOException {
    this.segmentHeader = header;
    this.subInputStream = sis;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
    parseHeader();
  }

  protected void setParameters(final CX cx, final ArithmeticDecoder arithmeticDecoder, final short grTemplate,
      final int regionWidth, final int regionHeight, final Bitmap grReference, final int grReferenceDX,
      final int grReferenceDY, final boolean isTPGRon, final short[] grAtX, final short[] grAtY) {

    if (null != cx) {
      this.cx = cx;
    }

    if (null != arithmeticDecoder) {
      this.arithDecoder = arithmeticDecoder;
    }

    this.templateID = grTemplate;

    this.regionInfo.setBitmapWidth(regionWidth);
    this.regionInfo.setBitmapHeight(regionHeight);

    this.referenceBitmap = grReference;
    this.referenceDX = grReferenceDX;
    this.referenceDY = grReferenceDY;

    this.isTPGROn = isTPGRon;

    this.grAtX = grAtX;
    this.grAtY = grAtY;

    this.regionBitmap = null;
  }

  public RegionSegmentInformation getRegionInfo() {
    return regionInfo;
  }
}
