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

package org.apache.pdfbox.jbig2.segments;

import org.apache.pdfbox.jbig2.Bitmap;

public class HalftonecomputeGrayScaleValues {

    public  int[][] computeGrayScaleValuesNew(final Bitmap[] grayScalePlanes, final int bitsPerValue, int hGridHeight, int hGridWidth ) {
        // Gray-scale decoding procedure, page 98
        final int[][] grayScaleValues = new int[hGridHeight][hGridWidth];

        // 4)
        for (int y = 0; y < hGridHeight; y++) {
            for (int x = 0; x < hGridWidth; x += 8) {
                final int minorWidth = hGridWidth - x > 8 ? 8 : hGridWidth - x;
                int byteIndex = grayScalePlanes[0].getByteIndex(x, y);

                for (int minorX = 0; minorX < minorWidth; minorX++) {
                    final int i = minorX + x;
                    grayScaleValues[y][i] = 0;

                    for (int j = 0; j < bitsPerValue; j++) {
                        grayScaleValues[y][i] += ((grayScalePlanes[j].getByte(byteIndex) >> (7 - i & 7)) & 1) * (1 << j);
                    }
                }
            }
        }
        return grayScaleValues;
    }

}
