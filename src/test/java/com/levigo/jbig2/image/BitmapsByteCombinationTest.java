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

package com.levigo.jbig2.image;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.levigo.jbig2.util.CombinationOperator;

@RunWith(Parameterized.class)
public class BitmapsByteCombinationTest {

  private static final byte value1 = 0xA;
  private static final byte value2 = 0xD;

  private final int expected;
  private final CombinationOperator operator;

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            0xF, CombinationOperator.OR
        }, {
            0x8, CombinationOperator.AND
        }, {
            0x7, CombinationOperator.XOR
        }, {
            -8, CombinationOperator.XNOR
        }, {
            value2, CombinationOperator.REPLACE
        }
    });
  }

  public BitmapsByteCombinationTest(final int expected, final CombinationOperator operator) {
    this.expected = expected;
    this.operator = operator;
  }


  @Test
  public void test() {
    assertEquals(expected, Bitmaps.combineBytes(value1, value2, operator));
  }

}
