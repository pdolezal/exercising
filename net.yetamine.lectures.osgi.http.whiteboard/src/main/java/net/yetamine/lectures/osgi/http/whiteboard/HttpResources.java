package net.yetamine.lectures.osgi.http.whiteboard;

import org.osgi.service.component.annotations.Component;

@Component(
    service = HttpResources.class,
    property = {
        "osgi.http.whiteboard.resource.pattern=/hello/*",
        "osgi.http.whiteboard.resource.prefix=/resources"
    })
public final class HttpResources {
    // Nothing there, serves just to specify the resources
}
