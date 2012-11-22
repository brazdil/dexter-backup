package uk.ac.cam.db538.dexter.dex;

import java.util.EnumSet;
import java.util.Set;

import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexClass_ObjectTaint extends DexClass {

  public DexClass_ObjectTaint(Dex parent) {
    super(parent,
          generateClassType(parent),
          DexClassType.parse(ObjectTaintClass_SuperType, parent.getParsingCache()),
          ObjectTaintClass_ClassAccessFlags,
          null, // fields
          null, // methods
          null, // interfaces
          null); // source file

    val cache = parent.getParsingCache();
    Fields.add(generateClassField_ObjectMap(cache));
  }

  /*
   * Needs to generate a short, but unique class name
   */
  private static DexClassType generateClassType(Dex parent) {
    val cache = parent.getParsingCache();

    String desc;
    long suffix = 0;
    do {
      desc = "L" + ObjectTaintClass_ClassBaseName + suffix + ";";
      suffix++;
    } while (cache.classTypeExists(desc));

    return cache.getClassType(desc);
  }

  private DexField generateClassField_ObjectMap(DexParsingCache cache) {
    return new DexField(this,
                        ObjectTaintClass_FieldObjectMap_Name,
                        cache.getClassType(ObjectTaintClass_FieldObjectMap_TypeName),
                        ObjectTaintClass_FieldObjectMap_AccessFlags);
  }

//  private DexMethod generateClassMethod_StaticInitializer(DexParsingCache cache) {
//	  val code = new DexCode();
//
//  }

  // CONSTANTS

  private static final String ObjectTaintClass_ClassBaseName = "t/$";
  private static final String ObjectTaintClass_SuperType = "Ljava/lang/Object;";
  private static final Set<AccessFlags> ObjectTaintClass_ClassAccessFlags = EnumSet.of(AccessFlags.PUBLIC, AccessFlags.FINAL);

  private static final String ObjectTaintClass_FieldObjectMap_Name = "obj_map";
  private static final String ObjectTaintClass_FieldObjectMap_TypeName = "Ljava/util/WeakHashMap;";
  private static final Set<AccessFlags> ObjectTaintClass_FieldObjectMap_AccessFlags = EnumSet.of(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL);

}
