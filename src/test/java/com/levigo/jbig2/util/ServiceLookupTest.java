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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

public class ServiceLookupTest {

  @Test
  public void withDefaultClassLoader() {
    runTest(null);
  }

  @Test
  public void withContextClassLoader() {
    runTest(Thread.currentThread().getContextClassLoader());
  }

  @Test
  public void withClassLoaderFromClass() {
    runTest(TestService.class.getClassLoader());
  }
  
  private void runTest(ClassLoader clsLoader) {
    ServiceLookup<TestService> serviceLookup = new ServiceLookup<TestService>();

    Iterator<TestService> services = clsLoader != null
        ? serviceLookup.getServices(TestService.class, clsLoader)
        : serviceLookup.getServices(TestService.class);

    assertTrue(services.hasNext());
    assertEquals(TestServiceImpl.class, services.next().getClass());
  }

}
