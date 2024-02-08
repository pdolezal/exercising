package net.yetamine.lectures.osgi.demo.ns.config;

import java.util.Objects;
import net.yetamine.lectures.osgi.demo.api.NameSupplier;

/**
 * Implements an immutable {@link NameSupplier}.
 */
public final class FixedNameSupplier implements NameSupplier {

    private final String name;

    /**
     * Creates a new instance.
     *
     * @param givenName
     *            the name to return. It must not be {@code null}.
     */
    public FixedNameSupplier(String givenName) {
        name = Objects.requireNonNull(givenName);
    }

    /**
     * @see net.yetamine.lectures.osgi.demo.api.NameSupplier#name()
     */
    @Override
    public String name() {
        return name;
    }
}
