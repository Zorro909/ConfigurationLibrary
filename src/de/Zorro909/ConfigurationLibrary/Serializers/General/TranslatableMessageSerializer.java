package de.Zorro909.ConfigurationLibrary.Serializers.General;

import java.util.HashMap;

import de.Zorro909.ConfigurationLibrary.Configuration;
import de.Zorro909.ConfigurationLibrary.ConfigurationPane;
import de.Zorro909.ConfigurationLibrary.Messages.Language;
import de.Zorro909.ConfigurationLibrary.Serializers.SerializedObject;
import de.Zorro909.ConfigurationLibrary.Serializers.SerializedObjectStructure;
import de.Zorro909.ConfigurationLibrary.Serializers.Serializer;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;

public class TranslatableMessageSerializer implements Serializer<TranslatableMessage> {

    SerializedObjectStructure objectStructure;

    public TranslatableMessageSerializer() {
        objectStructure = new SerializedObjectStructure(TranslatableMessage.class);
        objectStructure.addType("ENGLISH", String.class);
        objectStructure.addType("GERMAN", String.class);
    }

    @Override
    public Class<TranslatableMessage> getSerializedType() {
        return TranslatableMessage.class;
    }

    @Override
    public SerializedObject serialize(TranslatableMessage object) {
        SerializedObject sObj = new SerializedObject(getObjectStructure());
        HashMap<Language, String> translatedVersions = object.getTranslatedVersions();
        for (Language lang : translatedVersions.keySet()) {
            sObj.set(lang.name(), translatedVersions.get(lang));
        }
        return sObj;
    }

    @Override
    public TranslatableMessage deserialize(SerializedObject object) {
        HashMap<Language, String> translatedVersions = new HashMap<>();
        for (String lang : object.getSerializedMap().keySet()) {
            String msg = object.get(lang);
            if (msg != null) {
                translatedVersions.put(Language.valueOf(lang), object.get(lang));
            }
        }
        return new TranslatableMessage(translatedVersions);
    }

    @Override
    public SerializedObjectStructure getObjectStructure() {
        return objectStructure;
    }

}
