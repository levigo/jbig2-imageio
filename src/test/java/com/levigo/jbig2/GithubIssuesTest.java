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
