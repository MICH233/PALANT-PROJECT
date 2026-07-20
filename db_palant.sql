-- Script para crear la base de datos y sus tablas (FASE 4)
-- Aumentar el límite de MySQL para permitir subir imágenes pesadas (Banners)
SET GLOBAL max_allowed_packet=33554432;

CREATE DATABASE IF NOT EXISTS palant_db;
USE palant_db;

-- Eliminar tablas si existen (orden inverso)
DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS producto_imagen;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS vendedor;
DROP TABLE IF EXISTS banner;

-- Tabla Vendedor (Administrador)
CREATE TABLE vendedor (
    id_vendedor INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL
);

-- Tabla Cliente
CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni VARCHAR(15) NOT NULL,
    celular VARCHAR(20) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    foto_perfil LONGTEXT -- FASE 4: Avatar del usuario
);

-- Tabla Producto
CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    categoria ENUM('DEPORTIVO', 'CASUAL', 'ACCESORIO') NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    imagen_base64 LONGTEXT,
    tallas_disponibles VARCHAR(100),
    estado VARCHAR(20) DEFAULT 'Activo'
);

-- Tabla Producto Imágenes Extra
CREATE TABLE producto_imagen (
    id_imagen INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    imagen_base64 LONGTEXT NOT NULL,
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON DELETE CASCADE
);

