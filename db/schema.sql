CREATE DATABASE tienda;
USE tienda;

DROP TABLE  IF EXISTS tienda.producto;
CREATE TABLE tienda.producto (
    -- id INT AUTO_INCREMENT PRIMARY KEY,--se quita autoincrement para dejar insercion de id manual y validar regla de no duplicados
    id INT PRIMARY KEY,
    name VARCHAR(50),
    price DECIMAL(12,2),
    stock INT,
    status BOOLEAN,
    created_at TIMESTAMP,-- DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP-- DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP PROCEDURE IF EXISTS tienda.create_producto;
DELIMITER $$
CREATE PROCEDURE tienda.create_producto(
	IN p_id INT,
    IN p_name VARCHAR(50),
    IN p_price DECIMAL(12,2),
    IN p_stock INT,
    IN p_status BOOLEAN
)
BEGIN
    INSERT INTO tienda.producto (id, name, price, stock, status, created_at, updated_at)
    VALUES (p_id, p_name, p_price, p_stock, p_status,NOW(),NOW());
    -- SELECT LAST_INSERT_ID() AS id;
	SELECT p_id AS id;
END$$
DELIMITER ; 

DELIMITER $$
CREATE PROCEDURE tienda.get_producto_by_id(
    IN p_id INT
)
BEGIN
    SELECT id, name, price, stock, status, created_at, updated_at FROM tienda.producto WHERE id = p_id;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS tienda.update_producto;
DELIMITER $$
CREATE PROCEDURE tienda.update_producto(
    IN p_id INT,
    IN p_name VARCHAR(50),
    IN p_price DECIMAL(12,2),
    IN p_stock INT,
    IN p_status BOOLEAN
)
BEGIN
    UPDATE tienda.producto
    SET name = p_name,
        price = p_price,
        stock = p_stock,
        status = p_status
        ,updated_at = NOW()
    WHERE id = p_id;
END$$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE tienda.delete_producto(
    IN p_id INT
)
BEGIN
    DELETE FROM tienda.producto WHERE id = p_id;
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE tienda.get_all_productos()
BEGIN
    SELECT id, name, price, stock, status, created_at, updated_at FROM tienda.producto;
END$$
DELIMITER ;

-- Insertar producto
CALL tienda.create_producto(112,'Producto A', 10.50, 20, TRUE);

-- Listar todos productos
CALL tienda.get_all_productos();

-- Obtener producto por ID
CALL tienda.get_producto_by_id(1);

-- Actualizar producto
CALL tienda.update_producto(1, 'Producto A+', 12.00, 25, TRUE);

-- Eliminar producto
CALL tienda.delete_producto(1);