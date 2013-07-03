package uk.ac.cam.db538.dexter.dex;

import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.EncodedValue.AnnotationEncodedValue;
import org.jf.dexlib.EncodedValue.ArrayEncodedSubValue;
import org.jf.dexlib.EncodedValue.ArrayEncodedValue;
import org.jf.dexlib.EncodedValue.EncodedValue;
import org.jf.dexlib.EncodedValue.EnumEncodedValue;
import org.jf.dexlib.EncodedValue.FieldEncodedValue;
import org.jf.dexlib.EncodedValue.MethodEncodedValue;
import org.jf.dexlib.EncodedValue.StringEncodedValue;
import org.jf.dexlib.EncodedValue.TypeEncodedValue;

import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.dex.type.DexType_Register;
import uk.ac.cam.db538.dexter.dex.type.DexType;

public class DexEncodedValue {

	public static EncodedValue cloneEncodedValue(EncodedValue value, DexAssemblingCache asmCache) {
	    val parsingCache = asmCache.getParsingCache();
	
	    switch (value.getValueType()) {
	    case VALUE_ARRAY:
	      val arrayValue = (ArrayEncodedSubValue) value;
	      val isSubValue = !(value instanceof ArrayEncodedValue);
	
	      int innerValuesCount = arrayValue.values.length;
	      val innerValues = new EncodedValue[innerValuesCount];
	      for (int i = 0; i < innerValuesCount; ++i)
	        innerValues[i] = cloneEncodedValue(arrayValue.values[i], asmCache);
	
	      if (isSubValue)
	        return new ArrayEncodedSubValue(innerValues);
	      else
	        return new ArrayEncodedValue(innerValues);
	
	    case VALUE_BOOLEAN:
	    case VALUE_BYTE:
	    case VALUE_CHAR:
	    case VALUE_DOUBLE:
	    case VALUE_FLOAT:
	    case VALUE_INT:
	    case VALUE_LONG:
	    case VALUE_NULL:
	    case VALUE_SHORT:
	      return value;
	
	    case VALUE_ENUM:
	      val enumValue = (EnumEncodedValue) value;
	      return new EnumEncodedValue(
	               asmCache.getField(
	                 DexType_Class.parse(enumValue.value.getContainingClass().getTypeDescriptor(), parsingCache),
	                 DexType_Register.parse(enumValue.value.getFieldType().getTypeDescriptor(), parsingCache),
	                 enumValue.value.getFieldName().getStringValue()));
	
	    case VALUE_FIELD:
	      val fieldValue = (FieldEncodedValue) value;
	      return new FieldEncodedValue(
	               asmCache.getField(
	                 DexType_Class.parse(fieldValue.value.getContainingClass().getTypeDescriptor(), parsingCache),
	                 DexType_Register.parse(fieldValue.value.getFieldType().getTypeDescriptor(), parsingCache),
	                 fieldValue.value.getFieldName().getStringValue()));
	
	    case VALUE_METHOD:
	      val methodValue = (MethodEncodedValue) value;
	      return new MethodEncodedValue(
	               asmCache.getMethod(
	                 DexType_Class.parse(methodValue.value.getContainingClass().getTypeDescriptor(), parsingCache),
	                 new DexPrototype(methodValue.value.getPrototype(), parsingCache),
	                 methodValue.value.getMethodName().getStringValue()));
	
	    case VALUE_STRING:
	      val stringValue = (StringEncodedValue) value;
	      return new StringEncodedValue(asmCache.getStringConstant(stringValue.value.getStringValue()));
	
	    case VALUE_TYPE:
	      val typeValue = (TypeEncodedValue) value;
	      return new TypeEncodedValue(asmCache.getType(DexType.parse(typeValue.value.getTypeDescriptor(), parsingCache)));
	
	    case VALUE_ANNOTATION:
	      val annotationValue = (AnnotationEncodedValue) value;
	
	      val newNames = new StringIdItem[annotationValue.names.length];
	      for (int i = 0; i < annotationValue.names.length; ++i)
	        newNames[i] = asmCache.getStringConstant(annotationValue.names[i].getStringValue());
	
	      val newEncodedValues = new EncodedValue[annotationValue.values.length];
	      for (int i = 0; i < annotationValue.values.length; ++i)
	        newEncodedValues[i] = cloneEncodedValue(annotationValue.values[i], asmCache);
	
	      return new AnnotationEncodedValue(
	               asmCache.getType(DexType.parse(annotationValue.annotationType.getTypeDescriptor(), parsingCache)),
	               newNames,
	               newEncodedValues);
	
	    default:
	      throw new RuntimeException("Unexpected EncodedValue type: " + value.getValueType().name());
	    }
	  }

}
