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

import de.Zorro909.AnnotationProcessor.AnnotationProcessor;
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
    private static Thread autoReloadThread = null;

    public static boolean registerLibrary(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false),
                new TypeAnnotationsScanner(), new FieldAnnotationsScanner());
        searchForNewSerializers(reflections);
        registeredPackageNames.add(packageName);

        AnnotationProcessor<Config> configProcessor = new AnnotationProcessor<>(Config.class);
        final HashMap<String, Configuration> configurations = new HashMap<>();
        loadedConfigurations.put(packageName, configurations);
        configProcessor.registerClassAnnotationVerifier((Config config, Class clazz) -> {
            System.out.println("Found ConfigClass");
            Configuration conf = null;
            if (!isConfigLoaded(getPackageName(clazz.getName()), config.value())) {
                try {
                    conf = ConfigurationBuilder.createConfig(config,
                            getDefaultConfiguration(clazz));
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
            return true;
        });

        configProcessor.registerFieldSetter((annot, field, container) -> {
            if (!isConfigLoaded(packageName, annot.value())) {
                try {
                    Configuration conf = ConfigurationBuilder.createConfig(annot,
                            getDefaultConfiguration(container));
                    configurations.put(conf.getIdentifier(), conf);
                    if (field.getType().isAssignableFrom(conf.getClass())) {
                        return conf;
                    }
                    return null;
                } catch (NoSuchMethodException | IllegalArgumentException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return getConfiguration(packageName, annot.value());
            }
        });
        configProcessor.processPackage(packageName);

        AnnotationProcessor<AutoConfigured> autoConfiguredProcessor = new AnnotationProcessor<>(
                AutoConfigured.class);

        autoConfiguredProcessor.registerFieldSetter((annot, field, container, instance) -> {
            if (Modifier.isStatic(field.getModifiers())) {
                System.out.println("Load Field " + field.getName());
                return loadField(container, annot, field, instance);
            } else {
                System.err.println("ERROR: Field " + field.getName()
                        + " is a non static Field annotated with @AutoConfigured which is unsupported!");
                return null;
            }
        });
        autoConfiguredProcessor.processPackage(packageName);
        return true;
    }

    public static void save(String packageName) {
        for (List<Field> fields : staticFields.values()) {
            for (Field field : fields) {
                AutoConfigured annot = field.getAnnotation(AutoConfigured.class);
                String path = annot.path();
                Class<?> type = field.getType();
                Configuration config = null;
                if (field.isAnnotationPresent(Config.class)) {
                    Config annotation = field.getAnnotation(Config.class);
                    config = getConfiguration(getPackageName(field.getDeclaringClass().getName()),
                            annotation.value());
                } else {
                    config = getDefaultConfiguration(field.getDeclaringClass());
                }
                try {
                    if (List.class.isAssignableFrom(type)) {
                        config.setList(path, (List) field.get(null));
                    } else {
                        config.set(path, field.get(null));
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
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

    static Object loadField(Class classToLoad, AutoConfigured annot, Field field, Object instance) {
        if (!staticFields.containsKey(classToLoad)) {
            staticFields.put(classToLoad, new ArrayList<Field>());
        }
        if (!staticFields.get(classToLoad).contains(field)) {
            staticFields.get(classToLoad).add(field);
        }
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
                return null;
            }
            type = annot.type();
            returnedObject = config.getList(path, type);
            list = true;
        } else {
            returnedObject = config.get(path, type);
        }
        if (returnedObject != null) {
            return returnedObject;
        } else {
            if (instance != null) {
                throw new RuntimeException("Instanced AutoConfigured Values like " + annot.path()
                        + " always need to have a Value in the Config!");
            }
            try {
                returnedObject = field.get(null);
                System.out.println(returnedObject);
                if (list) {
                    config.setList(path, (List) returnedObject);
                } else {
                    config.set(path, returnedObject);
                }
                return returnedObject;
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
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

    public static void enableAutoreload(String packageName) {
        enableAutoreload(packageName, 60);
    }

    public static void enableAutoreload(final String packageName, final int reloadTimeSeconds) {
        autoReloadThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(reloadTimeSeconds * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                loadedConfigurations.clear();
                for (Class clazz : staticFields.keySet()) {
                    for (Field field : staticFields.get(clazz)) {
                        loadField(clazz, field.getAnnotation(AutoConfigured.class), field, null);
                    }
                }
            }
        });
        autoReloadThread.setDaemon(true);
        autoReloadThread.start();
    }
}
