package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction3rc;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;

public class DexInstruction_Invoke_Test {

  @Test
  public void testParse_Invoke_Standard_RegisterParsing_Static() throws InstructionParsingException {
    val file = new DexFile();
    val classType = TypeIdItem.internTypeIdItem(file, "Lcom.test;");
    val returnType = TypeIdItem.internTypeIdItem(file, "V");
    val intType = TypeIdItem.internTypeIdItem(file, "I");
    val methodName = StringIdItem.internStringIdItem(file, "myMethod");
    for (int i = 0; i <= 5; ++i) {
      val paramsList = new LinkedList<TypeIdItem>();
      for (int j = 0; j < i; ++j)
        paramsList.add(intType);

      val paramsItem = TypeListItem.internTypeListItem(file, paramsList);
      val protoItem = ProtoIdItem.internProtoIdItem(file, returnType, paramsItem);
      val methodItem = MethodIdItem.internMethodIdItem(file, classType, protoItem, methodName);

      Utils.parseAndCompare(
        new Instruction35c(Opcode.INVOKE_STATIC, (byte) i, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, methodItem),
        (i == 0) ? "invoke-static com.test.myMethod()"
        : (i == 1) ? "invoke-static com.test.myMethod(v11)"
        : (i == 2) ? "invoke-static com.test.myMethod(v11, v12)"
        : (i == 3) ? "invoke-static com.test.myMethod(v11, v12, v13)"
        : (i == 4) ? "invoke-static com.test.myMethod(v11, v12, v13, v14)"
        : "invoke-static com.test.myMethod(v11, v12, v13, v14, v15)"
      );
    }
  }

  @Test
  public void testParse_Invoke_Standard_RegisterParsing_NonStatic() throws InstructionParsingException {
    val file = new DexFile();
    val classType = TypeIdItem.internTypeIdItem(file, "Lcom.test;");
    val returnType = TypeIdItem.internTypeIdItem(file, "V");
    val intType = TypeIdItem.internTypeIdItem(file, "I");
    val methodName = StringIdItem.internStringIdItem(file, "myMethod");
    for (int i = 0; i <= 4; ++i) {
      val paramsList = new LinkedList<TypeIdItem>();
      for (int j = 0; j < i; ++j)
        paramsList.add(intType);

      val paramsItem = TypeListItem.internTypeListItem(file, paramsList);
      val protoItem = ProtoIdItem.internProtoIdItem(file, returnType, paramsItem);
      val methodItem = MethodIdItem.internMethodIdItem(file, classType, protoItem, methodName);

      Utils.parseAndCompare(
        new Instruction35c(Opcode.INVOKE_DIRECT, (byte) i + 1, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, methodItem),
        (i == 0) ? "invoke-direct com.test.myMethod{v11}()"
        : (i == 1) ? "invoke-direct com.test.myMethod{v11}(v12)"
        : (i == 2) ? "invoke-direct com.test.myMethod{v11}(v12, v13)"
        : (i == 3) ? "invoke-direct com.test.myMethod{v11}(v12, v13, v14)"
        : "invoke-direct com.test.myMethod{v11}(v12, v13, v14, v15)"
      );
    }
  }

