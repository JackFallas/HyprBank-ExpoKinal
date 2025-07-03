-- src/main/resources/data.sql

-- Este script se ejecuta automaticamente en el inicio de la aplicacion Spring Boot.
-- Se usa INSERT IGNORE para que no falle si el usuario o el rol ya existen.

-- 1. Insertar el usuario administrador en la tabla 'users'
-- La contraseña es el hash BCrypt de 'HyprCodeBank17$'
INSERT IGNORE INTO users (first_name, last_name, email, password, dpi, nit, phone_number, address, birth_date)
VALUES (
    'Admin',
    'HyprBank',
    'adminhyprbank@gmail.com', -- Correo electronico del admin de Hyprbank
    '$2a$12$3.2Ms2poLa.lvjmMPd.Tnu5ghcV6xpfXLaM5DoV7D2pddmLK5RRJ.', -- La contraseña es HyprCodeBank17$
    '9999999999999',      -- DPI Generico (13 digitos)
    'GENERICO123456',     -- NIT Generico (letras, numeros y guiones)
    '1234-5678',          -- Telefono Generico (formato XXXX-XXXX)
    'Calle Ficticia 101, Ciudad Imaginaria', -- Direccion Generica
    '1990-01-01'          -- Fecha de nacimiento generica (YYYY-MM-DD)
);

-- 2. Verificar o insertar el rol 'ROLE_ADMIN' en la tabla 'roles'
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');

-- 3. Asignar el rol 'ROLE_ADMIN' al usuario administrador
-- Esto se hace solo si la relacion no existe ya, usando subconsultas para obtener los IDs.
-- Se usa NOT EXISTS para evitar duplicados de forma mas clara que solo INSERT IGNORE en este caso.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'adminhyprbank@gmail.com'
  AND r.name = 'ROLE_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur2
    WHERE ur2.user_id = u.id AND ur2.role_id = r.id
  );