/**
 * Copyright (C) 1995-2015 levigo holding gmbh.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
