package net.yetamine.lectures.osgi.http.whiteboard;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
    service = Servlet.class,
    scope = ServiceScope.PROTOTYPE,
    property = {
        "osgi.http.whiteboard.servlet.pattern=/hello/secret"
    })
public final class SecretServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init(ServletConfig config) {
        // Do nothing
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("Do not tell it to anyone!");
    }
}
