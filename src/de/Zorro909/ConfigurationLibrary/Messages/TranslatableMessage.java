package de.Zorro909.ConfigurationLibrary.Messages;

import java.util.HashMap;

public class TranslatableMessage {

	private TranslatableMessage prefix = null, suffix = null;

	private HashMap<Language, String> translatedVersions;

	public TranslatableMessage(String englishVersion) {
		this.translatedVersions = new HashMap<>();
		translatedVersions.put(Language.ENGLISH, englishVersion);
	}

	public TranslatableMessage(HashMap<Language, String> translatedVersions) {
		this.translatedVersions = translatedVersions;
	}

	public void setPrefix(TranslatableMessage prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(TranslatableMessage suffix) {
		this.suffix = suffix;
	}

	public HashMap<Language, String> getTranslatedVersions() {
		return translatedVersions;
	}

	public String getText(Language forcedLanguage, StringReplace... replaces) {
		String text = null;
		if (translatedVersions.containsKey(forcedLanguage)) {
			text = translatedVersions.get(forcedLanguage);
		} else {
			text = translatedVersions.get(Language.ENGLISH);
		}
		if (text != null) {
			for (int i = 0; i < replaces.length; i++) {
				text = replaces[i].convert(text);
			}
		} else {
			text = "Something went wrong!";
		}
		if (prefix != null && suffix != null) {
			text = prefix.getText(forcedLanguage, replaces) + text + suffix.getText(forcedLanguage, replaces);
		} else if (prefix != null) {
			text = prefix.getText(forcedLanguage, replaces) + text;
		} else if (suffix != null) {
			text += suffix.getText(forcedLanguage, replaces);
		}
		return text;
	}

}
