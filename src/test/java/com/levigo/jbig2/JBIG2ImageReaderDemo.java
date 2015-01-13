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

import com.levigo.jbig2.err.JBIG2Exception;
import com.levigo.jbig2.image.Bitmaps;
import com.levigo.jbig2.image.FilterType;
import com.levigo.jbig2.io.DefaultInputStreamFactory;

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
