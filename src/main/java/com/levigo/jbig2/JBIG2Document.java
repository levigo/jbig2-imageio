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

import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * This class represents the document structure with its pages and global segments.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
class JBIG2Document {
  private static final Logger log = LoggerFactory.getLogger(JBIG2Document.class);

  /** ID string in file header, see ISO/IEC 14492:2001, D.4.1 */
  private int[] FILE_HEADER_ID = {
      0x97, 0x4A, 0x42, 0x32, 0x0D, 0x0A, 0x1A, 0x0A
  };

  /**
   * This map contains all pages of this document. The key is the number of the page.
   */
  private final Map<Integer, JBIG2Page> pages = new TreeMap<Integer, JBIG2Page>();


  /** BASIC INFORMATION ABOUT THE CURRENT JBIG2 DOCUMENT */

  /** The length of the file header if exists */
  private short fileHeaderLength = 9;

  /**
   * According to D.4.2 - File header bit 0
   * <p>
   * This flag contains information about the file organization:
   * <ul>
   * <li>{@code 1} for sequential</li>
   * <li>{@code 0} for random-access</li>
   * </ul>
   * You can use the constants {@link #RANDOM} and {@link JBIG2Document#SEQUENTIAL}.
   */
  private short organisationType = SEQUENTIAL;

  public static final int RANDOM = 0;
  public static final int SEQUENTIAL = 1;

  /**
   * According to D.4.2 - Bit 1<br>
   * <ul>
   * <li>{@code true} if amount of pages is unknown, amount of pages field is not present</li>
   * <li>{@code false} if there is a field in the file header where the amount of pages can be read</li>
   * </ul>
   */
  private boolean amountOfPagesUnknown = true;

  /**
   * According to D.4.3 - Number of pages field (4 bytes). Only present if
   * {@link #amountOfPagesUnknown} is {@code false}.
   */
  private int amountOfPages;

  /** Defines whether extended Template is used. */
  private boolean gbUseExtTemplate;

  /**
   * This is the source data stream wrapped into a {@link SubInputStream}.
   */
  private final SubInputStream subInputStream;

  /**
   * Flag:
   * <ul>
   * <li>{@code true} if stream is embedded in another file format and the file header is missing</li>
   * <li>{@code false} if stream is created of a native jbig2 file and the file header should be
   * present</li>
   * </ul>
   */

  /**
   * Holds a load of segments, that aren't associated with a page.
   */
  private JBIG2Globals globalSegments;

  protected JBIG2Document(ImageInputStream input) throws IOException {
    this(input, null);
  }

  protected JBIG2Document(ImageInputStream input, JBIG2Globals globals) throws IOException {
    if (input == null)
      throw new IllegalArgumentException("imageInputStream must not be null");

    this.subInputStream = new SubInputStream(input, 0, Long.MAX_VALUE);
    this.globalSegments = globals;

    mapStream();
  }

  /**
   * Retrieves the segment with the given segment number considering only segments that aren't
   * associated with a page.
   * 
   * @param segmentNr - The number of the wanted segment.
   * @return The requested {@link SegmentHeader}.
   */
  SegmentHeader getGlobalSegment(int segmentNr) {
    if (null != globalSegments) {
      return globalSegments.getSegment(segmentNr);
    }

    if (log.isErrorEnabled()) {
      log.error("Segment not found. Returning null.");
    }

    return null;
  }

  /**
   * Retrieves a {@link JBIG2Page} specified by the given page number.
   * 
   * @param pageNumber - The page number of the wanted {@link JBIG2Page}.
   * 
   * @return The requested {@link JBIG2Page}.
   */
  protected JBIG2Page getPage(int pageNumber) {
    return pages.get(pageNumber);
  }

  /**
   * Retrieves the amount of pages in this JBIG2 document. If the pages are striped, the document
   * will be completely parsed and the amount of pages will be gathered.
   * 
   * @return The amount of pages in this JBIG2 document.
   * @throws IOException
   */
  protected int getAmountOfPages() throws IOException {
    if (amountOfPagesUnknown || amountOfPages == 0) {
      if (pages.size() == 0) {
        mapStream();
      }

      return pages.size();
    } else {
      return amountOfPages;
    }
  }

