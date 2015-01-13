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

package com.levigo.jbig2.util;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class Utils {

  /**
   * Create a rectangle with the same area as the given input rectangle but with all of its edges
   * snapped (rounded) to the integer grid. The resulting rectangle is guaranteed to cover
   * <em>all</em> of the input rectangle's area, so that
   * <code>enlargeToGrid(r).contains(r) == true</code> holds. This can be depicted as the edges
   * being stretched in an outward direction.
   * 
   * @param r
   * @return
   */
  public static Rectangle enlargeRectToGrid(Rectangle2D r) {
    final int x0 = floor(r.getMinX());
    final int y0 = floor(r.getMinY());
    final int x1 = ceil(r.getMaxX());
    final int y1 = ceil(r.getMaxY());
    return new Rectangle(x0, y0, x1 - x0, y1 - y0);
  }
  
  /**
   * Return a new rectangle which covers the area of the given rectangle with an additional margin
   * on the sides.
   * 
   * @param r
   * @param marginX
   */
  public static Rectangle2D dilateRect(Rectangle2D r, double marginX, double marginY) {
    return new Rectangle2D.Double(r.getX() - marginX, r.getY() - marginY, r.getWidth() + 2 * marginX, r.getHeight() + 2
        * marginY);
  }
  
  /**
   * Clamp the value into the range [min..max].
   * 
   * @param value
   * @param min
   * @param max
   * @return the clamped value
   */
  public static double clamp(double value, double min, double max) {
    return Math.min(max, Math.max(value, min));
  }
  
  private static final int BIG_ENOUGH_INT = 16 * 1024;
  private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
  private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5;
  
  /**
   * A fast implementation of {@link Math#floor(double)}.
   * 
   * @param x the argument
   * @return
   */
  public static int floor(double x) {
    return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
  }

  /**
   * A fast implementation of {@link Math#round(double)}.
   * 
   * @param x the argument
   * @return
   */
  public static int round(double x) {
    return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
  }

  /**
   * A fast implementation of {@link Math#ceil(double)}.
   * 
   * @param x the argument
   * @return
   */
  public static int ceil(double x) {
    return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - x);
  }
  
}
