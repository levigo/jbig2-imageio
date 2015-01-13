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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.DefaultInputStreamFactory;

public class JBIG2ImageReaderTest {

  @Test
  public void testGetDefaultReadParams() throws Exception {
    ImageReader reader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    ImageReadParam param = reader.getDefaultReadParam();
    Assert.assertNotNull(param);

    Assert.assertNull(param.getSourceRegion());
    Assert.assertNull(param.getSourceRenderSize());

    Assert.assertEquals(1, param.getSourceXSubsampling());
    Assert.assertEquals(1, param.getSourceYSubsampling());
    Assert.assertEquals(0, param.getSubsamplingXOffset());
    Assert.assertEquals(0, param.getSubsamplingYOffset());
  }

  @Test
  public void testRead() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    String filepath = "/images/042_1.jb2";
    int imageIndex = 0;

    InputStream inputStream = getClass().getResourceAsStream(filepath);
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream imageInputStream = disf.getInputStream(inputStream);

    JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    imageReader.setInput(imageInputStream);

    // long timeStamp = System.currentTimeMillis();
    BufferedImage bufferedImage = imageReader.read(imageIndex, imageReader.getDefaultReadParam());
    // long duration = System.currentTimeMillis() - timeStamp;
    // System.out.println(filepath + " decoding took " + duration + " ms");

    Assert.assertNotNull(bufferedImage);
  }

  @Test
  public void testReadRaster() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    String filepath = "/images/042_1.jb2";
    int imageIndex = 0;

    InputStream inputStream = getClass().getResourceAsStream(filepath);
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream imageInputStream = disf.getInputStream(inputStream);

    JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    imageReader.setInput(imageInputStream);
    Raster raster = imageReader.readRaster(imageIndex, imageReader.getDefaultReadParam());

    Assert.assertNotNull(raster);
  }

  @Test
  public void testReadImageReadParamNull() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    String filepath = "/images/042_1.jb2";
    int imageIndex = 0;

    InputStream inputStream = getClass().getResourceAsStream(filepath);
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream imageInputStream = disf.getInputStream(inputStream);
    JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    imageReader.setInput(imageInputStream);
    BufferedImage bufferedImage = imageReader.read(imageIndex, null);

    Assert.assertNotNull(bufferedImage);
  }

  @Test
  public void testReadRasterImageReadParamNull() throws IOException, InvalidHeaderValueException,
      IntegerMaxValueException {
    String filepath = "/images/042_1.jb2";
    int imageIndex = 0;

    InputStream inputStream = getClass().getResourceAsStream(filepath);
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream imageInputStream = disf.getInputStream(inputStream);
    JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    imageReader.setInput(imageInputStream);
    Raster raster = imageReader.readRaster(imageIndex, null);

    Assert.assertNotNull(raster);
  }

  @Test
  public void testGetNumImages() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    String filepath = "/images/002.jb2";

    InputStream inputStream = getClass().getResourceAsStream(filepath);
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream imageInputStream = disf.getInputStream(inputStream);
    JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    imageReader.setInput(imageInputStream);
    int numImages = imageReader.getNumImages(true);
    Assert.assertEquals(17, numImages);
  }

  @Test
  public void testCanReadRaster() throws IOException {
    JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    Assert.assertTrue(imageReader.canReadRaster());
  }

}
