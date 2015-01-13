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
