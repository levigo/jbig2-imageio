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
import java.util.ArrayList;
import java.util.Arrays;

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.Region;
import com.levigo.jbig2.SegmentHeader;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.image.Bitmaps;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.CombinationOperator;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * This class represents the data of segment type "Halftone region". Parsing is described in 7.4.5,
 * page 67. Decoding procedure in 6.6.5 and 7.4.5.2.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
public class HalftoneRegion implements Region {

  private final Logger log = LoggerFactory.getLogger(HalftoneRegion.class);

  private SubInputStream subInputStream;
  private SegmentHeader segmentHeader;
  private long dataHeaderOffset;
  private long dataHeaderLength;
  private long dataOffset;
  private long dataLength;

  /** Region segment information field, 7.4.1 */
  private RegionSegmentInformation regionInfo;

  /** Halftone segment information field, 7.4.5.1.1 */
  private byte hDefaultPixel;
  private CombinationOperator hCombinationOperator;
  private boolean hSkipEnabled;
  private byte hTemplate;
  private boolean isMMREncoded;

  /** Halftone grid position and size, 7.4.5.1.2 */
  /** Width of the gray-scale image, 7.4.5.1.2.1 */
  private int hGridWidth;
  /** Height of the gray-scale image, 7.4.5.1.2.2 */
  private int hGridHeight;
  /** Horizontal offset of the grid, 7.4.5.1.2.3 */
  private int hGridX;
  /** Vertical offset of the grid, 7.4.5.1.2.4 */
  private int hGridY;

  /** Halftone grid vector, 7.4.5.1.3 */
  /** Horizontal coordinate of the halftone grid vector, 7.4.5.1.3.1 */
  private int hRegionX;
  /** Vertical coordinate of the halftone grod vector, 7.4.5.1.3.2 */
  private int hRegionY;

  /** Decoded data */
  private Bitmap halftoneRegionBitmap;

  /**
   * Previously decoded data from other regions or dictionaries, stored to use as patterns in this
   * region.
   */
  private ArrayList<Bitmap> patterns;

  public HalftoneRegion() {
  }

  public HalftoneRegion(final SubInputStream subInputStream) {
    this.subInputStream = subInputStream;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
  }

  public HalftoneRegion(final SubInputStream subInputStream, final SegmentHeader segmentHeader) {
    this.subInputStream = subInputStream;
    this.segmentHeader = segmentHeader;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
  }

  private void parseHeader() throws IOException, InvalidHeaderValueException {
    regionInfo.parseHeader();

    /* Bit 7 */
    hDefaultPixel = (byte) subInputStream.readBit();

    /* Bit 4-6 */
    hCombinationOperator = CombinationOperator.translateOperatorCodeToEnum((short) (subInputStream.readBits(3) & 0xf));

    /* Bit 3 */
    if (subInputStream.readBit() == 1) {
      hSkipEnabled = true;
    }

    /* Bit 1-2 */
    hTemplate = (byte) (subInputStream.readBits(2) & 0xf);

    /* Bit 0 */
    if (subInputStream.readBit() == 1) {
      isMMREncoded = true;
    }

    hGridWidth = (int) (subInputStream.readBits(32) & 0xffffffff);
    hGridHeight = (int) (subInputStream.readBits(32) & 0xffffffff);

    hGridX = (int) subInputStream.readBits(32);
    hGridY = (int) subInputStream.readBits(32);

    hRegionX = (int) subInputStream.readBits(16) & 0xffff;
    hRegionY = (int) subInputStream.readBits(16) & 0xffff;

    /* Segment data structure */
    computeSegmentDataStructure();

    this.checkInput();
  }

  private void computeSegmentDataStructure() throws IOException {
    dataOffset = subInputStream.getStreamPosition();
    dataHeaderLength = dataOffset - dataHeaderOffset;
    dataLength = subInputStream.length() - dataHeaderLength;
  }

  private void checkInput() throws InvalidHeaderValueException {
    if (isMMREncoded) {
      if (hTemplate != 0) {
        log.info("hTemplate = " + hTemplate + " (should contain the value 0)");
      }

      if (hSkipEnabled) {
        log.info("hSkipEnabled 0 " + hSkipEnabled + " (should contain the value false)");
      }
    }
  }

