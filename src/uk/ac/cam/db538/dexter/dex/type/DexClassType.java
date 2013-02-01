package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexClassType extends DexReferenceType {

  @Getter private final String shortName;
  @Getter private final String packageName;

  private boolean definedInternally;

  private static String checkDescriptor(String descriptor) {
    if (!descriptor.startsWith("L") || !descriptor.endsWith(";"))
      throw new UnknownTypeException(descriptor);
    return descriptor;
  }

  private DexClassType(String descriptor) {
    super(checkDescriptor(descriptor), descriptor.substring(1, descriptor.length() - 1).replace('/', '.'));

    val prettyName = getPrettyName();
    int lastDot = prettyName.lastIndexOf('.');
    if (lastDot == -1) {
      shortName = prettyName;
      packageName = null;
    } else {
      shortName = prettyName.substring(lastDot + 1);
      packageName = prettyName.substring(0, lastDot);
    }

    definedInternally = false;
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

  @Override
  public boolean isDefinedInternally() {
    return definedInternally;
  }

  public void setDefinedInternally(boolean val) {
    definedInternally = val;
  }
}
