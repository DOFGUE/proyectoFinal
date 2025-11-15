package edu.com.co.Proyecto.Final.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.com.co.Proyecto.Final.Model.resena;

public interface resenaRepository extends JpaRepository<resena, Long> {
	
	Optional<resena> findByIdResena(Long idResena);
	
	// Obtener reseña existente de un usuario para un producto específico
	@Query("SELECT r FROM resena r WHERE r.usuario.idUsuario = :idUsuario AND r.producto.idProducto = :idProducto")
	Optional<resena> findByUsuarioAndProducto(@Param("idUsuario") Long idUsuario, @Param("idProducto") Long idProducto);

}
