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

import com.levigo.jbig2.util.Utils;

class ParameterizedFilter {
  public ParameterizedFilter(final Filter f, final double scale) {
    filter = f;
    /*
     * find scale of filter in a space (source space) when minifying, ascale=1/scale, but when
     * magnifying, ascale=1
     */
    this.scale = f.blur * Math.max(1., 1. / scale);

    /*
     * find support radius of scaled filter if ax.supp and ay.supp are both <=.5 then we've got
     * point sampling. Point sampling is essentially a special filter whose width is fixed at one
     * source pixel.
     */
    support = Math.max(.5, this.scale * f.support);
    width = (int) Math.ceil(2. * support);
  }

  public ParameterizedFilter(final Filter f, final double scale, final double support, final int width) {
    filter = f;
    this.scale = scale;
    this.support = support;
    this.width = width;
  }

  final Filter filter;

  /* filter scale (spacing between centers in a space) */
  final double scale;

  /* scaled filter support radius */
  final double support;

  /* filter width: max number of nonzero samples */
  final int width;

  public double eval(double center, int i) {
    return filter.fWindowed((i + .5 - center) / scale);
  }

  public int minIndex(double center) {
    return Utils.floor(center - support);
  }

  public int maxIndex(double center) {
    return Utils.ceil(center + support);
  }
}