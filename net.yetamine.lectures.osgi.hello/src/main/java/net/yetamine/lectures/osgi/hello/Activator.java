package net.yetamine.lectures.osgi.hello;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Implements the <i>Hello World</i> in OSGi.
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
        System.out.println("Hello OSGi!");
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Bye OSGi!");
    }
}
