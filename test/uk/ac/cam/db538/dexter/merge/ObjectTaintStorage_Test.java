package uk.ac.cam.db538.dexter.merge;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import org.junit.Test;
import static org.junit.Assert.*;

import uk.ac.cam.db538.dexter.merge.ObjectTaintStorage.Entry;

public class ObjectTaintStorage_Test {

  private static Entry[] getMap() {
    try {
      Field fH = ObjectTaintStorage.class.getDeclaredField("H");
      fH.setAccessible(true);
      return (Entry[]) fH.get(null);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Couldn't get the internal H field of ObjectTaintStorage");
      return null;
    }
  }

  private static void forceGC() {
    Object obj = new Object();
    WeakReference<Object> ref = new WeakReference<Object>(obj);
    obj = null;
    while(ref.get() != null) {
      System.gc();
    }
  }

  @Test
  public void testInit() {
    ObjectTaintStorage.init(16);
    Entry[] H = getMap();
    assertEquals(16, H.length);
  }

  @Test
  public void testSet_Null() {
    ObjectTaintStorage.init(16);
    ObjectTaintStorage.set(null, 1);

    // expect nothing set

    for (Entry e : getMap())
      assertTrue(e == null);
  }

  @Test
  public void testSet_Single() {
    ObjectTaintStorage.init(16);

    Object obj = new Object();
    ObjectTaintStorage.set(obj, 32);

    Entry[] H = getMap();

    // expect a single entry
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single entry");
      }

