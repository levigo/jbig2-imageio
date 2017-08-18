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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.imageio.stream.ImageInputStream;

import org.apache.pdfbox.jbig2.Bitmap;
import org.apache.pdfbox.jbig2.JBIG2Document;
import org.apache.pdfbox.jbig2.err.JBIG2Exception;
import org.apache.pdfbox.jbig2.io.DefaultInputStreamFactory;
import org.junit.Ignore;
import org.junit.Test;

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
    Bitmap bitmap = doc.getPage(pageNumber).getBitmap();

    byte[] md5 = md5(bitmap);
    for (byte b : md5) {
      System.out.print(b);
    }
    System.out.println(Arrays.toString(md5));
  }

  public static byte[] md5(Bitmap b) throws NoSuchAlgorithmException {
    return MessageDigest.getInstance("MD5").digest(b.getByteArray());
  }
}
