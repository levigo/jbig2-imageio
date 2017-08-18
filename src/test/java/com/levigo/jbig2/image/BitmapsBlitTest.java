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

package com.levigo.jbig2.image;

import static org.junit.Assert.assertArrayEquals;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import org.junit.Test;

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.JBIG2DocumentFacade;
import com.levigo.jbig2.err.JBIG2Exception;
import com.levigo.jbig2.io.DefaultInputStreamFactory;
import com.levigo.jbig2.util.CombinationOperator;

public class BitmapsBlitTest {

  @Test
  public void testCompleteBitmapTransfer() throws IOException, JBIG2Exception {
    final InputStream inputStream = getClass().getResourceAsStream("/images/042_1.jb2");
    final DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    final ImageInputStream iis = disf.getInputStream(inputStream);

    final JBIG2DocumentFacade doc = new JBIG2DocumentFacade(iis);

    final Bitmap src = doc.getPageBitmap(1);
    final Bitmap dst = new Bitmap(src.getWidth(), src.getHeight());
    Bitmaps.blit(src, dst, 0, 0, CombinationOperator.REPLACE);

    final byte[] srcData = src.getByteArray();
    final byte[] dstData = dst.getByteArray();

    assertArrayEquals(srcData, dstData);
  }

  @Test
  public void test() throws IOException, JBIG2Exception {
    final InputStream inputStream = getClass().getResourceAsStream("/images/042_1.jb2");
    final DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    final ImageInputStream iis = disf.getInputStream(inputStream);

    final JBIG2DocumentFacade doc = new JBIG2DocumentFacade(iis);

    final Bitmap dst = doc.getPageBitmap(1);

    final Rectangle roi = new Rectangle(100, 100, 100, 100);
    final Bitmap src = new Bitmap(roi.width, roi.height);
    Bitmaps.blit(src, dst, roi.x, roi.y, CombinationOperator.REPLACE);

    final Bitmap dstRegionBitmap = Bitmaps.extract(roi, dst);

    final byte[] srcData = src.getByteArray();
    final byte[] dstRegionData = dstRegionBitmap.getByteArray();

    assertArrayEquals(srcData, dstRegionData);
  }

}
