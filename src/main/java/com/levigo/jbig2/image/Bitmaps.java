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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import javax.imageio.ImageReadParam;

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.JBIG2ReadParam;
import com.levigo.jbig2.util.CombinationOperator;

public class Bitmaps {

  public static WritableRaster asRaster(final Bitmap bitmap) {
    return asRaster(bitmap, FilterType.Gaussian);
  }

  public static WritableRaster asRaster(final Bitmap bitmap, final FilterType filterType) {
    if (bitmap == null)
      throw new IllegalArgumentException("bitmap must not be null");

    final JBIG2ReadParam param = new JBIG2ReadParam(1, 1, 0, 0, new Rectangle(0, 0, bitmap.getWidth(),
        bitmap.getHeight()), new Dimension(bitmap.getWidth(), bitmap.getHeight()));

    return asRaster(bitmap, param, filterType);
  }

  public static WritableRaster asRaster(Bitmap bitmap, final ImageReadParam param, final FilterType filterType) {
    if (bitmap == null)
      throw new IllegalArgumentException("bitmap must not be null");

    if (param == null)
      throw new IllegalArgumentException("param must not be null");

    final Dimension sourceRenderSize = param.getSourceRenderSize();

    double scaleX;
    double scaleY;
    if (sourceRenderSize != null) {
      scaleX = sourceRenderSize.getWidth() / bitmap.getWidth();
      scaleY = sourceRenderSize.getHeight() / bitmap.getHeight();
    } else {
      scaleX = scaleY = 1;
    }

    Rectangle sourceRegion = param.getSourceRegion();
    if (sourceRegion != null && !bitmap.getBounds().equals(sourceRegion)) {
      // make sure we don't request an area outside of the source bitmap
      sourceRegion = bitmap.getBounds().intersection(sourceRegion);

      // get region of interest
      bitmap = Bitmaps.extract(sourceRegion, bitmap);
    }

    /*
     * Subsampling is the advance of columns/rows for each pixel in the according direction. The
     * resulting image's quality will be bad because we loose information if we step over
     * columns/rows. For example, a thin line (1 pixel high) may disappear completely. To avoid this
     * we use resize filters if scaling will be performed anyway. The resize filters use scale
     * factors, one for horizontal and vertical direction. We care about the given subsampling steps
     * by adjusting the scale factors. If scaling is not performed, subsampling is performed in its
     * original manner.
     */

    final boolean requiresScaling = scaleX != 1 || scaleY != 1;

    final boolean requiresXSubsampling = param.getSourceXSubsampling() != 1;
    final boolean requiresYSubsampling = param.getSourceYSubsampling() != 1;

    if (requiresXSubsampling && requiresYSubsampling) {
      // Apply vertical and horizontal subsampling
      if (requiresScaling) {
        scaleX /= (double) param.getSourceXSubsampling();
        scaleY /= (double) param.getSourceYSubsampling();
      } else {
        bitmap = subsample(bitmap, param);
      }
    } else {
      if (requiresXSubsampling) {
        // Apply horizontal subsampling only
        if (requiresScaling) {
          scaleX /= (double) param.getSourceXSubsampling();
        } else {
          bitmap = Bitmaps.subsampleX(bitmap, param.getSourceXSubsampling(), param.getSubsamplingXOffset());
        }
      }

      if (requiresYSubsampling) {
        // Apply vertical subsampling only
        if (requiresScaling) {
          scaleY /= (double) param.getSourceYSubsampling();
        } else {
          bitmap = Bitmaps.subsampleY(bitmap, param.getSourceYSubsampling(), param.getSubsamplingYOffset());
        }
      }
    }

    return buildRaster(bitmap, filterType, scaleX, scaleY);
  }

  private static WritableRaster buildRaster(final Bitmap bitmap, final FilterType filterType, final double scaleX,
      final double scaleY) {
    final Rectangle dstBounds = new Rectangle(0, 0, //
        (int) Math.round(bitmap.getWidth() * scaleX), //
        (int) Math.round(bitmap.getHeight() * scaleY));

    final WritableRaster dst = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_BYTE, dstBounds.width,
        dstBounds.height, 1, new Point());

