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

package com.levigo.jbig2.segments;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import com.levigo.jbig2.*;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.*;
import com.levigo.jbig2.segments.*;

public class PatternDictionaryTest {
  @Test
  public void parseHeaderTest() throws IOException, InvalidHeaderValueException {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Sixth Segment (number 5)
    SubInputStream sis = new SubInputStream(iis, 245, 45);
    PatternDictionary pd = new PatternDictionary();
    pd.init(null, sis);
    Assert.assertEquals(true, pd.isMMREncoded());
    Assert.assertEquals(0, pd.getHdTemplate());
    Assert.assertEquals(4, pd.getHdpWidth());
    Assert.assertEquals(4, pd.getHdpHeight());
    Assert.assertEquals(15, pd.getGrayMax());
  }

  // TESTS WITH TESTOUTPUT
  // Ignore in build process

  @Ignore
  @Test
  public void decodeTestWithOutput() throws Throwable {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Sixth Segment (number 5)
    SubInputStream sis = new SubInputStream(iis, 245, 45);

    PatternDictionary pd = new PatternDictionary();
    pd.init(null, sis);

    ArrayList<Bitmap> b = pd.getDictionary();

    int i = 5;
    // for (int i = 0; i < 8; i++) {
    new TestImage(b.get(i).getByteArray(), (int) b.get(i).getWidth(), (int) b.get(i).getHeight(),
        b.get(i).getRowStride());
    // }
  }

  @Ignore
  @Test
  public void decodeTestWithOutput2() throws Throwable {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Twelfth Segment (number 12)
    SubInputStream sis = new SubInputStream(iis, 569, 28);

    PatternDictionary pd = new PatternDictionary();
    pd.init(null, sis);

    ArrayList<Bitmap> b = pd.getDictionary();

    int i = 2;
    // for (int i = 0; i < 8; i++) {
    new TestImage(b.get(i).getByteArray(), (int) b.get(i).getWidth(), (int) b.get(i).getHeight(),
        b.get(i).getRowStride());
    // }
  }
}
