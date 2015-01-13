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

import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;


abstract class Scanline {
  public interface ScanlineFilter {
    public void filter(int x, int y, int componentIndex, Object data, int length);
  }


  /**
   * A Scanline for pixel interleaved byte data with three components. Although its name contains
   * "BGR" it doesn't really matter how the components are organized, als long as there are three of
   * them.
   */
  protected static final class ByteBGRScanline extends Scanline {
    private final Raster srcRaster;
    private final WritableRaster dstRaster;

    private final int data[];

    protected ByteBGRScanline(Raster src, WritableRaster dst, final int length) {
      super(length);
      srcRaster = src;
      dstRaster = dst;

      data = new int[3 * length];
    }

    @Override
    protected void accumulate(final int weight, final Scanline dst) {
      final ByteBGRScanline bcs = (ByteBGRScanline) dst;

      final int abuf[] = data;
      final int bbuf[] = bcs.data;

      for (int b = 0; b < bbuf.length; b++)
        bbuf[b] += weight * abuf[b];
    }

    @Override
    protected void clear() {
      final int[] b = data;
      for (int j = 0; j < b.length; j++)
        b[j] = 0;
    }

    @Override
    protected void fetch(final int x, final int y) {
      srcRaster.getPixels(x, y, length, 1, data);
    }

    @Override
    protected void filter(final int[] preShift, final int[] postShift, final Weighttab[] tabs, final Scanline dst) {
      final ByteBGRScanline bcs = (ByteBGRScanline) dst;
      final int nx = dst.length;

      // start sum at 1<<shift-1 for rounding
      final int start[] = new int[]{
          1 << postShift[0] - 1, 1 << postShift[1] - 1, 1 << postShift[2] - 1
      };
      final int abuf[] = data;
      final int bbuf[] = bcs.data;

      // the next two blocks are duplicated except for the missing shift
      // operation if preShift==0.
      if (preShift[0] != 0 || preShift[1] != 0 || preShift[2] != 0)
        for (int bp = 0, b = 0; b < nx; b++) {
          final Weighttab wtab = tabs[b];
          final int an = wtab.weights.length;

          int sumr = start[0];
          int sumg = start[1];
          int sumb = start[2];
          for (int wp = 0, ap = wtab.i0 * 3; wp < an && ap < abuf.length; wp++) {
            final int w = wtab.weights[wp];
            sumr += w * (abuf[ap++] >> preShift[0]);
            sumg += w * (abuf[ap++] >> preShift[1]);
            sumb += w * (abuf[ap++] >> preShift[2]);
          }

          int t = sumr >> postShift[0];
          bbuf[bp++] = t < 0 ? 0 : t > 255 ? 255 : t;
          t = sumg >> postShift[1];
          bbuf[bp++] = t < 0 ? 0 : t > 255 ? 255 : t;
          t = sumb >> postShift[2];
          bbuf[bp++] = t < 0 ? 0 : t > 255 ? 255 : t;
        }
      else
        for (int bp = 0, b = 0; b < nx; b++) {
          final Weighttab wtab = tabs[b];
          final int an = wtab.weights.length;

          int sumr = start[0];
          int sumg = start[1];
          int sumb = start[2];
          for (int wp = 0, ap = wtab.i0 * 3; wp < an && ap < abuf.length; wp++) {
            final int w = wtab.weights[wp];
            sumr += w * abuf[ap++];
            sumg += w * abuf[ap++];
            sumb += w * abuf[ap++];
          }

          bbuf[bp++] = sumr >> postShift[0];
          bbuf[bp++] = sumg >> postShift[1];
          bbuf[bp++] = sumb >> postShift[2];
        }
    }

    @Override
    protected void shift(final int[] shift) {
      final int half[] = new int[]{
          1 << shift[0] - 1, 1 << shift[1] - 1, 1 << shift[2] - 1
      };

      final int abuf[] = data;

      for (int b = 0; b < abuf.length;) {
        for (int c = 0; c < 3; c++, b++) {
          final int t = abuf[b] + half[c] >> shift[c];
          abuf[b] = t < 0 ? 0 : t > 255 ? 255 : t;
        }
      }
    }

    @Override
    protected void store(final int x, final int y) {
      dstRaster.setPixels(x, y, length, 1, data);
    }
  }

