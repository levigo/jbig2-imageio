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

import org.apache.pdfbox.jbig2.err.InvalidHeaderValueException;
import org.apache.pdfbox.jbig2.io.*;
import org.apache.pdfbox.jbig2.segments.PageInformation;
import org.apache.pdfbox.jbig2.util.CombinationOperator;
import org.junit.Ignore;
import org.junit.Test;

public class PageInformationTest {

	@Test
	public void parseHeaderCompleteTest() throws IOException,
			InvalidHeaderValueException {
		InputStream is = getClass().getResourceAsStream("/images/sampledata.jb2");
		DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
		ImageInputStream iis = disf.getInputStream(is);
		// Second Segment (number 1)
		SubInputStream sis = new SubInputStream(iis, 59, 19);
		PageInformation pi = new PageInformation();
		pi.init(null, sis);
		Assert.assertEquals(64, pi.getBitmapWidth());
		Assert.assertEquals(56, pi.getBitmapHeight());
		Assert.assertEquals(0, pi.getResolutionX());
		Assert.assertEquals(0, pi.getResolutionY());
		Assert.assertEquals(true, pi.isLossless());
		Assert.assertEquals(false, pi.mightContainRefinements());
		Assert.assertEquals(0, pi.getDefaultPixelValue());
		Assert.assertEquals(CombinationOperator.OR, pi.getCombinationOperator());
		Assert.assertEquals(false, pi.isAuxiliaryBufferRequired());
		Assert.assertEquals(false, pi.isCombinationOperatorOverrideAllowed());
		Assert.assertEquals(false, pi.isStriped());
		Assert.assertEquals(0, pi.getMaxStripeSize());
	}

	@Ignore
	@Test
	public void parseHeaderXOROperatorTest() throws IOException,
			InvalidHeaderValueException {
		InputStream is = getClass().getResourceAsStream(
				"/sampledata_pageinformation_with_xor-opartor.jb2");
		DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
		ImageInputStream iis = disf.getInputStream(is);
		// Second Segment (number 1)
		SubInputStream sis = new SubInputStream(iis, 59, 19);
		PageInformation pi = new PageInformation();
		pi.init(null, sis);
		// XOR (2) als Operator erwartet
		Assert.assertEquals(2, pi.getCombinationOperator());
	}

	@Ignore
	@Test
	public void parseHeaderANDOperatorTest() throws IOException,
			InvalidHeaderValueException {
		InputStream is = getClass().getResourceAsStream(
				"/sampledata_pageinformation_with_and-opartor.jb2");
		DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
		ImageInputStream iis = disf.getInputStream(is);
		// Second Segment (number 1)
		SubInputStream sis = new SubInputStream(iis, 59, 19);
		PageInformation pi = new PageInformation();
		pi.init(null, sis);
		Assert.assertEquals(true, pi.isLossless());
		// AND (1) als Operator erwartet
		Assert.assertEquals(1, pi.getCombinationOperator());
	}
}
