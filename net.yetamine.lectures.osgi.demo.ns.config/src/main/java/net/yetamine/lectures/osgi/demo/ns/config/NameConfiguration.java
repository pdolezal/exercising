package net.yetamine.lectures.osgi.demo.ns.config;

import org.osgi.util.converter.Converter;

/**
 * Configuration definition to show some powers of {@link Converter}.
 */
public @interface NameConfiguration {

    /**
     * @return the name
     */
    String name();

    /**
     * @return an optional nick name that may be added to the name when the
     *         implementation supports nick names
     */
    String nick() default "";
}
