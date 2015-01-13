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

import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;

public class ServiceLookup<B> {

  public Iterator<B> getServices(Class<B> cls) {
    return getServices(cls, null);
  }

  public Iterator<B> getServices(Class<B> cls, ClassLoader clsLoader) {
    Iterator<B> services = ServiceRegistry.lookupProviders(cls);

    if (!services.hasNext()) {
      services = ServiceRegistry.lookupProviders(cls, cls.getClass().getClassLoader());
    }

    if (!services.hasNext() && clsLoader != null) {
      services = ServiceRegistry.lookupProviders(cls, clsLoader);
    }

    return services;
  }

}
