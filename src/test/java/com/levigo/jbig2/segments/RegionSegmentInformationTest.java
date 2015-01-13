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

import com.levigo.jbig2.io.DefaultInputStreamFactory;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.segments.RegionSegmentInformation;
import com.levigo.jbig2.util.CombinationOperator;

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
