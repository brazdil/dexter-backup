package uk.ac.cam.db538.dexter.utils;

import java.util.HashSet;
import java.util.Set;

import lombok.val;

public class Utils {

	public static <T> Set<T> createSet(T ... args) {
		val set = new HashSet<T>();
		for (val arg : args)
			set.add(arg);
		return set;
	}
}
