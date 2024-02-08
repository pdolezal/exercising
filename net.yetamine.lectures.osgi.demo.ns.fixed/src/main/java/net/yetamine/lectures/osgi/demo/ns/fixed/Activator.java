package net.yetamine.lectures.osgi.demo.ns.fixed;

import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Registers a single {@link NameSupplier} that provides a fixed hardwired name.
 */
public final class Activator implements BundleActivator {

    private ServiceRegistration<?> serviceRegistration;

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
        serviceRegistration = context.registerService(NameSupplier.class, new FixedNameSupplier("John Doe"), null);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping " + context.getBundle());
        serviceRegistration.unregister(); // Not necessary since the framework unregisters all when bundle stops
    }
}
