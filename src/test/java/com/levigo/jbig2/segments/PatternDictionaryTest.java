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
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import com.levigo.jbig2.*;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.*;
import com.levigo.jbig2.segments.*;

public class PatternDictionaryTest {
  @Test
  public void parseHeaderTest() throws IOException, InvalidHeaderValueException {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Sixth Segment (number 5)
    SubInputStream sis = new SubInputStream(iis, 245, 45);
    PatternDictionary pd = new PatternDictionary();
    pd.init(null, sis);
    Assert.assertEquals(true, pd.isMMREncoded());
    Assert.assertEquals(0, pd.getHdTemplate());
    Assert.assertEquals(4, pd.getHdpWidth());
    Assert.assertEquals(4, pd.getHdpHeight());
    Assert.assertEquals(15, pd.getGrayMax());
  }

  // TESTS WITH TESTOUTPUT
  // Ignore in build process

  @Ignore
  @Test
  public void decodeTestWithOutput() throws Throwable {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Sixth Segment (number 5)
    SubInputStream sis = new SubInputStream(iis, 245, 45);

    PatternDictionary pd = new PatternDictionary();
    pd.init(null, sis);

    ArrayList<Bitmap> b = pd.getDictionary();

    int i = 5;
    // for (int i = 0; i < 8; i++) {
    new TestImage(b.get(i).getByteArray(), (int) b.get(i).getWidth(), (int) b.get(i).getHeight(),
        b.get(i).getRowStride());
    // }
  }

  @Ignore
  @Test
  public void decodeTestWithOutput2() throws Throwable {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Twelfth Segment (number 12)
    SubInputStream sis = new SubInputStream(iis, 569, 28);

    PatternDictionary pd = new PatternDictionary();
    pd.init(null, sis);

    ArrayList<Bitmap> b = pd.getDictionary();

    int i = 2;
    // for (int i = 0; i < 8; i++) {
    new TestImage(b.get(i).getByteArray(), (int) b.get(i).getWidth(), (int) b.get(i).getHeight(),
        b.get(i).getRowStride());
    // }
  }
}
