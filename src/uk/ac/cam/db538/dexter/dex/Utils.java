package uk.ac.cam.db538.dexter.dex;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

public class Utils {

  public static Set<AccessFlags> getNonNullAccessFlagSet(Set<AccessFlags> accessFlags) {
    return (accessFlags == null) ? EnumSet.noneOf(AccessFlags.class) : accessFlags;
  }

  public static Set<AccessFlags> getAccessFlagSet(int accessFlags) {
    val list = Arrays.asList(AccessFlags.getAccessFlagsForField(accessFlags));
    if (list.isEmpty())
      return EnumSet.noneOf(AccessFlags.class);
    else
      return EnumSet.copyOf(list);
  }
}