  /**
   * This method maps the stream and stores all segments.
   */
  private void mapStream() throws IOException {
    final List<SegmentHeader> segments = new LinkedList<SegmentHeader>();

    long offset = 0;
    int segmentType = 0;

    /*
     * Parse the file header if there is one.
     */
    if (isFileHeaderPresent()) {
      parseFileHeader();
      offset += fileHeaderLength;
    }

    if (globalSegments == null) {
      globalSegments = new JBIG2Globals();
    }

    JBIG2Page page = null;

    /*
     * If organisation type is random-access: walk through the segment headers until EOF segment
     * appears (specified with segment number 51)
     */
    while (segmentType != 51 && !reachedEndOfStream(offset)) {
      SegmentHeader segment = new SegmentHeader(this, subInputStream, offset, organisationType);

      final int associatedPage = segment.getPageAssociation();
      segmentType = segment.getSegmentType();

      if (associatedPage != 0) {
        page = getPage(associatedPage);
        if (page == null) {
          page = new JBIG2Page(this, associatedPage);
          pages.put(associatedPage, page);
        }
        page.add(segment);
      } else {
        globalSegments.addSegment(segment.getSegmentNr(), segment);
      }
      segments.add(segment);

      if (JBIG2ImageReader.DEBUG) {
        if (log.isDebugEnabled()) {
          log.debug(segment.toString());
        }
      }

      offset = subInputStream.getStreamPosition();

      // Sequential organization skips data part and sets the offset
      if (organisationType == SEQUENTIAL) {
        offset += segment.getSegmentDataLength();
      }
    }

    /*
     * Random organization: segment headers are finished. Data part starts and the offset can be
     * set.
     */
    determineRandomDataOffsets(segments, offset);
  }

  private boolean isFileHeaderPresent() throws IOException {
    final SubInputStream input = subInputStream;
    input.mark();

    for (int magicByte : FILE_HEADER_ID) {
      if (magicByte != input.read()) {
        input.reset();
        return false;
      }
    }

    input.reset();
    return true;
  }

  /**
   * Determines the start of the data parts and sets the offset.
   * 
   * @param segments
   * @param offset
   */
  private void determineRandomDataOffsets(List<SegmentHeader> segments, long offset) {
    if (organisationType == RANDOM) {
      for (SegmentHeader s : segments) {
        s.setSegmentDataStartOffset(offset);
        offset += s.getSegmentDataLength();
      }
    }
  }

  /**
   * This method reads the stream and sets variables for information about organization type and
   * length etc.
   * 
   * @return
   * @throws IOException
   */
  private void parseFileHeader() throws IOException {
    subInputStream.seek(0);

    /* D.4.1 - ID string, read will be skipped */
    subInputStream.skipBytes(8);

    /*
     * D.4.2 Header flag (1 byte)
     */

    // Bit 3-7 are reserved and must be 0
    subInputStream.readBits(5);

    // Bit 2 - Indicates if extended templates are used
    if (subInputStream.readBit() == 1) {
      gbUseExtTemplate = true;
    }

    // Bit 1 - Indicates if amount of pages are unknown
    if (subInputStream.readBit() != 1) {
      amountOfPagesUnknown = false;
    }

    // Bit 0 - Indicates file organisation type
    organisationType = (short) subInputStream.readBit();

    // fileHeaderLength = 9;

    /*
     * D.4.3 Number of pages (field is only present if amount of pages are 'NOT unknown')
     */
    if (!amountOfPagesUnknown) {
      amountOfPages = (int) subInputStream.readUnsignedInt();
      fileHeaderLength = 13;
    }

  }

  /**
   * This method checks, if the stream is at its end to avoid {@link EOFException}s and reads 32
   * bits.
   * 
   * @param offset
   * @return <li>{@code true} if end of stream reached <li>{@code false} if there are more bytes to
   *         read
   * @throws IOException
   */
  private boolean reachedEndOfStream(long offset) throws IOException {
    try {
      subInputStream.seek(offset);
      subInputStream.readBits(32);
      return false;
    } catch (EOFException e) {
      return true;
    } catch (IndexOutOfBoundsException e) {
      return true;
    }
  }

  protected JBIG2Globals getGlobalSegments() {
    return globalSegments;
  }

  protected boolean isAmountOfPagesUnknown() {
    return amountOfPagesUnknown;
  }

  boolean isGbUseExtTemplate() {
    return gbUseExtTemplate;
  }

}
