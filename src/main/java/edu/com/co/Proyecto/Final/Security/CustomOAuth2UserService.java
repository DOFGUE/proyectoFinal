package edu.com.co.Proyecto.Final.Security;

import edu.com.co.Proyecto.Final.Model.roles;
import edu.com.co.Proyecto.Final.Model.usuario;
import edu.com.co.Proyecto.Final.Repository.rolRepository;
import edu.com.co.Proyecto.Final.Repository.usuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio para manejar usuarios OAuth2 (Google) y asignar roles de la base de datos
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	
	@Autowired
	private usuarioRepository usuarioRepository;
	
	@Autowired
	private rolRepository rolRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		
		String email = oAuth2User.getAttribute("email");
		String name = oAuth2User.getAttribute("name");
		String providerId = oAuth2User.getAttribute("sub");
		String provider = userRequest.getClientRegistration().getRegistrationId();
		
		System.out.println("=== OAuth2 Login Attempt ===");
		System.out.println("Email: " + email);
		System.out.println("Name: " + name);
		System.out.println("Provider: " + provider);
		
		// Buscar o crear usuario
		usuario user = usuarioRepository.findByEmailUsuario(email).orElseGet(() -> {
			System.out.println("=== Usuario no encontrado, creando nuevo usuario OAuth2 ===");
			usuario newUser = new usuario();
			
			// Generar un nombre de usuario único basado en el email
			String baseUsername = email.split("@")[0];
			String uniqueUsername = baseUsername;
			int counter = 1;
			while (usuarioRepository.findByNombreUsuario(uniqueUsername).isPresent()) {
				uniqueUsername = baseUsername + counter;
				counter++;
			}
			
			newUser.setNombreUsuario(uniqueUsername);
			newUser.setEmailUsuario(email);
			newUser.setProvider(provider);
			newUser.setProviderId(providerId);
			// Contraseña placeholder para usuarios OAuth2 (no utilizable para login tradicional)
			// Usando un UUID aleatorio con BCrypt para asegurar que no pueda ser adivinada
			String randomPassword = java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID().toString();
			newUser.setContrasenaUsuario(passwordEncoder.encode(randomPassword)); // Encriptada con BCrypt
			// Campos opcionales con valores por defecto
			newUser.setNumeroTelefonoUsuario(null);
			newUser.setDescripcionUsuario("Usuario registrado con " + provider);
			
			System.out.println("Buscando rol USER...");
			// Buscar primero USER, si no existe buscar ROLE_USER
			roles userRole = rolRepository.findByNombreRol("USER")
				.orElseGet(() -> rolRepository.findByNombreRol("ROLE_USER")
					.orElseThrow(() -> new RuntimeException("Role USER or ROLE_USER not found in database")));
			newUser.setRol(userRole);
			
			System.out.println("Guardando usuario en BD: " + email + " con username: " + uniqueUsername);
			usuario savedUser = usuarioRepository.save(newUser);
			System.out.println("Usuario guardado con ID: " + savedUser.getIdUsuario());
			return savedUser;
		});
		
		System.out.println("Usuario encontrado/creado - ID: " + user.getIdUsuario() + ", Email: " + user.getEmailUsuario());
		
		// Si el usuario existe pero no tiene provider, actualizarlo
		if (user.getProvider() == null) {
			System.out.println("Actualizando usuario existente con información de OAuth2");
			user.setProvider(provider);
			user.setProviderId(providerId);
			usuarioRepository.save(user);
		}
		
		// Obtener el rol de la BD y agregar el prefijo ROLE_ si no lo tiene
		String roleName = user.getRol().getNombreRol();
		String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
		System.out.println("OAuth2 User Authority (from DB): " + roleName);
		System.out.println("OAuth2 User Authority (final): " + authority);
		
		// Retornar OAuth2User con el rol correcto de la base de datos
		return new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority(authority)),
			oAuth2User.getAttributes(),
			"email"
		);
	}
}
