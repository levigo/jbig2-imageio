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

import java.awt.Rectangle;
import java.awt.image.WritableRaster;

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.util.Utils;

class Resizer {

  static final class Mapping {
    /** x and y scales */
    final double scale;

    /** x and y offset used by MAP, private fields */
    final double offset = .5;

    private final double a0;
    private final double b0;

    Mapping(double a0, double aw, double b0, double bw) {
      this.a0 = a0;
      this.b0 = b0;
      scale = bw / aw;

      if (scale <= 0.)
        throw new IllegalArgumentException("Negative scales are not allowed");
    }

    Mapping(double scaleX) {
      scale = scaleX;
      a0 = b0 = 0;
    }

    double mapPixelCenter(final int b) {
      return (b + offset - b0) / scale + a0;
    }

    double dstToSrc(final double b) {
      return (b - b0) / scale + a0;
    }

    double srcToDst(final double a) {
      return (a - a0) * scale + b0;
    }
  }

  /**
   * Order in which to apply filter
   */
  private enum Order {
    AUTO, XY, YX
  }

  /** Error tolerance */
  private static final double EPSILON = 1e-7;

  /** Number of bits in filter coefficients */
  private int weightBits = 14;

  private int weightOne = 1 << weightBits;

  /** Number of bits per channel */
  private int bitsPerChannel[] = new int[]{
      8, 8, 8
  };

  private static final int NO_SHIFT[] = new int[16];

  private int finalShift[] = new int[]{
      2 * weightBits - bitsPerChannel[0], 2 * weightBits - bitsPerChannel[1], 2 * weightBits - bitsPerChannel[2]
  };

  /**
   * Is x an integer?
   * 
   * @param x the double to check
   * @return <code>true</code> if x is an integer, <code>false</code> if not.
   */
  private static boolean isInteger(final double x) {
    return Math.abs(x - Math.floor(x + .5)) < EPSILON;
  }

  static final boolean debug = false;

  /**
   * Should filters be simplified if possible?
   */
  private final boolean coerce = true;

  /**
   * The order in which data is processed.
   * 
   * @see Order
   */
  private final Order order = Order.AUTO;

  /**
   * Should zeros be trimmed in x filter weight tables?
   */
  private final boolean trimZeros = true;

  private final Mapping mappingX;
  private final Mapping mappingY;

  /**
   * Creates an instance of {@link Resizer} with one scale factor for both x and y directions.
   * 
   * @param scale the scale factor for x and y direction
   */
  public Resizer(double scale) {
    this(scale, scale);
  }

  /**
   * Creates an instance of {@link Resizer} with a scale factor for each direction.
   * 
   * @param scaleX the scale factor for x direction
   * @param scaleY the scale factor for y direction
   */
  public Resizer(double scaleX, double scaleY) {
    mappingX = new Mapping(scaleX);
    mappingY = new Mapping(scaleY);
  }

  private Weighttab[] createXWeights(Rectangle srcBounds, final Rectangle dstBounds, final ParameterizedFilter filter) {
    final int srcX0 = srcBounds.x;
    final int srcX1 = srcBounds.x + srcBounds.width;

    final int dstX0 = dstBounds.x;
    final int dstX1 = dstBounds.x + dstBounds.width;

    final Weighttab tabs[] = new Weighttab[dstBounds.width];
    for (int dstX = dstX0; dstX < dstX1; dstX++) {
      final double center = mappingX.mapPixelCenter(dstX);
      tabs[dstX - dstX0] = new Weighttab(filter, weightOne, center, srcX0, srcX1 - 1, trimZeros);
    }

    return tabs;
  }

  /**
   * Checks if our discrete sampling of an arbitrary continuous filter, parameterized by the filter
   * spacing ({@link ParameterizedFilter#scale}), its radius ({@link ParameterizedFilter#support}),
   * and the scale and offset of the coordinate mapping, causes the filter to reduce to point
   * sampling.
   * <p>
   * It reduces if support is less than 1 pixel or if integer scale and translation, and filter is
   * cardinal.
   * 
   * @param filter the parameterized filter instance to be simplified
   * @param scale the scale of the coordinate mapping
   * @param offset the offset of the coordinate mapping
   */
  private ParameterizedFilter simplifyFilter(final ParameterizedFilter filter, final double scale, final double offset) {
    if (coerce
        && (filter.support <= .5 || filter.filter.cardinal && isInteger(1. / filter.scale)
            && isInteger(1. / (scale * filter.scale)) && isInteger((offset / scale - .5) / filter.scale)))
      return new ParameterizedFilter(new Filter.Point(), 1., .5, 1);

    return filter;
  }

