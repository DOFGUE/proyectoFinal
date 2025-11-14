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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class securityConfig {
	
	@Autowired
	private usuarioDetailsService usuarioDetailsService;
	
	/**
	 * Configuración del encoder de contraseñas
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
	
	/**
	 * Configuración del proveedor de autenticación
	 */
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(usuarioDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
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
				.requestMatchers("/", "/home", "/login", "/signup", "/css/**", "/imagenes/**", "/js/**").permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
				.requestMatchers("/access-denied").permitAll()
				.anyRequest().authenticated()
			)
			
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
			.httpBasic(basic -> basic.disable())
			.anonymous(anon -> anon.disable());
		
		return http.build();
	}

}
