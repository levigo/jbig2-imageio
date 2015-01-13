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

import java.awt.image.WritableRaster;

import com.levigo.jbig2.Bitmap;

final class BitmapScanline extends Scanline {

  private Bitmap bitmap;
  private WritableRaster raster;

  private int[] lineBuffer;

  public BitmapScanline(final Bitmap src, final WritableRaster dst, final int width) {
    super(width);
    this.bitmap = src;
    this.raster = dst;
    lineBuffer = new int[length];
  }

  @Override
  protected void clear() {
    lineBuffer = new int[length];
  }

  @Override
  protected void fetch(int x, final int y) {
    lineBuffer = new int[length]; // really required?
    int srcByteIdx = bitmap.getByteIndex(x, y);
    while (x < length) {
      final byte srcByte = (byte) ~bitmap.getByte(srcByteIdx++);
      final int bits = bitmap.getWidth() - x > 8 ? 8 : bitmap.getWidth() - x;
      for (int bitPosition = bits - 1; bitPosition >= 0; bitPosition--, x++) {
        if (((srcByte >> bitPosition) & 0x1) != 0)
          lineBuffer[x] = 255;
      }
    }
  }

  @Override
  protected void filter(final int[] preShift, final int[] postShift, final Weighttab[] tabs, final Scanline dst) {
    final BitmapScanline dstBitmapScanline = (BitmapScanline) dst;
    final int dstLength = dst.length;

    // start sum at 1 << shift - 1 for rounding
    final int start = 1 << postShift[0] - 1;
    final int srcBuffer[] = lineBuffer;
    final int dstBuffer[] = dstBitmapScanline.lineBuffer;

    // the next two blocks are duplicated except for the missing shift operation if preShift == 0.
    final int preShift0 = preShift[0];
    final int postShift0 = postShift[0];
    if (preShift0 != 0) {
      for (int dstIndex = 0, tab = 0; tab < dstLength; tab++) {
        final Weighttab weightTab = tabs[tab];
        final int weights = weightTab.weights.length;

        int sum = start;
        for (int weightIndex = 0, srcIndex = weightTab.i0; weightIndex < weights && srcIndex < srcBuffer.length; weightIndex++) {
          sum += weightTab.weights[weightIndex] * (srcBuffer[srcIndex++] >> preShift0);
        }

        final int t = sum >> postShift0;
        dstBuffer[dstIndex++] = t < 0 ? 0 : t > 255 ? 255 : t;
      }
    } else {
      for (int dstIndex = 0, tab = 0; tab < dstLength; tab++) {
        final Weighttab weightTab = tabs[tab];
        final int weights = weightTab.weights.length;

        int sum = start;
        for (int weightIndex = 0, srcIndex = weightTab.i0; weightIndex < weights && srcIndex < srcBuffer.length; weightIndex++) {
          sum += weightTab.weights[weightIndex] * srcBuffer[srcIndex++];
        }

        dstBuffer[dstIndex++] = sum >> postShift0;
      }
    }
  }

  @Override
  protected void accumulate(final int weight, final Scanline dst) {
    final BitmapScanline dstBitmapScanline = (BitmapScanline) dst;

    final int srcBuffer[] = lineBuffer;
    final int dstBuffer[] = dstBitmapScanline.lineBuffer;

    for (int b = 0; b < dstBuffer.length; b++)
      dstBuffer[b] += weight * srcBuffer[b];
  }

  @Override
  protected void shift(final int[] shift) {
    final int shift0 = shift[0];
    final int half = 1 << shift0 - 1;

    final int srcBuffer[] = lineBuffer;

    for (int b = 0; b < srcBuffer.length; b++) {
      final int pixel = srcBuffer[b] + half >> shift0;
      srcBuffer[b] = pixel < 0 ? 0 : pixel > 255 ? 255 : pixel;
    }
  }

  @Override
  protected void store(final int x, final int y) {
    raster.setSamples(x, y, length, 1, 0, lineBuffer);
  }

}