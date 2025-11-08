package com.nesterukia.blog;

import com.nesterukia.blog.config.AppConfig;
import com.nesterukia.blog.config.EmbeddedServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@Slf4j
public class BlogApplication {
    public static void main(String[] args) {
        try {
            AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
            appContext.register(AppConfig.class);
            Environment environment = appContext.getEnvironment();

            EmbeddedServer server = new EmbeddedServer(environment, appContext);
            server.start();
         } catch (Exception ex) {
            log.error("Error during server start.", ex);
        }
    }
}
