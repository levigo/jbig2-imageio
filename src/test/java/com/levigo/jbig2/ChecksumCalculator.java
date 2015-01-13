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
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.stream.ImageInputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.levigo.jbig2.err.JBIG2Exception;
import com.levigo.jbig2.image.Bitmaps;
import com.levigo.jbig2.io.DefaultInputStreamFactory;

@Ignore
public class ChecksumCalculator {

  @Ignore
  @Test
  public void computeChecksum() throws NoSuchAlgorithmException, IOException, JBIG2Exception {
    String filepath = "/images/sampledata_page3.jb2";
    int pageNumber = 1;

    InputStream is = getClass().getResourceAsStream(filepath);
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    JBIG2Document doc = new JBIG2Document(iis);
    Bitmap b = doc.getPage(pageNumber).getBitmap();
    
    MessageDigest md = MessageDigest.getInstance("MD5");

    byte[] digest = md.digest(b.getByteArray());
    for (byte d : digest) {
      System.out.print(d);
    }
  }
}
