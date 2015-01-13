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

public class PreconfiguredImageReadParam extends ImageReadParam {
  public PreconfiguredImageReadParam(Rectangle sourceRegion) {
    this.sourceRegion = sourceRegion;
  }

  public PreconfiguredImageReadParam(Dimension sourceRenderSize) {
    this.sourceRenderSize = sourceRenderSize;
  }

  public PreconfiguredImageReadParam(int sourceXSubsampling, int sourceYSubsampling, int subsamplingXOffset,
      int subsamplingYOffset) {
    this.sourceXSubsampling = sourceXSubsampling;
    this.sourceYSubsampling = sourceYSubsampling;
    this.subsamplingXOffset = subsamplingXOffset;
    this.subsamplingYOffset = subsamplingYOffset;
  }

  public PreconfiguredImageReadParam(Rectangle sourceRegion, Dimension sourceRenderSize) {
    this.sourceRegion = sourceRegion;
    this.sourceRenderSize = sourceRenderSize;
  }

  public PreconfiguredImageReadParam(Rectangle sourceRegion, Dimension sourceRenderSize, int sourceXSubsampling,
      int sourceYSubsampling, int subsamplingXOffset, int subsamplingYOffset) {
    this.sourceRegion = sourceRegion;
    this.sourceRenderSize = sourceRenderSize;
    this.sourceXSubsampling = sourceXSubsampling;
    this.sourceYSubsampling = sourceYSubsampling;
    this.subsamplingXOffset = subsamplingXOffset;
    this.subsamplingYOffset = subsamplingYOffset;
  }
}