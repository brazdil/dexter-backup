package uk.ac.cam.db538.dexter.dex.code;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;

public class DexCodeTest {

  @Test
  public void testInsertBefore_Middle() {
    val code = new DexCode(new DexParsingCache());

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem1);
    code.add(elem3);
    code.insertBefore(elem2, elem3);

    val insnList = code.getInstructionList();
    assertEquals(elem1, insnList.get(0));
    assertEquals(elem2, insnList.get(1));
    assertEquals(elem3, insnList.get(2));
  }

  @Test
  public void testInsertBefore_First() {
    val code = new DexCode(new DexParsingCache());

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem2);
    code.add(elem3);
    code.insertBefore(elem1, elem2);

    val insnList = code.getInstructionList();
    assertEquals(elem1, insnList.get(0));
    assertEquals(elem2, insnList.get(1));
    assertEquals(elem3, insnList.get(2));
  }

  @Test(expected=NoSuchElementException.class)
  public void testInsertBefore_NotFound() {
    val code = new DexCode(new DexParsingCache());

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem3);
    code.insertBefore(elem1, elem2);
  }

  @Test
  public void testInsertAfter_Middle() {
    val code = new DexCode(new DexParsingCache());

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem1);
    code.add(elem3);
    code.insertAfter(elem2, elem1);

    val insnList = code.getInstructionList();
    assertEquals(elem1, insnList.get(0));
    assertEquals(elem2, insnList.get(1));
    assertEquals(elem3, insnList.get(2));
  }

  @Test
  public void testInsertBefore_Last() {
    val code = new DexCode(new DexParsingCache());

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem1);
    code.add(elem2);
    code.insertAfter(elem3, elem2);

    val insnList = code.getInstructionList();
    assertEquals(elem1, insnList.get(0));
    assertEquals(elem2, insnList.get(1));
    assertEquals(elem3, insnList.get(2));
  }

  @Test(expected=NoSuchElementException.class)
  public void testInsertAfter_NotFound() {
    val code = new DexCode(new DexParsingCache());

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem3);
    code.insertAfter(elem1, elem2);
  }
}
