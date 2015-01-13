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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.err.JBIG2Exception;
import com.levigo.jbig2.image.Bitmaps;
import com.levigo.jbig2.image.FilterType;
import com.levigo.jbig2.util.cache.CacheFactory;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * @see ImageReader
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
public class JBIG2ImageReader extends ImageReader {
  private static final Logger log = LoggerFactory.getLogger(JBIG2ImageReader.class);

  public static final boolean DEBUG = false;
  public static final boolean PERFORMANCE_TEST = false;

  /** JBIG2 document to which we delegate current work. */
  private JBIG2Document document;

  /** Globals are JBIG2 segments for PDF wide use. */
  private JBIG2Globals globals;

  /**
   * @see ImageReader#ImageReader(ImageReaderSpi)
   * 
   * @param originatingProvider - The {@code ImageReaderSpi} that is invoking this constructor, or
   *          {@code null}.
   * @throws IOException if something went wrong while reading the provided stream.
   */
  public JBIG2ImageReader(ImageReaderSpi originatingProvider) throws IOException {
    super(originatingProvider);
  }

  /**
   * @see ImageReader#getDefaultReadParam()
   */
  @Override
  public JBIG2ReadParam getDefaultReadParam() {
    return new JBIG2ReadParam();
  }

  /**
   * Returns a default {@linkplain ImageReadParam} object for a specific page.
   * 
   * @param imageIndex - The page number.
   * @return
   */
  private JBIG2ReadParam getDefaultReadParam(final int imageIndex) {
    int width = 1;
    int height = 1;

    try {
      final int index = (imageIndex < getDocument().getAmountOfPages()) ? imageIndex : 0;
      width = getWidth(index);
      height = getHeight(index);
    } catch (IOException e) {
      if (log.isInfoEnabled()) {
        log.info("Dimensions could not be determined. Returning read params with size " + width + "x" + height);
      }
    }

    return new JBIG2ReadParam(1, 1, 0, 0, new Rectangle(0, 0, width, height), new Dimension(width, height));
  }

  /**
   * Calculates the width of the specified page.
   * 
   * @param imageIndex - The image index. In this case it is the page number.
   * 
   * @return The width of the specified page.
   * 
   * @throws IOException if an error occurs reading the width information from the input source.
   */
  @Override
  public int getWidth(int imageIndex) throws IOException {
    return getDocument().getPage(imageIndex + 1).getWidth();
  }

  /**
   * Calculates the height of the specified page.
   * 
   * @param imageIndex - The image index. In this case it is the page number.
   * 
   * @return The height of the specified page or {@code 0} if an error occurred.
   * 
   * @throws IOException if an error occurs reading the height information from the input source.
   */
  @Override
  public int getHeight(int imageIndex) throws IOException {
    try {
      return getDocument().getPage(imageIndex + 1).getHeight();
    } catch (JBIG2Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Simply returns the {@link JBIG2ImageMetadata}.
   * 
   * @return The associated {@link JBIG2ImageMetadata}.
   * 
   * @throws IOException if an error occurs reading the height information from the input source.
   */
  @Override
  public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    return new JBIG2ImageMetadata(getDocument().getPage(imageIndex + 1));
  }


  /**
   * Returns the iterator for available image types.
   * 
   * @param imageIndex - The page number.
   * 
   * @return An {@link Iterator} for available image types.
   * 
   * @throws IOException if an error occurs reading the height information from the input source.
   */
  @Override
  public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
    List<ImageTypeSpecifier> l = new ArrayList<ImageTypeSpecifier>();

    l.add(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_INDEXED));

