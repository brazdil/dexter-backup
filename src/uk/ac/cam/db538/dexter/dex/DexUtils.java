package uk.ac.cam.db538.dexter.dex;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.dex.type.DexType_Register;

public class DexUtils {

  public static Set<AccessFlags> getNonNullAccessFlagSet(Set<AccessFlags> accessFlags) {
    return (accessFlags == null) ? EnumSet.noneOf(AccessFlags.class) : accessFlags;
  }

  public static Set<AccessFlags> getAccessFlagSet(AccessFlags[] flags) {
    val list = Arrays.asList(flags);
    if (list.isEmpty())
      return EnumSet.noneOf(AccessFlags.class);
    else
      return EnumSet.copyOf(list);
  }

  public static int assembleAccessFlags(Set<AccessFlags> accessFlags) {
    int result = 0;
    for (val flag : accessFlags)
      result |= flag.getValue();
    return result;
  }

  public static DexField getField(Dex dex, DexType_Class fieldClass, String fieldName, DexType_Register fieldType) {
    for (val clazz : dex.getClasses())
      if (clazz.getType().equals(fieldClass)) {
        for (val field : clazz.getFields())
          if (field.getName().equals(fieldName) && field.getType().equals(fieldType))
            return field;
        return null;
      }
    return null;
  }
}
