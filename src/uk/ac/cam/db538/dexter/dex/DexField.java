package uk.ac.cam.db538.dexter.dex;

import java.util.Set;

import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

import lombok.Getter;
import lombok.Setter;

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

  public DexField(DexClass parent, EncodedField fieldInfo) {
    this(parent,
         StringIdItem.getStringValue(fieldInfo.field.getFieldName()),
         DexRegisterType.parse(fieldInfo.field.getFieldType().getTypeDescriptor(), parent.getParentFile().getKnownTypes()),
         Utils.getAccessFlagSet(AccessFlags.getAccessFlagsForField(fieldInfo.accessFlags)));
  }

  public boolean isStatic() {
    return AccessFlagSet.contains(AccessFlags.STATIC);
  }
}
