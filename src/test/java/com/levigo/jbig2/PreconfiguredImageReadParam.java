/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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