  /**
   * The procedure is described in JBIG2 ISO standard, 6.6.5.
   * 
   * @returns The decoded {@link Bitmap} of this region.
   */
  public Bitmap getRegionBitmap() throws IOException, InvalidHeaderValueException {
    if (null == halftoneRegionBitmap) {

      /* 6.6.5, page 40 */
      /* 1) */
      halftoneRegionBitmap = new Bitmap(regionInfo.getBitmapWidth(), regionInfo.getBitmapHeight());

      if (patterns == null) {
        patterns = getPatterns();
      }

      if (hDefaultPixel == 1) {
        Arrays.fill(halftoneRegionBitmap.getByteArray(), (byte) 0xff);
      }

      /* 2) */
      /*
       * 6.6.5.1 Computing hSkip - At the moment SKIP is not used... we are not able to test it.
       */
      // Bitmap hSkip;
      // if (hSkipEnabled) {
      // int hPatternHeight = (int) hPats.get(0).getHeight();
      // int hPatternWidth = (int) hPats.get(0).getWidth();
      // TODO: Set or get pattern width and height from referred
      // pattern segments. The method is called like this:
      // hSkip = computeHSkip(hPatternHeight, hPatternWidth);
      // }

      /* 3) */
      final int bitsPerValue = (int) Math.ceil(Math.log(patterns.size()) / Math.log(2));

      /* 4) */
      final int[][] grayScaleValues = grayScaleDecoding(bitsPerValue);

      /* 5), rendering the pattern, described in 6.6.5.2 */
      renderPattern(grayScaleValues);
    }
    /* 6) */
    return halftoneRegionBitmap;
  }

  /**
   * This method draws the pattern into the region bitmap ({@code htReg}), as described in 6.6.5.2,
   * page 42
   */
  private void renderPattern(final int[][] grayScaleValues) {
    int x = 0, y = 0;

    // 1)
    for (int m = 0; m < hGridHeight; m++) {
      // a)
      for (int n = 0; n < hGridWidth; n++) {
        // i)
        x = computeX(m, n);
        y = computeY(m, n);

        // ii)
        final Bitmap patternBitmap = patterns.get(grayScaleValues[m][n]);
        Bitmaps.blit(patternBitmap, halftoneRegionBitmap, (x + hGridX), (y + hGridY), hCombinationOperator);
      }
    }
  }

  /**
   * @throws IOException
   * @throws InvalidHeaderValueException
   * 
   */
  private ArrayList<Bitmap> getPatterns() throws InvalidHeaderValueException, IOException {
    final ArrayList<Bitmap> patterns = new ArrayList<Bitmap>();

    for (SegmentHeader s : segmentHeader.getRtSegments()) {
      final PatternDictionary patternDictionary = (PatternDictionary) s.getSegmentData();
      patterns.addAll(patternDictionary.getDictionary());
    }

    return patterns;
  }

  /**
   * Gray-scale image decoding procedure is special for halftone region decoding and is described in
   * Annex C.5 on page 98.
   */
  private int[][] grayScaleDecoding(final int bitsPerValue) throws IOException {

    short[] gbAtX = null;
    short[] gbAtY = null;

    if (!isMMREncoded) {
      gbAtX = new short[4];
      gbAtY = new short[4];
      // Set AT pixel values
      if (hTemplate <= 1)
        gbAtX[0] = 3;
      else if (hTemplate >= 2)
        gbAtX[0] = 2;

      gbAtY[0] = -1;
      gbAtX[1] = -3;
      gbAtY[1] = -1;
      gbAtX[2] = 2;
      gbAtY[2] = -2;
      gbAtX[3] = -2;
      gbAtY[3] = -2;
    }

    Bitmap[] grayScalePlanes = new Bitmap[bitsPerValue];

    // 1)
    GenericRegion genericRegion = new GenericRegion(subInputStream);
    genericRegion.setParameters(isMMREncoded, dataOffset, dataLength, hGridHeight, hGridWidth, hTemplate, false,
        hSkipEnabled, gbAtX, gbAtY);

    // 2)
    int j = bitsPerValue - 1;

    grayScalePlanes[j] = genericRegion.getRegionBitmap();

    while (j > 0) {
      j--;
      genericRegion.resetBitmap();
      // 3) a)
      grayScalePlanes[j] = genericRegion.getRegionBitmap();
      // 3) b)
      grayScalePlanes = combineGrayScalePlanes(grayScalePlanes, j);
    }

    // 4)
    return computeGrayScaleValues(grayScalePlanes, bitsPerValue);
  }

