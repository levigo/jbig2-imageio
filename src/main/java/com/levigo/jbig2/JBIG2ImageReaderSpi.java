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
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * Necessary extension for ImageIO standard.
 * 
 * @see ImageReaderSpi
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
public class JBIG2ImageReaderSpi extends ImageReaderSpi {

  private static final String VENDOR = "levigo solutions gmbh";
  private static final String VERSION = "1.4.1";
  private static final String READER_CLASS_NAME = "com.levigo.jbig2.JBIG2ImageReader";
  private static final String[] NAMES = {
      "jbig2", "JBIG2"
  };
  private static final String[] SUFFIXES = {
      "jb2", "jbig2", "JB2", "JBIG2"
  };
  private static final String[] MIME_TYPES = {
      "image/x-jbig2", "image/x-jb2"
  };
  private static final Class<?>[] INPUT_TYPES = STANDARD_INPUT_TYPE;

  /**
   * According to D.4.1:
   * <p>
   * This preamble contains the unique id string for jbig2 files and can be used to check if this
   * reader plugin can decode the given stream. This can only work with native jbig2 data. If the
   * stream is embedded in another file format this preamble might be missing.
   */
  private static final int[] FILEHEADER_PREAMBLE = {
      0x97, 0x4A, 0x42, 0x32, 0x0D, 0x0A, 0x1A, 0x0A
  };

  /* MK: I suppose, there won't be a writer in near future :-) */
  private static final String[] WRITER_SPI_NAMES = {};

  static final boolean SUPPORTS_STANDARD_STREAM_METADATE_FORMAT = false;
  static final String NATIVE_STREAM_METADATA_FORMAT_NAME = "JBIG2 Stream Metadata";
  static final String NATIVE_STREAM_METADATA_FORMAT_CLASSNAME = "JBIG2Metadata";
  static final String[] EXTRA_STREAM_METADATA_FORMAT_NAME = null;
  static final String[] EXTRA_STREAM_METADATA_FORMAT_CLASSNAME = null;

  static final boolean SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT = false;
  static final String NATIVE_IMAGE_METADATA_FORMAT_NAME = "JBIG2 File Metadata";
  static final String NATIVE_IMAGE_METADATA_FORMAT_CLASSNAME = "JBIG2Metadata";
  static final String[] EXTRA_IMAGE_METADATA_FORMAT_NAME = null;
  static final String[] EXTRA_IMAGE_METADATA_FORMAT_CLASSNAME = null;

  public JBIG2ImageReaderSpi() {
    super(VENDOR, VERSION, NAMES, SUFFIXES, MIME_TYPES, READER_CLASS_NAME, INPUT_TYPES, WRITER_SPI_NAMES,
        SUPPORTS_STANDARD_STREAM_METADATE_FORMAT, NATIVE_STREAM_METADATA_FORMAT_NAME,
        NATIVE_STREAM_METADATA_FORMAT_CLASSNAME, EXTRA_STREAM_METADATA_FORMAT_NAME,
        EXTRA_STREAM_METADATA_FORMAT_CLASSNAME, SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT,
        NATIVE_IMAGE_METADATA_FORMAT_NAME, NATIVE_IMAGE_METADATA_FORMAT_CLASSNAME, EXTRA_IMAGE_METADATA_FORMAT_NAME,
        EXTRA_IMAGE_METADATA_FORMAT_CLASSNAME);
  }

  /*
   * Checks, if the file header begins with the preamble id string defined in D.4.1, page 100
   * 
   * (non-Javadoc)
   * 
   * @see javax.imageio.spi.ImageReaderSpi#canDecodeInput(java.lang.Object)
   */
  @Override
  public boolean canDecodeInput(Object source) throws IOException {
    if (source == null)
      throw new IllegalArgumentException("source must not be null");

    if (!(source instanceof ImageInputStream)) {
      System.out.println("source is not an ImageInputStream");
      return false;
    }

    ImageInputStream iis = (ImageInputStream) source;
    iis.mark();

    for (int i = 0; i < FILEHEADER_PREAMBLE.length; i++) {
      int read = (iis.read() & 0xFF);
      if (read != FILEHEADER_PREAMBLE[i]) {
        return false;
      }
    }

    iis.reset();
    return true;
  }

  @Override
  public ImageReader createReaderInstance(Object extension) throws IOException {
    return new JBIG2ImageReader(this);
  }

  @Override
  public String getDescription(Locale locale) {
    return "JBIG2 Image Reader";
  }

}
