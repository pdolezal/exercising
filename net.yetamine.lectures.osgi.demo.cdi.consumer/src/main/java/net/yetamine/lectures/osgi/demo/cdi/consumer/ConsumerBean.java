package net.yetamine.lectures.osgi.demo.cdi.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import net.yetamine.lectures.osgi.demo.api.NameSupplier;
import org.osgi.service.cdi.annotations.Reference;

/**
 * Tells hello using a {@link NameSupplier}.
 */
@ApplicationScoped
public class ConsumerBean {

    @Reference
    @Inject
    private NameSupplier service;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object start) {
        System.out.println("[CDI] Hello " + service.name());
    }
}
