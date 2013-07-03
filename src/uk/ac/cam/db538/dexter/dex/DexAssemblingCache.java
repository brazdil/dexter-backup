package uk.ac.cam.db538.dexter.dex;

import java.util.List;

import lombok.Getter;
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
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.dex.type.DexType_Reference;
import uk.ac.cam.db538.dexter.dex.type.DexType_Register;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Triple;

public class DexAssemblingCache {

  @Getter private final DexTypeCache parsingCache;
  private final Cache<DexType, TypeIdItem> types;
  private final Cache<List<DexType_Register>, TypeListItem> typeLists;
  private final Cache<String, StringIdItem> stringConstants;
  private final Cache<DexPrototype, ProtoIdItem> prototypes;
  private final Cache<Triple<DexType_Class, DexType_Register, String>, FieldIdItem> fields;
  private final Cache<Triple<DexType_Reference, DexPrototype, String>, MethodIdItem> methods;

  public DexAssemblingCache(final DexFile outFile, DexTypeCache parsingCache) {
    this.parsingCache = parsingCache;

    val cache = this;

    types = DexType.createAssemblingCache(this, outFile);
    typeLists = DexType.createAssemblingCacheForLists(this, outFile);

    stringConstants = new Cache<String, StringIdItem>() {
      @Override
      protected StringIdItem createNewEntry(String constant) {
        return StringIdItem.internStringIdItem(outFile, constant);
      }
    };

    prototypes = DexPrototype.createAssemblingCache(cache, outFile);
    fields = DexField.createAssemblingCache(cache, outFile);
    methods = DexMethod.createAssemblingCache(cache, outFile);
  }

  public TypeIdItem getType(DexType key) {
    return types.getCachedEntry(key);
  }

  public TypeListItem getTypeList(List<DexType_Register> key) {
	// According to http://source.android.com/tech/dalvik/dex-format.html
	// empty type_list should be null, not a 0-length list
	if (key.size() == 0)
		return null;
	else
        return typeLists.getCachedEntry(key);
  }

  public StringIdItem getStringConstant(String key) {
    return stringConstants.getCachedEntry(key);
  }

  public ProtoIdItem getPrototype(DexPrototype prototype) {
    return prototypes.getCachedEntry(prototype);
  }

  public FieldIdItem getField(DexType_Class classType, DexType_Register fieldType, String name) {
    return fields.getCachedEntry(new Triple<DexType_Class, DexType_Register, String>(classType, fieldType, name));
  }

  public MethodIdItem getMethod(DexType_Reference classType, DexPrototype methodPrototype, String name) {
    return methods.getCachedEntry(new Triple<DexType_Reference, DexPrototype, String>(classType, methodPrototype, name));
  }
}
