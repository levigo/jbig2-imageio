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