  /**
   * Filtered zoom, x direction filtering before y direction filtering
   * <p>
   * Note: when calling {@link Resizer#createXWeights(Rectangle, Rectangle, ParameterizedFilter)},
   * we can trim leading and trailing zeros from the x weight buffers as an optimization, but not
   * for y weight buffers since the split formula is anticipating a constant amount of buffering of
   * source scanlines; trimming zeros in y weight could cause feedback.
   */
  private void resizeXfirst(final Object src, final Rectangle srcBounds, final Object dst, final Rectangle dstBounds,
      final ParameterizedFilter xFilter, final ParameterizedFilter yFilter) {
    // source scanline buffer
    final Scanline buffer = createScanline(src, dst, srcBounds.width);

    // accumulator buffer
    final Scanline accumulator = createScanline(src, dst, dstBounds.width);

    // a sampled filter for source pixels for each dest x position
    final Weighttab xWeights[] = createXWeights(srcBounds, dstBounds, xFilter);

    // Circular buffer of active lines
    final int yBufferSize = yFilter.width + 2;
    final Scanline lineBuffer[] = new Scanline[yBufferSize];
    for (int y = 0; y < yBufferSize; y++) {
      lineBuffer[y] = createScanline(src, dst, dstBounds.width);
      lineBuffer[y].y = -1; /* mark scanline as unread */
    }

    // range of source and destination scanlines in regions
    final int srcY0 = srcBounds.y;
    final int srcY1 = srcBounds.y + srcBounds.height;
    final int dstY0 = dstBounds.y;
    final int dstY1 = dstBounds.y + dstBounds.height;

    int yFetched = -1; // used to assert no backtracking

    // loop over dest scanlines
    for (int dstY = dstY0; dstY < dstY1; dstY++) {
      // a sampled filter for source pixels for each dest x position
      final Weighttab yWeight = new Weighttab(yFilter, weightOne, mappingY.mapPixelCenter(dstY), srcY0, srcY1 - 1, true);

      accumulator.clear();

      // loop over source scanlines that contribute to this dest scanline
      for (int srcY = yWeight.i0; srcY <= yWeight.i1; srcY++) {
        final Scanline srcBuffer = lineBuffer[srcY % yBufferSize];

        if (debug)
          System.out.println("  abuf.y / ayf " + srcBuffer.y + " / " + srcY);

        if (srcBuffer.y != srcY) {
          // scanline needs to be fetched from src raster
          srcBuffer.y = srcY;

          if (srcY0 + srcY <= yFetched)
            throw new AssertionError("Backtracking from line " + yFetched + " to " + (srcY0 + srcY));

          buffer.fetch(srcBounds.x, srcY0 + srcY);

          yFetched = srcY0 + srcY;

          // filter it into the appropriate line of linebuf (xfilt)
          buffer.filter(NO_SHIFT, bitsPerChannel, xWeights, srcBuffer);
        }

        // add weighted tbuf into accum (these do yfilt)
        srcBuffer.accumulate(yWeight.weights[srcY - yWeight.i0], accumulator);
      }

      accumulator.shift(finalShift);
      accumulator.store(dstBounds.x, dstY);
      if (debug)
        System.out.printf("\n");
    }
  }

  /**
   * Filtered zoom, y direction filtering before x direction filtering
   * */
  private void resizeYfirst(final Object src, final Rectangle srcBounds, final Object dst, final Rectangle dstBounds,
      final ParameterizedFilter xFilter, final ParameterizedFilter yFilter) {
    // destination scanline buffer
    final Scanline buffer = createScanline(src, dst, dstBounds.width);

    // accumulator buffer
    final Scanline accumulator = createScanline(src, dst, srcBounds.width);

    // a sampled filter for source pixels for each destination x position
    final Weighttab xWeights[] = createXWeights(srcBounds, dstBounds, xFilter);

    // Circular buffer of active lines
    final int yBufferSize = yFilter.width + 2;
    final Scanline lineBuffer[] = new Scanline[yBufferSize];
    for (int y = 0; y < yBufferSize; y++) {
      lineBuffer[y] = createScanline(src, dst, srcBounds.width);

      // mark scanline as unread
      lineBuffer[y].y = -1;
    }

    // range of source and destination scanlines in regions
    final int srcY0 = srcBounds.y;
    final int srcY1 = srcBounds.y + srcBounds.height;
    final int dstY0 = dstBounds.y;
    final int dstY1 = dstBounds.y + dstBounds.height;

    // used to assert no backtracking
    int yFetched = -1;

    // loop over destination scanlines
    for (int dstY = dstY0; dstY < dstY1; dstY++) {
      // prepare a weighttab for destination y position by a single sampled filter for current y
      // position
      final Weighttab yWeight = new Weighttab(yFilter, weightOne, mappingY.mapPixelCenter(dstY), srcY0, srcY1 - 1, true);

      accumulator.clear();

      // loop over source scanlines that contribute to this destination scanline
      for (int srcY = yWeight.i0; srcY <= yWeight.i1; srcY++) {
        final Scanline srcBuffer = lineBuffer[srcY % yBufferSize];
        if (srcBuffer.y != srcY) {
          // scanline needs to be fetched from source raster
          srcBuffer.y = srcY;

          if (srcY0 + srcY <= yFetched)
            throw new AssertionError("Backtracking from line " + yFetched + " to " + (srcY0 + srcY));

          srcBuffer.fetch(srcBounds.x, srcY0 + srcY);

          yFetched = srcY0 + srcY;
        }

        if (debug)
          System.out.println(dstY + "[] += " + srcY + "[] * " + yWeight.weights[srcY - yWeight.i0]);

        // add weighted source buffer into accumulator (these do y filter)
        srcBuffer.accumulate(yWeight.weights[srcY - yWeight.i0], accumulator);
      }

      // and filter it into the appropriate line of line buffer (x filter)
      accumulator.filter(bitsPerChannel, finalShift, xWeights, buffer);

      // store destination scanline into destination raster
      buffer.store(dstBounds.x, dstY);
      if (debug)
        System.out.printf("\n");
    }
  }

