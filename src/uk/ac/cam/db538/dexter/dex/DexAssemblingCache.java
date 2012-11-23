package uk.ac.cam.db538.dexter.dex;

import java.util.List;

import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Triple;

public class DexAssemblingCache {

  private final Cache<DexType, TypeIdItem> Types;
  private final Cache<List<DexRegisterType>, TypeListItem> TypeLists;
  private final Cache<String, StringIdItem> StringConstants;
  private final Cache<DexPrototype, ProtoIdItem> Prototypes;
  private final Cache<Triple<DexClassType, DexRegisterType, String>, FieldIdItem> Fields;
  private final Cache<Triple<DexClassType, DexPrototype, String>, MethodIdItem> Methods;

  public DexAssemblingCache(final DexFile outFile) {
    val cache = this;

    Types = DexType.createAssemblingCache(this, outFile);
    TypeLists = DexType.createAssemblingCacheForLists(this, outFile);

    StringConstants = new Cache<String, StringIdItem>() {
      @Override
      protected StringIdItem createNewEntry(String constant) {
        return StringIdItem.internStringIdItem(outFile, constant);
      }
    };

    Prototypes = DexPrototype.createAssemblingCache(cache, outFile);
    Fields = DexField.createAssemblingCache(cache, outFile);
    Methods = DexMethod.createAssemblingCache(cache, outFile);
  }

  public TypeIdItem getType(DexType key) {
    return Types.getCachedEntry(key);
  }

  public TypeListItem getTypeList(List<DexRegisterType> key) {
    return TypeLists.getCachedEntry(key);
  }

  public StringIdItem getStringConstant(String key) {
    return StringConstants.getCachedEntry(key);
  }

  public ProtoIdItem getPrototype(DexPrototype prototype) {
    return Prototypes.getCachedEntry(prototype);
  }

  public FieldIdItem getField(DexClassType classType, DexRegisterType fieldType, String name) {
    return Fields.getCachedEntry(new Triple<DexClassType, DexRegisterType, String>(classType, fieldType, name));
  }

  public MethodIdItem getMethod(DexClassType classType, DexPrototype methodPrototype, String name) {
    return Methods.getCachedEntry(new Triple<DexClassType, DexPrototype, String>(classType, methodPrototype, name));
  }
}
