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

package com.levigo.jbig2;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.imageio.ImageReadParam;

/**
 * This class extends {@code ImageReadParam} and contains region of interest and scale / subsampling
 * functionality
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
public class JBIG2ReadParam extends ImageReadParam {

  public JBIG2ReadParam() {
    this(1, 1, 0, 0, null, null);
  }

  public JBIG2ReadParam(final int sourceXSubsampling, final int sourceYSubsampling, final int subsamplingXOffset,
      final int subsamplingYOffset, final Rectangle sourceRegion, final Dimension sourceRenderSize) {
    this.canSetSourceRenderSize = true;
    this.sourceRegion = sourceRegion;
    this.sourceRenderSize = sourceRenderSize;

    if (sourceXSubsampling < 1 || sourceYSubsampling < 1) {
      throw new IllegalArgumentException("Illegal subsampling factor: shall be 1 or greater; but was "
          + " sourceXSubsampling=" + sourceXSubsampling + ", sourceYSubsampling=" + sourceYSubsampling);
    }

    setSourceSubsampling(sourceXSubsampling, sourceYSubsampling, subsamplingXOffset, subsamplingYOffset);
  }
}
