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

package org.apache.pdfbox.jbig2;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.imageio.ImageReadParam;

/**
 * This class extends {@code ImageReadParam} and contains region of interest and scale / subsampling
 * functionality
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
