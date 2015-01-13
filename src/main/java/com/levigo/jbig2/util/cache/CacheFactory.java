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

package com.levigo.jbig2.util.cache;

import java.util.Iterator;

import com.levigo.jbig2.util.ServiceLookup;

/**
 * Retrieves a {@link Cache} via registered {@link CacheBridge} through
 * <code>META-INF/services</code> lookup.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
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