  @Test
  public void testParse_Invoke_Standard_CallTypes() throws InstructionParsingException {
    val file = new DexFile();
    val classType = TypeIdItem.internTypeIdItem(file, "Lcom.test;");
    val returnType = TypeIdItem.internTypeIdItem(file, "V");
    val intType = TypeIdItem.internTypeIdItem(file, "I");
    val methodName = StringIdItem.internStringIdItem(file, "myMethod");

    val paramsList = new LinkedList<TypeIdItem>();
    paramsList.add(intType);

    val paramsItem = TypeListItem.internTypeListItem(file, paramsList);
    val protoItem = ProtoIdItem.internProtoIdItem(file, returnType, paramsItem);
    val methodItem = MethodIdItem.internMethodIdItem(file, classType, protoItem, methodName);

    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction35c(Opcode.INVOKE_STATIC, (byte) 1, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0, methodItem),
        new Instruction35c(Opcode.INVOKE_VIRTUAL, (byte) 2, (byte) 11, (byte) 12, (byte) 0, (byte) 0, (byte) 0, methodItem),
        new Instruction35c(Opcode.INVOKE_DIRECT, (byte) 2, (byte) 11, (byte) 12, (byte) 0, (byte) 0, (byte) 0, methodItem),
        new Instruction35c(Opcode.INVOKE_SUPER, (byte) 2, (byte) 11, (byte) 12, (byte) 0, (byte) 0, (byte) 0, methodItem),
        new Instruction35c(Opcode.INVOKE_INTERFACE, (byte) 2, (byte) 11, (byte) 12, (byte) 0, (byte) 0, (byte) 0, methodItem)
      }, new String[] {
        "invoke-static com.test.myMethod(v11)",
        "invoke-virtual com.test.myMethod{v11}(v12)",
        "invoke-direct com.test.myMethod{v11}(v12)",
        "invoke-super com.test.myMethod{v11}(v12)",
        "invoke-interface com.test.myMethod{v11}(v12)"
      });
  }

  @Test
  public void testParse_Invoke_Range() throws InstructionParsingException {
    val file = new DexFile();
    val classType = TypeIdItem.internTypeIdItem(file, "Lcom.test;");
    val returnType = TypeIdItem.internTypeIdItem(file, "V");
    val intType = TypeIdItem.internTypeIdItem(file, "I");
    val methodName = StringIdItem.internStringIdItem(file, "myMethod");

    val paramsList = new LinkedList<TypeIdItem>();
    for (int j = 0; j < 10; ++j)
      paramsList.add(intType);

    val paramsItem = TypeListItem.internTypeListItem(file, paramsList);
    val protoItem = ProtoIdItem.internProtoIdItem(file, returnType, paramsItem);
    val methodItem = MethodIdItem.internMethodIdItem(file, classType, protoItem, methodName);

    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction3rc(Opcode.INVOKE_STATIC_RANGE, (short) 10, 48000 , methodItem),
        new Instruction3rc(Opcode.INVOKE_VIRTUAL_RANGE, (short) 11, 48000 , methodItem),
        new Instruction3rc(Opcode.INVOKE_DIRECT_RANGE, (short) 11, 48000 , methodItem),
        new Instruction3rc(Opcode.INVOKE_SUPER_RANGE, (short) 11, 48000 , methodItem),
        new Instruction3rc(Opcode.INVOKE_INTERFACE_RANGE, (short) 11, 48000 , methodItem)
      }, new String[] {
        "invoke-static com.test.myMethod(v48000, v48001, v48002, v48003, v48004, v48005, v48006, v48007, v48008, v48009)",
        "invoke-virtual com.test.myMethod{v48000}(v48001, v48002, v48003, v48004, v48005, v48006, v48007, v48008, v48009, v48010)",
        "invoke-direct com.test.myMethod{v48000}(v48001, v48002, v48003, v48004, v48005, v48006, v48007, v48008, v48009, v48010)",
        "invoke-super com.test.myMethod{v48000}(v48001, v48002, v48003, v48004, v48005, v48006, v48007, v48008, v48009, v48010)",
        "invoke-interface com.test.myMethod{v48000}(v48001, v48002, v48003, v48004, v48005, v48006, v48007, v48008, v48009, v48010)"
      }
    );
  }

  @Test
  public void testCheckArguments_Static_Correct() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("J", cache)
                               });
    val regs = Arrays.asList(new DexRegister[] {
                               new DexRegister(),
                               new DexRegister()
                             });

    new DexInstruction_Invoke(new DexCode(),
                              DexClassType.parse("Lcom.test;", cache),
                              "myMethod",
                              new DexPrototype(DexType.parse("V", cache), params),
                              regs,
                              Opcode_Invoke.Static);
  }

  @Test(expected=InstructionArgumentException.class)
  public void testCheckArguments_Static_Incorrect() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("J", cache)
                               });
    val regs = Arrays.asList(new DexRegister[] {
                               new DexRegister(),
                               // new DexRegister()
                             });

    new DexInstruction_Invoke(new DexCode(),
                              DexClassType.parse("Lcom.test;", cache),
                              "myMethod",
                              new DexPrototype(DexType.parse("V", cache), params),
                              regs,
                              Opcode_Invoke.Static);
  }

  @Test
  public void testCheckArguments_Direct_Correct() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("J", cache)
                               });
    val regs = Arrays.asList(new DexRegister[] {
                               new DexRegister(),
                               new DexRegister(),
                               new DexRegister()
                             });

    new DexInstruction_Invoke(new DexCode(),
                              DexClassType.parse("Lcom.test;", cache),
                              "myMethod",
                              new DexPrototype(DexType.parse("V", cache), params),
                              regs,
                              Opcode_Invoke.Direct);
  }

  @Test(expected=InstructionArgumentException.class)
  public void testCheckArguments_Direct_Incorrect() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("J", cache)
                               });
    val regs = Arrays.asList(new DexRegister[] {
                               new DexRegister(),
                               new DexRegister(),
                               // new DexRegister()
                             });

    new DexInstruction_Invoke(new DexCode(),
                              DexClassType.parse("Lcom.test;", cache),
                              "myMethod",
                              new DexPrototype(DexType.parse("V", cache), params),
                              regs,
                              Opcode_Invoke.Direct);
  }

  @Test
  public void testAssemble_Invoke_Standard_NoParams() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] { });
    val r = new DexRegister[] { };

    val insn = new DexInstruction_Invoke(
      new DexCode(),
      DexClassType.parse("Lcom.test;", cache),
      "myMethod",
      new DexPrototype(DexType.parse("V", cache), params),
      Arrays.asList(r),
      Opcode_Invoke.Static);

    val regAlloc = new HashMap<DexRegister, Integer>();

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);

    val asm0 = asm[0];
    assertTrue(asm0 instanceof Instruction35c);

    val asm35c = (Instruction35c) asm0;
    assertEquals(0, asm35c.getRegCount());
  }

  @Test
  public void testAssemble_Invoke_Standard_OneParam() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("I", cache)
                               });
    val r = new DexRegister[] {
      new DexRegister()
    };

    val insn = new DexInstruction_Invoke(
      new DexCode(),
      DexClassType.parse("Lcom.test;", cache),
      "myMethod",
      new DexPrototype(DexType.parse("V", cache), params),
      Arrays.asList(r),
      Opcode_Invoke.Static);

    val regAlloc = new HashMap<DexRegister, Integer>();
    regAlloc.put(r[0], 3);

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);

    val asm0 = asm[0];
    assertTrue(asm0 instanceof Instruction35c);

    val asm35c = (Instruction35c) asm0;
    assertEquals(1, asm35c.getRegCount());
    assertEquals(3, asm35c.getRegisterD());
  }

  @Test
  public void testAssemble_Invoke_Standard_TwoParams() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache)
                               });
    val r = new DexRegister[] {
      new DexRegister(),
      new DexRegister()
    };

    val insn = new DexInstruction_Invoke(
      new DexCode(),
      DexClassType.parse("Lcom.test;", cache),
      "myMethod",
      new DexPrototype(DexType.parse("V", cache), params),
      Arrays.asList(r),
      Opcode_Invoke.Static);

    val regAlloc = new HashMap<DexRegister, Integer>();
    regAlloc.put(r[0], 3);
    regAlloc.put(r[1], 8);

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);

    val asm0 = asm[0];
    assertTrue(asm0 instanceof Instruction35c);

    val asm35c = (Instruction35c) asm0;
    assertEquals(2, asm35c.getRegCount());
    assertEquals(3, asm35c.getRegisterD());
    assertEquals(8, asm35c.getRegisterE());
  }

  @Test
  public void testAssemble_Invoke_Standard_ThreeParams() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache)
                               });
    val r = new DexRegister[] {
      new DexRegister(),
      new DexRegister(),
      new DexRegister()
    };

    val insn = new DexInstruction_Invoke(
      new DexCode(),
      DexClassType.parse("Lcom.test;", cache),
      "myMethod",
      new DexPrototype(DexType.parse("V", cache), params),
      Arrays.asList(r),
      Opcode_Invoke.Static);

    val regAlloc = new HashMap<DexRegister, Integer>();
    regAlloc.put(r[0], 3);
    regAlloc.put(r[1], 8);
    regAlloc.put(r[2], 2);

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);

    val asm0 = asm[0];
    assertTrue(asm0 instanceof Instruction35c);

    val asm35c = (Instruction35c) asm0;
    assertEquals(3, asm35c.getRegCount());
    assertEquals(3, asm35c.getRegisterD());
    assertEquals(8, asm35c.getRegisterE());
    assertEquals(2, asm35c.getRegisterF());
  }

  @Test
  public void testAssemble_Invoke_Standard_FourParams() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache)
                               });
    val r = new DexRegister[] {
      new DexRegister(),
      new DexRegister(),
      new DexRegister(),
      new DexRegister()
    };

    val insn = new DexInstruction_Invoke(
      new DexCode(),
      DexClassType.parse("Lcom.test;", cache),
      "myMethod",
      new DexPrototype(DexType.parse("V", cache), params),
      Arrays.asList(r),
      Opcode_Invoke.Static);

    val regAlloc = new HashMap<DexRegister, Integer>();
    regAlloc.put(r[0], 3);
    regAlloc.put(r[1], 8);
    regAlloc.put(r[2], 2);
    regAlloc.put(r[3], 9);

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);

    val asm0 = asm[0];
    assertTrue(asm0 instanceof Instruction35c);

    val asm35c = (Instruction35c) asm0;
    assertEquals(4, asm35c.getRegCount());
    assertEquals(3, asm35c.getRegisterD());
    assertEquals(8, asm35c.getRegisterE());
    assertEquals(2, asm35c.getRegisterF());
    assertEquals(9, asm35c.getRegisterG());
  }

  @Test
  public void testAssemble_Invoke_Standard_FiveParams() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache)
                               });
    val r = new DexRegister[] {
      new DexRegister(),
      new DexRegister(),
      new DexRegister(),
      new DexRegister(),
      new DexRegister()
    };

    val insn = new DexInstruction_Invoke(
      new DexCode(),
      DexClassType.parse("Lcom.test;", cache),
      "myMethod",
      new DexPrototype(DexType.parse("V", cache), params),
      Arrays.asList(r),
      Opcode_Invoke.Static);

    val regAlloc = new HashMap<DexRegister, Integer>();
    regAlloc.put(r[0], 3);
    regAlloc.put(r[1], 8);
    regAlloc.put(r[2], 2);
    regAlloc.put(r[3], 9);
    regAlloc.put(r[4], 15);

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);

    val asm0 = asm[0];
    assertTrue(asm0 instanceof Instruction35c);

    val asm35c = (Instruction35c) asm0;
    assertEquals(5, asm35c.getRegCount());
    assertEquals(3, asm35c.getRegisterD());
    assertEquals(8, asm35c.getRegisterE());
    assertEquals(2, asm35c.getRegisterF());
    assertEquals(9, asm35c.getRegisterG());
    assertEquals(15, asm35c.getRegisterA());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_Invoke_Standard_WrongAllocation() {
    val cache = new DexParsingCache();
    val params = Arrays.asList(new DexRegisterType[] {
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache),
                                 DexRegisterType.parse("I", cache)
                               });
    val r = new DexRegister[] {
      new DexRegister(),
      new DexRegister(),
      new DexRegister(),
      new DexRegister(),
      new DexRegister()
    };

    val insn = new DexInstruction_Invoke(
      new DexCode(),
      DexClassType.parse("Lcom.test;", cache),
      "myMethod",
      new DexPrototype(DexType.parse("V", cache), params),
      Arrays.asList(r),
      Opcode_Invoke.Static);

    val regAlloc = new HashMap<DexRegister, Integer>();
    regAlloc.put(r[0], 3);
    regAlloc.put(r[1], 16); // this one doesn't fit into 4 bits
    regAlloc.put(r[2], 2);
    regAlloc.put(r[3], 9);
    regAlloc.put(r[4], 15);

    insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }
}

