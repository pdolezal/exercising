package net.yetamine.lectures.osgi.demo.cdi.provider;

import javax.enterprise.context.ApplicationScoped;
import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.service.cdi.annotations.Service;

/**
 * Implements a {@link NameSupplier} with a fixed name.
 */
@Service
@ApplicationScoped
public class ServiceBean implements NameSupplier {

    /**
     * Creates a new instance.
     */
    public ServiceBean() {
        // Default constructor
    }

    /**
     * @see net.yetamine.lectures.osgi.demo.api.NameSupplier#name()
     */
    @Override
    public String name() {
        return "CDI";
    }
}
