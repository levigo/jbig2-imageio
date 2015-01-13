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

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

public class BitmapTest {

  @Test
  public void getPixelAndSetPixelTest() {
    final Bitmap bitmap = new Bitmap(37, 49);
    assertEquals(0, bitmap.getPixel(3, 19));
    
    bitmap.setPixel(3, 19, (byte) 1);

    assertEquals(1, bitmap.getPixel(3, 19));
  }

  @Test
  public void getByteAndSetByteTest() {
    Bitmap bitmap = new Bitmap(16, 16);

    byte value = (byte) 4;
    bitmap.setByte(0, value);
    bitmap.setByte(31, value);

    assertEquals(value, bitmap.getByte(0));
    assertEquals(value, bitmap.getByte(31));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void getByteThrowsExceptionTest() {
    Bitmap bitmap = new Bitmap(16, 16);
    bitmap.getByte(32);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void setByteThrowsExceptionTest() {
    Bitmap bitmap = new Bitmap(16, 16);
    bitmap.setByte(32, (byte) 0);
  }

  @Test
  public void getByteAsIntegerTest() {
    Bitmap bitmap = new Bitmap(16, 16);

    byte byteValue = (byte) 4;
    int integerValue = byteValue;
    bitmap.setByte(0, byteValue);
    bitmap.setByte(31, byteValue);

    Assert.assertEquals(integerValue, bitmap.getByteAsInteger(0));
    Assert.assertEquals(integerValue, bitmap.getByteAsInteger(31));

  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void getByteAsIntegerThrowsExceptionTest() {
    Bitmap bitmap = new Bitmap(16, 16);
    bitmap.getByte(32);
  }

  @Test
  public void getHeightTest() {
    int height = 16;
    Bitmap bitmap = new Bitmap(1, height);
    Assert.assertEquals(height, bitmap.getHeight());
  }

  @Test
  public void getWidthTest() {
    int width = 16;
    Bitmap bitmap = new Bitmap(width, 1);
    Assert.assertEquals(width, bitmap.getWidth());
  }

}
