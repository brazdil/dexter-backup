package uk.ac.cam.db538.dexter.dex.type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jf.dexlib.DexFile;

import uk.ac.cam.db538.dexter.hierarchy.builder.HierarchyBuilder;

import lombok.val;


public class ClassRenamer implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Map<String, String> rules;
	
	/*
	 * Generates a ClassRenamer for the given file.
	 * Scans all its classes and if classes of the same name are already present
	 * in the builder, it generates a new name.
	 */
	public ClassRenamer(DexFile file, HierarchyBuilder builder) {
		rules = new HashMap<String, String>();
		
		for (val clsItem : file.ClassDefsSection.getItems()) {
			val typeCache = builder.getTypeCache();
			val clsDesc = clsItem.getClassType().getTypeDescriptor();
			
			long i = 0;
			while (builder.hasClass(DexClassType.parse(addSuffix(clsDesc, i), typeCache)))
				i++;
			
			if (i != 0)
				addRule(clsDesc, addSuffix(clsDesc, i));
		}
	}
	
	/*
	 * Takes a class descriptor in format 'Lpackage/classname;'
	 * and appends a suffix, forming 'Lpackage/classname$suffix;'.
	 * Returns the original descriptor if (suffix == 0).
	 */
	private static String addSuffix(String desc, long suffix) {
		if (suffix == 0)
			return desc;
		else {
			val str = new StringBuilder();
			str.append(desc.substring(0, desc.length() -1)); // remove the semicolon
			str.append("$");
			str.append(suffix);
			str.append(";");
			return str.toString();
		}
	}
	
	private void addRule(String original, String replacement) {
		if (!DexClassType.isClassDescriptor(original) || !DexClassType.isClassDescriptor(replacement))
			throw new Error("Invalid class type descriptor");
		else if (rules.containsKey(original))
			throw new Error("Multiple name replacement rules for class " + original);
		else
			rules.put(original, replacement);
	}
	
	public String applyRules(String clazz) {
		val rule = rules.get(clazz);
		if (rule == null)
			return clazz;
		else
			return rule;
	}
}
