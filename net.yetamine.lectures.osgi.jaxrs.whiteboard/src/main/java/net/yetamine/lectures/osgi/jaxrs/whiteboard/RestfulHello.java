package net.yetamine.lectures.osgi.jaxrs.whiteboard;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.osgi.service.component.annotations.Component;

@Path("/rest/hello")
@Component(service = RestfulHello.class, property = { "osgi.jaxrs.resource=true" })
public final class RestfulHello {

    @Path("/")
    @GET
    @Produces("text/plain")
    public String root() {
        return "Append your name as the next path component.";
    }

    @Path("/{id}")
    @GET
    @Produces("application/json")
    public Map<String, ?> jsonGreeting(@PathParam("id") String id) {
        final Map<String, String> result = new HashMap<>();
        result.put("greeting", "hello");
        result.put("name", id);
        return result;
    }

    @Path("/{id}")
    @GET
    @Produces("text/plain")
    public String textGreeting(@PathParam("id") String id) {
        return "Hello " + id;
    }
}
