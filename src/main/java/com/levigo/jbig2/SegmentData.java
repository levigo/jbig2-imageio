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

package com.levigo.jbig2;

import java.io.IOException;

import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.io.SubInputStream;

/**
 * Interface for all data parts of segments.
 */
public interface SegmentData {

  /**
   * Parse the stream and read information of header.
   * 
   * @param header - The segments' header (to make referred-to segments available in data part).
   * @param sis - Wrapped {@code ImageInputStream} into {@code SubInputStream}.
   * 
   * @throws InvalidHeaderValueException
   * @throws IntegerMaxValueException
   * @throws IOException
   */
  public void init(SegmentHeader header, SubInputStream sis) throws InvalidHeaderValueException, IntegerMaxValueException,
      IOException;
}
