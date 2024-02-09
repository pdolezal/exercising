package net.yetamine.lectures.osgi.jdbc;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * This component registers {@link DataSource} based on its configuration.
 *
 * <p>
 * The configuration should specify all properties for the connection under
 * properties with {@code connection.} prefix (the properties without the prefix
 * will be passed to the database driver), while it may include properties with
 * {@code database.} prefix to be attached to the registered {@link DataSource}.
 * The usual usage is providing the name of the database for the other services
 * to bind.
 *
 * <p>
 * The component binds to any {@link DataSourceFactory} by default. If there are
 * multiple instances available, it is highly recommended to specify the desired
 * one by providing a suitable filter for the {@code driver} reference (i.e.,
 * adjust {@code driver.target} property).
 */
@Component(
    configurationPid = "net.yetamine.lectures.osgi.jdbc.database",
    configurationPolicy = ConfigurationPolicy.REQUIRE)
public final class Database {

    private ServiceRegistration<?> service;

    @Activate
    public Database(
            @Reference(name = "driver",
                cardinality = ReferenceCardinality.MANDATORY,
                policy = ReferencePolicy.STATIC,
                policyOption = ReferencePolicyOption.GREEDY) DataSourceFactory factory,
            Map<String, ?> configuration,
            BundleContext context)
            throws SQLException {

        final var connectionProperties = new Properties();
        final var dataSourceProperties = new Hashtable<String, Object>();

        configuration.forEach((k, v) -> {
            final var connectionPrefix = "connection.";
            if (k.startsWith(connectionPrefix)) {
                connectionProperties.put(k.substring(connectionPrefix.length()), v);
            } else if (k.startsWith("database.")) {
                dataSourceProperties.put(k, v);
            }
        });

        final var dataSource = factory.createDataSource(connectionProperties);
        service = context.registerService(DataSource.class, dataSource, dataSourceProperties);
    }

    @Deactivate
    void deactivate() {
        final var registration = service;
        service = null;
        if (registration != null) {
            registration.unregister();
        }
    }
}
