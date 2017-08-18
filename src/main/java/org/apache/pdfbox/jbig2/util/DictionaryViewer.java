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

package org.apache.pdfbox.jbig2.util;

import java.util.List;

import org.apache.pdfbox.jbig2.Bitmap;
import org.apache.pdfbox.jbig2.TestImage;
import org.apache.pdfbox.jbig2.image.Bitmaps;

/**
 * This class is for debug purpose only. The {@code DictionaryViewer} is able to show a single
 * bitmap or all symbol bitmaps.
 */
class DictionaryViewer {

  public static void show(Bitmap b) {
    new TestImage(Bitmaps.asBufferedImage(b));
  }

  public static void show(List<Bitmap> symbols) {
    int width = 0;
    int height = 0;

    for (Bitmap b : symbols) {
      width += b.getWidth();

      if (b.getHeight() > height) {
        height = b.getHeight();
      }
    }

    Bitmap result = new Bitmap(width, height);

    int xOffset = 0;

    for (Bitmap b : symbols) {
      Bitmaps.blit(b, result, xOffset, 0, CombinationOperator.REPLACE);
      xOffset += b.getWidth();
    }

    new TestImage(Bitmaps.asBufferedImage(result));
  }
}