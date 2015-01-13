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

package com.levigo.jbig2.segments;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;
import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.*;
import com.levigo.jbig2.segments.PageInformation;
import com.levigo.jbig2.util.CombinationOperator;

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
