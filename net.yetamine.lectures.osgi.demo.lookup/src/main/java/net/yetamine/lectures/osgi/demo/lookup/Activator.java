package net.yetamine.lectures.osgi.demo.lookup;

import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Uses direct service lookup for greeting the most preferred
 * {@link NameSupplier} service.
 */
public final class Activator implements BundleActivator {

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
        say("Hello", context);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping " + context.getBundle());
        say("Bye", context);
    }

    private static void say(String greeting, BundleContext context) {
        // Be careful, basically anything may return null and even when a step
        // succeeds, the next one may fail as something changes meanwhile
        final ServiceReference<NameSupplier> reference = context.getServiceReference(NameSupplier.class);
        if (reference == null) {
            return;
        }

        final NameSupplier service = context.getService(reference);
        if (service == null) {
            return;
        }

        try {
            System.out.println("[Lookup] " + greeting + " " + service.name());
        } finally {
            context.ungetService(reference); // Balance the service usage counter!
        }
    }
}
