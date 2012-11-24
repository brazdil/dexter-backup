package uk.ac.cam.db538.dexter.dex;

import java.util.Collections;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Triple;

public class DexField {

  @Getter @Setter private DexClass parentClass;
  @Getter private final String name;
  @Getter private final DexRegisterType type;
  private final Set<AccessFlags> accessFlagSet;

  public DexField(DexClass parent, String name, DexRegisterType type, Set<AccessFlags> accessFlags) {
    this.parentClass = parent;
    this.name = name;
    this.type = type;
    this.accessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
  }

  public DexField(DexClass parent, EncodedField fieldInfo) throws UnknownTypeException {
    this(parent,
         StringIdItem.getStringValue(fieldInfo.field.getFieldName()),
         DexRegisterType.parse(fieldInfo.field.getFieldType().getTypeDescriptor(), parent.getParentFile().getParsingCache()),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForField(fieldInfo.accessFlags)));
  }

  public Set<AccessFlags> getAccessFlagSet() {
    return Collections.unmodifiableSet(accessFlagSet);
  }

  public boolean isStatic() {
    return accessFlagSet.contains(AccessFlags.STATIC);
  }

  public EncodedField writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val fieldItem = cache.getField(parentClass.getType(), type, name);
    val accessFlags = DexUtils.assembleAccessFlags(accessFlagSet);

    return new EncodedField(fieldItem, accessFlags);
  }

  public static Cache<Triple<DexClassType, DexRegisterType, String>, FieldIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<Triple<DexClassType, DexRegisterType, String>, FieldIdItem>() {
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
}
