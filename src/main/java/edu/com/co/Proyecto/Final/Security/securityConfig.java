package edu.com.co.Proyecto.Final.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class securityConfig {
	
	@Autowired
	private usuarioDetailsService usuarioDetailsService;
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/**
	 * Configuración del proveedor de autenticación
	 */
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(usuarioDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		authProvider.setHideUserNotFoundExceptions(true);
		return authProvider;
	}
	
	/**
	 * Configuración del AuthenticationManager
	 */
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder
				.authenticationProvider(authenticationProvider());
		return authenticationManagerBuilder.build();
	}
	
	/**
	 * Configuración de la cadena de filtros de seguridad
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// Configuración de autorización por rutas
			.authorizeHttpRequests(authz -> authz
				// Endpoints públicos
				.requestMatchers("/", "/home", "/login", "/signup", "/css/**", "/imagenes/**", "/js/**").permitAll()
				// Endpoints de Swagger UI (públicos para documentación)
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
				// Endpoints de autenticación JWT (API REST)
				.requestMatchers("/api/auth/**").permitAll()
				// Endpoints de API con protección JWT
				.requestMatchers("/api/admin/**").hasRole("ADMIN")
				.requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
				// Endpoints web tradicionales
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
				.requestMatchers("/access-denied").permitAll()
				.anyRequest().authenticated()
			)
			
			// Agregar el filtro JWT antes del filtro de autenticación de usuario/contraseña
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			
			// Configuración de sesión
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.sessionConcurrency(concurrency -> concurrency
					.maximumSessions(1)
					.expiredUrl("/login?expired")
				)
			)
			
			// Configuración de headers de seguridad
			.headers(headers -> headers
				.cacheControl()
				.and()
				.xssProtection()
				.and()
				.contentSecurityPolicy("default-src 'self' https://cdn.jsdelivr.net; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; img-src 'self' data: https:; font-src 'self' https://cdn.jsdelivr.net")
				.and()
				.frameOptions().sameOrigin()
			)
			
			// Configuración de login
			.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.usernameParameter("username")
				.passwordParameter("password")
				.successHandler((request, response, authentication) -> {
					// Redirige según el rol del usuario
					if (authentication.getAuthorities().stream()
						.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
						response.sendRedirect("/admin/home");
					} else {
						response.sendRedirect("/user/home");
					}
				})
				.failureUrl("/login?error=true")
				.permitAll()
			)
			
			// Configuración de OAuth2 Login (Google)
			.oauth2Login(oauth2 -> oauth2
				.loginPage("/login")
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)
				)
				.successHandler((request, response, authentication) -> {
					// Debug: Mostrar las autoridades del usuario
					System.out.println("OAuth2 Authentication successful!");
					System.out.println("Principal: " + authentication.getPrincipal());
					System.out.println("Authorities: " + authentication.getAuthorities());
					authentication.getAuthorities().forEach(auth -> 
						System.out.println("  - Authority: " + auth.getAuthority())
					);
					
					// Redirige según el rol del usuario
					if (authentication.getAuthorities().stream()
						.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
						System.out.println("Redirecting to /admin/home");
						response.sendRedirect("/admin/home");
					} else {
						System.out.println("Redirecting to /user/home");
						response.sendRedirect("/user/home");
					}
				})
			)
			
			// Configuración de logout
			.logout(logout -> logout
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.logoutSuccessUrl("/home")
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.deleteCookies("JSESSIONID")
				.permitAll()
			)
			
			// Configuración de manejo de excepciones
			.exceptionHandling(exception -> exception
				.accessDeniedPage("/access-denied")
				.authenticationEntryPoint((request, response, authException) -> 
					response.sendRedirect("/login?error=unauthorized")
				)
			)
			
			// CSRF Protection
			.csrf(csrf -> csrf.disable())
			
			// Seguridad adicional
			.httpBasic(basic -> basic.disable());
		
		return http.build();
	}

}
