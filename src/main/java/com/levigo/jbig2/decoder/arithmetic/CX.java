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

/**
 * CX represents the context used by arithmetic decoding and arithmetic integer decoding. It selects
 * the probability estimate and statistics used during decoding procedure.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
 */
public final class CX {
  private int index;

  private final byte cx[];
  private final byte mps[];

  /**
   * @param size - Amount of context values.
   * @param index - Start index.
   */
  public CX(int size, int index) {
    this.index = index;
    cx = new byte[size];
    mps = new byte[size];
  }

  protected int cx() {
    return cx[index] & 0x7f;
  }

  protected void setCx(int value) {
    cx[index] = (byte) (value & 0x7f);
  }

  /**
   * @return The decision. Possible values are {@code 0} or {@code 1}.
   */
  protected byte mps() {
    return mps[index];
  }

  /**
   * Flips the bit in actual "more predictable symbol" array element.
   */
  protected void toggleMps() {
    mps[index] ^= 1;
  }

  protected int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}
