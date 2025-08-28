package com.cgdev.tienda.repository;

import com.cgdev.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Reemplazo del método basado en stored procedure por consulta nativa
    @Query(value = "SELECT * FROM producto WHERE id = :id", nativeQuery = true)
    Optional<Producto> findProductoById(@Param("id") Integer id);

}
