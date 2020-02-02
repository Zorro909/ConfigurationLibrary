package de.Zorro909.ConfigurationLibrary;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AutoConfigured {

    /**
     * Config Path
     */
    String path();
    
    Class<?> type() default AutoConfigured.class;

}
