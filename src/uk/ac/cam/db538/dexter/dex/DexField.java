package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationDirectoryItem.FieldAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;

import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexField {

	@Getter private final DexClass parentClass;
	@Getter private final FieldDefinition fieldDef;
  
	private final Set<DexAnnotation> _annotations;
	@Getter private final Set<DexAnnotation> annotations;
  
	public DexField(DexClass parentClass, FieldDefinition fieldDef, Set<DexAnnotation> annotations) {
		this.parentClass = parentClass;
		this.fieldDef = fieldDef;

		this._annotations = new HashSet<DexAnnotation>();
		this.annotations = Collections.unmodifiableSet(this._annotations);
	}

	public DexField(DexClass parentClass, FieldDefinition fieldDef, EncodedField fieldItem, AnnotationDirectoryItem annoDir) {
		this(parentClass,
		     fieldDef,
		     init_ParseAnnotations(parentClass.getParentFile(), fieldItem, annoDir));
	}
	
	private static Set<DexAnnotation> init_ParseAnnotations(Dex dex, EncodedField fieldInfo, AnnotationDirectoryItem annoDir) {
		if (annoDir == null)
			return Collections.emptySet();
		else
			return DexAnnotation.parseAll(annoDir.getFieldAnnotations(fieldInfo.field), dex.getTypeCache());
	}
	
	public void addAnnotation(DexAnnotation anno) {
		_annotations.add(anno);
	}

	public EncodedField writeToFile(DexFile outFile, DexAssemblingCache cache) {
		val fieldItem = cache.getField(fieldDef);
		val accessFlags = DexUtils.assembleAccessFlags(fieldDef.getAccessFlags());

		return new EncodedField(fieldItem, accessFlags);
	}

	public static Cache<FieldDefinition, FieldIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
		return new Cache<FieldDefinition, FieldIdItem>() {
			@Override
			protected FieldIdItem createNewEntry(FieldDefinition key) {
				return FieldIdItem.internFieldIdItem(
						outFile,
						cache.getType(key.getParentClass().getType()),
						cache.getType(key.getFieldId().getType()),
						cache.getStringConstant(key.getFieldId().getName()));
			}
		};
	}

	public FieldAnnotation assembleAnnotations(DexFile outFile, DexAssemblingCache cache) {
		if (annotations.size() == 0)
			return null;
		
		val annoList = new ArrayList<AnnotationItem>(annotations.size());
		for (val anno : annotations)
			annoList.add(anno.writeToFile(outFile, cache));

		val annoSet = AnnotationSetItem.internAnnotationSetItem(outFile, annoList);
		val fieldAnno = new FieldAnnotation(cache.getField(fieldDef), annoSet);

		return fieldAnno;
	}

//  private String generateTaintFieldName() {
//    long suffix = 0L;
//    String baseFieldName = name + "$T";
//    String fieldName = baseFieldName;
//    while (parentClass.containsField(fieldName))
//      fieldName = baseFieldName + (suffix++);
//    return fieldName;
//  }

	public void instrument() {
//    if (type instanceof DexPrimitiveType) {
//      val newName = generateTaintFieldName();
//      val newType = DexPrimitiveType.parse("I", parentClass.getParentFile().getTypeCache());
//
//      val newField = new DexField(parentClass, newName, newType, accessFlagSet, annotations);
//      parentClass.addField(newField);
//
//      return newField;
//    } else
//      return null;
	}
}
