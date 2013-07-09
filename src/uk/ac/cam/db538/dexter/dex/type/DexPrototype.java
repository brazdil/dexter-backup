package uk.ac.cam.db538.dexter.dex.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexParameterRegister;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class DexPrototype implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter private final DexType returnType;
	private final List<DexRegisterType> _parameterTypes;
	@Getter private final List<DexRegisterType> parameterTypes;
  
	private final int hashcode;

	public DexPrototype(DexType returnType, List<DexRegisterType> argTypes) {
		this.returnType = returnType;
		if (argTypes == null)
			this._parameterTypes = Collections.emptyList();
		else
			this._parameterTypes = argTypes;
		this.parameterTypes = Collections.unmodifiableList(_parameterTypes);
    
		// precompute hashcode
		int result = 31 + _parameterTypes.hashCode();
		result = 31 * result + returnType.hashCode();
		this.hashcode = result;
	}

	public static DexPrototype parse(ProtoIdItem protoItem, DexTypeCache cache) {
		val proto = new DexPrototype(parseReturnType(protoItem.getReturnType(), cache),
		                             parseArgumentTypes(protoItem.getParameters(), cache));
		return cache.getCachedPrototype(proto); // will return 'proto' if not cached
	}

	private static DexType parseReturnType(TypeIdItem item, DexTypeCache cache) {
		return DexType.parse(item.getTypeDescriptor(), cache);
	}

	private static List<DexRegisterType> parseArgumentTypes(TypeListItem params, DexTypeCache cache) {
		if (params != null) {
			val list = new ArrayList<DexRegisterType>(params.getTypeCount());
			for (val type : params.getTypes())
				list.add(DexRegisterType.parse(type.getTypeDescriptor(), cache));
			return list;
		}
		return Collections.emptyList();
	}

	public int countParamWords(boolean isStatic) {
		int totalWords = 0;
		if (!isStatic)
			totalWords += DexClassType.TypeSize.getRegisterCount();
		for (val param : _parameterTypes)
			totalWords += param.getRegisters();
		return totalWords;
	}

	public int getParameterCount(boolean isStatic) {
		return _parameterTypes.size() + (isStatic ? 0 : 1);
	}

	public int getParameterRegisterId(int paramId, int registerCount, boolean isStatic) {
		return getFirstParameterRegisterIndex(paramId, isStatic) + registerCount - countParamWords(isStatic);
	}

	public int getFirstParameterRegisterIndex(int paramId, boolean isStatic) {
		if (paramId == 0)
			return 0;

		int regId = 0;

		if (!isStatic) {
			regId += DexClassType.TypeSize.getRegisterCount();
			paramId--;
		}

		for (int i = 0; i < paramId; ++i)
			regId += _parameterTypes.get(i).getRegisters();

		return regId;
	}

	public DexRegisterType getParameterType(int paramId, boolean isStatic, DexClass clazz) {
		if (!isStatic) {
			if (paramId == 0)
				return clazz.getType();
			else
				paramId--;
		}
		return _parameterTypes.get(paramId);
	}

	public NoDuplicatesList<DexRegister> generateParameterRegisters(boolean isStatic) {
		val regs = new NoDuplicatesList<DexRegister>();

		val paramWords = this.countParamWords(isStatic);
		for (int i = 0; i < paramWords; ++i)
			regs.add(new DexParameterRegister(i));

		return regs;
	}

	public List<DexRegister> generateArgumentTaintStoringRegisters(List<DexRegister> argumentRegisters, boolean isStatic, DexCode_InstrumentationState state) {
		val argStoreRegs = new ArrayList<DexRegister>();

		int i = isStatic ? 0 : 1;
		for (val paramType : _parameterTypes) {
			if (paramType instanceof DexPrimitiveType)
				for (int j = 0; j < paramType.getRegisters(); ++j)
					argStoreRegs.add(state.getTaintRegister(argumentRegisters.get(i + j)));
			i += paramType.getRegisters();
		}

		return argStoreRegs;
	}

	public static Cache<DexPrototype, ProtoIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
		return new Cache<DexPrototype, ProtoIdItem>() {
			@Override
			protected ProtoIdItem createNewEntry(DexPrototype prototype) {
				return ProtoIdItem.internProtoIdItem(
						outFile,
						cache.getType(prototype.getReturnType()),
						cache.getTypeList(prototype.getParameterTypes()));
			}
		};
	}	

	public boolean hasPrimitiveArgument() {
		for (val paramType : _parameterTypes)
			if (paramType instanceof DexPrimitiveType)
				return true;
		return false;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof DexPrototype))
			return false;
		DexPrototype other = (DexPrototype) obj;

		return 
				this.returnType.equals(other.returnType) &&
				this._parameterTypes.equals(other._parameterTypes); 
	}
  
	public String getDescriptor() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(val parameter : _parameterTypes) {
			sb.append(parameter.getDescriptor());
		}
		sb.append(")");
		sb.append(returnType.getDescriptor());
		return sb.toString();
	}
  
	@Override
	public String toString() {
		return getDescriptor();
	}
}
