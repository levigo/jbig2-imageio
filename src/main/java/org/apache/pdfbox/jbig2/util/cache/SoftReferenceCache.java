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

package org.apache.pdfbox.jbig2.util.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class SoftReferenceCache implements Cache {

  private HashMap<Object, SoftReference<?>> cache = new HashMap<Object, SoftReference<?>>();

  public Object put(Object key, Object value, int sizeEstimate) {
    SoftReference<Object> softReference = new SoftReference<Object>(value);
    SoftReference<?> oldValue = cache.put(key, softReference);
    return getValueNullSafe(oldValue);
  }

  public Object get(Object key) {
    SoftReference<?> softReference = cache.get(key);
    return getValueNullSafe(softReference);
  }

  public void clear() {
    cache.clear();
  }

  public Object remove(Object key) {
    SoftReference<?> removedObj = cache.remove(key);
    return getValueNullSafe(removedObj);
  }

  private Object getValueNullSafe(SoftReference<?> softReference) {
    return softReference == null ? null : softReference.get();
  }
}
