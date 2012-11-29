package uk.ac.cam.db538.dexter.merge;

import java.lang.ref.WeakReference;

// TODO: add synchronized !!!

public class ObjectTaintStorage {
  private static Entry[] H; // hash table
  private static int S; // size of the hash table
  private static int M; // module mask

  static {
    init(1024);
  }

  static final void init(int size) {
    S = size;
    M = S - 1;
    H = new Entry[S];
  }

  public static final int get(Object obj) {
    if (obj == null)
      return 0;

    // generate hash code and table index
    int objTableIndex = obj.getClass().hashCode() & M;

//    synchronized (H) {
    Entry currentEntry = H[objTableIndex];
    Entry previousEntry = null;
    while (currentEntry != null) {
      // retrieve reference from the entry
      Object entryObj = currentEntry.o.get();

      if (entryObj == null) {
        // it has been GCed
        // remove it from the entry list
        if (previousEntry == null)
          H[objTableIndex] = currentEntry.n;
        else
          previousEntry.n = currentEntry.n;

        // don't update previousEntry, just move to the next one
        currentEntry = currentEntry.n;
      } else if (entryObj == obj) {
        // found it, move to front and return

        // move it to the beginning of the list (temporal locality)
        if (previousEntry != null) {
          previousEntry.n = currentEntry.n;
          currentEntry.n = H[objTableIndex];
          H[objTableIndex] = currentEntry;
        }

        return currentEntry.t;
      } else {
        // move to another entry
        previousEntry = currentEntry;
        currentEntry = currentEntry.n;
      }
    }
//    }

    return 0;
  }

  public static final void set(Object obj, int taint) {
    if (obj == null)
      return;

    // generate hash code and table index
    int objTableIndex = obj.getClass().hashCode() & M;

//    synchronized (H) {
    // try to update existing entry
    Entry currentEntry = H[objTableIndex];
    Entry previousEntry = null;
    while (currentEntry != null) {
      // retrieve reference from the entry
      Object entryObj = currentEntry.o.get();

      if (entryObj == null) {
        // it has been GCed
        // remove it from the entry list
        if (previousEntry == null)
          H[objTableIndex] = currentEntry.n;
        else
          previousEntry.n = currentEntry.n;

        // don't update previousEntry, just move to the next one
        currentEntry = currentEntry.n;
      } else if (entryObj == obj) {
        // found it, update
        currentEntry.t |= taint;

        // move it to the beginning of the list (temporal locality)
        if (previousEntry != null) {
          previousEntry.n = currentEntry.n;
          currentEntry.n = H[objTableIndex];
          H[objTableIndex] = currentEntry;
        }

        // stop searching
        break;
      } else {
        // move to next entry
        previousEntry = currentEntry;
        currentEntry = currentEntry.n;
      }
    }

    if (currentEntry == null) {
      // object not in the map
      // create new entry and put it at the beginning of the list
      Entry newEntry = new Entry();
      newEntry.o = new WeakReference<Object>(obj);
      newEntry.t = taint;
      newEntry.n = H[objTableIndex];
      H[objTableIndex] = newEntry;
    }
//    }
  }

  static class Entry {
    public WeakReference<Object> o; // object
    public int t; // taint
    public Entry n; // next
  }
}
