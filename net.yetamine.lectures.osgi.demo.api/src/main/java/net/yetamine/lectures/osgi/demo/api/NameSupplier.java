package net.yetamine.lectures.osgi.demo.api;

import org.osgi.annotation.versioning.ProviderType;

/**
 * A name supplier.
 */
@ProviderType
@FunctionalInterface
public interface NameSupplier {

    /**
     * @return a name, never an empty string or {@code null}
     */
    String name();
}
