package uk.ac.cam.db538.dexter.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Utils {

	public static <T> List<T> finalList(List<T> list) {
		if (list == null || list.isEmpty())
			return Collections.emptyList();
		else
			return Collections.unmodifiableList(new ArrayList<T>(list));
	}

}