  /**
   * A Scanline for packed integer pixels.
   */
  protected static final class IntegerSinglePixelPackedScanline extends Scanline {
    private final Raster srcRaster;
    private final WritableRaster dstRaster;

    private final int data[];
    private final int[] bitMasks;
    private final int[] bitOffsets;
    private final int componentCount;
    private final SinglePixelPackedSampleModel srcSM;
    private final int tmp[];

    protected IntegerSinglePixelPackedScanline(Raster src, WritableRaster dst, final int length) {
      super(length);
      srcRaster = src;
      dstRaster = dst;

      srcSM = (SinglePixelPackedSampleModel) srcRaster.getSampleModel();

      bitMasks = srcSM.getBitMasks();
      bitOffsets = srcSM.getBitOffsets();
      componentCount = bitMasks.length;

      if (componentCount != bitOffsets.length || bitOffsets.length != srcSM.getNumBands())
        throw new IllegalArgumentException("weird: getBitMasks().length != getBitOffsets().length");

      tmp = new int[componentCount];

      data = new int[componentCount * length];
    }

    @Override
    protected void accumulate(final int weight, final Scanline dst) {
      final IntegerSinglePixelPackedScanline ispps = (IntegerSinglePixelPackedScanline) dst;

      final int abuf[] = data;
      final int bbuf[] = ispps.data;

      for (int b = 0; b < bbuf.length; b++)
        bbuf[b] += weight * abuf[b];
    }

    @Override
    protected void clear() {
      final int[] b = data;
      for (int j = 0; j < b.length; j++)
        b[j] = 0;
    }

    @Override
    protected void fetch(final int x, final int y) {
      srcRaster.getPixels(x, y, length, 1, data);
    }

    @Override
    protected void filter(final int[] preShift, final int[] postShift, final Weighttab[] tabs, final Scanline dst) {
      final IntegerSinglePixelPackedScanline ispps = (IntegerSinglePixelPackedScanline) dst;
      final int nx = dst.length;

      // start sum at 1<<shift-1 for rounding
      final int start[] = tmp;
      for (int c = 0; c < componentCount; c++)
        start[c] = 1 << postShift[c] - 1;

      final int abuf[] = data;
      final int bbuf[] = ispps.data;

      // the next two blocks are duplicated except for the missing shift
      // operation if preShift==0.
      boolean hasPreShift = false;
      for (int c = 0; c < componentCount && !hasPreShift; c++)
        hasPreShift |= preShift[c] != 0;
      if (hasPreShift)
        for (int bp = 0, b = 0; b < nx; b++) {
          final Weighttab wtab = tabs[b];
          final int an = wtab.weights.length;

          for (int c = 0; c < componentCount; c++) {
            int sum = start[c];
            for (int wp = 0, ap = wtab.i0 * componentCount + c; wp < an && ap < abuf.length; wp++, ap += componentCount)
              sum += wtab.weights[wp] * (abuf[ap] >> preShift[c]);

            final int t = sum >> postShift[c];
            bbuf[bp++] = t < 0 ? 0 : t > 255 ? 255 : t;
          }
        }
      else
        for (int bp = 0, b = 0; b < nx; b++) {
          final Weighttab wtab = tabs[b];
          final int an = wtab.weights.length;

          for (int c = 0; c < componentCount; c++) {
            int sum = start[c];
            for (int wp = 0, ap = wtab.i0 * componentCount + c; wp < an && ap < abuf.length; wp++, ap += componentCount)
              sum += wtab.weights[wp] * abuf[ap];

            bbuf[bp++] = sum >> postShift[c];
          }
        }
    }

    @Override
    protected void shift(final int[] shift) {
      final int half[] = tmp;
      for (int c = 0; c < componentCount; c++)
        half[c] = 1 << shift[c] - 1;

      final int abuf[] = data;

      for (int b = 0; b < abuf.length;) {
        for (int c = 0; c < componentCount; c++, b++) {
          final int t = abuf[b] + half[c] >> shift[c];
          abuf[b] = t < 0 ? 0 : t > 255 ? 255 : t;
        }
      }
    }

    @Override
    protected void store(final int x, final int y) {
      dstRaster.setPixels(x, y, length, 1, data);
    }
  }

