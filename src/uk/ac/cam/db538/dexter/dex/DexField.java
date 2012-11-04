package uk.ac.cam.db538.dexter.dex;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexField {

  @Getter @Setter private DexClass ParentClass;
  @Getter private final String Name;
  @Getter private final DexRegisterType Type;
  @Getter private final Set<AccessFlags> AccessFlagSet;

  public DexField(DexClass parent, String name, DexRegisterType type, Set<AccessFlags> accessFlags) {
    ParentClass = parent;
    Name = name;
    Type = type;
    AccessFlagSet = Utils.getNonNullAccessFlagSet(accessFlags);
  }

  public DexField(DexClass parent, EncodedField fieldInfo) throws UnknownTypeException {
    this(parent,
         StringIdItem.getStringValue(fieldInfo.field.getFieldName()),
         DexRegisterType.parse(fieldInfo.field.getFieldType().getTypeDescriptor(), parent.getParentFile().getParsingCache()),
         Utils.getAccessFlagSet(AccessFlags.getAccessFlagsForField(fieldInfo.accessFlags)));
  }

  public boolean isStatic() {
    return AccessFlagSet.contains(AccessFlags.STATIC);
  }

  public EncodedField writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val fieldItem = cache.getField(ParentClass.getType(), Type, Name);
    val accessFlags = Utils.assembleAccessFlags(AccessFlagSet);

    return new EncodedField(fieldItem, accessFlags);
  }
}