    return l.iterator();
  }

  /**
   * @see ImageReader#getNumImages(boolean)
   */
  @Override
  public int getNumImages(boolean allowSearch) throws IOException {
    if (allowSearch) {
      if (getDocument().isAmountOfPagesUnknown()) {
        log.info("Amount of pages is unknown.");
      } else {
        return getDocument().getAmountOfPages();
      }
    } else {
      log.info("Search is not allowed.");
    }
    return -1;
  }

  /**
   * This ImageIO plugin doesn't record {@link IIOMetadata}.
   * 
   * @return {@code null} at every call.
   */
  @Override
  public IIOMetadata getStreamMetadata() {
    log.info("No metadata recorded");
    return null;
  }

  /**
   * Returns decoded segments that has been set as globals. Globals are jbig2 segments that are used
   * in embedded case for file wide access. They are not assigned to a specific page.
   * 
   * @return Decoded global segments.
   * 
   * @throws IOException if an error occurs reading the height information from the input source.
   */
  public JBIG2Globals getGlobals() throws IOException {
    return getDocument().getGlobalSegments();
  }

  /**
   * Returns the decoded image of specified page considering the given {@link JBIG2ReadParam}s.
   * 
   * @see ImageReader#read(int, ImageReadParam)
   */
  @Override
  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    if (param == null) {
      log.info("JBIG2ReadParam not specified. Default will be used.");
      param = (JBIG2ReadParam) getDefaultReadParam(imageIndex);
    }

    JBIG2Page page = getPage(imageIndex);

    Bitmap pageBitmap = (Bitmap) CacheFactory.getCache().get(page);

    if (pageBitmap == null) {
      try {
        pageBitmap = page.getBitmap();
        CacheFactory.getCache().put(page, pageBitmap, pageBitmap.getMemorySize());
        page.clearPageData();
      } catch (JBIG2Exception e) {
        throw new IOException(e.getMessage());
      }
    }

    return Bitmaps.asBufferedImage(pageBitmap, param, FilterType.Gaussian);
  }

  public boolean canReadRaster() {
    return true;
  }

  @Override
  public Raster readRaster(int imageIndex, ImageReadParam param) throws IOException {
    if (param == null) {
      log.info("JBIG2ReadParam not specified. Default will be used.");
      param = (JBIG2ReadParam) getDefaultReadParam(imageIndex);
    }

    JBIG2Page page = getPage(imageIndex);

    Bitmap pageBitmap = (Bitmap) CacheFactory.getCache().get(page);
    if (pageBitmap == null) {
      try {
        pageBitmap = page.getBitmap();
        CacheFactory.getCache().put(page, pageBitmap, pageBitmap.getMemorySize());
        page.clearPageData();
      } catch (JBIG2Exception e) {
        throw new IOException(e.getMessage());
      }
    }

    return Bitmaps.asRaster(pageBitmap, param, FilterType.Gaussian);
  }

  /**
   * Decodes and returns the global segments.
   * 
   * @param globalsInputStream - The input stream of globals data.
   * 
   * @return The decoded {@link JBIG2Globals}.
   * 
   * @throws IOException if an error occurs reading the height information from the input source.
   */
  public JBIG2Globals processGlobals(ImageInputStream globalsInputStream) throws IOException {
    JBIG2Document doc = new JBIG2Document(globalsInputStream);
    return doc.getGlobalSegments();
  }

  /**
   * Simply sets the globals.
   * 
   * @param globals - The globals to set.
   * @throws IOException
   */
  public void setGlobals(JBIG2Globals globals) throws IOException {
    this.globals = globals;
    this.document = null;
  }

  /**
   * @see ImageReader#setInput(Object, boolean, boolean)
   */
  @Override
  public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
    super.setInput(input, seekForwardOnly, ignoreMetadata);
    document = null;
  }

  private JBIG2Document getDocument() throws IOException {
    if (this.document == null) {
      if (this.input == null) {
        throw new IOException("Input not set.");
      }

      if (this.globals == null) {
        log.debug("Globals not set.");
      }

      this.document = new JBIG2Document((ImageInputStream) this.input, this.globals);
    }
    return this.document;
  }

  private JBIG2Page getPage(int imageIndex) throws IOException {
    JBIG2Page page = getDocument().getPage(imageIndex + 1);

    if (page == null)
      throw new IndexOutOfBoundsException("Requested page at index=" + imageIndex + " does not exist.");

    return page;
  }
}