  /**
   * A Scanline for packed integer pixels.
   */
  protected static final class GenericRasterScanline extends Scanline {
    private final Raster srcRaster;
    private final WritableRaster dstRaster;

    private final int componentCount;
    private final int data[][];
    private final SampleModel srcSM;
    private final SampleModel dstSM;
    private final int channelMask[];
    private final int[] tmp;
    private final ScanlineFilter inputFilter;

    protected GenericRasterScanline(Raster src, WritableRaster dst, final int length, int bitsPerChannel[],
        ScanlineFilter inputFilter) {
      super(length);
      srcRaster = src;
      dstRaster = dst;
      this.inputFilter = inputFilter;

      srcSM = srcRaster.getSampleModel();
      dstSM = dstRaster.getSampleModel();

      componentCount = srcSM.getNumBands();

      if (componentCount != dstSM.getNumBands())
        throw new IllegalArgumentException("weird: src raster num bands != dst raster num bands");

      tmp = new int[componentCount];

      data = new int[componentCount][];
      for (int i = 0; i < data.length; i++)
        data[i] = new int[length];

      channelMask = new int[componentCount];
      for (int c = 0; c < componentCount; c++)
        channelMask[c] = (1 << bitsPerChannel[c]) - 1;
    }

    @Override
    protected void accumulate(final int weight, final Scanline dst) {
      final GenericRasterScanline grs = (GenericRasterScanline) dst;

      final int l = grs.data[0].length;
      for (int c = 0; c < componentCount; c++) {
        final int ac[] = data[c];
        final int bc[] = grs.data[c];

        for (int b = 0; b < l; b++)
          bc[b] += weight * ac[b];
      }
    }

    @Override
    protected void clear() {
      for (int c = 0; c < componentCount; c++) {
        final int[] b = data[c];
        for (int x = 0; x < b.length; x++)
          b[x] = 0;
      }
    }

    @Override
    protected void fetch(final int x, final int y) {
      for (int c = 0; c < componentCount; c++) {
        srcRaster.getSamples(x, y, length, 1, c, data[c]);
        if (null != inputFilter)
          inputFilter.filter(x, y, c, data[c], length);
      }
    }

    @Override
    protected void filter(final int[] preShift, final int[] postShift, final Weighttab[] tabs, final Scanline dst) {
      final GenericRasterScanline grs = (GenericRasterScanline) dst;
      final int nx = dst.length;

      // start sum at 1<<shift-1 for rounding
      final int start[] = tmp;
      for (int c = 0; c < componentCount; c++)
        start[c] = 1 << postShift[c] - 1;

      final int l = data[0].length;

      // the next two blocks are duplicated except for the missing shift
      // operation if preShift==0.
      boolean hasPreShift = false;
      for (int c = 0; c < componentCount && !hasPreShift; c++)
        hasPreShift |= preShift[c] != 0;
      if (hasPreShift)
        for (int c = 0; c < componentCount; c++) {
          final int ac[] = data[c];
          final int bc[] = grs.data[c];
          final int m = channelMask[c];
          for (int b = 0; b < nx; b++) {
            final Weighttab wtab = tabs[b];
            final int an = wtab.weights.length;

            int sum = start[c];
            for (int wp = 0, ap = wtab.i0; wp < an && ap < l; wp++, ap++)
              sum += wtab.weights[wp] * (ac[ap] >> preShift[c]);

            final int t = sum >> postShift[c];
            bc[b] = t < 0 ? 0 : t > m ? m : t;
          }
        }
      else
        for (int c = 0; c < componentCount; c++) {
          final int ac[] = data[c];
          final int bc[] = grs.data[c];

          for (int b = 0; b < nx; b++) {
            final Weighttab wtab = tabs[b];
            final int an = wtab.weights.length;

            int sum = start[c];
            for (int wp = 0, ap = wtab.i0; wp < an && ap < l; wp++, ap++)
              sum += wtab.weights[wp] * ac[ap];

            bc[b] = sum >> postShift[c];
          }
        }
    }

    @Override
    protected void shift(final int[] shift) {
      final int half[] = tmp;
      for (int c = 0; c < componentCount; c++)
        half[c] = 1 << shift[c] - 1;

      final int abuf[][] = data;

      final int l = abuf[0].length;
      for (int c = 0; c < componentCount; c++) {
        final int ac[] = data[c];
        final int m = channelMask[c];

        for (int a = 0; a < l; a++) {
          final int t = ac[a] + half[c] >> shift[c];
          ac[a] = t < 0 ? 0 : t > m ? m : t;
        }
      }
    }

