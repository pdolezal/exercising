package net.yetamine.lectures.osgi.demo.ns.config;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;
import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;

/**
 * Demonstrates multiple ways of using Configuration Admin.
 */
@RequireConfigurationAdmin
public final class Activator implements BundleActivator {

    /* Notes:
    *
    * @RequireConfigurationAdmin is available since Configuration Admin 1.6 and
    * it causes Require-Capability header requiring the Configuration Admin to
    * be available.
    */

    private static final String PID = "net.yetamine.lectures.osgi.demo.ns.config";

    private static final String CONFIG_NAME = "name";

    private ServiceRegistration<?> singletonSupplierRegistration;
    private ServiceRegistration<?> supplierFactoryRegistration;
    private NameSupplierFactory supplierFactory;

    /**
     * Creates a new instance.
     */
    public Activator() {
        // Default constructor
    }

    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting " + context.getBundle());

        singletonSupplierRegistration = context.registerService(NameSupplier.class,
            newSingletonNameSupplier(context),
            null);

        supplierFactory = new NameSupplierFactory(context);
        final Dictionary<String, Object> supplierFactoryProperties = new Hashtable<>();
        supplierFactoryProperties.put(Constants.SERVICE_PID, PID);
        supplierFactoryRegistration = context.registerService(ManagedServiceFactory.class,
            supplierFactory,
            supplierFactoryProperties);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping " + context.getBundle());

        // Not necessary since the framework unregisters all when bundle stops
        supplierFactoryRegistration.unregister();
        singletonSupplierRegistration.unregister();
        supplierFactory.close();
    }

    private static NameSupplier newSingletonNameSupplier(BundleContext context)
            throws ConfigurationException, IOException {

        return new FixedNameSupplier(configuredName(context));
    }

    private static String configuredName(BundleContext context) throws ConfigurationException, IOException {
        // The service might be missing if not using @RequireConfigurationAdmin/Require-Capability,
        // or in some corner cases of race conditions. However, it is probably cleaner to check it
        // for null even under the assumption that the required capability should be available.
        final ServiceReference<ConfigurationAdmin> reference = context.getServiceReference(ConfigurationAdmin.class);

        final ConfigurationAdmin service;
        if ((reference == null) || ((service = context.getService(reference)) == null)) {
            throw new ConfigurationException(CONFIG_NAME, "Missing Configuration Admin.", null);
        }

        try {
            return Optional.of(service.getConfiguration(PID))   // Never returns null, rather creates a new object
                .map(Configuration::getProperties)              // However, this may return null for empty properties!
                .map(properties -> properties.get(CONFIG_NAME)) // Some value...
                .map(Object::toString)                          // ...which needs converting to something more useful
                .orElseThrow(() -> new ConfigurationException(CONFIG_NAME, "Missing value.", null));
        } finally {
            context.ungetService(reference);
        }
    }
}
