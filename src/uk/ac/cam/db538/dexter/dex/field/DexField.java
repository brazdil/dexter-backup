package uk.ac.cam.db538.dexter.dex.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationDirectoryItem.FieldAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexAnnotation;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
import uk.ac.cam.db538.dexter.utils.Cache;

public abstract class DexField {

	@Getter private final DexClass parentClass;

	private final List<DexAnnotation> _annotations;
	@Getter private final List<DexAnnotation> annotations;
  
	public DexField(DexClass parentClass) {
		this.parentClass = parentClass;

		this._annotations = new ArrayList<DexAnnotation>();
		this.annotations = Collections.unmodifiableList(this._annotations);
	}

	public DexField(DexClass parentClass, EncodedField fieldItem, AnnotationDirectoryItem annoDir) {
		this(parentClass);
		
		this._annotations.addAll(init_ParseAnnotations(getParentFile(), fieldItem, annoDir));
	}
	
	private static List<DexAnnotation> init_ParseAnnotations(Dex dex, EncodedField fieldInfo, AnnotationDirectoryItem annoDir) {
		if (annoDir == null)
			return Collections.emptyList();
		else
			return DexAnnotation.parseAll(annoDir.getFieldAnnotations(fieldInfo.field), dex.getTypeCache());
	}
	
	public Dex getParentFile() {
		return parentClass.getParentFile();
	}
	
	public void addAnnotation(DexAnnotation anno) {
		_annotations.add(anno);
	}

	protected abstract FieldDefinition internal_GetFieldDef(); 
	
	public EncodedField writeToFile(DexFile outFile, DexAssemblingCache cache) {
		val fieldDef = internal_GetFieldDef();
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
		if (_annotations.size() == 0)
			return null;
		
		val annoList = new ArrayList<AnnotationItem>(_annotations.size());
		for (val anno : _annotations)
			annoList.add(anno.writeToFile(outFile, cache));

		val annoSet = AnnotationSetItem.internAnnotationSetItem(outFile, annoList);
		val fieldAnno = new FieldAnnotation(cache.getField(internal_GetFieldDef()), annoSet);

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