    assertEquals(obj, entry.o.get());
    assertEquals(32, entry.t);
    assertEquals(null, entry.n);
  }

  @Test
  public void testSet_DoubleSameType() {
    ObjectTaintStorage.init(16);

    Object obj1 = new Object();
    Object obj2 = new Object();

    ObjectTaintStorage.set(obj1, 32);
    ObjectTaintStorage.set(obj2, 64);

    Entry[] H = getMap();

    // expect a single occupied slot, but two entries
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj2, entry.o.get());
    assertEquals(64, entry.t);

    assertEquals(obj1, entry.n.o.get());
    assertEquals(32, entry.n.t);
    assertEquals(null, entry.n.n);
  }

  @Test
  public void testSet_DoubleDifferentType() {
    ObjectTaintStorage.init(4);

    Object obj1 = new Object();
    Object obj2 = new Integer(1);

    ObjectTaintStorage.set(obj1, 32);
    ObjectTaintStorage.set(obj2, 64);

    Entry[] H = getMap();

    // expect two occupied slots, each with one entry
    Entry entry1 = null;
    Entry entry2 = null;
    for (Entry e : H)
      if (e != null) {
        if (entry1 == null)
          entry1 = e;
        else if (entry2 == null)
          entry2 = e;
        else
          fail("should only have two slots occupied");
      }

    if (entry2 != null) {
      // the allocation depends on the hashing (random),
      // so swap if they are the other way round
      if (entry1.o.get() == obj2) {
        Entry temp = entry2;
        entry2 = entry1;
        entry1 = temp;
      }

      assertEquals(obj1, entry1.o.get());
      assertEquals(32, entry1.t);
      assertEquals(null, entry1.n);

      assertEquals(obj2, entry2.o.get());
      assertEquals(64, entry2.t);
      assertEquals(null, entry2.n);
    } else {
      // might have happened that the hashes collide
      assertEquals(obj2, entry1.o.get());
      assertEquals(64, entry1.t);

      assertEquals(obj1, entry1.n.o.get());
      assertEquals(32, entry1.n.t);
      assertEquals(null, entry1.n.n);
    }
  }

  @Test
  public void testSet_UpdateFirst() {
    ObjectTaintStorage.init(16);

    Object obj1 = new Object();
    Object obj2 = new Object();

    ObjectTaintStorage.set(obj1, 32);
    ObjectTaintStorage.set(obj2, 64);
    ObjectTaintStorage.set(obj2, 1);

    Entry[] H = getMap();

    // expect a single occupied slot, but two entries
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj2, entry.o.get());
    assertEquals(65, entry.t);

    assertEquals(obj1, entry.n.o.get());
    assertEquals(32, entry.n.t);
    assertEquals(null, entry.n.n);
  }

  @Test
  public void testSet_UpdateSecond() {
    ObjectTaintStorage.init(16);

    Object obj1 = new Object();
    Object obj2 = new Object();

    ObjectTaintStorage.set(obj1, 32);
    ObjectTaintStorage.set(obj2, 64);
    ObjectTaintStorage.set(obj1, 1);

    Entry[] H = getMap();

    // expect a single occupied slot, but two entries
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj1, entry.o.get());
    assertEquals(33, entry.t);

    assertEquals(obj2, entry.n.o.get());
    assertEquals(64, entry.n.t);
    assertEquals(null, entry.n.n);
  }

  @Test
  public void testSet_RemoveGCed_First() {
    ObjectTaintStorage.init(16);

    Object obj1 = new Object();
    Object obj2 = new Object();
    Object obj3 = new Object();

    ObjectTaintStorage.set(obj3, 128);
    ObjectTaintStorage.set(obj2, 64);
    ObjectTaintStorage.set(obj1, 32);

    // remove first object
    obj1 = null;
    forceGC();

    // update second object
    ObjectTaintStorage.set(obj2, 1);

    Entry[] H = getMap();

    // expect a single occupied slot
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj2, entry.o.get());
    assertEquals(65, entry.t);

    assertEquals(obj3, entry.n.o.get());
    assertEquals(128, entry.n.t);
    assertEquals(null, entry.n.n);
  }

  @Test
  public void testSet_RemoveGCed_Second_DontMove() {
    ObjectTaintStorage.init(16);

    Object obj1 = new Object();
    Object obj2 = new Object();
    Object obj3 = new Object();

    ObjectTaintStorage.set(obj3, 128);
    ObjectTaintStorage.set(obj2, 64);
    ObjectTaintStorage.set(obj1, 32);

    // remove first object
    obj2 = null;
    forceGC();

    // update first object
    // (doesn't remove obj2)
    ObjectTaintStorage.set(obj1, 1);

    Entry[] H = getMap();

    // expect a single occupied slot
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj1, entry.o.get());
    assertEquals(33, entry.t);

    assertEquals(null, entry.n.o.get());
    assertEquals(64, entry.n.t);

    assertEquals(obj3, entry.n.n.o.get());
    assertEquals(128, entry.n.n.t);
    assertEquals(null, entry.n.n.n);
  }

  @Test
  public void testSet_RemoveGCed_Second_Move() {
    ObjectTaintStorage.init(16);

    Object obj1 = new Object();
    Object obj2 = new Object();
    Object obj3 = new Object();

    ObjectTaintStorage.set(obj3, 128);
    ObjectTaintStorage.set(obj2, 64);
    ObjectTaintStorage.set(obj1, 32);

    // remove first object
    obj2 = null;
    forceGC();

    // update third object
    // (removes obj2)
    ObjectTaintStorage.set(obj3, 1);

    Entry[] H = getMap();

    // expect a single occupied slot
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj3, entry.o.get());
    assertEquals(129, entry.t);

    assertEquals(obj1, entry.n.o.get());
    assertEquals(32, entry.n.t);
    assertEquals(null, entry.n.n);
  }

  @Test
  public void testGet_Null() {
    assertEquals(0, ObjectTaintStorage.get(null));
  }

  @Test
  public void testGet_NotFound_Empty() {
    ObjectTaintStorage.init(4);
    assertEquals(0, ObjectTaintStorage.get(new Object()));
  }

  @Test
  public void testGet_NotFound_NonEmpty() {
    ObjectTaintStorage.init(4);
    ObjectTaintStorage.set(new Object(), 32);
    assertEquals(0, ObjectTaintStorage.get(new Object()));
  }

  @Test
  public void testGet_Found_First() {
    ObjectTaintStorage.init(4);

    Object obj = new Object();

    ObjectTaintStorage.set(new Object(), 1);
    ObjectTaintStorage.set(new Object(), 2);
    ObjectTaintStorage.set(new Object(), 4);
    ObjectTaintStorage.set(obj, 32);

    assertEquals(32, ObjectTaintStorage.get(obj));

    Entry[] H = getMap();

    // expect a single occupied slot
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj, entry.o.get());
    assertEquals(32, entry.t);

    assertEquals(4, entry.n.t);
    assertEquals(2, entry.n.n.t);
    assertEquals(1, entry.n.n.n.t);

    assertEquals(null, entry.n.n.n.n);
  }

  @Test
  public void testGet_Found_Second() {
    ObjectTaintStorage.init(4);

    Object obj = new Object();

    ObjectTaintStorage.set(new Object(), 1);
    ObjectTaintStorage.set(new Object(), 2);
    ObjectTaintStorage.set(obj, 32);
    ObjectTaintStorage.set(new Object(), 4);

    assertEquals(32, ObjectTaintStorage.get(obj));

    Entry[] H = getMap();

    // expect a single occupied slot
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj, entry.o.get());
    assertEquals(32, entry.t);

    assertEquals(4, entry.n.t);
    assertEquals(2, entry.n.n.t);
    assertEquals(1, entry.n.n.n.t);

    assertEquals(null, entry.n.n.n.n);
  }

  @Test
  public void testGet_RemoveGCed_First() {
    ObjectTaintStorage.init(4);

    Object obj1 = new Object();
    Object obj2 = new Object();
    Object obj3 = new Object();

    ObjectTaintStorage.set(obj1, 1);
    ObjectTaintStorage.set(obj2, 2);
    ObjectTaintStorage.set(obj3, 4);

    obj3 = null;
    forceGC();

    assertEquals(2, ObjectTaintStorage.get(obj2));

    Entry[] H = getMap();

    // expect a single occupied slot
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj2, entry.o.get());
    assertEquals(2, entry.t);

    assertEquals(obj1, entry.n.o.get());
    assertEquals(1, entry.n.t);
    assertEquals(null, entry.n.n);
  }

  @Test
  public void testGet_RemoveGCed_Second_DontMove() {
    ObjectTaintStorage.init(4);

    Object obj1 = new Object();
    Object obj2 = new Object();
    Object obj3 = new Object();

    ObjectTaintStorage.set(obj1, 1);
    ObjectTaintStorage.set(obj2, 2);
    ObjectTaintStorage.set(obj3, 4);

    obj2 = null;
    forceGC();

    assertEquals(4, ObjectTaintStorage.get(obj3));

    Entry[] H = getMap();

    // expect a single occupied slot
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj3, entry.o.get());
    assertEquals(4, entry.t);

    assertEquals(null, entry.n.o.get());
    assertEquals(2, entry.n.t);

    assertEquals(obj1, entry.n.n.o.get());
    assertEquals(1, entry.n.n.t);
    assertEquals(null, entry.n.n.n);
  }

  @Test
  public void testGet_RemoveGCed_Second_Move() {
    ObjectTaintStorage.init(4);

    Object obj1 = new Object();
    Object obj2 = new Object();
    Object obj3 = new Object();

    ObjectTaintStorage.set(obj1, 1);
    ObjectTaintStorage.set(obj2, 2);
    ObjectTaintStorage.set(obj3, 4);

    obj2 = null;
    forceGC();

    assertEquals(1, ObjectTaintStorage.get(obj1));

    Entry[] H = getMap();

    // expect a single occupied slot
    Entry entry = null;
    for (Entry e : H)
      if (e != null) {
        if (entry == null)
          entry = e;
        else
          fail("should only have a single slot occupied");
      }

    assertEquals(obj1, entry.o.get());
    assertEquals(1, entry.t);

    assertEquals(obj3, entry.n.o.get());
    assertEquals(4, entry.n.t);
    assertEquals(null, entry.n.n);
  }
}

