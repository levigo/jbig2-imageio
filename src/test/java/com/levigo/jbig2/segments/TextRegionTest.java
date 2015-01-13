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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.levigo.jbig2.JBIG2ImageReader;
import com.levigo.jbig2.JBIG2ImageReaderSpi;
import com.levigo.jbig2.TestImage;
import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.DefaultInputStreamFactory;

public class TextRegionTest {

  // TESTS WITH TESTOUTPUT
  // Ignore for in build process

  @Ignore
  @Test
  public void textRegionWith() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    String filepath = "/images/042_11.jb2";
    int pageNumber = 1;

    InputStream is = getClass().getResourceAsStream(filepath);
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    JBIG2ImageReader jb2 = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    jb2.setInput(iis);
    BufferedImage b = jb2.read(pageNumber);
    new TestImage(b);
  }
}
