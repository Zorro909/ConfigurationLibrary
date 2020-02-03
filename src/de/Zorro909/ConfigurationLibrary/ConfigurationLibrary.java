package de.Zorro909.ConfigurationLibrary;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import de.Zorro909.ConfigurationLibrary.Serializers.Serializer;
import de.Zorro909.ConfigurationLibrary.Serializers.Serializers;

public class ConfigurationLibrary {

    static {
        Reflections reflections = new Reflections("de.Zorro909.ConfigurationLibrary.Serializers",
                new SubTypesScanner(), new TypeAnnotationsScanner());
        searchForNewSerializers(reflections);
    }

    private static HashMap<String, Configuration> defaultConfig = new HashMap<>();
    private static HashMap<Class, List<Field>> staticFields = new HashMap<>();

    private static HashMap<Class, Configuration> defaultClassConfigurations = new HashMap<>();

    private static HashMap<String, HashMap<String, Configuration>> loadedConfigurations = new HashMap<>();

    private static ArrayList<String> registeredPackageNames = new ArrayList<>();

    public static boolean registerLibrary(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(),
                new TypeAnnotationsScanner(), new FieldAnnotationsScanner());
        searchForNewSerializers(reflections);

        Set<Class<?>> configSelectingClasses = reflections.getTypesAnnotatedWith(Config.class);
        Set<Class<?>> configContainerClasses = reflections
                .getTypesAnnotatedWith(ConfigContainer.class);
        if (configSelectingClasses.size() == 0 && configContainerClasses.size() == 0) {
            System.err.println("ERROR: No Config Definition found!");
            return false;
        }
        registeredPackageNames.add(packageName);
        HashMap<String, Configuration> configurations = new HashMap<>();
        loadedConfigurations.put(packageName, configurations);
        for (Class containers : configContainerClasses) {
            ConfigContainer container = (ConfigContainer) containers
                    .getAnnotation(ConfigContainer.class);
            Configuration last = null;
            for (Config config : container.value()) {
                if (!isConfigLoaded(getPackageName(containers.getName()), config.value())) {
                    try {
                        last = ConfigurationBuilder.createConfig(config, last);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                        return false;
                    }
                } else {
                    last = getConfiguration(packageName, config.value());
                }
                configurations.put(last.getIdentifier(), last);
                if (config.globalDefault()) {
                    defaultConfig.put(packageName, last);
                }
            }
            defaultClassConfigurations.put(containers, last);
        }

        for (Class clazz : configSelectingClasses) {
            Config config = (Config) clazz.getAnnotation(Config.class);
            Configuration conf = null;
            if (!isConfigLoaded(getPackageName(clazz.getName()), config.value())) {
                try {
                    conf = ConfigurationBuilder.createConfig(config, null);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                conf = getConfiguration(packageName, config.value());
            }
            configurations.put(conf.getIdentifier(), conf);
            if (config.globalDefault()) {
                defaultConfig.put(packageName, conf);
            }
            defaultClassConfigurations.put(clazz, conf);
        }

        Set<Field> configFields = reflections.getFieldsAnnotatedWith(Config.class);
        for (Field configField : configFields) {
            Config annot = configField.getAnnotation(Config.class);
            if (!isConfigLoaded(packageName, annot.value())) {
                try {
                    Configuration conf = ConfigurationBuilder.createConfig(annot,
                            getDefaultConfiguration(configField.getDeclaringClass()));
                    configurations.put(conf.getIdentifier(), conf);
                    if (configField.getType().isAssignableFrom(conf.getClass())) {
                        configField.set(conf, null);
                    }
                } catch (NoSuchMethodException | IllegalArgumentException
                        | IllegalAccessException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        Set<Field> fields = reflections.getFieldsAnnotatedWith(AutoConfigured.class);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                loadField(field.getDeclaringClass(), field);
            } else {
                System.err.println("ERROR: Field " + field.getName()
                        + " is a non static Field annotated with @AutoConfigured which is unsupported!");
            }
        }
        return true;
    }

    public static void save(String packageName) {
        for (Configuration conf : loadedConfigurations.get(packageName).values()) {
            if (conf instanceof ConfigurationPane)
                continue;
            conf.save();
        }
    }

    private static void searchForNewSerializers(Reflections reflections) {
        for (Class<? extends Serializer> serializer : reflections.getSubTypesOf(Serializer.class)) {
            Serializer instance = null;
            try {
                instance = serializer.newInstance();
                Serializers.registerSerializer(instance.getSerializedType(), instance);
            } catch (InstantiationException | IllegalAccessException e) {
                System.err.println(
                        "Serializer " + serializer.getName() + " has no empty Constructor!");
                e.printStackTrace();
            }
        }
    }

    private static Configuration getConfiguration(String packageName, String identifier) {
        if (isConfigLoaded(packageName, identifier)) {
            if (identifier.contains(":")) {
                return loadedConfigurations.get(packageName).get(identifier.split(":")[0])
                        .getRegion(identifier.split(":")[1]);
            }
            return loadedConfigurations.get(packageName).get(identifier);
        } else {
            return null;
        }
    }

    private static boolean isConfigLoaded(String packageName, String identifier) {
        if (identifier.contains(":")) {
            identifier = identifier.split(":")[0];
        }
        return loadedConfigurations.containsKey(packageName)
                && loadedConfigurations.get(packageName).containsKey(identifier);
    }

    static void loadField(Class classToLoad, Field field) {
        if (!staticFields.containsKey(classToLoad)) {
            staticFields.put(classToLoad, new ArrayList<Field>());
            staticFields.get(classToLoad).add(field);
        }
        AutoConfigured annot = field.getAnnotation(AutoConfigured.class);
        String path = annot.path();
        Class<?> type = field.getType();
        Object returnedObject = null;
        Configuration config = null;
        if (field.isAnnotationPresent(Config.class)) {
            Config annotation = field.getAnnotation(Config.class);
            config = getConfiguration(getPackageName(classToLoad.getName()), annotation.value());
        } else {
            config = getDefaultConfiguration(classToLoad);
        }
        boolean list = false;
        if (List.class.isAssignableFrom(type)) {
            if (annot.type() == AutoConfigured.class) {
                System.err.println("List Field " + field.getName() + " from Class "
                        + classToLoad.getSimpleName()
                        + " has no annotated Type! (Ex: @AutoConfigured(value='path', type=YourType.class))");
                return;
            }
            type = annot.type();
            returnedObject = config.getList(path, type);
            list = true;
        } else {
            returnedObject = config.get(path, type);
        }
        if (returnedObject != null) {
            try {
                field.set(null, returnedObject);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                Object defaultValue = field.get(null);
                if (defaultValue != null) {
                    if (list) {
                        config.setList(path, (List) defaultValue);
                    } else {
                        config.set(path, defaultValue);
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static Configuration getDefaultConfiguration(Class classToLoad) {
        if (defaultClassConfigurations.containsKey(classToLoad)) {
            return defaultClassConfigurations.get(classToLoad);
        } else {
            String packageName = getPackageName(classToLoad.getName());
            return defaultConfig.get(packageName);
        }
    }

    private static String getPackageName(String name) {
        return registeredPackageNames.stream().filter((packageName) -> name.startsWith(packageName))
                .findFirst().get();
    }
}
