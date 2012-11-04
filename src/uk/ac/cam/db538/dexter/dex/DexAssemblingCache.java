package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Pair;
import uk.ac.cam.db538.dexter.utils.Triple;

public class DexAssemblingCache {

  private final Cache<DexType, TypeIdItem> Types;
  private final Cache<List<DexType>, TypeListItem> TypeLists;
  private final Cache<String, StringIdItem> StringConstants;
  private final Cache<Pair<DexType, List<DexRegisterType>>, ProtoIdItem> Prototypes;
  private final Cache<Triple<DexClassType, DexRegisterType, String>, FieldIdItem> Fields;

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

    Prototypes = new Cache<Pair<DexType,List<DexRegisterType>>, ProtoIdItem>() {
      @Override
      protected ProtoIdItem createNewEntry(
      Pair<DexType, List<DexRegisterType>> key) {
        return ProtoIdItem.internProtoIdItem(
                 outFile,
                 cache.getType(key.getValA()),
                 cache.getTypeList(new ArrayList<DexType>(key.getValB())));
      }
    };

    Fields = new Cache<Triple<DexClassType, DexRegisterType, String>, FieldIdItem>() {
      @Override
      protected FieldIdItem createNewEntry(Triple<DexClassType, DexRegisterType, String> key) {
        return FieldIdItem.internFieldIdItem(
                 outFile,
                 cache.getType(key.getValA()),
                 cache.getType(key.getValB()),
                 cache.getStringConstant(key.getValC()));
      }
    };
  }

  public TypeIdItem getType(DexType key) {
    return Types.getCachedEntry(key);
  }

  public TypeListItem getTypeList(List<DexType> key) {
    return TypeLists.getCachedEntry(key);
  }

  public StringIdItem getStringConstant(String key) {
    return StringConstants.getCachedEntry(key);
  }

  public ProtoIdItem getPrototype(DexType returnType, List<DexRegisterType> parameterTypes) {
    return Prototypes.getCachedEntry(new Pair<DexType, List<DexRegisterType>>(returnType, parameterTypes));
  }

  public FieldIdItem getField(DexClassType classType, DexRegisterType fieldType, String name) {
    return Fields.getCachedEntry(new Triple<DexClassType, DexRegisterType, String>(classType, fieldType, name));
  }
}
