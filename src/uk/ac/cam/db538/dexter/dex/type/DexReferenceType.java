package uk.ac.cam.db538.dexter.dex.type;

import java.util.Arrays;
import java.util.List;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;

public abstract class DexReferenceType extends DexRegisterType {

  public static final DexRegisterTypeSize TypeSize = DexRegisterTypeSize.SINGLE;

  protected DexReferenceType(String descriptor, String prettyName) {
    super(descriptor, prettyName, TypeSize);
  }

  public static DexReferenceType parse(String typeDescriptor, DexParsingCache cache) throws UnknownTypeException {
    try {
      return DexClassType.parse(typeDescriptor, cache);
    } catch (UnknownTypeException e) {
    }

    try {
      return DexArrayType.parse(typeDescriptor, cache);
    } catch (UnknownTypeException e) {
    }

    throw new UnknownTypeException(typeDescriptor);
  }

  public boolean isDefinedInternally() {
    return false;
  }

  private static List<String> sImmutables = Arrays.asList(
	  "Ljava/lang/String;",
	  "Ljava/lang/Boolean;",
	  "Ljava/lang/Byte;",
	  "Ljava/lang/Character;",
	  "Ljava/lang/Double;",
	  "Ljava/lang/Float;",
	  "Ljava/lang/Integer;",
	  "Ljava/lang/Long;",
	  "Ljava/lang/Short;"
  );
  
  public boolean isImmutable() {
	return sImmutables.contains(this.getDescriptor());
  }
}
