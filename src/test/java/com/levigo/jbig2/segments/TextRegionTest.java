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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.levigo.jbig2.JBIG2ImageReader;
import com.levigo.jbig2.JBIG2ImageReaderSpi;
import com.levigo.jbig2.TestImage;
import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.DefaultInputStreamFactory;

public class TextRegionTest {

  // TESTS WITH TESTOUTPUT
  // Ignore for in build process

  @Ignore
  @Test
  public void textRegionWith() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    String filepath = "/images/042_11.jb2";
    int pageNumber = 1;

    InputStream is = getClass().getResourceAsStream(filepath);
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    JBIG2ImageReader jb2 = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
    jb2.setInput(iis);
    BufferedImage b = jb2.read(pageNumber);
    new TestImage(b);
  }
}
