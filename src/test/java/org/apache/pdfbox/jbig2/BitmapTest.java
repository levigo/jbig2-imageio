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

package org.apache.pdfbox.jbig2;

import static org.junit.Assert.*;

import org.apache.pdfbox.jbig2.Bitmap;

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
