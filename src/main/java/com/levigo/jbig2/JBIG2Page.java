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

package com.levigo.jbig2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.err.JBIG2Exception;
import com.levigo.jbig2.image.Bitmaps;
import com.levigo.jbig2.segments.EndOfStripe;
import com.levigo.jbig2.segments.PageInformation;
import com.levigo.jbig2.segments.RegionSegmentInformation;
import com.levigo.jbig2.util.CombinationOperator;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * This class represents a JBIG2 page.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
 */
class JBIG2Page {

  private static final Logger log = LoggerFactory.getLogger(JBIG2Page.class);

  /**
   * This list contains all segments of this page, sorted by segment number in ascending order.
   */
  private final Map<Integer, SegmentHeader> segments = new TreeMap<Integer, SegmentHeader>();

  /** NOTE: page number != segmentList index */
  private final int pageNumber;

  /** The page bitmap that represents the page buffer */
  private Bitmap pageBitmap;

  private int finalHeight;
  private int finalWidth;
  private int resolutionX;
  private int resolutionY;

  private final JBIG2Document document;

  protected JBIG2Page(JBIG2Document document, int pageNumber) {
    this.document = document;
    this.pageNumber = pageNumber;
  }

  /**
   * This method searches for a segment specified by its number.
   * 
   * @param number - Segment number of the segment to search.
   * 
   * @return The retrieved {@link SegmentHeader} or {@code null} if not available.
   */
  public SegmentHeader getSegment(int number) {
    SegmentHeader s = segments.get(number);

    if (null != s) {
      return s;
    }

    if (null != document) {
      return document.getGlobalSegment(number);
    }

    log.info("Segment not found, returning null.");
    return null;
  }

  /**
   * Returns the associated page information segment.
   * 
   * @return The associated {@link PageInformation} segment or {@code null} if not available.
   */
  protected SegmentHeader getPageInformationSegment() {
    for (SegmentHeader s : segments.values()) {
      if (s.getSegmentType() == 48) {
        return s;
      }
    }

    log.info("Page information segment not found.");
    return null;
  }

  /**
   * This method returns the decoded bitmap if present. Otherwise the page bitmap will be composed
   * before returning the result.
   * 
   * @return pageBitmap - The result of decoding a page
   * @throws JBIG2Exception
   * @throws IOException
   */
  protected Bitmap getBitmap() throws JBIG2Exception, IOException {
    long timestamp;

    if (JBIG2ImageReader.PERFORMANCE_TEST) {
      timestamp = System.currentTimeMillis();
    }

    if (null == pageBitmap) {
      composePageBitmap();
    }

    if (JBIG2ImageReader.PERFORMANCE_TEST) {
      log.info("PAGE DECODING: " + (System.currentTimeMillis() - timestamp) + " ms");
    }

    return pageBitmap;
  }

  /**
   * This method composes the segments' bitmaps to a page and stores the page as a {@link Bitmap}
   * 
   * @throws IOException
   * @throws JBIG2Exception
   */
  private void composePageBitmap() throws IOException, JBIG2Exception {
    if (pageNumber > 0) {
      // Page 79, 1) Decoding the page information segment
      PageInformation pageInformation = (PageInformation) getPageInformationSegment().getSegmentData();
      createPage(pageInformation);
      clearSegmentData();
    }
  }

  private void createPage(PageInformation pageInformation) throws IOException, IntegerMaxValueException,
      InvalidHeaderValueException {
    if (!pageInformation.isStriped() || pageInformation.getHeight() != -1) {
      // Page 79, 4)
      createNormalPage(pageInformation);
    } else {
      createStripedPage(pageInformation);
    }
  }

  private void createNormalPage(PageInformation pageInformation) throws IOException, IntegerMaxValueException,
      InvalidHeaderValueException {

    pageBitmap = new Bitmap(pageInformation.getWidth(), pageInformation.getHeight());

    // Page 79, 3)
    // If default pixel value is not 0, byte will be filled with 0xff
    if (pageInformation.getDefaultPixelValue() != 0) {
      Arrays.fill(pageBitmap.getByteArray(), (byte) 0xff);
    }

    for (SegmentHeader s : segments.values()) {
      // Page 79, 5)
      switch (s.getSegmentType()){
        case 6 : // Immediate text region
        case 7 : // Immediate lossless text region
        case 22 : // Immediate halftone region
        case 23 : // Immediate lossless halftone region
        case 38 : // Immediate generic region
        case 39 : // Immediate lossless generic region
        case 42 : // Immediate generic refinement region
        case 43 : // Immediate lossless generic refinement region
          final Region r = (Region) s.getSegmentData();

          final Bitmap regionBitmap = r.getRegionBitmap();

          if (fitsPage(pageInformation, regionBitmap)) {
            pageBitmap = regionBitmap;
          } else {
            final RegionSegmentInformation regionInfo = r.getRegionInfo();
            final CombinationOperator op = getCombinationOperator(pageInformation,
                regionInfo.getCombinationOperator());
            Bitmaps.blit(regionBitmap, pageBitmap, regionInfo.getXLocation(), regionInfo.getYLocation(), op);
          }

          break;
      }
    }
  }