-- Tabla Pedido
CREATE TABLE pedido (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    monto_total DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    codigo_voucher VARCHAR(50),
    imagen_voucher LONGTEXT, -- FASE 4: Foto del comprobante de pago
    estado VARCHAR(50) DEFAULT 'Pendiente', 
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

-- Tabla Detalle_Pedido
CREATE TABLE detalle_pedido (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    talla VARCHAR(20),
    FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- Datos de Ejemplo
INSERT INTO vendedor (nombres, apellidos, correo, contrasena) 
VALUES ('Administrador', 'Principal', 'admin@palant.com', '123456');

-- Tabla Banner (FASE 5)
CREATE TABLE banner (
    id_banner INT AUTO_INCREMENT PRIMARY KEY,
    imagen_base64 LONGTEXT NOT NULL,
    fecha_subida DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- PROCEDIMIENTOS ALMACENADOS
-- ==========================================
DELIMITER //

DROP PROCEDURE IF EXISTS usp_login_vendedor//
CREATE PROCEDURE usp_login_vendedor(IN p_correo VARCHAR(100), IN p_contrasena VARCHAR(255))
BEGIN
    SELECT * FROM vendedor WHERE correo = p_correo AND contrasena = p_contrasena;
END//

DROP PROCEDURE IF EXISTS usp_login_usuario//
CREATE PROCEDURE usp_login_usuario(IN p_correo VARCHAR(100), IN p_contrasena VARCHAR(255))
BEGIN
    SELECT * FROM cliente WHERE correo = p_correo AND contrasena = p_contrasena;
END//

DROP PROCEDURE IF EXISTS usp_insertar_cliente//
CREATE PROCEDURE usp_insertar_cliente(
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_dni VARCHAR(15),
    IN p_celular VARCHAR(20),
    IN p_correo VARCHAR(100),
    IN p_contrasena VARCHAR(255),
    OUT p_id_cliente INT
)
BEGIN
    INSERT INTO cliente (nombres, apellidos, dni, celular, correo, contrasena, foto_perfil) 
    VALUES (p_nombres, p_apellidos, p_dni, p_celular, p_correo, p_contrasena, NULL);
    SET p_id_cliente = LAST_INSERT_ID();
END//

DROP PROCEDURE IF EXISTS usp_actualizar_cliente//
CREATE PROCEDURE usp_actualizar_cliente(
    IN p_id_cliente INT,
    IN p_nombres VARCHAR(100),
    IN p_apellidos VARCHAR(100),
    IN p_celular VARCHAR(20),
    IN p_contrasena VARCHAR(255),
    IN p_foto_perfil LONGTEXT
)
BEGIN
    IF p_foto_perfil IS NOT NULL AND p_foto_perfil != '' THEN
        UPDATE cliente SET nombres = p_nombres, apellidos = p_apellidos, celular = p_celular, contrasena = p_contrasena, foto_perfil = p_foto_perfil WHERE id_cliente = p_id_cliente;
    ELSE
        UPDATE cliente SET nombres = p_nombres, apellidos = p_apellidos, celular = p_celular, contrasena = p_contrasena WHERE id_cliente = p_id_cliente;
    END IF;
END//

DROP PROCEDURE IF EXISTS usp_listar_productos//
CREATE PROCEDURE usp_listar_productos()
BEGIN
    SELECT * FROM producto WHERE estado = 'Activo';
END//

DROP PROCEDURE IF EXISTS usp_obtener_producto_por_id//
CREATE PROCEDURE usp_obtener_producto_por_id(IN p_id INT)
BEGIN
    SELECT * FROM producto WHERE id_producto = p_id;
END//

DROP PROCEDURE IF EXISTS usp_insertar_producto//
CREATE PROCEDURE usp_insertar_producto(
    IN p_nombre VARCHAR(150),
    IN p_categoria VARCHAR(100),
    IN p_descripcion TEXT,
    IN p_precio DECIMAL(10,2),
    IN p_stock INT,
    IN p_imagen LONGTEXT,
    IN p_tallas VARCHAR(100),
    OUT p_id_producto INT
)
BEGIN
    INSERT INTO producto (nombre, categoria, descripcion, precio, stock, imagen_base64, tallas_disponibles) 
    VALUES (p_nombre, p_categoria, p_descripcion, p_precio, p_stock, p_imagen, p_tallas);
    SET p_id_producto = LAST_INSERT_ID();
END//

DROP PROCEDURE IF EXISTS usp_insertar_producto_imagen//
CREATE PROCEDURE usp_insertar_producto_imagen(
    IN p_id_producto INT,
    IN p_imagen LONGTEXT
)
BEGIN
    INSERT INTO producto_imagen (id_producto, imagen_base64) VALUES (p_id_producto, p_imagen);
END//

DROP PROCEDURE IF EXISTS usp_listar_imagenes_producto//
CREATE PROCEDURE usp_listar_imagenes_producto(
    IN p_id_producto INT
)
BEGIN
    SELECT * FROM producto_imagen WHERE id_producto = p_id_producto;
END//

DROP PROCEDURE IF EXISTS usp_actualizar_producto//
CREATE PROCEDURE usp_actualizar_producto(
    IN p_id INT,
    IN p_nombre VARCHAR(150),
    IN p_categoria VARCHAR(100),
    IN p_descripcion TEXT,
    IN p_precio DECIMAL(10,2),
    IN p_stock INT,
    IN p_imagen LONGTEXT,
    IN p_tallas VARCHAR(100)
)
BEGIN
    IF p_imagen IS NOT NULL AND p_imagen != '' THEN
        UPDATE producto SET nombre = p_nombre, categoria = p_categoria, descripcion = p_descripcion, 
        precio = p_precio, stock = p_stock, imagen_base64 = p_imagen, tallas_disponibles = p_tallas WHERE id_producto = p_id;
    ELSE
        UPDATE producto SET nombre = p_nombre, categoria = p_categoria, descripcion = p_descripcion, 
        precio = p_precio, stock = p_stock, tallas_disponibles = p_tallas WHERE id_producto = p_id;
    END IF;
END//

DROP PROCEDURE IF EXISTS usp_eliminar_producto//
CREATE PROCEDURE usp_eliminar_producto(IN p_id INT)
BEGIN
    UPDATE producto SET estado = 'Inactivo' WHERE id_producto = p_id;
END//

DROP PROCEDURE IF EXISTS usp_insertar_pedido//
CREATE PROCEDURE usp_insertar_pedido(
    IN p_id_cliente INT,
    IN p_monto_total DECIMAL(10,2),
    IN p_metodo_pago VARCHAR(50),
    IN p_codigo_voucher VARCHAR(50),
    IN p_imagen_voucher LONGTEXT,
    OUT p_id_pedido INT
)
BEGIN
    INSERT INTO pedido (id_cliente, monto_total, metodo_pago, codigo_voucher, imagen_voucher, estado) 
    VALUES (p_id_cliente, p_monto_total, p_metodo_pago, p_codigo_voucher, p_imagen_voucher, 'Pendiente');
    SET p_id_pedido = LAST_INSERT_ID();
END//

DROP PROCEDURE IF EXISTS usp_insertar_detalle_pedido//
CREATE PROCEDURE usp_insertar_detalle_pedido(
    IN p_id_pedido INT,
    IN p_id_producto INT,
    IN p_cantidad INT,
    IN p_subtotal DECIMAL(10,2),
    IN p_talla VARCHAR(20)
)
BEGIN
    INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, subtotal, talla) 
    VALUES (p_id_pedido, p_id_producto, p_cantidad, p_subtotal, p_talla);
    UPDATE producto SET stock = stock - p_cantidad WHERE id_producto = p_id_producto;
END//

DROP PROCEDURE IF EXISTS usp_listar_pedidos_vendedor//
CREATE PROCEDURE usp_listar_pedidos_vendedor()
BEGIN
    SELECT 
        p.id_pedido, p.fecha, p.monto_total, p.metodo_pago, p.codigo_voucher, p.imagen_voucher, p.estado,
        c.nombres, c.apellidos, c.celular
    FROM pedido p
    INNER JOIN cliente c ON p.id_cliente = c.id_cliente
    ORDER BY p.fecha DESC;
END//

DROP PROCEDURE IF EXISTS usp_validar_pago_pedido//
CREATE PROCEDURE usp_validar_pago_pedido(
    IN p_id_pedido INT,
    IN p_nuevo_estado VARCHAR(50)
)
BEGIN
    UPDATE pedido SET estado = p_nuevo_estado WHERE id_pedido = p_id_pedido;
    
    IF p_nuevo_estado = 'Cancelado' THEN
        UPDATE producto pr
        INNER JOIN detalle_pedido dp ON pr.id_producto = dp.id_producto
        SET pr.stock = pr.stock + dp.cantidad
        WHERE dp.id_pedido = p_id_pedido;
    END IF;
END//

DROP PROCEDURE IF EXISTS usp_listar_pedidos_cliente//
CREATE PROCEDURE usp_listar_pedidos_cliente(IN p_id_cliente INT)
BEGIN
    SELECT 
        id_pedido, fecha, monto_total, metodo_pago, codigo_voucher, imagen_voucher, estado
    FROM pedido
    WHERE id_cliente = p_id_cliente
    ORDER BY fecha DESC;
END//

-- ==========================================
-- BANNERS (FASE 5)
-- ==========================================

DROP PROCEDURE IF EXISTS usp_insertar_banner//
CREATE PROCEDURE usp_insertar_banner(IN p_imagen LONGTEXT)
BEGIN
    INSERT INTO banner (imagen_base64) VALUES (p_imagen);
END//

DROP PROCEDURE IF EXISTS usp_listar_banners//
CREATE PROCEDURE usp_listar_banners()
BEGIN
    SELECT * FROM banner ORDER BY fecha_subida DESC;
END//

DROP PROCEDURE IF EXISTS usp_eliminar_banner//
CREATE PROCEDURE usp_eliminar_banner(IN p_id INT)
BEGIN
    DELETE FROM banner WHERE id_banner = p_id;
END//

DELIMITER ;
