package com.rx201.dx.translator;

import java.util.HashMap;

import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.ClassPath.ClassDef;

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
	public static final RopType Reference = getRopType("Ljava/lang/Object;");
	public static final RopType Array = Reference;
	
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
        Conflicted;
	
    protected static Category[][] mergeTable  =
        {
                /*              Unknown     Null        Zero,       One,        Boolean     Byte        Short       Char        Integer,    Float,      LongLo      LongHi      DoubleLo    DoubleHi    Primitive,  IntFloat,   Wide,       Reference   Conflicted*/
                /*Unknown*/    {Unknown,    Null,       Zero,       One,        Boolean,    Byte,       Short,      Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   Primitive,  IntFloat,   Wide,       Reference,  Conflicted},
                /*Null*/       {Null,       Null,       Null,       Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted},
                /*Zero*/       {Zero,       Null,       Zero,       Boolean,    Boolean,    Byte,       Short,      Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   Integer,    Integer,    Wide,       Reference,  Conflicted},
                /*One*/        {One,        Conflicted, Boolean,    One,        Boolean,    Byte,       Short,      Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   One,        One,        One,        Conflicted, Conflicted},
                /*Boolean*/    {Boolean,    Conflicted, Boolean,    Boolean,    Boolean,    Byte,       Short,      Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Boolean,    Integer,    Conflicted, Conflicted, Conflicted},
                /*Byte*/       {Byte,       Conflicted, Byte,       Byte,       Byte,       Byte,       Short,      Integer,    Byte,       Float,      Conflicted, Conflicted, Conflicted, Conflicted, Byte,       Integer,    Conflicted, Conflicted, Conflicted},
                /*Short*/      {Short,      Conflicted, Short,      Short,      Short,      Short,      Short,      Integer,    Short,      Float,      Conflicted, Conflicted, Conflicted, Conflicted, Short,      Integer,    Conflicted, Conflicted, Conflicted},
                /*Char*/       {Char,       Conflicted, Char,       Char,       Char,       Integer,    Integer,    Char,       Char,       Float,      Conflicted, Conflicted, Conflicted, Conflicted, Char,       Integer,    Conflicted, Conflicted, Conflicted},
                /*Integer*/    {Integer,    Conflicted, Integer,    Integer,    Integer,    Byte,       Short,      Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   Integer,    Integer,    Conflicted, Conflicted, Conflicted},
                /*Float*/      {Float,      Conflicted, Float,      Float,      Float,      Float,      Float,      Float,      Float,      Float,      Conflicted, Conflicted, Conflicted, Conflicted, Float,      Float,      Conflicted, Conflicted, Conflicted},
                /*LongLo*/     {LongLo,     Conflicted, LongLo,     LongLo,     Conflicted, Conflicted, Conflicted, Conflicted, LongLo,     Conflicted, LongLo,     Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, LongLo,     Conflicted, Conflicted},
                /*LongHi*/     {LongHi,     Conflicted, LongHi,     LongHi,     Conflicted, Conflicted, Conflicted, Conflicted, LongHi,     Conflicted, Conflicted, LongLo,     Conflicted, Conflicted, Conflicted, Conflicted, LongHi,     Conflicted, Conflicted},
                /*DoubleLo*/   {DoubleLo,   Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, DoubleLo,   Conflicted, Conflicted, Conflicted, DoubleLo,   Conflicted, Conflicted, Conflicted, DoubleLo,   Conflicted, Conflicted},
                /*DoubleHi*/   {DoubleHi,   Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, DoubleHi,   Conflicted, Conflicted, Conflicted, Conflicted, DoubleHi,   Conflicted, Conflicted, DoubleHi,   Conflicted, Conflicted},
                /*Primitive*/  {Primitive,  Conflicted, Zero,       One,        Boolean,    Byte,       Short,      Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Primitive,  IntFloat,   Conflicted, Conflicted, Conflicted},
                /*IntFloat*/   {IntFloat,   Conflicted, Zero,       One,        Integer,    Integer,    Integer,    Integer,    Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, IntFloat,   IntFloat,   Conflicted, Conflicted, Conflicted},
                /*Wide*/       {Wide,       Conflicted, Wide,       Wide,       Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, LongLo,     LongHi,     DoubleLo,   DoubleHi,   Conflicted, Conflicted, Wide,       Conflicted, Conflicted},
                /*Reference*/  {Reference,  Reference,  Reference,  Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted, Conflicted, Reference,  Conflicted},
                /*Conflicted*/ {Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted}
        };
	}
	
    public final Category category;
    public final ClassDef type;
	
    private RopType(Category category, ClassDef type) {
    	this.category = category;
    	this.type = type;
    }
    
    private static String toDescriptor(Category category, ClassDef type) {
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
			return type.getClassType();
		case Unknown:
			return "*?";
		case Conflicted:
			return "*CONFLICTED";
		default:
			throw new RuntimeException("Unknown RopType");
    	}
    }

    private static HashMap<String, RopType> cachedRefTypes;
    private static RopType getRopType(Category category, ClassDef type) {
    	String desc = toDescriptor(category, type);
    	if (cachedRefTypes == null)
    		cachedRefTypes = new HashMap<String, RopType>();
    	if (!cachedRefTypes.containsKey(desc))
    		cachedRefTypes.put(desc, 
    				new RopType(category, type));
    	
    	return cachedRefTypes.get(desc);
    }
    
    public static RopType getRopType(Category category) {
    	return getRopType(category, null);
    }
    
    public static RopType getRopType(String descriptor) {
        switch (descriptor.charAt(0)) {
        case 'Z':
            return getRopType(Category.Boolean, null);
        case 'B':
            return getRopType(Category.Byte, null);
        case 'S':
            return getRopType(Category.Short, null);
        case 'C':
            return getRopType(Category.Char, null);
        case 'I':
            return getRopType(Category.Integer, null);
        case 'F':
            return getRopType(Category.Float, null);
        case 'J':
            return getRopType(Category.LongLo, null);
        case 'D':
            return getRopType(Category.DoubleLo, null);
        case 'L':
        case '[':
            return getRopType(Category.Reference, ClassPath.getClassDef(descriptor));
        default:
            throw new RuntimeException("Invalid type: " + descriptor);
        }   
    }
    
    public RopType merge(RopType type) {
        if (type == null || type == this) {
            return this;
        }

        Category mergedCategory = Category.mergeTable[this.category.ordinal()][type.category.ordinal()];

        ClassDef mergedType = null;
        if (mergedCategory == Category.Reference) {
            if (this.type instanceof ClassPath.UnresolvedClassDef ||
                type.type instanceof ClassPath.UnresolvedClassDef) {
                mergedType = ClassPath.getUnresolvedObjectClassDef();
            } else if (this == Reference && (type != Unknown && type != Zero)) {
            	return type;
            } else if (type == Reference && (this != Unknown && this != Zero)) {
            	return this;
            } else {
                mergedType = ClassPath.getCommonSuperclass(this.type, type.type);
            }
        } 
        	
        return getRopType(mergedCategory, mergedType);
    	
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
    	return this.category == Category.Reference && this.type.getClassType().charAt(0) == '[';
    }
    
    public RopType getArrayElement() {
    	if (isArray())
    		return getRopType(this.type.getClassType().substring(1));
    	else
    		return Unknown;
    }
    
    public RopType toArrayType() {
    	assert !isPolymorphic();
    	return getRopType("[" + toDescriptor(this.category, this.type));
    }
    
    @Override
    public String toString() {
    	return toDescriptor(this.category, this.type);
    }
//    
//    public boolean isCompatibleWith(RopType other) {
//    	
//    }
    
}
