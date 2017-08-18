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

package org.apache.pdfbox.jbig2.segments;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.apache.pdfbox.jbig2.io.DefaultInputStreamFactory;
import org.apache.pdfbox.jbig2.io.SubInputStream;
import org.apache.pdfbox.jbig2.segments.RegionSegmentInformation;
import org.apache.pdfbox.jbig2.util.CombinationOperator;
import org.junit.Ignore;
import org.junit.Test;

public class RegionSegmentInformationTest {

  @Test
  public void parseHeaderTest() throws IOException {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    SubInputStream sis = new SubInputStream(iis, 130, 49);
    RegionSegmentInformation rsi = new RegionSegmentInformation(sis);
    rsi.parseHeader();
    Assert.assertEquals(37, rsi.getBitmapWidth());
    Assert.assertEquals(8, rsi.getBitmapHeight());
    Assert.assertEquals(4, rsi.getXLocation());
    Assert.assertEquals(1, rsi.getYLocation());
    Assert.assertEquals(CombinationOperator.OR, rsi.getCombinationOperator());
  }
}
