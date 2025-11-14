package edu.com.co.Proyecto.Final.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.com.co.Proyecto.Final.Model.producto;
import edu.com.co.Proyecto.Final.Repository.productoRepository;

@Service
public class productoService {
	
	@Autowired
	private productoRepository productoRepo;
	
	// Obtener todos los productos
	public List<producto> obtenerTodosProductos() {
		return productoRepo.findAll();
	}
	
	// Obtener producto por ID
	public Optional<producto> obtenerProductoPorId(Long idProducto) {
		return productoRepo.findById(idProducto);
	}
	
	// Obtener producto por nombre
	public Optional<producto> obtenerProductoPorNombre(String nombreProducto) {
		return productoRepo.findByNombreProducto(nombreProducto);
	}
	
	// Crear nuevo producto
	public producto crearProducto(producto producto) {
		validarProducto(producto);
		
		// Verificar que el nombre no exista
		if (productoRepo.findByNombreProducto(producto.getNombreProducto()).isPresent()) {
			throw new IllegalArgumentException("El nombre del producto ya existe");
		}
		
		return productoRepo.save(producto);
	}
	
	// Actualizar producto
	public producto actualizarProducto(Long idProducto, producto productoActualizado) {
		Optional<producto> productoExistente = productoRepo.findById(idProducto);
		
		if (!productoExistente.isPresent()) {
			throw new IllegalArgumentException("El producto con ID " + idProducto + " no existe");
		}
		
		producto producto = productoExistente.get();
		
		if (productoActualizado.getNombreProducto() != null && !productoActualizado.getNombreProducto().isEmpty()) {
			producto.setNombreProducto(productoActualizado.getNombreProducto());
		}
		if (productoActualizado.getPrecioProducto() != null) {
			if (productoActualizado.getPrecioProducto() <= 0) {
				throw new IllegalArgumentException("El precio debe ser mayor a 0");
			}
			producto.setPrecioProducto(productoActualizado.getPrecioProducto());
		}
		if (productoActualizado.getDescripcionProducto() != null) {
			producto.setDescripcionProducto(productoActualizado.getDescripcionProducto());
		}
		if (productoActualizado.getRutaImagenProducto() != null) {
			producto.setRutaImagenProducto(productoActualizado.getRutaImagenProducto());
		}
		if (productoActualizado.getIngredientesProducto() != null) {
			producto.setIngredientesProducto(productoActualizado.getIngredientesProducto());
		}
		
		return productoRepo.save(producto);
	}
	
	// Eliminar producto
	public void eliminarProducto(Long idProducto) {
		if (!productoRepo.existsById(idProducto)) {
			throw new IllegalArgumentException("El producto con ID " + idProducto + " no existe");
		}
		productoRepo.deleteById(idProducto);
	}
	
	// Método de validación privado
	private void validarProducto(producto producto) {
		if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
		}
		if (producto.getPrecioProducto() == null || producto.getPrecioProducto() <= 0) {
			throw new IllegalArgumentException("El precio debe ser mayor a 0");
		}
		if (producto.getDescripcionProducto() == null || producto.getDescripcionProducto().trim().isEmpty()) {
			throw new IllegalArgumentException("La descripción no puede estar vacía");
		}
		if (producto.getRutaImagenProducto() == null || producto.getRutaImagenProducto().trim().isEmpty()) {
			throw new IllegalArgumentException("La ruta de la imagen no puede estar vacía");
		}
	}
	
	// Buscar productos por nombre, descripción o ingredientes
	public List<producto> buscarProductos(String termino) {
		if (termino == null || termino.trim().isEmpty()) {
			return obtenerTodosProductos();
		}
		return productoRepo.buscarProductos(termino);
	}
}