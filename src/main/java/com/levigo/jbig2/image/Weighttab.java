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

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.levigo.jbig2.util.Utils;

final class Weighttab {
  final int weights[]; /* weight[i] goes with pixel at i0+i */
  final int i0, i1; /* range of samples is [i0..i1-1] */

  /*
   * make_weighttab: sample the continuous filter, scaled by ap.scale and positioned at continuous
   * source coordinate cen, for source coordinates in the range [0..len-1], writing the weights into
   * wtab. Scale the weights so they sum to WEIGHTONE, and trim leading and trailing zeros if
   * trimzeros!=0. b is the dest coordinate (for diagnostics).
   */
  public Weighttab(ParameterizedFilter pf, int weightOne, final double center, int a0, final int a1,
      final boolean trimzeros) {
    // find the source coord range of this positioned filter: [i0..i1-1] and clamp to input range
    int i0 = max(pf.minIndex(center), a0);
    int i1 = min(pf.maxIndex(center), a1);

    // find scale factor sc to normalize the filter
    double den = 0;
    for (int i = i0; i <= i1; i++)
      den += pf.eval(center, i);

    // set sc so that sum of sc*func() is approximately WEIGHTONE
    final double scale = den == 0. ? weightOne : weightOne / den;

    // find range of non-zero samples
    if (trimzeros) {
      boolean stillzero = trimzeros;
      int lastnonzero = 0;
      for (int i = i0; i <= i1; i++) {
        /* evaluate the filter function at p */
        final double tr = Utils.clamp(scale * pf.eval(center, i), Short.MIN_VALUE, Short.MAX_VALUE);

        final int t = (int) Math.floor(tr + .5);
        if (stillzero && t == 0)
          i0++; /* find first nonzero */
        else {
          stillzero = false;
          if (t != 0)
            lastnonzero = i; /* find last nonzero */
        }
      }

      i1 = max(lastnonzero, i0);
    }

    // initialize weight table of appropriate length
    weights = new int[i1 - i0 + 1];

    // compute the discrete, sampled filter coefficients
    int sum = 0;
    for (int idx = 0, i = i0; i <= i1; i++) {
      /* evaluate the filter function at p */
      final double tr = Utils.clamp(scale * pf.eval(center, i), Short.MIN_VALUE, Short.MAX_VALUE);

      final int t = (int) Math.floor(tr + .5);
      weights[idx++] = t; /* add weight to table */
      sum += t;
    }

    if (sum == 0) {
      i1 = i0;
      weights[0] = weightOne;
    } else if (sum != weightOne) {
      /*
       * Fudge the center slightly to make sum=WEIGHTONE exactly. Is this the best way to normalize
       * a discretely sampled continuous filter?
       */
      int i = (int) (center + .5);
      if (i >= i1)
        i = i1 - 1;
      if (i < i0)
        i = i0;
      final int t = weightOne - sum;
      if (Resizer.debug)
        System.out.printf("[%d]+=%d ", i, t);
      weights[i - i0] += t; /* fudge center sample */
    }

    this.i0 = i0 - a0;
    this.i1 = i1 - a0;

    if (Resizer.debug) {
      System.out.printf("\t");
      for (int idx = 0, i = i0; i < i1; i++, idx++)
        System.out.printf("%5d ", weights[idx]);
      System.out.printf("\n");
    }
  }
  
}