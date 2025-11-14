package edu.com.co.Proyecto.Final.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de recursos web para servir archivos estáticos
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Configurar el mapeo de recursos estáticos
		// Esto permite servir archivos desde /imagenes/archivo.webp directamente desde static/imagenes/
		registry.addResourceHandler("/**")
				.addResourceLocations(
					"classpath:/static/",
					"classpath:/static/imagenes/",
					"classpath:/static/css/",
					"classpath:/static/js/")
				.setCachePeriod(31536000);
	}
}
