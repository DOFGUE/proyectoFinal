package edu.com.co.Proyecto.Final.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.com.co.Proyecto.Final.Model.usuario;

public interface usuarioRepository extends JpaRepository<usuario, Long> {
	Optional<usuario> findByNombreUsuario(String nombreUsuario);

}
