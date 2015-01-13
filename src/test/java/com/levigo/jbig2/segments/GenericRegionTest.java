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

import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.levigo.jbig2.TestImage;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.image.Bitmaps;
import com.levigo.jbig2.io.DefaultInputStreamFactory;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.CombinationOperator;

public class GenericRegionTest {

  @Test
  public void parseHeaderTest() throws IOException, InvalidHeaderValueException {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);

    // Twelfth Segment (number 11)
    SubInputStream sis = new SubInputStream(iis, 523, 35);
    GenericRegion gr = new GenericRegion();
    gr.init(null, sis);

    Assert.assertEquals(54, gr.getRegionInfo().getBitmapWidth());
    Assert.assertEquals(44, gr.getRegionInfo().getBitmapHeight());
    Assert.assertEquals(4, gr.getRegionInfo().getXLocation());
    Assert.assertEquals(11, gr.getRegionInfo().getYLocation());
    Assert.assertEquals(CombinationOperator.OR, gr.getRegionInfo().getCombinationOperator());

    Assert.assertFalse(gr.useExtTemplates());
    Assert.assertFalse(gr.isMMREncoded());
    Assert.assertEquals(0, gr.getGbTemplate());
    Assert.assertTrue(gr.isTPGDon());

    short[] gbAtX = gr.getGbAtX();
    short[] gbAtY = gr.getGbAtY();
    Assert.assertEquals(3, gbAtX[0]);
    Assert.assertEquals(-1, gbAtY[0]);
    Assert.assertEquals(-3, gbAtX[1]);
    Assert.assertEquals(-1, gbAtY[1]);
    Assert.assertEquals(2, gbAtX[2]);
    Assert.assertEquals(-2, gbAtY[2]);
    Assert.assertEquals(-2, gbAtX[3]);
    Assert.assertEquals(-2, gbAtY[3]);
  }

  // TESTS WITH TESTOUTPUT
  // Ignore in build process

  @Ignore
  @Test
  public void decodeTemplate0Test() throws Throwable {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Twelfth Segment (number 11)
    SubInputStream sis = new SubInputStream(iis, 523, 35);
    GenericRegion gr = new GenericRegion();

    gr.init(null, sis);
    new TestImage(Bitmaps.asBufferedImage(gr.getRegionBitmap()));
  }

  @Ignore
  @Test
  public void decodeWithArithmetichCoding() throws Throwable {

    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Twelfth Segment (number 11)
    SubInputStream sis = new SubInputStream(iis, 523, 35);
    GenericRegion gr = new GenericRegion(sis);

    gr.init(null, sis);
    new TestImage(Bitmaps.asBufferedImage(gr.getRegionBitmap()));
  }

  @Ignore
  @Test
  public void decodeWithMMR() throws Throwable {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Fifth Segment (number 4)
    SubInputStream sis = new SubInputStream(iis, 190, 59);
    GenericRegion gr = new GenericRegion(sis);
    gr.init(null, sis);
    new TestImage(Bitmaps.asBufferedImage(gr.getRegionBitmap()));
  }
}
