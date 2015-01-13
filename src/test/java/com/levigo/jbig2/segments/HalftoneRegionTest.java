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
