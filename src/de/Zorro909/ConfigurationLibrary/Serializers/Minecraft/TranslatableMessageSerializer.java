package de.Zorro909.ConfigurationLibrary.Serializers.Minecraft;

import java.util.HashMap;

import de.Zorro909.ConfigurationLibrary.Configuration;
import de.Zorro909.ConfigurationLibrary.ConfigurationPane;
import de.Zorro909.ConfigurationLibrary.Messages.Language;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;
import de.Zorro909.ConfigurationLibrary.Serializers.Serializer;

public class TranslatableMessageSerializer implements Serializer<TranslatableMessage> {

    @Override
    public void serialize(TranslatableMessage object, ConfigurationPane configPane) {
        String key = configPane.getAbsolutePrefix();
        if (key.contains(".")) {
            key = key.substring(key.lastIndexOf(".") + 1);
        }
        Configuration parent = configPane.getParent();
        HashMap<Language, String> translatedVersions = object.getTranslatedVersions();
        for (Language lang : translatedVersions.keySet()) {
            parent.set(lang.toString(), translatedVersions.get(lang));
        }
    }

    @Override
    public TranslatableMessage deserialize(ConfigurationPane configPane) {
        String key = configPane.getAbsolutePrefix();
        if (key.contains(".")) {
            key = key.substring(key.lastIndexOf(".") + 1);
        }
        Configuration parent = configPane.getParent();
        HashMap<Language, String> translatedVersions = new HashMap<>();
        if (parent.getKeys().contains(key)) {
            translatedVersions.put(Language.ENGLISH, parent.getString(key));
        } else {
            for (String language : parent.getKeys()) {
                Language lang = Language.fromString(language);
                if (lang != null) {
                    String translatedVersion = parent.getString(lang + "." + key);
                    if (translatedVersion != null) {
                        translatedVersions.put(lang, translatedVersion);
                    }
                }
            }
        }
        return new TranslatableMessage(translatedVersions);
    }

    @Override
    public Class<TranslatableMessage> getSerializedType() {
        return TranslatableMessage.class;
    }

}
