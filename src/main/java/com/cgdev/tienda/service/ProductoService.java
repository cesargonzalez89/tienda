package com.cgdev.tienda.service;

import com.cgdev.tienda.model.Product;
import com.cgdev.tienda.model.Producto;
import com.cgdev.tienda.repository.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import javax.sql.DataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductoService {
    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Transactional
    public Producto createProducto(Producto producto) {
        Integer id = producto.getId();
        logger.info("Iniciando la creacion del producto con id: {}", id);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                "CALL create_producto(?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, id);
            stmt.setString(2, producto.getName());
            stmt.setDouble(3, producto.getPrice().doubleValue());
            stmt.setInt(4, producto.getStock());
            stmt.setBoolean(5, producto.getStatus());

            boolean hasResultSet = stmt.execute();
            ResultSet rs = null;
            while (!hasResultSet && stmt.getUpdateCount() != -1) {
                hasResultSet = stmt.getMoreResults();
            }
            if (hasResultSet) {
                logger.info("Recibiendo el conjunto de resultados del procedimiento almacenado.");
                rs = stmt.getResultSet();
                if (rs.next()) {
                    id = rs.getInt(1);
                }
                rs.close();
            }
            stmt.close();
        } catch (Exception e) {
            logger.error("Error al crear el producto: ", e);
            throw new RuntimeException("Error al insertar producto", e);
        }
        logger.info("Producto creado con id: {}", id);
        return productoRepository.findProductoById(id).orElse(null);
    }

    @Transactional
    public Optional<Producto> getProductoById(Integer id) {
        logger.info("Iniciando la obtencion del producto con id: {}", id);
        return productoRepository.findProductoById(id);
    }

    @Transactional
    public Optional<Producto> updateProducto(Integer id, Producto producto) {
        logger.info("Iniciando la actualizacion del producto con id: {}", id);
        Optional<Producto> existingOpt = productoRepository.findById(id);
        if (existingOpt.isPresent()) {
            Producto existing = existingOpt.get();
            existing.setName(producto.getName());
            existing.setPrice(producto.getPrice());
            existing.setStock(producto.getStock());
            existing.setStatus(producto.getStatus());
            // Puedes agregar aquí la lógica para updatedAt si aplica
            productoRepository.save(existing);
            return Optional.of(existing);
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean deleteProducto(Integer id) {
        logger.info("Iniciando la eliminacion del producto con id: {}", id);
        if (productoRepository.existsById(id)) {
            logger.info("Producto con id {} encontrado. Procediendo a eliminar.", id);
            productoRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public List<Producto> getAllProductos() {
        logger.info("Obteniendo todos los productos.");
        return productoRepository.findAll();
    }

    private CompletableFuture<List<Product>> readProductsFromJson(String filename) {
        logger.info("Leyendo productos desde el archivo: {}", filename);
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream is = getClass().getClassLoader().getResourceAsStream("data/" + filename);
                if (is == null) throw new RuntimeException("Archivo no encontrado: " + filename);
                return mapper.readValue(is, new TypeReference<List<Product>>(){});
            } catch (Exception e) {
                throw new RuntimeException("Error leyendo productos de " + filename, e);
            }
        });
    }

    public CompletableFuture<List<Product>> getTopTenProducts() {
        logger.info("Iniciando la obtención de los 10 productos más vendidos.");
        CompletableFuture<List<Product>> product1 = readProductsFromJson("products-1.json");
        CompletableFuture<List<Product>> product2 = readProductsFromJson("products-2.json");
        CompletableFuture<List<Product>> product3 = readProductsFromJson("products-3.json");

        return CompletableFuture.allOf(product1, product2, product3)
            .thenApply(v -> {
                try {
                    List<Product> allProducts = product1.get();
                    allProducts.addAll(product2.get());
                    allProducts.addAll(product3.get());
                    return allProducts.stream()
                        .sorted(Comparator.comparing(Product::getPrice).reversed())
                        .limit(10)
                        .toList();
                } catch (Exception e) {
                    logger.error("Error al obtener los productos: ", e);
                    throw new RuntimeException(e);
                }
            });
    }
}
