package net.yetamine.lectures.osgi.demo.ds;

import java.util.ArrayList;
import java.util.Collection;
import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Demonstrates using references with binding via methods. This service binds
 * all suppliers matching the reference's target (or all if the target is not
 * specified) and reacts on all changes using the dynamic and greedy policies.
 * The component is immediate, hence starts as soon as possible.
 */
@Component(immediate = true)
public final class NamePrinter {

    private final Collection<NameSupplier> suppliers = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public NamePrinter() {
        // Default constructor
    }

    // Methods for binding references

    @Reference(name = "names",
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY)
    synchronized void bindSupplier(NameSupplier supplier) {
        suppliers.add(supplier);
        System.out.println("[DS] Hello " + supplier.name());
    }

    synchronized void unbindSupplier(NameSupplier supplier) {
        suppliers.remove(supplier);
        System.out.println("[DS] Bye " + supplier.name());
    }
}
