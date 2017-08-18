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

import java.util.Iterator;

import com.levigo.jbig2.util.ServiceLookup;

/**
 * Retrieves a {@link Cache} via registered {@link CacheBridge} through
 * <code>META-INF/services</code> lookup.
 */
public class CacheFactory {

  private static CacheBridge cacheBridge;

  private static ClassLoader clsLoader;

  public static Cache getCache(ClassLoader clsLoader) {
    if (null == cacheBridge) {
      final ServiceLookup<CacheBridge> serviceLookup = new ServiceLookup<CacheBridge>();
      final Iterator<CacheBridge> cacheBridgeServices = serviceLookup.getServices(CacheBridge.class, clsLoader);

      if (!cacheBridgeServices.hasNext()) {
        throw new IllegalStateException("No implementation of " + CacheBridge.class
            + " was avaliable using META-INF/services lookup");
      }
      cacheBridge = cacheBridgeServices.next();
    }
    return cacheBridge.getCache();
  }

  public static Cache getCache() {
    return getCache(clsLoader != null ? clsLoader : CacheBridge.class.getClassLoader());
  }

  public static void setClassLoader(ClassLoader clsLoader) {
    CacheFactory.clsLoader = clsLoader;
  }
}
