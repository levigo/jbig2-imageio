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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;

import org.apache.pdfbox.jbig2.Bitmap;
import org.apache.pdfbox.jbig2.JBIG2Document;
import org.apache.pdfbox.jbig2.JBIG2ImageReader;
import org.apache.pdfbox.jbig2.JBIG2ImageReaderSpi;
import org.apache.pdfbox.jbig2.TestImage;
import org.apache.pdfbox.jbig2.err.JBIG2Exception;
import org.apache.pdfbox.jbig2.image.Bitmaps;
import org.apache.pdfbox.jbig2.image.FilterType;
import org.apache.pdfbox.jbig2.io.DefaultInputStreamFactory;

public class JBIG2ImageReaderDemo {

  private String filepath;
  private int imageIndex;

  public JBIG2ImageReaderDemo(String filepath, int imageIndex) {
    this.filepath = filepath;
    this.imageIndex = imageIndex;
  }

  public void show() throws IOException, JBIG2Exception {
    InputStream inputStream = new FileInputStream(new File(filepath));
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream imageInputStream = disf.getInputStream(inputStream);

    JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());

    imageReader.setInput(imageInputStream);
    ImageReadParam param = new PreconfiguredImageReadParam(new Rectangle(100, 100, 500, 500));

    long timeStamp = System.currentTimeMillis();

    final JBIG2Document doc = new JBIG2Document(imageInputStream);
    final Bitmap bitmap = doc.getPage(imageIndex).getBitmap();
    final BufferedImage bufferedImage = Bitmaps.asBufferedImage(bitmap, param, FilterType.Lanczos);
    long duration = System.currentTimeMillis() - timeStamp;
    System.out.println(filepath + " decoding took " + duration + " ms");

    new TestImage(bufferedImage);
  }

  public static void main(String[] args) throws InterruptedException, InvocationTargetException, IOException,
      JBIG2Exception {
    URL imageUrl = JBIG2ImageReaderDemo.class.getResource("/images/042_1.jb2");
    new JBIG2ImageReaderDemo(imageUrl.getPath(), 1).show();
  }

}
