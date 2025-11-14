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
import java.util.Set;

@Service
public class usuarioDetailsService implements UserDetailsService {
	
	@Autowired
	private usuarioRepository usuarioRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		usuario usuario = usuarioRepository.findByNombreUsuario(username)
			.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
		
		// Crear las autoridades basadas en el rol del usuario
		Set<GrantedAuthority> authorities = new HashSet<>();
		if (usuario.getRol() != null) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombreRol()));
		}
		
		return User.builder()
			.username(usuario.getNombreUsuario())
			.password(usuario.getContrasenaUsuario())
			.authorities(authorities)
			.accountExpired(false)
			.accountLocked(false)
			.credentialsExpired(false)
			.disabled(false)
			.build();
	}

}
