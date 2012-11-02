package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexClassType extends DexReferenceType {

  @Getter	private final String ShortName;
  @Getter	private final String PackageName;

  private static String checkDescriptor(String descriptor) {
    if (!descriptor.startsWith("L") || !descriptor.endsWith(";"))
      throw new UnknownTypeException(descriptor);
    return descriptor;
  }

  public static final int NumberOfRegisters = 1;

  public DexClassType(String descriptor) {
    super(checkDescriptor(descriptor), descriptor.substring(1, descriptor.length() - 1).replace('/', '.'), NumberOfRegisters);

    val prettyName = getPrettyName();
    int lastDot = prettyName.lastIndexOf('.');
    if (lastDot == -1) {
      ShortName = prettyName;
      PackageName = null;
    } else {
      ShortName = prettyName.substring(lastDot + 1);
      PackageName = prettyName.substring(0, lastDot);
    }
  }

  public static DexClassType parse(String typeDescriptor, DexParsingCache cache) {
    return cache.getClassType(typeDescriptor);
  }

  public static Cache<String, DexClassType> createParsingCache() {
    return new Cache<String, DexClassType>() {
      @Override
      protected DexClassType createNewEntry(String typeDescriptor) {
        return new DexClassType(typeDescriptor);
      }
    };
  }
}
