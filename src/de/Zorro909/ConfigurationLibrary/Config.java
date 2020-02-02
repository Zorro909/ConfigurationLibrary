package de.Zorro909.ConfigurationLibrary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConfigContainer.class)
public @interface Config {

    String value();

    Class<? extends Configuration> type() default YamlConfiguration.class;
    
    boolean globalDefault() default false;

}
