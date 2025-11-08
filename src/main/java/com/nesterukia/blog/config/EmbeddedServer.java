package com.nesterukia.blog.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.springframework.core.env.Environment;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Slf4j
public class EmbeddedServer {
    private Server server;
    private int port;
    private static final int DEFAULT_PORT = 8080;
    private static final String PORT_PROPERTY_KEY = "blog.server.port";

    public EmbeddedServer(Environment environment, AnnotationConfigWebApplicationContext appContext) {
        this.port = Integer.parseInt(environment.getProperty(PORT_PROPERTY_KEY, String.valueOf(DEFAULT_PORT)));
    }

    public void start() throws Exception {
        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(AppConfig.class);

        appContext.setServletContext(context.getServletContext());

        DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
        ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
        context.addServlet(servletHolder, "/");

        context.addEventListener(new ContextLoaderListener(appContext));

        server.setHandler(context);
        server.start();
        server.join();
        log.info("Started BLOG backend on port {}", this.port);
    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}