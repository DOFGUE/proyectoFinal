package edu.com.co.Proyecto.Final.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.com.co.Proyecto.Final.Model.producto;

public interface productoRepository extends JpaRepository<producto, Long> {
	Optional<producto> findByNombreProducto(String nombreProducto);
	
	// Búsqueda por nombre o descripción (case-insensitive)
	@Query("SELECT p FROM producto p WHERE " +
	       "LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
	       "LOWER(p.descripcionProducto) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
	       "LOWER(p.ingredientesProducto) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
	List<producto> buscarProductos(@Param("busqueda") String busqueda);

}
