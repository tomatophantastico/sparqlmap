package org.aksw.sparqlmap.web.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "org.aksw.sparqlmap.web", "org.aksw.sparqlmap.spring" })
public class WebAppConfig extends WebMvcConfigurerAdapter {
	
	@Autowired
	org.springframework.core.env.Environment env;
	

	
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    registry.addResourceHandler("/assets/**").addResourceLocations("/assets/");
	    registry.addResourceHandler("/snorql/**").addResourceLocations("/snorql/");

	    registry.addResourceHandler("/*.html").addResourceLocations("/");
	    registry.addResourceHandler("/*.jsp").addResourceLocations("/");
	    registry.setOrder(0);
	}
	  


	

}
