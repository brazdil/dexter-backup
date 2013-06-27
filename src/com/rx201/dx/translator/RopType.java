package com.rx201.dx.translator;

import java.util.HashMap;

import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.RegisterType;
import org.jf.dexlib.Code.Analysis.ValidationException;
import org.jf.dexlib.Code.Analysis.ClassPath.ClassDef;

public class RopType {
	public static final RopType NULL = getRopType(Category.Null);
	public static final RopType ONE = getRopType(Category.One);
	public static final RopType BOOLEAN = getRopType(Category.Boolean);
	public static final RopType BYTE = getRopType(Category.Byte);
	public static final RopType SHORT = getRopType(Category.Short);
	public static final RopType CHAR = getRopType(Category.Char);
	public static final RopType INTEGER = getRopType(Category.Integer);
	public static final RopType FLOAT = getRopType(Category.Float);
	public static final RopType LONGLO = getRopType(Category.LongLo);
	public static final RopType LONGHI = getRopType(Category.LongHi);
	public static final RopType DOUBLELO = getRopType(Category.DoubleLo);
	public static final RopType DOUBLEHI = getRopType(Category.DoubleHi);
	public static final RopType REFERENCE = getRopType("Ljava/lang/Object;");
	
	public static enum Category {
        Unknown,
        Null,
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
        Reference,
        Conflicted;
	
    protected static Category[][] mergeTable  =
        {
                /*              Unknown     Null        Zero,       One,        Boolean     Byte            Short          Char        Integer,    Float,      LongLo      LongHi      DoubleLo    DoubleHi    Reference   Conflicted*/
                /*Unknown*/    {Unknown,    Null,       Null,       One,        Boolean,    Byte,           Short,         Char,       Integer,    Float,      LongLo,     LongHi,     DoubleLo,   DoubleHi,   Reference,  Conflicted},
                /*Null*/       {Null,       Conflicted, Null,       Boolean,    Boolean,    Byte,           Short,         Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted},
                /*Zero*/       {Null,       Conflicted, Null,       Boolean,    Boolean,    Byte,           Short,         Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted},
                /*One*/        {One,        Conflicted, Boolean,    One,        Boolean,    Byte,           Short,         Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Boolean*/    {Boolean,    Conflicted, Boolean,    Boolean,    Boolean,    Byte,           Short,         Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Byte*/       {Byte,       Conflicted, Byte,       Byte,       Byte,       Byte,           Short,         Integer,    Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Short*/      {Short,      Conflicted, Short,      Short,      Short,      Short,          Short,         Integer,    Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Char*/       {Char,       Conflicted, Char,       Char,       Char,       Integer,        Integer,       Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Integer*/    {Integer,    Conflicted, Integer,    Integer,    Integer,    Integer,        Integer,       Integer,    Integer,    Integer,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Float*/      {Float,      Conflicted, Float,      Float,      Float,      Float,          Float,         Float,      Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*LongLo*/     {LongLo,     Conflicted, Conflicted, Conflicted, Conflicted, Conflicted,     Conflicted,    Conflicted, Conflicted, Conflicted, LongLo,     Conflicted, LongLo,     Conflicted, Conflicted, Conflicted},
                /*LongHi*/     {LongHi,     Conflicted, Conflicted, Conflicted, Conflicted, Conflicted,     Conflicted,    Conflicted, Conflicted, Conflicted, Conflicted, LongHi,     Conflicted, LongHi,     Conflicted, Conflicted},
                /*DoubleLo*/   {DoubleLo,   Conflicted, Conflicted, Conflicted, Conflicted, Conflicted,     Conflicted,    Conflicted, Conflicted, Conflicted, LongLo,     Conflicted, DoubleLo,   Conflicted, Conflicted, Conflicted},
                /*DoubleHi*/   {DoubleHi,   Conflicted, Conflicted, Conflicted, Conflicted, Conflicted,     Conflicted,    Conflicted, Conflicted, Conflicted, Conflicted, LongHi,     Conflicted, DoubleHi,   Conflicted, Conflicted},
                /*Reference*/  {Reference,  Conflicted, Reference,  Conflicted, Conflicted, Conflicted,     Conflicted,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted},
                /*Conflicted*/ {Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted,     Conflicted,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted}
        };
	}
	
    public final Category category;
    public final ClassDef type;
	
    private RopType(Category category, ClassDef type) {
    	this.category = category;
    	this.type = type;
    }
    public static RopType getRopType(Category category) {
    	return new RopType(category, null);
    }
    
    private static HashMap<String, RopType> cachedRefTypes = new HashMap<String, RopType>();
    public static RopType getRopType(String descriptor) {
    	assert descriptor.charAt(0) == 'L' ||  descriptor.charAt(0) == '[';
    	if (!cachedRefTypes.containsKey(descriptor))
    		cachedRefTypes.put(descriptor, 
    				new RopType(Category.Reference, ClassPath.getClassDef(descriptor)));
    	
    	return cachedRefTypes.get(descriptor);
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
            } else {
                mergedType = ClassPath.getCommonSuperclass(this.type, type.type);
            }
        } 
        	
        return new RopType(mergedCategory, mergedType);
    	
    }
//    
//    public boolean isCompatibleWith(RopType other) {
//    	
//    }
    
}
