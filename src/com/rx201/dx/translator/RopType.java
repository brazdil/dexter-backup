package com.rx201.dx.translator;

import java.util.HashMap;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexBoolean;
import uk.ac.cam.db538.dexter.dex.type.DexByte;
import uk.ac.cam.db538.dexter.dex.type.DexChar;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexDouble;
import uk.ac.cam.db538.dexter.dex.type.DexFloat;
import uk.ac.cam.db538.dexter.dex.type.DexInteger;
import uk.ac.cam.db538.dexter.dex.type.DexLong;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexShort;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class RopType {
	public static final RopType Unknown = getRopType(Category.Unknown);
	public static final RopType Primitive = getRopType(Category.Primitive);
	public static final RopType Wide = getRopType(Category.Wide);
	public static final RopType Null = getRopType(Category.Null);
	public static final RopType One = getRopType(Category.One);
	public static final RopType Zero = getRopType(Category.Zero);
	public static final RopType Boolean = getRopType(Category.Boolean);
	public static final RopType Byte = getRopType(Category.Byte);
	public static final RopType Short = getRopType(Category.Short);
	public static final RopType Char = getRopType(Category.Char);
	public static final RopType IntFloat = getRopType(Category.IntFloat);
	public static final RopType Integer = getRopType(Category.Integer);
	public static final RopType Float = getRopType(Category.Float);
	public static final RopType LongLo = getRopType(Category.LongLo);
	public static final RopType LongHi = getRopType(Category.LongHi);
	public static final RopType DoubleLo = getRopType(Category.DoubleLo);
	public static final RopType DoubleHi = getRopType(Category.DoubleHi);
	public static final RopType WildcardReference = getRopType(0);
	public static final RopType Array = getRopType(0);
	
	public static enum Category {
        Unknown,
        Null, // CstKnownNull: object reference only
        Zero,
        One,
        Boolean,
        Byte,
        Short,
        Char,
        Integer,
        Float,
        LongLo,
        LongHi,
        DoubleLo,
        DoubleHi,
        Primitive,
        IntFloat,
        Wide,
        Reference,
        WildcardRef,
        Conflicted;
	
    protected static Category[][] mergeTable  =
        {
                /*              Unknown     Null        Zero,       One,        Boolean     Byte        Short       Char        Integer,    Float,      LongLo      LongHi      DoubleLo    DoubleHi    Primitive,  IntFloat,   Wide,       Reference   WildcardRef, Conflicted*/
                /*Unknown*/    {Unknown,    Null,       Zero,       One,        Boolean,    Byte,       Short,      Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   Primitive,  IntFloat,   Wide,       Reference,  WildcardRef, Conflicted},
                /*Null*/       {Null,       Null,       Null,       Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Null,        Conflicted},
                /*Zero*/       {Zero,       Null,       Zero,       Boolean,    Boolean,    Byte,       Short,      Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   Integer,    Integer,    Wide,       Reference,  Null,        Conflicted},
                /*One*/        {One,        Conflicted, Boolean,    One,        Boolean,    Byte,       Short,      Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   One,        One,        One,        Conflicted, Conflicted,  Conflicted},
                /*Boolean*/    {Boolean,    Conflicted, Boolean,    Boolean,    Boolean,    Byte,       Short,      Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Boolean,    Integer,    Conflicted, Conflicted, Conflicted,  Conflicted},
                /*Byte*/       {Byte,       Conflicted, Byte,       Byte,       Byte,       Byte,       Short,      Integer,    Byte,       Float,      Conflicted, Conflicted, Conflicted, Conflicted, Byte,       Integer,    Conflicted, Conflicted, Conflicted,  Conflicted},
                /*Short*/      {Short,      Conflicted, Short,      Short,      Short,      Short,      Short,      Integer,    Short,      Float,      Conflicted, Conflicted, Conflicted, Conflicted, Short,      Integer,    Conflicted, Conflicted, Conflicted,  Conflicted},
                /*Char*/       {Char,       Conflicted, Char,       Char,       Char,       Integer,    Integer,    Char,       Char,       Float,      Conflicted, Conflicted, Conflicted, Conflicted, Char,       Integer,    Conflicted, Conflicted, Conflicted,  Conflicted},
                /*Integer*/    {Integer,    Conflicted, Integer,    Integer,    Integer,    Byte,       Short,      Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   Integer,    Integer,    Conflicted, Conflicted, Conflicted,  Conflicted},
                /*Float*/      {Float,      Conflicted, Float,      Float,      Float,      Float,      Float,      Float,      Float,      Float,      Conflicted, Conflicted, Conflicted, Conflicted, Float,      Float,      Conflicted, Conflicted, Conflicted,  Conflicted},
                /*LongLo*/     {LongLo,     Conflicted, LongLo,     LongLo,     Conflicted, Conflicted, Conflicted, Conflicted, LongLo,     Conflicted, LongLo,     Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, LongLo,     Conflicted, Conflicted,  Conflicted},
                /*LongHi*/     {LongHi,     Conflicted, LongHi,     LongHi,     Conflicted, Conflicted, Conflicted, Conflicted, LongHi,     Conflicted, Conflicted, LongLo,     Conflicted, Conflicted, Conflicted, Conflicted, LongHi,     Conflicted, Conflicted,  Conflicted},
                /*DoubleLo*/   {DoubleLo,   Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, DoubleLo,   Conflicted, Conflicted, Conflicted, DoubleLo,   Conflicted, Conflicted, Conflicted, DoubleLo,   Conflicted, Conflicted,  Conflicted},
                /*DoubleHi*/   {DoubleHi,   Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, DoubleHi,   Conflicted, Conflicted, Conflicted, Conflicted, DoubleHi,   Conflicted, Conflicted, DoubleHi,   Conflicted, Conflicted,  Conflicted},
                /*Primitive*/  {Primitive,  Conflicted, Zero,       One,        Boolean,    Byte,       Short,      Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Primitive,  IntFloat,   Conflicted, Conflicted, Conflicted,  Conflicted},
                /*IntFloat*/   {IntFloat,   Conflicted, Zero,       One,        Integer,    Integer,    Integer,    Integer,    Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, IntFloat,   IntFloat,   Conflicted, Conflicted, Conflicted,  Conflicted},
                /*Wide*/       {Wide,       Conflicted, Wide,       Wide,       Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, LongLo,     LongHi,     DoubleLo,   DoubleHi,   Conflicted, Conflicted, Wide,       Conflicted, Conflicted,  Conflicted},
                /*Reference*/  {Reference,  Reference,  Reference,  Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted, Conflicted, Reference,  Reference,   Conflicted},
                /*WildcardRef*/{WildcardRef,Null,       Null,       Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted, Conflicted, Reference,  WildcardRef, Conflicted},
                /*Conflicted*/ {Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted,  Conflicted}
        };
	}
	
    public final Category category;
    public final DexReferenceType type;
	public final int arrayDepth; // Only applicable to WildReference
	
    private RopType(Category category, DexReferenceType type, int arrayDepth) {
    	this.category = category;
    	this.type = type;
    	this.arrayDepth = arrayDepth;
    }
    
    private static String toDescriptor(Category category, DexReferenceType type, int arrayDepth) {
    	switch (category) {
		case Null:
			return "*Null";
		case Zero:
			return "*0";
		case One:
			return "*1";
		case Boolean:
			return "Z";
		case Byte:
			return "B";
		case Char:
			return "C";
		case Float:
			return "F";
		case Integer:
			return "I";
		case Short:
			return "S";
		case DoubleHi:
			return "D";
		case DoubleLo:
			return "D";
		case LongHi:
			return "J";
		case LongLo:
			return "J";
		case IntFloat:
			return "*IntFloat";
		case Primitive:
			return "*Primitive";
		case Wide:
			return "*Wide";
		case Reference:
			return type.getDescriptor();
		case WildcardRef: {
			if (arrayDepth == 0)
				return "*obj";
			else
				return "*[" + java.lang.Integer.toString(arrayDepth);
		}
		case Unknown:
			return "*?";
		case Conflicted:
			return "*CONFLICTED";
		default:
			throw new RuntimeException("Unknown RopType");
    	}
    }

    private static HashMap<String, RopType> cachedRefTypes;
    private static RopType getRopType(Category category, DexReferenceType type, int arrayDepth) {
    	String desc = toDescriptor(category, type, arrayDepth);
    	if (cachedRefTypes == null)
    		cachedRefTypes = new HashMap<String, RopType>();
    	if (!cachedRefTypes.containsKey(desc))
    		cachedRefTypes.put(desc, 
    				new RopType(category, type, arrayDepth));
    	
    	return cachedRefTypes.get(desc);
    }
    
    public static RopType getRopType(Category category) {
    	return getRopType(category, null, 0);
    }
    
    public static RopType getRopType(DexReferenceType type) {
    	return getRopType(Category.Reference, type, 0);
    }
    
    private static RopType getRopType(int arrayDepth) {
    	return getRopType(Category.WildcardRef, null, arrayDepth);
    }
    
    public static RopType getRopType(DexRegisterType regType) {
    	if (regType instanceof DexBoolean)
            return getRopType(Category.Boolean);
    	else if (regType instanceof DexByte)
            return getRopType(Category.Byte);
    	else if (regType instanceof DexShort)
            return getRopType(Category.Short);
    	else if (regType instanceof DexChar)
            return getRopType(Category.Char);
    	else if (regType instanceof DexInteger)
            return getRopType(Category.Integer);
    	else if (regType instanceof DexFloat)
            return getRopType(Category.Float);
    	else if (regType instanceof DexLong)
            return getRopType(Category.LongLo);
    	else if (regType instanceof DexDouble)
            return getRopType(Category.DoubleLo);
    	else if (regType instanceof DexReferenceType)
            return getRopType((DexReferenceType)regType);
    	else
            throw new RuntimeException("Invalid type: " + regType.getDescriptor());
    }
    
    public RopType merge(RopType other, RuntimeHierarchy hierarchy) {
        if (other == null || other == this) {
            return this;
        }

        Category mergedCategory = Category.mergeTable[this.category.ordinal()][other.category.ordinal()];

        DexReferenceType mergedType = null;
        
        if (mergedCategory == Category.Reference) {
        	if (this.category == Category.WildcardRef) {
        		return other;
        	} else if (other.category == Category.WildcardRef) {
        		return this;
        	} 
        	
        	mergedType = getCommonSuperclass(this.type, other.type, hierarchy);
        	
            return getRopType(mergedCategory, mergedType, 0);
        } else {
            int mergedArrayDepth = this.arrayDepth > other.arrayDepth ? this.arrayDepth : other.arrayDepth;
            return getRopType(mergedCategory, mergedType, mergedArrayDepth);
        }
        	
    	
    }
    
    private DexReferenceType getCommonSuperclass(DexReferenceType type0, DexReferenceType type1, 
    		RuntimeHierarchy hierarchy) {
    	if (type0 == null)
    		return type1;
    	else if (type1 == null)
    		return type0;
    	else if (type0 == type1)
    		return type0;
    	
    	if (type0 instanceof DexClassType || type1 instanceof DexClassType) {
    		BaseClassDefinition def0 = hierarchy.getBaseClassDefinition(type0);
    		BaseClassDefinition def1 = hierarchy.getBaseClassDefinition(type1);
    		return def0.getCommonParent(def1).getType();
    	}
		return DexClassType.parse("Ljava/lang/Object;", hierarchy.getTypeCache());
	}

	public RopType lowToHigh() {
    	if (category == Category.LongLo)
    		return getRopType(Category.LongHi);
    	else if (category == Category.DoubleLo)
    		return getRopType(Category.DoubleHi);
    	else
    		throw new RuntimeException("Not a wide register.");
    }
    
    public boolean isPolymorphic() {
    	return  this.category == Category.Unknown ||
    			this.category == Category.Zero ||
				this.category == Category.Primitive ||
				this.category == Category.IntFloat ||
				this.category == Category.Wide;
    }
    
    public boolean isArray() {
    	if (this.category == Category.Reference && this.type instanceof DexArrayType)
    		return true;
    	else if (this.category == Category.WildcardRef)
    		return true;
    	else
    		return false;
    }
    
    public RopType getArrayElement() {
    	if (isArray()) {
    		if (this.category == Category.Reference) {
    			return getRopType(  ((DexArrayType)this.type).getElementType());
    		} else /* WildcardRef */ {
    			if (arrayDepth > 1) //TODO: Lose information here because otherwise: Zero -toArray->> *[1 --toElement-->> becomes WildcardRef
    				return getRopType(arrayDepth - 1);
    			else
    				return Unknown;
    		}
    	}
    	else
    		return Unknown;
    }
    
    public RopType toArrayType(DexTypeCache cache) {
    	if (this == Zero || this == One) {
    			return getRopType(1);
    	} else {
    		if (this.category == Category.WildcardRef)
    			return getRopType(arrayDepth + 1);
    		else if (this == Null || (isPolymorphic()))
    			return getRopType(1);
    		else
    			return getRopType(DexArrayType.parse("[" + getDescriptor(), cache));
    	}
    }
    
    
    private String getDescriptor() {
    	return toDescriptor(this.category, this.type, this.arrayDepth);
	}

	@Override
    public String toString() {
		return getDescriptor();
    }
    
}
