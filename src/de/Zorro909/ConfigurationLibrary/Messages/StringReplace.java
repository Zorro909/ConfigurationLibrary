package de.Zorro909.ConfigurationLibrary.Messages;

public class StringReplace {

    private String key, value;
    
    public StringReplace(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String convert(String text) {
        return text.replace("$" + key, value);
    }
    
    public static StringReplace createReplace(String key, String value) {
        return new StringReplace(key, value);
    }
    
}
