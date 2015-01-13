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

package com.levigo.jbig2.image;

import static org.junit.Assert.assertArrayEquals;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.JBIG2DocumentFacade;
import com.levigo.jbig2.JBIG2ImageReaderDemo;
import com.levigo.jbig2.PreconfiguredImageReadParam;
import com.levigo.jbig2.err.JBIG2Exception;
import com.levigo.jbig2.io.DefaultInputStreamFactory;
import com.levigo.jbig2.io.InputStreamFactory;


@RunWith(Parameterized.class)
public class BitmapsChecksumTest {

  private String resourcePath;
  private ImageReadParam param;
  private FilterType filterType;
  private String checksum;
  private int pageNumber;

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(new Dimension(500, 500)), FilterType.Bessel,
            "101-6467-126-3534108-8927-58-26-37248672"
        },
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(new Dimension(500, 800)), FilterType.Box,
            "-748135-126-6412111-11925-1038826-95-32-6-104"
        },
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(new Dimension(4000, 5500)), FilterType.Box,
            "-646510160-466410970-77-1031184396-8-23-18"
        },
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(new Dimension(600, 300)), FilterType.Bessel,
            "-69-11478-721003586-100-72-85-1559101-118-24-94"
        },
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(2, 2, 0, 0), FilterType.Bessel,
            "-4979-94-68-125645751-2111712617-59-295"
        },
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(2, 2, 0, 0), FilterType.Lanczos,
            "-4979-94-68-125645751-2111712617-59-295"
        },
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(3, 3, 1, 1), FilterType.Lanczos,
            "84-1069410599-9575-7934-1279-80-85127-18-128"
        },
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(new Rectangle(100, 100, 500, 500)),
            FilterType.Lanczos, "1245-23-127954634-1232173-109-5739-303-48"
        },
        {
            "/images/042_1.jb2", 1, new PreconfiguredImageReadParam(new Rectangle(500, 500, 2000, 2000)),
            FilterType.Lanczos, "-60-45-117-90-6596-11556-47-30-112-741138412082"
        },
        {
            "/images/042_1.jb2", 1,
            new PreconfiguredImageReadParam(new Rectangle(500, 500, 2000, 2000), new Dimension(678, 931)),
            FilterType.Lanczos, "-17-95-5543-12062-625054-94-88-31-4-120-1971"
        },
        {
            "/images/042_1.jb2", 1,
            new PreconfiguredImageReadParam(new Rectangle(500, 500, 2000, 2000), new Dimension(678, 931), 3, 3, 1, 1),
            FilterType.Lanczos, "-109-60118-41999255-94113-5019-2818-10-39-71"
        }
    });
  }

  public BitmapsChecksumTest(String resourcePath, int pageNumber, ImageReadParam param, FilterType filterType,
      String checksum) {
    this.resourcePath = resourcePath;
    this.pageNumber = pageNumber;
    this.param = param;
    this.filterType = filterType;
    this.checksum = checksum;
  }

  @Test
  public void test() throws IOException, JBIG2Exception, NoSuchAlgorithmException {
    final URL imageUrl = JBIG2ImageReaderDemo.class.getResource(resourcePath);

    final InputStream inputStream = new FileInputStream(new File(imageUrl.getPath()));
    final InputStreamFactory disf = new DefaultInputStreamFactory();
    final ImageInputStream iis = disf.getInputStream(inputStream);

    final JBIG2DocumentFacade doc = new JBIG2DocumentFacade(iis);
    final Bitmap b = doc.getPageBitmap(pageNumber);
    final WritableRaster raster = Bitmaps.asRaster(b, param, filterType);

    final DataBufferByte dataBufferByte = (DataBufferByte) raster.getDataBuffer();
    final byte[] bytes = dataBufferByte.getData();

    final MessageDigest md = MessageDigest.getInstance("MD5");

    final byte[] digest = md.digest(bytes);
    final StringBuilder sb = new StringBuilder();
    for (byte toAppend : digest) {
      sb.append(toAppend);
    }

    assertArrayEquals(checksum.getBytes(), sb.toString().getBytes());
  }

  static class RasterChecksumCalculator {
    public static void main(String[] args) throws IOException, JBIG2Exception, NoSuchAlgorithmException {
      final String resourcePath = "/images/042_1.jb2";

      final int pageNumber = 1;

      final URL imageUrl = JBIG2ImageReaderDemo.class.getResource(resourcePath);

      final InputStream inputStream = new FileInputStream(new File(imageUrl.getPath()));
      final InputStreamFactory disf = new DefaultInputStreamFactory();
      final ImageInputStream iis = disf.getInputStream(inputStream);

      final JBIG2DocumentFacade doc = new JBIG2DocumentFacade(iis);
      final Bitmap b = doc.getPageBitmap(pageNumber);

      final ImageReadParam param = new PreconfiguredImageReadParam(new Rectangle(100, 100, 500, 500));

      final WritableRaster raster = Bitmaps.asRaster(b, param, FilterType.Lanczos);
      final DataBufferByte dataBufferByte = (DataBufferByte) raster.getDataBuffer();
      final byte[] bytes = dataBufferByte.getData();

      final MessageDigest md = MessageDigest.getInstance("MD5");

      final byte[] digest = md.digest(bytes);
      for (byte d : digest) {
        System.out.print(d);
      }
    }
  }
}
