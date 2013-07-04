package uk.ac.cam.db538.dexter.dex.type;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class DexTypeCache {

  private final Map<String, DexType_Class> cachedTypes_Class;
  private final Map<String, DexType_Array> cachedTypes_Array;
  // private final Map<String, String> descriptorReplacements;
  
  @Getter private final DexType_Boolean cachedType_Boolean = new DexType_Boolean();
  @Getter private final DexType_Byte cachedType_Byte = new DexType_Byte();
  @Getter private final DexType_Char cachedType_Char = new DexType_Char();
  @Getter private final DexType_Double cachedType_Double = new DexType_Double();
  @Getter private final DexType_Float cachedType_Float = new DexType_Float();
  @Getter private final DexType_Integer cachedType_Integer = new DexType_Integer();
  @Getter private final DexType_Long cachedType_Long = new DexType_Long();
  @Getter private final DexType_Short cachedType_Short = new DexType_Short();

  // From com.android.dx.rop.code.Exceptions
  public final DexType_Class TYPE_Throwable;
  public final DexType_Class TYPE_ArithmeticException;
  public final DexType_Class TYPE_ArrayIndexOutOfBoundsException;
  public final DexType_Class TYPE_ArrayStoreException;
  public final DexType_Class TYPE_ClassCastException;
  public final DexType_Class TYPE_Error;
  public final DexType_Class TYPE_IllegalMonitorStateException;
  public final DexType_Class TYPE_NegativeArraySizeException;
  public final DexType_Class TYPE_NullPointerException;
  
  public final DexType_Class[] LIST_Throwable;
  public final DexType_Class[] LIST_Error;
  public final DexType_Class[] LIST_Error_ArithmeticException;
  public final DexType_Class[] LIST_Error_ClassCastException;
  public final DexType_Class[] LIST_Error_NegativeArraySizeException;
  public final DexType_Class[] LIST_Error_NullPointerException;
  public final DexType_Class[] LIST_Error_Null_ArrayIndexOutOfBounds;
  public final DexType_Class[] LIST_Error_Null_ArrayIndex_ArrayStore;
  public final DexType_Class[] LIST_Error_Null_IllegalMonitorStateException;

  public DexTypeCache() {
    cachedTypes_Class = new HashMap<String, DexType_Class>();
    cachedTypes_Array = new HashMap<String, DexType_Array>();
    // descriptorReplacements = new HashMap<String, String>();
    
    TYPE_ArithmeticException = DexType_Class.parse("Ljava/lang/ArithmeticException;", this);
    TYPE_ArrayIndexOutOfBoundsException = DexType_Class.parse("Ljava/lang/ArrayIndexOutOfBoundsException;", this);
    TYPE_ArrayStoreException = DexType_Class.parse("Ljava/lang/ArrayStoreException;", this);
    TYPE_ClassCastException = DexType_Class.parse("Ljava/lang/ClassCastException;", this);
    TYPE_IllegalMonitorStateException = DexType_Class.parse("Ljava/lang/IllegalMonitorStateException;", this);
    TYPE_NegativeArraySizeException = DexType_Class.parse("Ljava/lang/NegativeArraySizeException;", this);
    TYPE_NullPointerException = DexType_Class.parse("Ljava/lang/NullPointerException;", this);
    TYPE_Error = DexType_Class.parse("Ljava/lang/Error;", this);
    TYPE_Throwable = DexType_Class.parse("Ljava/lang/Throwable;", this);
    
    LIST_Error_ArithmeticException = new DexType_Class[] {TYPE_Error, TYPE_ArithmeticException};
    LIST_Error_ClassCastException = new DexType_Class[] {TYPE_Error, TYPE_ClassCastException};
    LIST_Error_NegativeArraySizeException = new DexType_Class[] {TYPE_Error, TYPE_NegativeArraySizeException};
    LIST_Error_NullPointerException = new DexType_Class[] {TYPE_Error, TYPE_NullPointerException};
    LIST_Error_Null_ArrayIndexOutOfBounds = new DexType_Class[] {TYPE_Error, 
    															TYPE_NullPointerException, 
    															TYPE_ArrayIndexOutOfBoundsException};
    LIST_Error_Null_ArrayIndex_ArrayStore = new DexType_Class[] {TYPE_Error, 
    															TYPE_NullPointerException, 
    															TYPE_ArrayIndexOutOfBoundsException, 
    															TYPE_ArrayStoreException};
    LIST_Error_Null_IllegalMonitorStateException = new DexType_Class[] {TYPE_Error, 
    																	TYPE_NullPointerException,
																		TYPE_IllegalMonitorStateException};
    LIST_Error = new DexType_Class[] {TYPE_Error};
    LIST_Throwable = new DexType_Class[] {TYPE_Throwable};
  }

  DexType_Class getCachedType_Class(String desc) {
	  return cachedTypes_Class.get(desc);
  }
  
  void putCachedType_Class(String desc, DexType_Class type) {
	  cachedTypes_Class.put(desc, type);
  }

  DexType_Array getCachedType_Array(String desc) {
	  return cachedTypes_Array.get(desc);
  }

  void putCachedType_Array(String desc, DexType_Array type) {
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
