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

package com.levigo.jbig2.decoder.arithmetic;

import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.levigo.jbig2.io.DefaultInputStreamFactory;

public class ArithmeticIntegerDecoderTest {

  @Test
  public void decodeTest() throws Throwable {
    InputStream is = getClass().getResourceAsStream("/images/arith/encoded testsequence");
    DefaultInputStreamFactory isFactory = new DefaultInputStreamFactory();
    ImageInputStream iis = isFactory.getInputStream(is);

    ArithmeticDecoder ad = new ArithmeticDecoder(iis);
    ArithmeticIntegerDecoder aid = new ArithmeticIntegerDecoder(ad);

    long result = aid.decode(null);

    Assert.assertEquals(1, result);
  }
}
