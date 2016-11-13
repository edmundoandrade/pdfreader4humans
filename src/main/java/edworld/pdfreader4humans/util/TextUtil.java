package edworld.pdfreader4humans.util;

import java.text.Normalizer;

public abstract class TextUtil {
	public static String removeDiacritics(String text) {
		return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
}
