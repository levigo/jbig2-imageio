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

import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.DefaultInputStreamFactory;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.CombinationOperator;

public class HalftoneRegionTest {

  @Test
  public void parseHeaderTest() throws IOException, InvalidHeaderValueException {
    InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
    DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
    ImageInputStream iis = disf.getInputStream(is);
    // Seventh Segment (number 6)
    SubInputStream sis = new SubInputStream(iis, 302, 87);
    HalftoneRegion hr = new HalftoneRegion(sis);
    hr.init(null, sis);

    Assert.assertEquals(true, hr.isMMREncoded());
    Assert.assertEquals(0, hr.getHTemplate());
    Assert.assertEquals(false, hr.isHSkipEnabled());
    Assert.assertEquals(CombinationOperator.OR, hr.getCombinationOperator());
    Assert.assertEquals(0, hr.getHDefaultPixel());

    Assert.assertEquals(8, hr.getHGridWidth());
    Assert.assertEquals(9, hr.getHGridHeight());
    Assert.assertEquals(0, hr.getHGridX());
    Assert.assertEquals(0, hr.getHGridY());
    Assert.assertEquals(1024, hr.getHRegionX());
    Assert.assertEquals(0, hr.getHRegionY());

  }

}
