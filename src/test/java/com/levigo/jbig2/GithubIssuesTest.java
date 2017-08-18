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

import static com.levigo.jbig2.ChecksumCalculator.*;
import static com.levigo.jbig2.JBIG2DocumentFacade.*;

import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Collection of tests for <a href="https://github.com/levigo/jbig2-imageio/issues">Github
 * issues</a>.
 */
public class GithubIssuesTest {

  /**
   * <a href="https://github.com/levigo/jbig2-imageio/issues/21">Github issue 21s</a>
   */
  @Test
  public void issue21() throws Exception {
    final byte[] md5Expected = new byte[]{
        83, 74, -69, -60, -122, -99, 21, 126, -115, 13, 9, 107, -31, -109, 77, -119
    };

    final InputStream imageStream = getClass().getResourceAsStream("/com/levigo/jbig2/github/21.jb2");
    final InputStream globalsStream = getClass().getResourceAsStream("/com/levigo/jbig2/github/21.glob");
    final ImageInputStream globalsIIS = ImageIO.createImageInputStream(globalsStream);
    final ImageInputStream imageIIS = ImageIO.createImageInputStream(imageStream);

    byte[] md5Actual = null;
    try {
      final JBIG2Document doc = doc(imageIIS, globalsIIS);
      final JBIG2Page page = doc.getPage(1);
      final Bitmap bitmap = page.getBitmap();
      md5Actual = md5(bitmap);
    } finally {
      Assert.assertArrayEquals(md5Expected, md5Actual);

      globalsIIS.close();
      globalsStream.close();
      imageIIS.close();
      imageStream.close();
    }

  }
}
