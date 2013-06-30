package uk.ac.cam.db538.dexter.dex;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexParsingCache {

  private final Cache<String, DexClassType> classTypes;
  private final Cache<String, DexArrayType> arrayTypes;

  private final Map<String, String> descriptorReplacements;

  // From com.android.dx.rop.code.Exceptions
  public final DexClassType TYPE_ArithmeticException;
  public final DexClassType TYPE_ArrayIndexOutOfBoundsException;
  public final DexClassType TYPE_ArrayStoreException;
  public final DexClassType TYPE_ClassCastException;
  public final DexClassType TYPE_Error;
  public final DexClassType TYPE_IllegalMonitorStateException;
  public final DexClassType TYPE_NegativeArraySizeException;
  public final DexClassType TYPE_NullPointerException;
  
  public final DexClassType[] LIST_Error;
  public final DexClassType[] LIST_Error_ArithmeticException;
  public final DexClassType[] LIST_Error_ClassCastException;
  public final DexClassType[] LIST_Error_NegativeArraySizeException;
  public final DexClassType[] LIST_Error_NullPointerException;
  public final DexClassType[] LIST_Error_Null_ArrayIndexOutOfBounds;
  public final DexClassType[] LIST_Error_Null_ArrayIndex_ArrayStore;
  public final DexClassType[] LIST_Error_Null_IllegalMonitorStateException;

  public DexParsingCache() {
    classTypes = DexClassType.createParsingCache();
    arrayTypes = DexArrayType.createParsingCache(this);
    descriptorReplacements = new HashMap<String, String>();
    
    TYPE_ArithmeticException = getClassType("Ljava/lang/ArithmeticException;");
    TYPE_ArrayIndexOutOfBoundsException = getClassType("Ljava/lang/ArrayIndexOutOfBoundsException;");
    TYPE_ArrayStoreException = getClassType("Ljava/lang/ArrayStoreException;");
    TYPE_ClassCastException = getClassType("Ljava/lang/ClassCastException;");
    TYPE_IllegalMonitorStateException = getClassType("Ljava/lang/IllegalMonitorStateException;");
    TYPE_NegativeArraySizeException = getClassType("Ljava/lang/NegativeArraySizeException;");
    TYPE_NullPointerException = getClassType("Ljava/lang/NullPointerException;");
    TYPE_Error = getClassType("Ljava/lang/Error;");
    

    LIST_Error_ArithmeticException = new DexClassType[] {TYPE_Error, TYPE_ArithmeticException};
    LIST_Error_ClassCastException = new DexClassType[] {TYPE_Error, TYPE_ClassCastException};
    LIST_Error_NegativeArraySizeException = new DexClassType[] {TYPE_Error, TYPE_NegativeArraySizeException};
    LIST_Error_NullPointerException = new DexClassType[] {TYPE_Error, TYPE_NullPointerException};
    LIST_Error_Null_ArrayIndexOutOfBounds = new DexClassType[] {TYPE_Error, 
    															TYPE_NullPointerException, 
    															TYPE_ArrayIndexOutOfBoundsException};
    LIST_Error_Null_ArrayIndex_ArrayStore = new DexClassType[] {TYPE_Error, 
    															TYPE_NullPointerException, 
    															TYPE_ArrayIndexOutOfBoundsException, 
    															TYPE_ArrayStoreException};
    LIST_Error_Null_IllegalMonitorStateException = new DexClassType[] {TYPE_Error, 
    																	TYPE_NullPointerException,
																		TYPE_IllegalMonitorStateException};
    LIST_Error = new DexClassType[] {TYPE_Error};
  }

  public DexClassType getClassType(String desc) {
    return classTypes.getCachedEntry(getDesc(desc));
  }

  public DexArrayType getArrayType(String desc) {
    return arrayTypes.getCachedEntry(getDesc(desc));
  }

  public boolean classTypeExists(String desc) {
    return classTypes.contains(getDesc(desc));
  }

  public void setDescriptorReplacement(String descOld, String descNew) {
    descriptorReplacements.put(descOld, descNew);
  }

  public void removeDescriptorReplacement(String descOld) {
    descriptorReplacements.remove(descOld);
  }

  private String getDesc(String descOld) {
    val descNew = descriptorReplacements.get(descOld);
    if (descNew == null)
      return descOld;
    else
      return descNew;
  }
}
