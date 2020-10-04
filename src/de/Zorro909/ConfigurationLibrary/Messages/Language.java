package de.Zorro909.ConfigurationLibrary.Messages;

public enum Language {

    GERMAN("de", "german"), ENGLISH("en", "english");

    private String shortForm, longForm;

    Language(String shortForm, String longForm) {

    }

    public static Language fromString(String string) {
        Language found = valueOf(string);
        if (found == null) {
            for (Language lang : values()) {
                if (lang.shortForm.equalsIgnoreCase(string)
                        || lang.longForm.equalsIgnoreCase(string)) {
                    return lang;
                }
            }
        }
        return null;
    }

}
