package uk.ac.cam.db538.dexter.dex;

import java.util.Arrays;
import java.util.EnumSet;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexType;

public class DexClass_ObjectTaint extends DexClass {

  @Getter private final DexField field_ObjectMap;
  @Getter private final DexMethod method_Clinit;
  @Getter private final DexMethod method_Init;

  public DexClass_ObjectTaint(Dex parent) {
    super(parent,
          // class type
          generateClassType(parent),
          // super type
          DexClassType.parse("Ljava/lang/Object;", parent.getParsingCache()),
          // access flags
          EnumSet.of(AccessFlags.PUBLIC, AccessFlags.FINAL),
          null, // fields
          null, // methods
          null, // interfaces
          null); // source file

    val cache = parent.getParsingCache();

    field_ObjectMap = genField_ObjectMap(cache);
    fields.add(field_ObjectMap);

    method_Init = genMethod_Init(cache);
    method_Clinit = genMethod_Clinit(cache);
    methods.add(method_Init);
    methods.add(method_Clinit);
  }

  /*
   * Needs to generate a short, but unique class name
   */
  private static DexClassType generateClassType(Dex parent) {
    val cache = parent.getParsingCache();

    String desc;
    long suffix = 0;
    do {
      desc = "Lt/$" + suffix + ";";
      suffix++;
    } while (cache.classTypeExists(desc));

    return DexClassType.parse(desc, cache);
  }

  private DexField genField_ObjectMap(DexParsingCache cache) {
    return new DexField(this,
                        "obj_map",
                        DexClassType.parse("Ljava/util/WeakHashMap;", cache),
                        EnumSet.of(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL));
  }

  private DexMethod genMethod_Clinit(DexParsingCache cache) {
    val code = new DexCode();
    // val rObjectMap = new DexRegister();
    // code.add(new DexInstruction_NewInstance(code, rObjectMap, (DexClassType) fieldObjectMap.getType()));
    code.add(new DexInstruction_ReturnVoid(code));

    return new DexDirectMethod(this,
                               "<clinit>",
                               EnumSet.of(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR),
                               new DexPrototype(
                                 DexType.parse("V", cache), // return value
                                 null), // parameters
                               code);
  }

  private DexMethod genMethod_Init(DexParsingCache cache) {
    val rThis = new DexRegister(0); // argument only for GUI here

    val code = new DexCode();
    code.add(new DexInstruction_Invoke(code,
                                       DexClassType.parse("Ljava/lang/Object;", cache),
                                       "<init>",
                                       new DexPrototype(DexType.parse("V", cache), null),
                                       Arrays.asList(new DexRegister[] { rThis }),
                                       Opcode_Invoke.Direct));
    code.add(new DexInstruction_ReturnVoid(code));

    val method = new DexDirectMethod(this,
                                     "<init>",
                                     EnumSet.of(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR),
                                     new DexPrototype(
                                       DexType.parse("V", cache), // return value
                                       null), // parameters
                                     code);

    method.addParameterMapping_Single(0, rThis);

    return method;
  }
}
