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