  private Bitmap[] combineGrayScalePlanes(Bitmap[] grayScalePlanes, int j) {
    int byteIndex = 0;
    for (int y = 0; y < grayScalePlanes[j].getHeight(); y++) {

      for (int x = 0; x < grayScalePlanes[j].getWidth(); x += 8) {
        final byte newValue = grayScalePlanes[j + 1].getByte(byteIndex);
        final byte oldValue = grayScalePlanes[j].getByte(byteIndex);

        grayScalePlanes[j].setByte(byteIndex++, Bitmaps.combineBytes(oldValue, newValue, CombinationOperator.XOR));
      }
    }
    return grayScalePlanes;
  }

  private int[][] computeGrayScaleValues(final Bitmap[] grayScalePlanes, final int bitsPerValue) {
    // Gray-scale decoding procedure, page 98
    final int[][] grayScaleValues = new int[hGridHeight][hGridWidth];

    // 4)
    for (int y = 0; y < hGridHeight; y++) {
      for (int x = 0; x < hGridWidth; x += 8) {
        final int minorWidth = hGridWidth - x > 8 ? 8 : hGridWidth - x;
        int byteIndex = grayScalePlanes[0].getByteIndex(x, y);

        for (int minorX = 0; minorX < minorWidth; minorX++) {
          final int i = minorX + x;
          grayScaleValues[y][i] = 0;

          for (int j = 0; j < bitsPerValue; j++) {
            grayScaleValues[y][i] += ((grayScalePlanes[j].getByte(byteIndex) >> (7 - i & 7)) & 1) * (1 << j);
          }
        }
      }
    }
    return grayScaleValues;
  }

  private int computeX(final int m, final int n) {
    return shiftAndFill((hGridX + m * hRegionY + n * hRegionX));
  }

  private int computeY(final int m, final int n) {
    return shiftAndFill((hGridY + m * hRegionX - n * hRegionY));
  }

  private int shiftAndFill(int value) {
    // shift value by 8 and let the leftmost 8 bits be 0
    value >>= 8;

    if (value < 0) {
      // fill the leftmost 8 bits with 1
      final int bitPosition = (int) (Math.log(Integer.highestOneBit(value)) / Math.log(2));

      for (int i = 1; i < 31 - bitPosition; i++) {
        // bit flip
        value |= 1 << (31 - i);
      }
    }

    return value;
  }

  public void init(final SegmentHeader header, final SubInputStream sis) throws InvalidHeaderValueException,
      IOException {
    this.segmentHeader = header;
    this.subInputStream = sis;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
    parseHeader();
  }

  public CombinationOperator getCombinationOperator() {
    return hCombinationOperator;
  }

  public RegionSegmentInformation getRegionInfo() {
    return regionInfo;
  }

  protected byte getHTemplate() {
    return hTemplate;
  }

  protected boolean isHSkipEnabled() {
    return hSkipEnabled;
  }

  protected boolean isMMREncoded() {
    return isMMREncoded;
  }

  protected int getHGridWidth() {
    return hGridWidth;
  }

  protected int getHGridHeight() {
    return hGridHeight;
  }

  protected int getHGridX() {
    return hGridX;
  }

  protected int getHGridY() {
    return hGridY;
  }

  protected int getHRegionX() {
    return hRegionX;
  }

  protected int getHRegionY() {
    return hRegionY;
  }

  protected byte getHDefaultPixel() {
    return hDefaultPixel;
  }
}
