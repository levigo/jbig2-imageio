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

import java.awt.Rectangle;

/**
 * This class represents a bi-level image that is organized like a bitmap.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
public class Bitmap {


  /** The height of the bitmap in pixels. */
  private final int height;

  /** The width of the bitmap in pixels. */
  private final int width;

  /** The amount of bytes used per row. */
  private final int rowStride;

  /** 8 pixels per byte, 0 for white, 1 for black */
  private byte[] bitmap;

  /**
   * Creates an instance of a blank image.<br>
   * The image data is stored in a byte array. Each pixels is stored as one bit, so that each byte
   * contains 8 pixel. A pixel has by default the value {@code 0} for white and {@code 1} for black. <br>
   * Row stride means the amount of bytes per line. It is computed automatically and fills the pad
   * bits with 0.<br>
   * 
   * @param height - The real height of the bitmap in pixels.
   * @param width - The real width of the bitmap in pixels.
   */
  public Bitmap(int width, int height) {
    this.height = height;
    this.width = width;
    this.rowStride = (width + 7) >> 3;

    bitmap = new byte[this.height * this.rowStride];
  }

  /**
   * Returns the value of a pixel specified by the given coordinates.
   * <p>
   * By default, the value is {@code 0} for a white pixel and {@code 1} for a black pixel. The value
   * is placed in the rightmost bit in the byte.
   * 
   * @param x - The x coordinate of the pixel.
   * @param y - The y coordinate of the pixel.
   * @return The value of a pixel.
   */
  public byte getPixel(int x, int y) {
    int byteIndex = this.getByteIndex(x, y);
    int bitOffset = this.getBitOffset(x);

    int toShift = 7 - bitOffset;
    return (byte) ((this.getByte(byteIndex) >> toShift) & 0x01);
  }
  
  public void setPixel(int x, int y, byte pixelValue) {
    final int byteIndex = getByteIndex(x, y);
    final int bitOffset = getBitOffset(x);
    
    final int shift = 7 - bitOffset;
    
    final byte src = bitmap[byteIndex];
    final byte result = (byte) (src | (pixelValue << shift));
    bitmap[byteIndex] = result;
  }

  /**
   * 
   * <p>
   * Returns the index of the byte that contains the pixel, specified by the pixel's x and y
   * coordinates.
   * 
   * @param x - The pixel's x coordinate.
   * @param y - The pixel's y coordinate.
   * @return The index of the byte that contains the specified pixel.
   */
  public int getByteIndex(int x, int y) {
    return y * this.rowStride + (x >> 3);
  }

  /**
   * Simply returns the byte array of this bitmap.
   * 
   * @return The byte array of this bitmap.
   */
  public byte[] getByteArray() {
    return bitmap;
  }

  /**
   * Simply returns a byte from the bitmap byte array. Throws an {@link IndexOutOfBoundsException}
   * if the given index is out of bound.
   * 
   * @param index - The array index that specifies the position of the wanted byte.
   * @return The byte at the {@code index}-position.
   * 
   * @throws IndexOutOfBoundsException if the index is out of bound.
   */
  public byte getByte(int index) {
    return this.bitmap[index];
  }

  /**
   * Simply sets the given value at the given array index position. Throws an
   * {@link IndexOutOfBoundsException} if the given index is out of bound.
   * 
   * @param index - The array index that specifies the position of a byte.
   * @param value - The byte that should be set.
   * 
   * @throws IndexOutOfBoundsException if the index is out of bound.
   */
  public void setByte(int index, byte value) {
    this.bitmap[index] = value;
  }

  /**
   * Converts the byte at specified index into an integer and returns the value. Throws an
   * {@link IndexOutOfBoundsException} if the given index is out of bound.
   * 
   * @param index - The array index that specifies the position of the wanted byte.
   * @return The converted byte at the {@code index}-position as an integer.
   * 
   * @throws IndexOutOfBoundsException if the index is out of bound.
   */
  public int getByteAsInteger(int index) {
    return (this.bitmap[index] & 0xff);
  }


  /**
   * Computes the offset of the given x coordinate in its byte. The method uses optimized modulo
   * operation for a better performance.
   * 
   * @param x - The x coordinate of a pixel.
   * @return The bit offset of a pixel in its byte.
   */
  public int getBitOffset(int x) {
    // The same like x % 8.
    // The rightmost three bits are 1. The value masks all bits upon the value "7".
    return (x & 0x07);
  }

  /**
   * Simply returns the height of this bitmap.
   * 
   * @return The height of this bitmap.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Simply returns the width of this bitmap.
   * 
   * @return The width of this bitmap.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Simply returns the row stride of this bitmap. <br>
   * (Row stride means the amount of bytes per line.)
   * 
   * @return The row stride of this bitmap.
   */
  public int getRowStride() {
    return rowStride;
  }

  public Rectangle getBounds() {
    return new Rectangle(0, 0, width, height);
  }

  public int getMemorySize() {
    return bitmap.length;
  }
}
