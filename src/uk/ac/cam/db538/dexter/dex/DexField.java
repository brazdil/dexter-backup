package uk.ac.cam.db538.dexter.dex;

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

  @Getter @Setter private DexClass ParentClass;
  @Getter private final String Name;
  @Getter private final DexRegisterType Type;
  @Getter private final Set<AccessFlags> AccessFlagSet;

  public DexField(DexClass parent, String name, DexRegisterType type, Set<AccessFlags> accessFlags) {
    ParentClass = parent;
    Name = name;
    Type = type;
    AccessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
  }

  public DexField(DexClass parent, EncodedField fieldInfo) throws UnknownTypeException {
    this(parent,
         StringIdItem.getStringValue(fieldInfo.field.getFieldName()),
         DexRegisterType.parse(fieldInfo.field.getFieldType().getTypeDescriptor(), parent.getParentFile().getParsingCache()),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForField(fieldInfo.accessFlags)));
  }

  public boolean isStatic() {
    return AccessFlagSet.contains(AccessFlags.STATIC);
  }

  public EncodedField writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val fieldItem = cache.getField(ParentClass.getType(), Type, Name);
    val accessFlags = DexUtils.assembleAccessFlags(AccessFlagSet);

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
