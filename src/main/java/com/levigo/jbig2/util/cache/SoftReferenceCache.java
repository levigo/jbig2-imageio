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

import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 */
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
