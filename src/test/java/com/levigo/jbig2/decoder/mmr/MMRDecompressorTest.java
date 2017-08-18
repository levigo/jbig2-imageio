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

package com.levigo.jbig2.decoder.mmr;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import org.junit.Test;

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.DefaultInputStreamFactory;
import com.levigo.jbig2.io.SubInputStream;

public class MMRDecompressorTest {

  @Test
  public void mmrDecodingTest() throws IOException, InvalidHeaderValueException {
    final byte[] expected = new byte[]{
        0, 0, 2, 34, 38, 102, -17, -1, 2, 102, 102, //
        -18, -18, -17, -1, -1, 0, 2, 102, 102, 127, //
        -1, -1, -1, 0, 0, 0, 4, 68, 102, 102, 127
    };

    final InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    final DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    final ImageInputStream iis = disf.getInputStream(is);

    // Sixth Segment (number 5)
    final SubInputStream sis = new SubInputStream(iis, 252, 38);

    final MMRDecompressor mmrd = new MMRDecompressor(16 * 4, 4, sis);

    final Bitmap b = mmrd.uncompress();
    final byte[] actual = b.getByteArray();

    assertArrayEquals(expected, actual);

    // new TestImage(b.getByteArray(), (int) b.getWidth(), (int) b.getHeight(), b.getRowStride());
  }
}
