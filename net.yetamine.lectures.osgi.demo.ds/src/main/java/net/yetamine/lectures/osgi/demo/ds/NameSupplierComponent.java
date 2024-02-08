package net.yetamine.lectures.osgi.demo.ds;

import java.util.Map;
import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

/**
 * Implements {@link NameSupplier} that uses the value from the configuration,
 * which is required for the component to be satisfied.
 */
@Component(configurationPid = "net.yetamine.lectures.osgi.demo.ds", configurationPolicy = ConfigurationPolicy.REQUIRE)
public final class NameSupplierComponent implements NameSupplier {

    private volatile String name;

    /**
     * Creates a new instance.
     */
    public NameSupplierComponent() {
        // Default constructor
    }

    /**
     * @see net.yetamine.lectures.osgi.demo.api.NameSupplier#name()
     */
    @Override
    public String name() {
        return name;
    }

    // Component lifecycle methods

    @Activate
    void activate(Map<String, ?> configuration) {
        System.out.println("Activating " + this);
        update(configuration);
    }

    @Modified
    void update(Map<String, ?> configuration) {
        name = configuration.get("name").toString(); // For the simplicity no checks etc. are employed
    }

    @Deactivate
    void deactivate() {
        System.out.println("Deactivating " + this);
    }
}
