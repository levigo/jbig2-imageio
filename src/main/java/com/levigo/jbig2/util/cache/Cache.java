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

package com.levigo.jbig2.util.cache;

public interface Cache {

/**
 * 
 * @param key
 * @param value
 * @param sizeEstimate
 * 
 * @return the old object, that was replaced if present. Otherwise {@code null}.
 */
  Object put(Object key, Object value, int sizeEstimate);

  Object get(Object key);

  /**
   * Removes all mappings from a map (optional operation).
   * 
   * @throws UnsupportedOperationException if {@code clear()} is not supported by the map.
   */
  void clear();

  /**
   * 
   * @param key
   * @return the removed object, if present. Otherwise {@code null}.
   */
  Object remove(Object key);
}