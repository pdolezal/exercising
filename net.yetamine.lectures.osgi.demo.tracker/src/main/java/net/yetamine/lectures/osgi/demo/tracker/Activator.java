package net.yetamine.lectures.osgi.demo.tracker;

import java.util.Map;
import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Registers a tracker that greets coming and leaving {@link NameSupplier}
 * services.
 */
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public final class Activator implements BundleActivator {

    private ServiceTracker<NameSupplier, NameSupplier> nameSuppliers;

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

        // Nowadays Java compiler can often handle anonymous generic types (like here),
        // we leave generic type parameters here for better readability
        nameSuppliers = new ServiceTracker<NameSupplier, NameSupplier>(context, NameSupplier.class, null) {

            @Override
            public NameSupplier addingService(ServiceReference<NameSupplier> reference) {
                final NameSupplier result = super.addingService(reference);
                System.out.println("[Tracker] Hello " + result.name());
                return result;
            }

            @Override
            public void removedService(ServiceReference<NameSupplier> reference, NameSupplier service) {
                System.out.println("[Tracker] Bye " + service.name());
                super.removedService(reference, service);
            }
        };

        nameSuppliers.open();

        final Map<ServiceReference<NameSupplier>, NameSupplier> snapshot = nameSuppliers.getTracked();
        System.out.format("Currently %d services tracked:%n%s%n", snapshot.size(), snapshot.values());
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping " + context.getBundle());
        nameSuppliers.close(); // Not necessary since the framework unregisters all when bundle stops
    }
}
