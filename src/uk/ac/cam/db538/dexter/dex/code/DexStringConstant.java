package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.StringIdItem;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;

import lombok.Getter;
import lombok.val;

public class DexStringConstant {
	@Getter private final String Value;
	
	private DexStringConstant(String value) {
		Value = value;
	}
	
	public static DexStringConstant create(String value, DexParsingCache cache) {
		if (cache != null) {
			val cachedStr = cache.getStringConstants().get(value);
			if (cachedStr != null)
				return cachedStr;
		}
		
		val newStr = new DexStringConstant(value);
		if (cache != null)
			cache.getStringConstants().put(value, newStr);
		return newStr;
	}

	public static DexStringConstant create(StringIdItem item, DexParsingCache cache) {
		return create(item.getStringValue(), cache);
	}
}
