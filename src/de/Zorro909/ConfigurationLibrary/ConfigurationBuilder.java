package de.Zorro909.ConfigurationLibrary;

import java.lang.reflect.InvocationTargetException;

public class ConfigurationBuilder {

    public static Configuration createConfig(Config configAnnotation, Configuration previouslyLoaded)
            throws NoSuchMethodException {
        Class<? extends Configuration> configType = configAnnotation.type();
        try {
            return configType.getConstructor(Config.class, Configuration.class)
                    .newInstance(configAnnotation, previouslyLoaded);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            NoSuchMethodException nsme = new NoSuchMethodException(
                    "No Default Constructor found for Configuration Implementation '"
                            + configType.getSimpleName()
                            + "'(de.Zorro909.ConfigurationLibrary.Config configAnnotation, de.Zorro909.ConfigurationLibrary.Configuration previouslyLoaded)\n"
                            + e.getMessage());
            nsme.setStackTrace(e.getStackTrace());
            throw nsme;
        }
    }

}
