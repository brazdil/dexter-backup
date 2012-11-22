package uk.ac.cam.db538.dexter.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Getter;

public abstract class Cache<K, V> {

  @Getter private final Map<K, V> CacheMap;

  public Cache() {
    CacheMap = new HashMap<K, V>();
  }

  public V getCachedEntry(K key) {
    V cachedVal = CacheMap.get(key);
    if (cachedVal != null)
      return cachedVal;

    V newValue = createNewEntry(key);
    CacheMap.put(key, newValue);
    return newValue;
  }

  public Set<Entry<K, V>> entrySet() {
    return CacheMap.entrySet();
  }

  protected abstract V createNewEntry(K key);

  public boolean contains(K key) {
    return CacheMap.containsKey(key);
  }
}
