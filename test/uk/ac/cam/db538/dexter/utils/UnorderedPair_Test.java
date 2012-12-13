package uk.ac.cam.db538.dexter.utils;

import static org.junit.Assert.*;
import lombok.val;

import org.junit.Test;

public class UnorderedPair_Test {

  @Test(expected=NullPointerException.class)
  public void testNonNull_A() {
    new UnorderedPair<Integer>(null, 1);
  }

  @Test(expected=NullPointerException.class)
  public void testNonNull_B() {
    new UnorderedPair<Integer>(1, null);
  }

  @Test(expected=NullPointerException.class)
  public void testNonNull_Both() {
    new UnorderedPair<Integer>(null, null);
  }

  @Test
  public void testEquals() {
    val p = new UnorderedPair<Integer>(1, 2);
    val p1 = new UnorderedPair<Integer>(1, 2);
    val p2 = new UnorderedPair<Integer>(2, 1);
    val p3 = new UnorderedPair<Integer>(1, 3);
    val p4 = new UnorderedPair<Integer>(2, 2);
    val p5 = new UnorderedPair<Integer>(3, 1);

    assertTrue(p.equals(p1));
    assertTrue(p.equals(p2));
    assertFalse(p.equals(p3));
    assertFalse(p.equals(p4));
    assertFalse(p.equals(p5));
  }
}