    @Override
    protected void store(final int x, final int y) {
      final int nx = length;
      for (int c = 0; c < componentCount; c++)
        dstRaster.setSamples(x, y, nx, 1, c, data[c]);
    }
  }
  
  /**
   * A Scanline for BiLevel input data ({@link MultiPixelPackedSampleModel}) to indexed output data
   * (<code>sun.awt.image.ByteInterleavedRaster</code>).
   */
  protected static final class ByteBiLevelPackedScanline extends Scanline {
    private final Raster srcRaster;
    private final WritableRaster dstRaster;

    private final int data[];

    protected ByteBiLevelPackedScanline(Raster src, WritableRaster dst, final int length) {
      super(length);
      srcRaster = src;
      dstRaster = dst;

      data = new int[length];
    }

    @Override
    protected void accumulate(final int weight, final Scanline dst) {
      final ByteBiLevelPackedScanline bblps = (ByteBiLevelPackedScanline) dst;

      final int abuf[] = data;
      final int bbuf[] = bblps.data;

      for (int b = 0; b < bbuf.length; b++)
        bbuf[b] += weight * abuf[b];
    }

    @Override
    protected void clear() {
      final int[] b = data;
      for (int j = 0; j < b.length; j++)
        b[j] = 0;
    }

    @Override
    protected void fetch(final int x, final int y) {
      srcRaster.getPixels(x, y, length, 1, data);
      for (int i = 0; i < length; i++)
        if (data[i] != 0)
          data[i] = 255;
    }

    @Override
    protected void filter(final int[] preShift, final int[] postShift, final Weighttab[] tabs, final Scanline dst) {
      final ByteBiLevelPackedScanline bblps = (ByteBiLevelPackedScanline) dst;
      final int nx = dst.length;

      // start sum at 1<<shift-1 for rounding
      final int start = 1 << postShift[0] - 1;
      final int abuf[] = data;
      final int bbuf[] = bblps.data;

      // the next two blocks are duplicated except for the missing shift
      // operation if preShift==0.
      final int preShift0 = preShift[0];
      final int postShift0 = postShift[0];
      if (preShift0 != 0)
        for (int bp = 0, b = 0; b < nx; b++) {
          final Weighttab wtab = tabs[b];
          final int an = wtab.weights.length;

          int sum = start;
          for (int wp = 0, ap = wtab.i0; wp < an && ap < abuf.length; wp++) {
            sum += wtab.weights[wp] * (abuf[ap++] >> preShift0);
          }

          final int t = sum >> postShift0;
          bbuf[bp++] = t < 0 ? 0 : t > 255 ? 255 : t;
        }
      else
        for (int bp = 0, b = 0; b < nx; b++) {
          final Weighttab wtab = tabs[b];
          final int an = wtab.weights.length;

          int sum = start;
          for (int wp = 0, ap = wtab.i0; wp < an && ap < abuf.length; wp++) {
            sum += wtab.weights[wp] * abuf[ap++];
          }

          bbuf[bp++] = sum >> postShift0;
        }
    }

    @Override
    protected void shift(final int[] shift) {
      final int shift0 = shift[0];
      final int half = 1 << shift0 - 1;

      final int abuf[] = data;

      for (int b = 0; b < abuf.length; b++) {
        final int t = abuf[b] + half >> shift0;
        abuf[b] = t < 0 ? 0 : t > 255 ? 255 : t;
      }
    }

    @Override
    protected void store(final int x, final int y) {
      dstRaster.setPixels(x, y, length, 1, data);
    }
  }

  int y;
  protected final int length;

  protected Scanline(final int width) {
    length = width;
  }

  protected final int getWidth() {
    return length;
  }

  protected abstract void clear();

  protected abstract void fetch(int x, int y);

  protected abstract void filter(int[] preShift, int[] postShift, Weighttab[] xweights, Scanline dst);

  protected abstract void accumulate(int weight, Scanline dst);

  protected abstract void shift(int[] finalshift);

  protected abstract void store(int x, int y);
}