package uk.ac.cam.db538.dexter.dex.type;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class DexTypeCache {

  private final Map<String, DexClassType> cachedTypes_Class;
  private final Map<String, DexArrayType> cachedTypes_Array;
  // private final Map<String, String> descriptorReplacements;
  
  @Getter private final DexBoolean cachedType_Boolean = new DexBoolean();
  @Getter private final DexByte cachedType_Byte = new DexByte();
  @Getter private final DexChar cachedType_Char = new DexChar();
  @Getter private final DexDouble cachedType_Double = new DexDouble();
  @Getter private final DexFloat cachedType_Float = new DexFloat();
  @Getter private final DexInteger cachedType_Integer = new DexInteger();
  @Getter private final DexLong cachedType_Long = new DexLong();
  @Getter private final DexShort cachedType_Short = new DexShort();

  // From com.android.dx.rop.code.Exceptions
  public final DexClassType TYPE_Throwable;
  public final DexClassType TYPE_ArithmeticException;
  public final DexClassType TYPE_ArrayIndexOutOfBoundsException;
  public final DexClassType TYPE_ArrayStoreException;
  public final DexClassType TYPE_ClassCastException;
  public final DexClassType TYPE_Error;
  public final DexClassType TYPE_IllegalMonitorStateException;
  public final DexClassType TYPE_NegativeArraySizeException;
  public final DexClassType TYPE_NullPointerException;
  
  public final DexClassType[] LIST_Throwable;
  public final DexClassType[] LIST_Error;
  public final DexClassType[] LIST_Error_ArithmeticException;
  public final DexClassType[] LIST_Error_ClassCastException;
  public final DexClassType[] LIST_Error_NegativeArraySizeException;
  public final DexClassType[] LIST_Error_NullPointerException;
  public final DexClassType[] LIST_Error_Null_ArrayIndexOutOfBounds;
  public final DexClassType[] LIST_Error_Null_ArrayIndex_ArrayStore;
  public final DexClassType[] LIST_Error_Null_IllegalMonitorStateException;

  public DexTypeCache() {
    cachedTypes_Class = new HashMap<String, DexClassType>();
    cachedTypes_Array = new HashMap<String, DexArrayType>();
    // descriptorReplacements = new HashMap<String, String>();
    
    TYPE_ArithmeticException = DexClassType.parse("Ljava/lang/ArithmeticException;", this);
    TYPE_ArrayIndexOutOfBoundsException = DexClassType.parse("Ljava/lang/ArrayIndexOutOfBoundsException;", this);
    TYPE_ArrayStoreException = DexClassType.parse("Ljava/lang/ArrayStoreException;", this);
    TYPE_ClassCastException = DexClassType.parse("Ljava/lang/ClassCastException;", this);
    TYPE_IllegalMonitorStateException = DexClassType.parse("Ljava/lang/IllegalMonitorStateException;", this);
    TYPE_NegativeArraySizeException = DexClassType.parse("Ljava/lang/NegativeArraySizeException;", this);
    TYPE_NullPointerException = DexClassType.parse("Ljava/lang/NullPointerException;", this);
    TYPE_Error = DexClassType.parse("Ljava/lang/Error;", this);
    TYPE_Throwable = DexClassType.parse("Ljava/lang/Throwable;", this);
    
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
    LIST_Throwable = new DexClassType[] {TYPE_Throwable};
  }

  DexClassType getCachedType_Class(String desc) {
	  return cachedTypes_Class.get(desc);
  }
  
  void putCachedType_Class(String desc, DexClassType type) {
	  cachedTypes_Class.put(desc, type);
  }

  DexArrayType getCachedType_Array(String desc) {
	  return cachedTypes_Array.get(desc);
  }

  void putCachedType_Array(String desc, DexArrayType type) {
	  cachedTypes_Array.put(desc, type);
  }

//  public void setDescriptorReplacement(String descOld, String descNew) {
//    descriptorReplacements.put(descOld, descNew);
//  }
//
//  public void removeDescriptorReplacement(String descOld) {
//    descriptorReplacements.remove(descOld);
//  }
//
//  private String getDesc(String descOld) {
//    val descNew = descriptorReplacements.get(descOld);
//    if (descNew == null)
//      return descOld;
//    else
//      return descNew;
//  }
}
