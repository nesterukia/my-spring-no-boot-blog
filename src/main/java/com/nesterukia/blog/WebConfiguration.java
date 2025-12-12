package com.nesterukia.blog;

import com.nesterukia.blog.config.YamlPropertySourceFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.nesterukia.blog"})
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class WebConfiguration {}