    if (scaleX != 1 || scaleY != 1) {
      // scaling required
      final Resizer resizer = new Resizer(scaleX, scaleY);
      final Filter filter = Filter.byType(filterType);
      resizer.resize(bitmap, bitmap.getBounds() /* sourceRegion */, dst, dstBounds, filter, filter);
    } else {
      // scaling not required, paste bitmap into raster pixel per pixel
      int byteIndex = 0;
      for (int y = 0; y < bitmap.getHeight(); y++) {
        for (int x = 0; x < bitmap.getWidth(); byteIndex++) {
          final int pixels = (~bitmap.getByte(byteIndex)) & 0xFF;
          final int relevantPixels = bitmap.getWidth() - x > 8 ? 8 : bitmap.getWidth() - x;
          final int endIdx = 7 - relevantPixels;
          for (int bytePosition = 7; bytePosition > endIdx; bytePosition--, x++) {
            dst.setSample(x, y, 0, (pixels >> bytePosition) & 0x1);
          }
        }
      }
    }

    return dst;
  }

  public static BufferedImage asBufferedImage(Bitmap bitmap) {
    return asBufferedImage(bitmap, FilterType.Gaussian);
  }

  public static BufferedImage asBufferedImage(Bitmap bitmap, FilterType filterType) {
    if (bitmap == null)
      throw new IllegalArgumentException("bitmap must not be null");

    final JBIG2ReadParam param = new JBIG2ReadParam(1, 1, 0, 0, new Rectangle(0, 0, bitmap.getWidth(),
        bitmap.getHeight()), new Dimension(bitmap.getWidth(), bitmap.getHeight()));

    return asBufferedImage(bitmap, param, filterType);
  }

  public static BufferedImage asBufferedImage(Bitmap bitmap, ImageReadParam param, FilterType filterType) {
    if (bitmap == null)
      throw new IllegalArgumentException("bitmap must not be null");

    if (param == null)
      throw new IllegalArgumentException("param must not be null");

    final WritableRaster raster = asRaster(bitmap, param, filterType);

    final Dimension sourceRenderSize = param.getSourceRenderSize();

    final double scaleX;
    final double scaleY;
    if (sourceRenderSize != null) {
      scaleX = sourceRenderSize.getWidth() / bitmap.getWidth();
      scaleY = sourceRenderSize.getHeight() / bitmap.getHeight();
    } else {
      scaleX = scaleY = 1d;
    }

    ColorModel cm = null;
    final boolean isScaled = scaleX != 1 || scaleY != 1;
    if (isScaled) {
      final int size = 256;
      final int divisor = size - 1;

      final byte[] gray = new byte[size];
      for (int i = size - 1, s = 0; i >= 0; i--, s++) {
        gray[i] = (byte) (255 - s * 255 / divisor);
      }
      cm = new IndexColorModel(8, size, gray, gray, gray);
    } else {

      cm = new IndexColorModel(8, 2, //
          new byte[]{
              0x00, (byte) 0xff
          }, new byte[]{
              0x00, (byte) 0xff
          }, new byte[]{
              0x00, (byte) 0xff
          });
    }

    return new BufferedImage(cm, raster, false, null);
  }

  /**
   * Returns the specified rectangle area of the bitmap.
   * 
   * @param roi - A {@link Rectangle} that specifies the requested image section.
   * @return A {@code Bitmap} that represents the requested image section.
   */
  public static Bitmap extract(final Rectangle roi, final Bitmap src) {
    final Bitmap dst = new Bitmap(roi.width, roi.height);

    final int upShift = roi.x & 0x07;
    final int downShift = 8 - upShift;
    int dstLineStartIdx = 0;

    final int padding = (8 - dst.getWidth() & 0x07);
    int srcLineStartIdx = src.getByteIndex(roi.x, roi.y);
    int srcLineEndIdx = src.getByteIndex(roi.x + roi.width - 1, roi.y);
    final boolean usePadding = dst.getRowStride() == srcLineEndIdx + 1 - srcLineStartIdx;

    for (int y = roi.y; y < roi.getMaxY(); y++) {
      int srcIdx = srcLineStartIdx;
      int dstIdx = dstLineStartIdx;

      if (srcLineStartIdx == srcLineEndIdx) {
        final byte pixels = (byte) (src.getByte(srcIdx) << upShift);
        dst.setByte(dstIdx, unpad(padding, pixels));
      } else if (upShift == 0) {
        for (int x = srcLineStartIdx; x <= srcLineEndIdx; x++) {
          byte value = src.getByte(srcIdx++);

          if (x == srcLineEndIdx && usePadding) {
            value = unpad(padding, value);
          }

          dst.setByte(dstIdx++, value);
        }
      } else {
        copyLine(src, dst, upShift, downShift, padding, srcLineStartIdx, srcLineEndIdx, usePadding, srcIdx, dstIdx);
      }

      srcLineStartIdx += src.getRowStride();
      srcLineEndIdx += src.getRowStride();
      dstLineStartIdx += dst.getRowStride();
    }

    return dst;
  }

  private static void copyLine(Bitmap src, Bitmap dst, int sourceUpShift, int sourceDownShift, int padding,
      int firstSourceByteOfLine, int lastSourceByteOfLine, boolean usePadding, int sourceOffset, int targetOffset) {
    for (int x = firstSourceByteOfLine; x < lastSourceByteOfLine; x++) {

      if (sourceOffset + 1 < src.getByteArray().length) {
        final boolean isLastByte = x + 1 == lastSourceByteOfLine;
        byte value = (byte) (src.getByte(sourceOffset++) << sourceUpShift | (src.getByte(sourceOffset) & 0xff) >>> sourceDownShift);

        if (isLastByte && !usePadding) {
          value = unpad(padding, value);
        }

        dst.setByte(targetOffset++, value);

        if (isLastByte && usePadding) {
          value = unpad(padding, (byte) ((src.getByte(sourceOffset) & 0xff) << sourceUpShift));
          dst.setByte(targetOffset, value);
        }

      } else {
        final byte value = (byte) (src.getByte(sourceOffset++) << sourceUpShift & 0xff);
        dst.setByte(targetOffset++, value);
      }
    }
  }

  /**
   * Removes unnecessary bits from a byte.
   * 
   * @param padding - The amount of unnecessary bits.
   * @param value - The byte that should be cleaned up.
   * @return A cleaned byte.
   */
  private static byte unpad(int padding, byte value) {
    return (byte) (value >> padding << padding);
  }

  public static Bitmap subsample(Bitmap src, ImageReadParam param) {
    if (src == null)
      throw new IllegalArgumentException("src must not be null");

    if (param == null)
      throw new IllegalArgumentException("param must not be null");

    final int xSubsampling = param.getSourceXSubsampling();
    final int ySubsampling = param.getSourceYSubsampling();
    final int xSubsamplingOffset = param.getSubsamplingXOffset();
    final int ySubsamplingOffset = param.getSubsamplingYOffset();

    final int dstWidth = (src.getWidth() - xSubsamplingOffset) / xSubsampling;
    final int dstHeight = (src.getHeight() - ySubsamplingOffset) / ySubsampling;

    final Bitmap dst = new Bitmap(dstWidth, dstHeight);

    for (int yDst = 0, ySrc = ySubsamplingOffset; yDst < dst.getHeight(); yDst++, ySrc += ySubsampling) {
      for (int xDst = 0, xSrc = xSubsamplingOffset; xDst < dst.getWidth(); xDst++, xSrc += xSubsampling) {
        final byte pixel = src.getPixel(xSrc, ySrc);
        if (pixel != 0)
          dst.setPixel(xDst, yDst, pixel);
      }
    }

    return dst;
  }

  public static Bitmap subsampleX(Bitmap src, final int xSubsampling, final int xSubsamplingOffset) {
    if (src == null)
      throw new IllegalArgumentException("src must not be null");

    final int dstHeight = (src.getWidth() - xSubsamplingOffset) / xSubsampling;
    final Bitmap dst = new Bitmap(src.getWidth(), dstHeight);

    for (int yDst = 0; yDst < dst.getHeight(); yDst++) {
      for (int xDst = 0, xSrc = xSubsamplingOffset; xDst < dst.getWidth(); xDst++, xSrc += xSubsampling) {
        final byte pixel = src.getPixel(xSrc, yDst);
        if (pixel != 0)
          dst.setPixel(xDst, yDst, pixel);
      }
    }

    return dst;
  }

  public static Bitmap subsampleY(Bitmap src, final int ySubsampling, final int ySubsamplingOffset) {
    if (src == null)
      throw new IllegalArgumentException("src must not be null");

    final int dstWidth = (src.getWidth() - ySubsamplingOffset) / ySubsampling;
    final Bitmap dst = new Bitmap(dstWidth, src.getHeight());

    for (int yDst = 0, ySrc = ySubsamplingOffset; yDst < dst.getHeight(); yDst++, ySrc += ySubsampling) {
      for (int xDst = 0; xDst < dst.getWidth(); xDst++) {
        final byte pixel = src.getPixel(xDst, ySrc);
        if (pixel != 0)
          dst.setPixel(xDst, yDst, pixel);
      }
    }

    return dst;
  }

  /**
   * The method combines two given bytes with an logical operator.
   * <p>
   * The JBIG2 Standard specifies 5 possible combinations of bytes.<br>
   * <p>
   * <b>Hint:</b> Please take a look at ISO/IEC 14492:2001 (E) for detailed definition and
   * description of the operators.
   * 
   * @param value1 - The value that should be combined with value2.
   * @param value2 - The value that should be combined with value1.
   * @param op - The specified combination operator.
   * 
   * @return The combination result.
   */
  public static byte combineBytes(byte value1, byte value2, CombinationOperator op) {

    switch (op){
      case OR :
        return (byte) (value2 | value1);
      case AND :
        return (byte) (value2 & value1);
      case XOR :
        return (byte) (value2 ^ value1);
      case XNOR :
        return (byte) ~(value1 ^ value2);
      case REPLACE :
      default :
        // Old value is replaced by new value.
        return value2;
    }
  }

  /**
   * This method combines a given bitmap with the current instance.
   * <p>
   * Parts of the bitmap to blit that are outside of the target bitmap will be ignored.
   * 
   * @param src - The bitmap that should be combined with the one of the current instance.
   * @param x - The x coordinate where the upper left corner of the bitmap to blit should be
   *          positioned.
   * @param y - The y coordinate where the upper left corner of the bitmap to blit should be
   *          positioned.
   * @param combinationOperator - The combination operator for combining two pixels.
   */
  public static void blit(Bitmap src, Bitmap dst, int x, int y, CombinationOperator combinationOperator) {

    int startLine = 0;
    int srcStartIdx = 0;
    int srcEndIdx = (src.getRowStride() - 1);

    // Ignore those parts of the source bitmap which would be placed outside the target bitmap.
    if (x < 0) {
      srcStartIdx = -x;
      x = 0;
    } else if (x + src.getWidth() > dst.getWidth()) {
      srcEndIdx -= (src.getWidth() + x - dst.getWidth());
    }

    if (y < 0) {
      startLine = -y;
      y = 0;
      srcStartIdx += src.getRowStride();
      srcEndIdx += src.getRowStride();
    } else if (y + src.getHeight() > dst.getHeight()) {
      startLine = src.getHeight() + y - dst.getHeight();
    }

    final int shiftVal1 = x & 0x07;
    final int shiftVal2 = 8 - shiftVal1;

    final int padding = src.getWidth() & 0x07;
    final int toShift = shiftVal2 - padding;

    final boolean useShift = (shiftVal2 & 0x07) != 0;
    final boolean specialCase = src.getWidth() <= ((srcEndIdx - srcStartIdx) << 3) + shiftVal2;

    final int dstStartIdx = dst.getByteIndex(x, y);

    final int lastLine = Math.min(src.getHeight(), startLine + dst.getHeight());

    if (!useShift) {
      blitUnshifted(src, dst, startLine, lastLine, dstStartIdx, srcStartIdx, srcEndIdx, combinationOperator);
    } else if (specialCase) {
      blitSpecialShifted(src, dst, startLine, lastLine, dstStartIdx, srcStartIdx, srcEndIdx, toShift, shiftVal1,
          shiftVal2, combinationOperator);
    } else {
      blitShifted(src, dst, startLine, lastLine, dstStartIdx, srcStartIdx, srcEndIdx, toShift, shiftVal1, shiftVal2,
          combinationOperator, padding);
    }
  }

  private static void blitUnshifted(Bitmap src, Bitmap dst, int startLine, int lastLine, int dstStartIdx,
      int srcStartIdx, int srcEndIdx, CombinationOperator op) {

    for (int dstLine = startLine; dstLine < lastLine; dstLine++, dstStartIdx += dst.getRowStride(), srcStartIdx += src.getRowStride(), srcEndIdx += src.getRowStride()) {
      int dstIdx = dstStartIdx;

      // Go through the bytes in a line of the Symbol
      for (int srcIdx = srcStartIdx; srcIdx <= srcEndIdx; srcIdx++) {
        byte oldByte = dst.getByte(dstIdx);
        byte newByte = src.getByte(srcIdx);
        dst.setByte(dstIdx++, Bitmaps.combineBytes(oldByte, newByte, op));
      }
    }
  }

  private static void blitSpecialShifted(Bitmap src, Bitmap dst, int startLine, int lastLine, int dstStartIdx,
      int srcStartIdx, int srcEndIdx, int toShift, int shiftVal1, int shiftVal2, CombinationOperator op) {

    for (int dstLine = startLine; dstLine < lastLine; dstLine++, dstStartIdx += dst.getRowStride(), srcStartIdx += src.getRowStride(), srcEndIdx += src.getRowStride()) {
      short register = 0;
      int dstIdx = dstStartIdx;

      // Go through the bytes in a line of the Symbol
      for (int srcIdx = srcStartIdx; srcIdx <= srcEndIdx; srcIdx++) {
        byte oldByte = dst.getByte(dstIdx);
        register = (short) ((register | src.getByte(srcIdx) & 0xff) << shiftVal2);
        byte newByte = (byte) (register >> 8);

        if (srcIdx == srcEndIdx) {
          newByte = unpad(toShift, newByte);
        }

        dst.setByte(dstIdx++, Bitmaps.combineBytes(oldByte, newByte, op));
        register <<= shiftVal1;
      }
    }
  }

  private static void blitShifted(Bitmap src, Bitmap dst, int startLine, int lastLine, int dstStartIdx,
      int srcStartIdx, int srcEndIdx, int toShift, int shiftVal1, int shiftVal2, CombinationOperator op, int padding) {

    for (int dstLine = startLine; dstLine < lastLine; dstLine++, dstStartIdx += dst.getRowStride(), srcStartIdx += src.getRowStride(), srcEndIdx += src.getRowStride()) {
      short register = 0;
      int dstIdx = dstStartIdx;

      // Go through the bytes in a line of the symbol
      for (int srcIdx = srcStartIdx; srcIdx <= srcEndIdx; srcIdx++) {
        byte oldByte = dst.getByte(dstIdx);
        register = (short) ((register | src.getByte(srcIdx) & 0xff) << shiftVal2);

        byte newByte = (byte) (register >> 8);
        dst.setByte(dstIdx++, Bitmaps.combineBytes(oldByte, newByte, op));

        register <<= shiftVal1;

        if (srcIdx == srcEndIdx) {
          newByte = (byte) (register >> (8 - shiftVal2));

          if (padding != 0) {
            newByte = unpad(8 + toShift, newByte);
          }

          oldByte = dst.getByte(dstIdx);
          dst.setByte(dstIdx, Bitmaps.combineBytes(oldByte, newByte, op));
        }
      }
    }
  }


}
