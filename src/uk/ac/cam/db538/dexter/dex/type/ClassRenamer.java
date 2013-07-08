package uk.ac.cam.db538.dexter.dex.type;

import java.util.HashMap;
import java.util.Map;

import lombok.val;


public class ClassRenamer {

	private final Map<String, String> rules;
	
	public ClassRenamer() {
		rules = new HashMap<String, String>();
	}
	
	public void addRule(String original, String replacement) {
		if (!DexClassType.isClassDescriptor(original) || !DexClassType.isClassDescriptor(replacement))
			throw new Error("Invalid class type descriptor");
		else if (rules.containsKey(original))
			throw new Error("Multiple name replacement rules for class " + original);
		else
			rules.put(original, replacement);
	}
	
	public void removeRule(String original) {
		rules.remove(original);
	}

	public String applyRules(String clazz) {
		val rule = rules.get(clazz);
		if (rule == null)
			return clazz;
		else
			return rule;
	}
}
