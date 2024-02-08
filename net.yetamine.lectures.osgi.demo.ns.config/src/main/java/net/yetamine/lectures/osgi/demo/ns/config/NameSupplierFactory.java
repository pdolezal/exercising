package net.yetamine.lectures.osgi.demo.ns.config;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.converter.Converters;

/**
 * Creates and registers {@link NameSupplier} instances with different
 * parameters provided by configuration objects.
 */
public final class NameSupplierFactory implements ManagedServiceFactory, AutoCloseable {

    private final BundleContext context;
    private final Map<String, ServiceRegistration<?>> registrations = new HashMap<>();

    /**
     * Creates a new instance.
     *
     * @param givenContext
     *            the context to use for registering the services. It must not be
     *            {@code null}.
     */
    public NameSupplierFactory(BundleContext givenContext) {
        context = Objects.requireNonNull(givenContext);
    }

    /**
     * @see org.osgi.service.cm.ManagedServiceFactory#getName()
     */
    @Override
    public String getName() {
        return getClass().getName();
    }

    /**
     * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String,
     *      java.util.Dictionary)
     */
    @Override
    public synchronized void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
        unregister(pid); // Since we use immutable implementation, just unregister the service

        final String name = name(properties);
        if (name == null) {
            return;
        }

        registrations.put(pid, context.registerService(NameSupplier.class, new FixedNameSupplier(name), null));
    }

    /**
     * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
     */
    @Override
    public synchronized void deleted(String pid) {
        unregister(pid);
    }

    /**
     * Unregisters all registered services.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public synchronized void close() {
        registrations.values().forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    private void unregister(String pid) {
        final ServiceRegistration<?> registration = registrations.remove(pid);
        if (registration != null) {
            registration.unregister();
        }
    }

    private static String name(Dictionary<String, ?> properties) {
        // Simple variant: no Converter, no nick names etc.
        // return Objects.toString(properties.get("name"), null);

        final NameConfiguration config = Converters.standardConverter().convert(properties).to(NameConfiguration.class);
        final String name = config.name();
        final String nick = config.nick();
        if (nick.isEmpty()) {
            return name;
        }

        final String[] words = name.split(" ");
        if (words.length < 2) {
            return name;
        }

        return Stream.of(
            Stream.of(words).limit(words.length - 1),   // All names but the last
            Stream.of('"' + nick + '"'),                // Quoted nick name
            Stream.of(words[words.length - 1])          // Surname at the end
        ).flatMap(Function.identity()).collect(Collectors.joining(" "));
    }
}
