package com.levigo.jbig2;

import static com.levigo.jbig2.ChecksumCalculator.md5;
import static com.levigo.jbig2.JBIG2DocumentFacade.doc;

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
        -79, 69, 103, 64, 59, 120, -74, 117, -96, -86, -23, 36, -122, 113, 101, -99
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
