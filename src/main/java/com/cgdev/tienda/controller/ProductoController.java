package com.cgdev.tienda.controller;

import com.cgdev.tienda.model.Product;
import com.cgdev.tienda.model.Producto;
import com.cgdev.tienda.service.ProductoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoService productoService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> getById(@PathVariable Integer id) {
        if (id == null) {
            logger.error("El id proporcionado es nulo.");
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "El id es obligatorio", null));
        }
        Optional<Producto> producto = productoService.getProductoById(id);
        if (producto.isPresent()) {
            logger.info("Producto con id {} encontrado.", id);
            return ResponseEntity.ok(new ApiResponse<>(200, "informacion encontrada.", producto.get()));
        } else {
            logger.warn("Producto con id {} no encontrado.", id);
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "no encontrado.", null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Producto>>> getAll() {
        List<Producto> productos = productoService.getAllProductos();
        ApiResponse<List<Producto>> response = new ApiResponse<>(
            200,
            productos.isEmpty() ? "No hay productos." : "informacion encontrada.",
            productos
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Producto>> create(@Valid @RequestBody Producto producto, BindingResult bindingResult) {
        if (producto.getId() == null) {
            logger.error("El id proporcionado es nulo.");
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "El id es obligatorio", null));
        }
        if (producto.getStatus() == null) {
            logger.error("El campo status es nulo.");
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "El campo status es obligatorio y debe ser booleano (true/false)", null));
        }
        if (bindingResult.hasErrors()) {
            logger.error("Errores de validacion: {}", bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", ")));

            String mensaje = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, mensaje, null));
        }
        try {
            Producto created = productoService.createProducto(producto);
            ApiResponse<Producto> response = new ApiResponse<>(200, "producto creado.", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al crear el producto: ", e);
            if (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().toLowerCase().contains("duplicate")) {
                return ResponseEntity.status(409).body(new ApiResponse<>(409, "id duplicado.", null));
            }
            return ResponseEntity.status(422).body(new ApiResponse<>(422, "error de negocio.", null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Producto>> update(@PathVariable Integer id, @Valid @RequestBody Producto producto, BindingResult bindingResult) {
        producto.setId(id);
        if (producto.getStatus() == null) {
            logger.error("El campo status es nulo.");
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "El campo status es obligatorio y debe ser booleano (true/false)", null));
        }
        if (bindingResult.hasErrors()) {
            logger.error("Errores de validacion: {}", bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", ")));

            String mensaje = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, mensaje, null));
        }
        Optional<Producto> updated = productoService.updateProducto(id, producto);
        if (updated.isPresent()) {
            logger.info("Producto con id {} actualizado.", id);
            return ResponseEntity.ok(new ApiResponse<>(200, "informacion actualizada.", updated.get()));
        } else {
            logger.warn("Producto con id {} no encontrado para actualizar.", id);
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "no encontrado.", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        if (id == null) {
            logger.error("El id proporcionado es nulo.");
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "El id es obligatorio", null));
        }
        boolean deleted = productoService.deleteProducto(id);
        if (deleted) {
            logger.info("Producto con id {} eliminado.", id);
            return ResponseEntity.ok(new ApiResponse<>(200, "informacion borrada.", null));
        } else {
            logger.warn("Producto con id {} no encontrado para eliminar.", id);
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "no encontrado.", null));
        }
    }

    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<Product>>> getTopTenProducts() throws InterruptedException, ExecutionException {
        List<Product> top10 = productoService.getTopTenProducts().get();
        ApiResponse<List<Product>> response = new ApiResponse<>(
            200,
            top10.isEmpty() ? "No hay productos." : "informacion encontrada.",
            top10
        );
        return ResponseEntity.ok(response);
    }
}
