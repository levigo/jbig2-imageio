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

import java.util.HashMap;
import java.util.Map;

import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;


/**
 * This class stores segments, that aren't associated to a page.
 * 
 * If the data is embedded in another format, for example PDF, this segments might be stored
 * separately in the file.
 * 
 * This segments will be decoded on demand and all results are stored in the document object and can
 * be retrieved from there.
 */
public class JBIG2Globals {
  private static final Logger log = LoggerFactory.getLogger(JBIG2Globals.class);

  /**
   * This map contains all segments, that are not associated with a page. The key is the segment
   * number.
   */
  private Map<Integer, SegmentHeader> globalSegments = new HashMap<Integer, SegmentHeader>();

  protected SegmentHeader getSegment(int segmentNr) {
    if (globalSegments.size() == 0) {
      if (log.isErrorEnabled()) {
        log.error("No global segment added so far. Use JBIG2ImageReader.setGlobals().");
      }
    }

    return globalSegments.get(segmentNr);
  }

  protected void addSegment(Integer segmentNumber, SegmentHeader segment) {
    globalSegments.put(segmentNumber, segment);
  }

}