  /**
   * Check if we have only one region that forms the complete page. If the dimension equals the
   * page's dimension set the region's bitmap as the page's bitmap. Otherwise we have to blit the
   * smaller region's bitmap into the page's bitmap (see Issue 6).
   * 
   * @param pageInformation
   * @param regionBitmap
   * @return
   */
  private boolean fitsPage(PageInformation pageInformation, final Bitmap regionBitmap) {
    return countRegions() == 1 && pageInformation.getDefaultPixelValue() == 0
        && pageInformation.getWidth() == regionBitmap.getWidth()
        && pageInformation.getHeight() == regionBitmap.getHeight();
  }

  private void createStripedPage(PageInformation pageInformation) throws IOException, IntegerMaxValueException,
      InvalidHeaderValueException {
    final ArrayList<SegmentData> pageStripes = collectPageStripes();

    pageBitmap = new Bitmap(pageInformation.getWidth(), finalHeight);

    int startLine = 0;
    for (SegmentData sd : pageStripes) {
      if (sd instanceof EndOfStripe) {
        startLine = ((EndOfStripe) sd).getLineNumber() + 1;
      } else {
        final Region r = (Region) sd;
        final RegionSegmentInformation regionInfo = r.getRegionInfo();
        final CombinationOperator op = getCombinationOperator(pageInformation, regionInfo.getCombinationOperator());
        Bitmaps.blit(r.getRegionBitmap(), pageBitmap, regionInfo.getXLocation(), startLine, op);
      }
    }
  }

  private ArrayList<SegmentData> collectPageStripes() {
    final ArrayList<SegmentData> pageStripes = new ArrayList<SegmentData>();
    for (SegmentHeader s : segments.values()) {
      // Page 79, 5)
      switch (s.getSegmentType()){
        case 6 : // Immediate text region
        case 7 : // Immediate lossless text region
        case 22 : // Immediate halftone region
        case 23 : // Immediate lossless halftone region
        case 38 : // Immediate generic region
        case 39 : // Immediate lossless generic region
        case 42 : // Immediate generic refinement region
        case 43 : // Immediate lossless generic refinement region
          Region r = (Region) s.getSegmentData();
          pageStripes.add(r);
          break;

        case 50 : // End of stripe
          EndOfStripe eos = (EndOfStripe) s.getSegmentData();
          pageStripes.add(eos);
          finalHeight = eos.getLineNumber() + 1;
          break;
      }
    }

    return pageStripes;
  }

  /**
   * This method counts the regions segments. If there is only one region, the bitmap of this
   * segment is equal to the page bitmap and blitting is not necessary.
   * 
   * @return Amount of regions.
   */
  private int countRegions() {
    int regionCount = 0;

    for (SegmentHeader s : segments.values()) {
      switch (s.getSegmentType()){
        case 6 : // Immediate text region
        case 7 : // Immediate lossless text region
        case 22 : // Immediate halftone region
        case 23 : // Immediate lossless halftone region
        case 38 : // Immediate generic region
        case 39 : // Immediate lossless generic region
        case 42 : // Immediate generic refinement region
        case 43 : // Immediate lossless generic refinement region
          regionCount++;
      }
    }

    return regionCount;
  }

  /**
   * This method checks and sets, which combination operator shall be used.
   * 
   * @param pi - <code>PageInformation</code> object
   * @param newOperator - The combination operator, specified by actual segment
   * @return the new combination operator
   */
  private CombinationOperator getCombinationOperator(PageInformation pi, CombinationOperator newOperator) {
    if (pi.isCombinationOperatorOverrideAllowed()) {
      return newOperator;
    } else {
      return pi.getCombinationOperator();
    }
  }

  /**
   * Adds a {@link SegmentHeader} into the page's segments map.
   * 
   * @param segment - The segment to be added.
   */
  protected void add(SegmentHeader segment) {

    segments.put(segment.getSegmentNr(), segment);
  }

  /**
   * Resets the memory-critical segments to force on-demand-decoding and to avoid holding the
   * segments' bitmap too long.
   */
  private void clearSegmentData() {
    Set<Integer> keySet = segments.keySet();

    for (Integer key : keySet) {
      segments.get(key).cleanSegmentData();
    }
  }

  /**
   * Reset memory-critical parts of page.
   */
  protected void clearPageData() {
    pageBitmap = null;
  }

  /**
   * Returns the final height of the page.
   * 
   * @return The final height of the page.
   * @throws IOException
   * @throws JBIG2Exception
   */
  protected int getHeight() throws IOException, JBIG2Exception {
    if (finalHeight == 0) {
      PageInformation pi = (PageInformation) getPageInformationSegment().getSegmentData();
      if (pi.getHeight() == 0xffffffff) {
        getBitmap();
      } else {
        finalHeight = pi.getHeight();
      }
    }
    return finalHeight;
  }

  protected int getWidth() {
    if (finalWidth == 0) {
      PageInformation pi = (PageInformation) getPageInformationSegment().getSegmentData();
      finalWidth = pi.getWidth();
    }
    return finalWidth;
  }

  protected int getResolutionX() {
    if (resolutionX == 0) {
      PageInformation pi = (PageInformation) getPageInformationSegment().getSegmentData();
      resolutionX = pi.getResolutionX();
    }
    return resolutionX;
  }

  protected int getResolutionY() {
    if (resolutionY == 0) {
      PageInformation pi = (PageInformation) getPageInformationSegment().getSegmentData();
      resolutionY = pi.getResolutionY();
    }
    return resolutionY;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " (Page number: " + pageNumber + ")";
  }
}