  /**
   * @param src Source object
   * @param srcBounds Bounds of the source object
   * @param dst Destination object
   * @param dstBounds Bounds of the destination object
   * @param xFilter The filter used for x direction filtering
   * @param yFilter The filter used for y direction filtering
   */
  public void resize(final Object src, final Rectangle srcBounds, final Object dst, Rectangle dstBounds,
      Filter xFilter, Filter yFilter) {
    /*
     * find scale of filter in a space (source space) when minifying, source scale=1/scale, but when
     * magnifying, source scale=1
     */
    ParameterizedFilter xFilterParameterized = new ParameterizedFilter(xFilter, mappingX.scale);
    ParameterizedFilter yFilterParameterized = new ParameterizedFilter(yFilter, mappingY.scale);

    /* find valid destination window (transformed source + support margin) */
    final Rectangle dstRegion = new Rectangle();
    final int x1 = Utils.ceil(mappingX.srcToDst(srcBounds.x - xFilterParameterized.support) + EPSILON);
    final int y1 = Utils.ceil(mappingY.srcToDst(srcBounds.y - yFilterParameterized.support) + EPSILON);
    final int x2 = Utils.floor(mappingX.srcToDst(srcBounds.x + srcBounds.width + xFilterParameterized.support)
        - EPSILON);
    final int y2 = Utils.floor(mappingY.srcToDst(srcBounds.y + srcBounds.height + yFilterParameterized.support)
        - EPSILON);
    dstRegion.setFrameFromDiagonal(x1, y1, x2, y2);

    if (dstBounds.x < dstRegion.x || dstBounds.getMaxX() > dstRegion.getMaxX() || dstBounds.y < dstRegion.y
        || dstBounds.getMaxY() > dstRegion.getMaxY()) {
      /* requested destination window lies outside the valid destination, so clip destination */
      dstBounds = dstBounds.intersection(dstRegion);
    }

    if (srcBounds.isEmpty() || dstBounds.width <= 0 || dstBounds.height <= 0) {
      return;
    }

    /* check for high-level simplifications of filter */
    xFilterParameterized = simplifyFilter(xFilterParameterized, mappingX.scale, mappingX.offset);
    yFilterParameterized = simplifyFilter(yFilterParameterized, mappingY.scale, mappingY.offset);

    /*
     * decide which filtering order (x->y or y->x) is faster for this mapping by counting
     * convolution multiplies
     */
    final boolean orderXY = order != Order.AUTO
        ? order == Order.XY
        : dstBounds.width
            * (srcBounds.height * xFilterParameterized.width + dstBounds.height * yFilterParameterized.width) < dstBounds.height
            * (dstBounds.width * xFilterParameterized.width + srcBounds.width * yFilterParameterized.width);

    // choose most efficient filtering order
    if (orderXY) {
      resizeXfirst(src, srcBounds, dst, dstBounds, xFilterParameterized, yFilterParameterized);
    } else {
      resizeYfirst(src, srcBounds, dst, dstBounds, xFilterParameterized, yFilterParameterized);
    }
  }

  private static Scanline createScanline(final Object src, Object dst, final int length) {
    if (src == null)
      throw new IllegalArgumentException("src must not be null");

    if (!(src instanceof Bitmap))
      throw new IllegalArgumentException("src must be from type " + Bitmap.class.getName());

    if (dst == null)
      throw new IllegalArgumentException("dst must not be null");

    if (!(dst instanceof WritableRaster))
      throw new IllegalArgumentException("dst must be from type " + WritableRaster.class.getName());

    return new BitmapScanline((Bitmap) src, (WritableRaster) dst, length);
  }

}
