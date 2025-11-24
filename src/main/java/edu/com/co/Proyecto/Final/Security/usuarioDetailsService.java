package edu.com.co.Proyecto.Final.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.com.co.Proyecto.Final.Model.usuario;
import edu.com.co.Proyecto.Final.Repository.usuarioRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class usuarioDetailsService implements UserDetailsService {
	
	@Autowired
	private usuarioRepository usuarioRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Intentar buscar por nombre de usuario primero
		Optional<usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(username);
		
		// Si no se encuentra, intentar buscar por email (para OAuth2)
		if (usuarioOpt.isEmpty()) {
			usuarioOpt = usuarioRepository.findByEmailUsuario(username);
		}
		
		usuario usuario = usuarioOpt
			.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
		
		// Crear las autoridades basadas en el rol del usuario
		Set<GrantedAuthority> authorities = new HashSet<>();
		if (usuario.getRol() != null) {
			String roleName = usuario.getRol().getNombreRol();
			// Agregar el prefijo ROLE_ si no lo tiene
			String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
			authorities.add(new SimpleGrantedAuthority(authority));
		}
		
		// Para usuarios OAuth2 sin contraseña, usar un placeholder
		String password = usuario.getContrasenaUsuario();
		if (password == null || password.isEmpty()) {
			password = "{noop}"; // Usuarios OAuth2 no usan contraseña
		}
		
		return User.builder()
			.username(usuario.getNombreUsuario())
			.password(password)
			.authorities(authorities)
			.accountExpired(false)
			.accountLocked(false)
			.credentialsExpired(false)
			.disabled(false)
			.build();
	}

}
