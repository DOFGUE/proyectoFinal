package edu.com.co.Proyecto.Final.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración del PasswordEncoder separada para evitar dependencias circulares
 */
@Configuration
public class PasswordEncoderConfig {
	
	/**
	 * Bean del PasswordEncoder con BCrypt (12 rounds)
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
}
