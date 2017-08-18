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

package com.levigo.jbig2.err;

/**
 * Can be used if the maximum value limit of an integer is exceeded.
 */
public class IntegerMaxValueException extends JBIG2Exception {

  private static final long serialVersionUID = -5534202639860867867L;

  public IntegerMaxValueException() {
  }

  public IntegerMaxValueException(String message) {
    super(message);
  }

  public IntegerMaxValueException(Throwable cause) {
    super(cause);
  }

  public IntegerMaxValueException(String message, Throwable cause) {
    super(message, cause);
  }

}
