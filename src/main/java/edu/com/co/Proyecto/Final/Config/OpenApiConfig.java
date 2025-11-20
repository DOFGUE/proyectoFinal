package edu.com.co.Proyecto.Final.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	
	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "bearerAuth";
		
		return new OpenAPI()
			.info(new Info()
				.title("Bakery and Pastry Shop | Laura Amaya")
				.version("1.0.0")
				.description("API REST de Bakery and Pastry Shop | Laura Amaya con autenticación JWT y Spring Security, inclutye tambien endpoints y documentacion para la gestion de usuarios, productos y reseñas. ")
				.contact(new Contact()
					.name("Equipo JojatSoftware S.A.S")
					.email("u20241220741@usco.edu.co")
					.url("localhot:8080/home"))
				.license(new License()
					.name("Apache 2.0")
					.url("https://www.apache.org/licenses/LICENSE-2.0.html")))
			.addSecurityItem(new SecurityRequirement()
				.addList(securitySchemeName))
			.components(new Components()
				.addSecuritySchemes(securitySchemeName, new SecurityScheme()
					.name(securitySchemeName)
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("Ingrese el token JWT obtenido del endpoint /api/auth/login")));
	}
}
