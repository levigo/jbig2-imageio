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

package org.apache.pdfbox.jbig2.util;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ServiceLookup<B> {

  public Iterator<B> getServices(Class<B> cls) {
    return getServices(cls, null);
  }

  public Iterator<B> getServices(Class<B> cls, ClassLoader clsLoader) {
    Iterator<B> services = ServiceLoader.load(cls).iterator();

    if (!services.hasNext()) {
      services = ServiceLoader.load(cls, cls.getClass().getClassLoader()).iterator();
    }

    if (!services.hasNext() && clsLoader != null) {
      services = ServiceLoader.load(cls, clsLoader).iterator();
    }

    return services;
  }

}
