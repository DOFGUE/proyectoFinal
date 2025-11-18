package edu.com.co.Proyecto.Final.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.com.co.Proyecto.Final.Model.usuario;
import edu.com.co.Proyecto.Final.Model.roles;
import edu.com.co.Proyecto.Final.Repository.usuarioRepository;

@Service
public class usuarioService {
	
	@Autowired
	private usuarioRepository usuarioRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private rolService rolService;
	
	// ...existing code...
	public List<usuario> obtenerTodosUsuarios() {
		return usuarioRepo.findAll();
	}
	
	// Obtener usuario por ID
	public Optional<usuario> obtenerUsuarioPorId(Long idUsuario) {
		return usuarioRepo.findById(idUsuario);
	}
	
	// Obtener usuario por nombre
	public Optional<usuario> obtenerUsuarioPorNombre(String nombreUsuario) {
		return usuarioRepo.findByNombreUsuario(nombreUsuario);
	}
	
	// Registrar nuevo usuario (desde signup)
	public usuario registrarNuevoUsuario(String nombreUsuario, String emailUsuario, String contrasenaUsuario, 
										 String confirmPassword, Long numeroTelefonoUsuario, String descripcionUsuario) {
		
		// Validar que los campos obligatorios no estén vacíos
		if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
		}
		if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
			throw new IllegalArgumentException("El email no puede estar vacío");
		}
		if (contrasenaUsuario == null || contrasenaUsuario.trim().isEmpty()) {
			throw new IllegalArgumentException("La contraseña no puede estar vacía");
		}
		if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
			throw new IllegalArgumentException("La confirmación de contraseña no puede estar vacía");
		}
		if (numeroTelefonoUsuario == null) {
			throw new IllegalArgumentException("El número de teléfono no puede estar vacío");
		}
		
		// Validar que las contraseñas coincidan
		if (!contrasenaUsuario.equals(confirmPassword)) {
			throw new IllegalArgumentException("Las contraseñas no coinciden");
		}
		
		// Validar formato de email
		if (!validarFormatoEmail(emailUsuario)) {
			throw new IllegalArgumentException("El formato del email no es válido");
		}
		
		// Validar que el usuario no exista
		if (usuarioRepo.findByNombreUsuario(nombreUsuario).isPresent()) {
			throw new IllegalArgumentException("El nombre de usuario ya existe");
		}
		
		// Obtener el rol USER automáticamente
		roles rolUser = rolService.obtenerRolPorNombre("USER")
			.orElseThrow(() -> new IllegalArgumentException("El rol USER no existe en el sistema"));
		
		// Crear nuevo usuario
		usuario nuevoUsuario = new usuario();
		nuevoUsuario.setNombreUsuario(nombreUsuario);
		nuevoUsuario.setEmailUsuario(emailUsuario);
		nuevoUsuario.setContrasenaUsuario(passwordEncoder.encode(contrasenaUsuario));
		nuevoUsuario.setNumeroTelefonoUsuario(numeroTelefonoUsuario);
		nuevoUsuario.setDescripcionUsuario(descripcionUsuario != null ? descripcionUsuario : "");
		nuevoUsuario.setRol(rolUser);
		
		return usuarioRepo.save(nuevoUsuario);
	}
	
	// Crear nuevo usuario
	public usuario crearUsuario(usuario usuario) {
		if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
		}
		if (usuario.getEmailUsuario() == null || usuario.getEmailUsuario().trim().isEmpty()) {
			throw new IllegalArgumentException("El email no puede estar vacío");
		}
		if (!validarFormatoEmail(usuario.getEmailUsuario())) {
			throw new IllegalArgumentException("El formato del email no es válido");
		}
		if (usuario.getContrasenaUsuario() == null || usuario.getContrasenaUsuario().trim().isEmpty()) {
			throw new IllegalArgumentException("La contraseña no puede estar vacía");
		}
		if (usuario.getRol() == null) {
			throw new IllegalArgumentException("El usuario debe tener un rol asignado");
		}
		
		// Verificar que el usuario no exista
		if (usuarioRepo.findByNombreUsuario(usuario.getNombreUsuario()).isPresent()) {
			throw new IllegalArgumentException("El nombre de usuario ya existe");
		}
		
		// Encriptar contraseña
		usuario.setContrasenaUsuario(passwordEncoder.encode(usuario.getContrasenaUsuario()));
		
		return usuarioRepo.save(usuario);
	}
	
	// Actualizar usuario
	public usuario actualizarUsuario(Long idUsuario, usuario usuarioActualizado) {
		Optional<usuario> usuarioExistente = usuarioRepo.findById(idUsuario);
		
		if (!usuarioExistente.isPresent()) {
			throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe");
		}
		
		usuario usuario = usuarioExistente.get();
		
		if (usuarioActualizado.getEmailUsuario() != null && !usuarioActualizado.getEmailUsuario().isEmpty()) {
			if (!validarFormatoEmail(usuarioActualizado.getEmailUsuario())) {
				throw new IllegalArgumentException("El formato del email no es válido");
			}
			usuario.setEmailUsuario(usuarioActualizado.getEmailUsuario());
		}
		if (usuarioActualizado.getNumeroTelefonoUsuario() != null) {
			usuario.setNumeroTelefonoUsuario(usuarioActualizado.getNumeroTelefonoUsuario());
		}
		if (usuarioActualizado.getDescripcionUsuario() != null) {
			usuario.setDescripcionUsuario(usuarioActualizado.getDescripcionUsuario());
		}
		if (usuarioActualizado.getRol() != null) {
			usuario.setRol(usuarioActualizado.getRol());
		}
		
		return usuarioRepo.save(usuario);
	}
	
	/**
	 * Actualizar perfil de usuario con validaciones completas
	 * Valida email, teléfono y descripción según reglas de negocio
	 */
	public usuario actualizarPerfilUsuario(Long idUsuario, String email, Long telefono, String descripcion) {
		// Validar que el usuario existe
		usuario usuario = usuarioRepo.findById(idUsuario)
			.orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
		
		// Validar email obligatorio
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("El correo electrónico es obligatorio");
		}
		
		// Validar formato de email
		if (!validarFormatoEmail(email.trim())) {
			throw new IllegalArgumentException("El formato del correo electrónico no es válido");
		}
		
		// Validar que el email no esté en uso por otro usuario
		Optional<usuario> usuarioConEmail = usuarioRepo.findByEmailUsuario(email.trim());
		if (usuarioConEmail.isPresent() && !usuarioConEmail.get().getIdUsuario().equals(idUsuario)) {
			throw new IllegalArgumentException("El correo electrónico ya está en uso por otro usuario");
		}
		
		// Validar teléfono obligatorio
		if (telefono == null) {
			throw new IllegalArgumentException("El número de teléfono es obligatorio");
		}
		
		// Validar que el teléfono tenga al menos 10 dígitos
		String telefonoStr = String.valueOf(telefono);
		if (telefonoStr.length() < 10) {
			throw new IllegalArgumentException("El teléfono debe contener al menos 10 dígitos");
		}
		
		// Validar descripción (máximo 500 caracteres)
		if (descripcion != null && descripcion.length() > 500) {
			throw new IllegalArgumentException("La descripción no puede tener más de 500 caracteres");
		}
		
		// Actualizar datos
		usuario.setEmailUsuario(email.trim());
		usuario.setNumeroTelefonoUsuario(telefono);
		usuario.setDescripcionUsuario(descripcion != null ? descripcion.trim() : "");
		
		return usuarioRepo.save(usuario);
	}
	
	// Cambiar contraseña
	public void cambiarContrasena(Long idUsuario, String contrasenaNueva) {
		Optional<usuario> usuarioOpt = usuarioRepo.findById(idUsuario);
		
		if (!usuarioOpt.isPresent()) {
			throw new IllegalArgumentException("El usuario no existe");
		}
		
		if (contrasenaNueva == null || contrasenaNueva.trim().isEmpty()) {
			throw new IllegalArgumentException("La contraseña no puede estar vacía");
		}
		
		usuario usuario = usuarioOpt.get();
		usuario.setContrasenaUsuario(passwordEncoder.encode(contrasenaNueva));
		usuarioRepo.save(usuario);
	}
	
	// Validar contraseña
	public boolean validarContrasena(Long idUsuario, String contrasena) {
		Optional<usuario> usuarioOpt = usuarioRepo.findById(idUsuario);
		
		if (!usuarioOpt.isPresent()) {
			return false;
		}
		
		usuario usuario = usuarioOpt.get();
		return passwordEncoder.matches(contrasena, usuario.getContrasenaUsuario());
	}
	
	// Eliminar usuario
	public void eliminarUsuario(Long idUsuario) {
		if (!usuarioRepo.existsById(idUsuario)) {
			throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe");
		}
		usuarioRepo.deleteById(idUsuario);
	}
	
	// Obtener usuarios por rol
	public List<usuario> obtenerUsuariosPorRol(String nombreRol) {
		return usuarioRepo.findAll().stream()
				.filter(u -> u.getRol().getNombreRol().equalsIgnoreCase(nombreRol))
				.toList();
	}
	
	// Validar formato de email
	private boolean validarFormatoEmail(String email) {
		return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
	}
	
}
