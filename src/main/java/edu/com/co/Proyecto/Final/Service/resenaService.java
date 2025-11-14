package edu.com.co.Proyecto.Final.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.com.co.Proyecto.Final.Model.resena;
import edu.com.co.Proyecto.Final.Model.producto;
import edu.com.co.Proyecto.Final.Model.usuario;
import edu.com.co.Proyecto.Final.Repository.resenaRepository;
import edu.com.co.Proyecto.Final.Repository.productoRepository;
import edu.com.co.Proyecto.Final.Repository.usuarioRepository;

@Service
public class resenaService {
	
	@Autowired
	private resenaRepository resenaRepo;
	
	@Autowired
	private productoRepository productoRepo;
	
	@Autowired
	private usuarioRepository usuarioRepo;
	
	// Obtener todas las reseñas
	public List<resena> obtenerTodasResenas() {
		return resenaRepo.findAll();
	}
	
	// Obtener reseña por ID
	public Optional<resena> obtenerResenaPorId(Long idResena) {
		return resenaRepo.findById(idResena);
	}
	
	// Obtener reseñas de un producto
	public List<resena> obtenerResenasPorProducto(Long idProducto) {
		Optional<producto> productoOpt = productoRepo.findById(idProducto);
		
		if (!productoOpt.isPresent()) {
			throw new IllegalArgumentException("El producto con ID " + idProducto + " no existe");
		}
		
		producto p = productoOpt.get();
		return p.getResenas() != null ? p.getResenas() : List.of();
	}
	
	// Obtener reseñas de un usuario
	public List<resena> obtenerResenasPorUsuario(Long idUsuario) {
		Optional<usuario> usuarioOpt = usuarioRepo.findById(idUsuario);
		
		if (!usuarioOpt.isPresent()) {
			throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe");
		}
		
		usuario u = usuarioOpt.get();
		return u.getResenas() != null ? u.getResenas() : List.of();
	}
	
	// Crear nueva reseña
	public resena crearResena(Long idUsuario, Long idProducto, Integer calificacion, String comentario) {
		// Validar calificación
		if (calificacion == null || calificacion < 1 || calificacion > 5) {
			throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas");
		}
		
		// Validar existencia de usuario
		Optional<usuario> usuarioOpt = usuarioRepo.findById(idUsuario);
		if (!usuarioOpt.isPresent()) {
			throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe");
		}
		
		// Validar existencia de producto
		Optional<producto> productoOpt = productoRepo.findById(idProducto);
		if (!productoOpt.isPresent()) {
			throw new IllegalArgumentException("El producto con ID " + idProducto + " no existe");
		}
		
		// Validar que el usuario no haya dejado ya una reseña en este producto
		List<resena> resenasPorUsuario = obtenerResenasPorUsuario(idUsuario);
		boolean yaExisteResena = resenasPorUsuario.stream()
				.anyMatch(r -> r.getProducto().getIdProducto().equals(idProducto));
		
		if (yaExisteResena) {
			throw new IllegalArgumentException("El usuario ya ha dejado una reseña en este producto");
		}
		
		// Crear la reseña
		resena nuevaResena = new resena(usuarioOpt.get(), productoOpt.get(), calificacion);
		if (comentario != null && !comentario.trim().isEmpty()) {
			nuevaResena.setComentarioResena(comentario);
		}
		
		resena resenaGuardada = resenaRepo.save(nuevaResena);
		
		// Actualizar calificación promedio del producto
		actualizarCalificacionProducto(idProducto);
		
		return resenaGuardada;
	}
	
	// Actualizar reseña
	public resena actualizarResena(Long idResena, Integer calificacion, String comentario) {
		Optional<resena> resenaOpt = resenaRepo.findById(idResena);
		
		if (!resenaOpt.isPresent()) {
			throw new IllegalArgumentException("La reseña con ID " + idResena + " no existe");
		}
		
		// Validar calificación si se proporciona
		if (calificacion != null && (calificacion < 1 || calificacion > 5)) {
			throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas");
		}
		
		resena resena = resenaOpt.get();
		Long idProducto = resena.getProducto().getIdProducto();
		
		if (calificacion != null) {
			resena.setCalificacion(calificacion);
		}
		if (comentario != null) {
			resena.setComentarioResena(comentario);
		}
		
		resena resenaActualizada = resenaRepo.save(resena);
		
		// Actualizar calificación promedio del producto
		actualizarCalificacionProducto(idProducto);
		
		return resenaActualizada;
	}
	
	// Eliminar reseña
	public void eliminarResena(Long idResena) {
		Optional<resena> resenaOpt = resenaRepo.findById(idResena);
		
		if (!resenaOpt.isPresent()) {
			throw new IllegalArgumentException("La reseña con ID " + idResena + " no existe");
		}
		
		Long idProducto = resenaOpt.get().getProducto().getIdProducto();
		resenaRepo.deleteById(idResena);
		
		// Actualizar calificación promedio del producto
		actualizarCalificacionProducto(idProducto);
	}
	
	// Obtener calificación promedio de un producto
	public Double obtenerCalificacionPromedio(Long idProducto) {
		List<resena> resenas = obtenerResenasPorProducto(idProducto);
		
		if (resenas.isEmpty()) {
			return 0.0;
		}
		
		return resenas.stream()
				.mapToDouble(resena::getCalificacion)
				.average()
				.orElse(0.0);
	}
	
	// Obtener cantidad de reseñas de un producto
	public Integer obtenerCantidadResenas(Long idProducto) {
		return obtenerResenasPorProducto(idProducto).size();
	}
	
	// Obtener reseñas por calificación
	public List<resena> obtenerResenasPorCalificacion(Long idProducto, Integer calificacion) {
		if (calificacion == null || calificacion < 1 || calificacion > 5) {
			throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
		}
		
		return obtenerResenasPorProducto(idProducto).stream()
				.filter(r -> r.getCalificacion().equals(calificacion))
				.toList();
	}
	
	// Obtener reseñas con comentario
	public List<resena> obtenerResenasPorProductoConComentario(Long idProducto) {
		return obtenerResenasPorProducto(idProducto).stream()
				.filter(r -> r.getComentarioResena() != null && !r.getComentarioResena().isEmpty())
				.toList();
	}
	
	// Actualizar calificación promedio del producto (privado)
	private void actualizarCalificacionProducto(Long idProducto) {
		Optional<producto> productoOpt = productoRepo.findById(idProducto);
		
		if (productoOpt.isPresent()) {
			Double promedioCalificacion = obtenerCalificacionPromedio(idProducto);
			producto producto = productoOpt.get();
			producto.setCalificacionProducto(promedioCalificacion);
			productoRepo.save(producto);
		}
	}
	
}
