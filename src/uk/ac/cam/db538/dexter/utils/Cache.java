package uk.ac.cam.db538.dexter.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class Cache<K, V> {

  private final Map<K, V> cacheMap;

  public Cache() {
    cacheMap = new HashMap<K, V>();
  }

  public V getCachedEntry(K key) {
    V cachedVal = cacheMap.get(key);
    if (cachedVal != null)
      return cachedVal;

    V newValue = createNewEntry(key);
    cacheMap.put(key, newValue);
    return newValue;
  }

  public Set<Entry<K, V>> entrySet() {
    return cacheMap.entrySet();
  }

  protected abstract V createNewEntry(K key);

  public boolean contains(K key) {
    return cacheMap.containsKey(key);
  }
}
