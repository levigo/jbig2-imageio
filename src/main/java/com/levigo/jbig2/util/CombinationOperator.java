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

package com.levigo.jbig2.util;

/**
 * This enumeration keeps the available logical operator defined in the JBIG2 ISO standard.
 */
public enum CombinationOperator {
  OR, AND, XOR, XNOR, REPLACE;

  public static CombinationOperator translateOperatorCodeToEnum(short combinationOperatorCode) {
    switch (combinationOperatorCode){
      case 0 :
        return OR;
      case 1 :
        return AND;
      case 2 :
        return XOR;
      case 3 :
        return XNOR;
      default :
        return REPLACE;
    }
  }
}
