/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
