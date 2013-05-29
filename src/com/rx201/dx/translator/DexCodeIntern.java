package com.rx201.dx.translator;

import java.util.ArrayList;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

public class DexCodeIntern {
	
	public static TypeIdItem intern(DexFile dexFile, TypeIdItem oldItem) {
		return TypeIdItem.internTypeIdItem(dexFile, oldItem.getTypeDescriptor());
	}

	public static TypeListItem intern(DexFile dexFile, TypeListItem oldItem) {
		if (oldItem == null)
			return null;
		ArrayList<TypeIdItem> typeList = new ArrayList<TypeIdItem>();
		for(TypeIdItem item : oldItem.getTypes())
			typeList.add(intern(dexFile, item));
		return TypeListItem.internTypeListItem(dexFile, typeList);
	}

	public static StringIdItem intern(DexFile dexFile, StringIdItem oldItem) {
		return StringIdItem.internStringIdItem(dexFile, oldItem.getStringValue());
	}

	public static FieldIdItem intern(DexFile dexFile, FieldIdItem oldItem) {
		TypeIdItem classType = intern(dexFile, oldItem.getContainingClass()); 
		TypeIdItem fieldType = intern(dexFile, oldItem.getFieldType());
		StringIdItem fieldName = intern(dexFile, oldItem.getFieldName());
		return FieldIdItem.internFieldIdItem(dexFile, classType, fieldType, fieldName);
	}

	public static ProtoIdItem intern(DexFile dexFile, ProtoIdItem prototype) {
		TypeIdItem returnType = intern(dexFile, prototype.getReturnType());
		TypeListItem parameters = intern(dexFile, prototype.getParameters());
		return ProtoIdItem.internProtoIdItem(dexFile, returnType, parameters);
	}

	public static MethodIdItem intern(DexFile dexFile, MethodIdItem oldItem) {
		TypeIdItem classType = intern(dexFile, oldItem.getContainingClass());
		ProtoIdItem methodPrototype = intern(dexFile, oldItem.getPrototype());
		StringIdItem methodName = intern(dexFile, oldItem.getMethodName());
		return MethodIdItem.internMethodIdItem(dexFile, classType, methodPrototype, methodName);
	}
}
