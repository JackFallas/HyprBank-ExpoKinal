-- src/main/resources/data.sql

-- Este script se ejecuta automaticamente en el inicio de la aplicacion Spring Boot.
-- Se usa INSERT IGNORE para que no falle si el usuario o el rol ya existen.

-- 1. Insertar el usuario administrador en la tabla 'users'
-- La contraseña es el hash BCrypt de 'HyprCodeBank17$'
INSERT IGNORE INTO users (first_name, last_name, email, password, dpi, nit, phone_number, address, birth_date, enabled)
VALUES (
    'Admin',
    'HyprBank',
    'adminhyprbank@gmail.com', -- Correo electronico del admin de Hyprbank
    '$2a$12$3.2Ms2poLa.lvjmMPd.Tnu5ghcV6xpfXLaM5DoV7D2pddmLK5RRJ.', -- La contraseña es HyprCodeBank17$
    '9999999999999',      -- DPI Generico (13 digitos)
    'GENERICO123456',     -- NIT Generico (letras, numeros y guiones)
    '1234-5678',          -- Telefono Generico (formato XXXX-XXXX)
    'Calle Ficticia 101, Ciudad Imaginaria', -- Direccion Generica
    '1990-01-01',          -- Fecha de nacimiento generica (YYYY-MM-DD)
    TRUE                 -- Habilitado por defecto
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

-- ---------------------------------------------------------------------------------------------------------------------
-- CUENTAS DE SERVICIO
-- ---------------------------------------------------------------------------------------------------------------------

-- 4. Verificar o insertar el rol 'ROLE_SERVICE' en la tabla 'roles' si no existe
INSERT IGNORE INTO roles (name) VALUES ('ROLE_SERVICE');

-- Usuario y Cuenta para Servicio de Luz
INSERT IGNORE INTO users (first_name, last_name, email, password, dpi, nit, phone_number, address, birth_date, enabled)
VALUES (
    'Servicio',
    'Luz',
    'luz@hyprbank.com',
    '$2a$10$Nf4c.7q.J.V.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.', -- Contraseña: password123 (BCrypt)
    '1111111111111',
    'LUZSERV123',
    '1111-2222',
    'Zona 1, Ciudad',
    '2000-01-01',
    TRUE
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'luz@hyprbank.com'
  AND r.name = 'ROLE_SERVICE'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur2
    WHERE ur2.user_id = u.id AND ur2.role_id = r.id
  );

INSERT IGNORE INTO accounts (account_number, balance, account_type, status, creation_date, user_id)
SELECT
    '9000000001', -- Número de cuenta para servicio de luz
    10000.00,
    'SERVICE',
    'ACTIVE',
    CURRENT_TIMESTAMP(),
    u.id
FROM users u
WHERE u.email = 'luz@hyprbank.com'
AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.account_number = '9000000001');

-- Usuario y Cuenta para Servicio de Agua Potable
INSERT IGNORE INTO users (first_name, last_name, email, password, dpi, nit, phone_number, address, birth_date, enabled)
VALUES (
    'Servicio',
    'Agua',
    'agua@hyprbank.com',
    '$2a$10$Nf4c.7q.J.V.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.', -- Contraseña: password123 (BCrypt)
    '2222222222222',
    'AGUASERV456',
    '3333-4444',
    'Zona 2, Ciudad',
    '2001-02-02',
    TRUE
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'agua@hyprbank.com'
  AND r.name = 'ROLE_SERVICE'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur2
    WHERE ur2.user_id = u.id AND ur2.role_id = r.id
  );

INSERT IGNORE INTO accounts (account_number, balance, account_type, status, creation_date, user_id)
SELECT
    '9000000002', -- Número de cuenta para servicio de agua
    5000.00,
    'SERVICE',
    'ACTIVE',
    CURRENT_TIMESTAMP(),
    u.id
FROM users u
WHERE u.email = 'agua@hyprbank.com'
AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.account_number = '9000000002');

-- Usuario y Cuenta para Servicio de Internet del Hogar
INSERT IGNORE INTO users (first_name, last_name, email, password, dpi, nit, phone_number, address, birth_date, enabled)
VALUES (
    'Servicio',
    'Internet',
    'internet@hyprbank.com',
    '$2a$10$Nf4c.7q.J.V.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.', -- Contraseña: password123 (BCrypt)
    '3333333333333',
    'INTSERV789',
    '5555-6666',
    'Zona 3, Ciudad',
    '1999-03-03',
    TRUE
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'internet@hyprbank.com'
  AND r.name = 'ROLE_SERVICE'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur2
    WHERE ur2.user_id = u.id AND ur2.role_id = r.id
  );

INSERT IGNORE INTO accounts (account_number, balance, account_type, status, creation_date, user_id)
SELECT
    '9000000003', -- Número de cuenta para servicio de internet
    7500.00,
    'SERVICE',
    'ACTIVE',
    CURRENT_TIMESTAMP(),
    u.id
FROM users u
WHERE u.email = 'internet@hyprbank.com'
AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.account_number = '9000000003');

-- Usuario y Cuenta para Servicio de Teléfono Móvil
INSERT IGNORE INTO users (first_name, last_name, email, password, dpi, nit, phone_number, address, birth_date, enabled)
VALUES (
    'Servicio',
    'Movil',
    'movil@hyprbank.com',
    '$2a$10$Nf4c.7q.J.V.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.L.', -- Contraseña: password123 (BCrypt)
    '4444444444444',
    'MOVILSERV012',
    '7777-8888',
    'Zona 4, Ciudad',
    '1998-04-04',
    TRUE
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'movil@hyprbank.com'
  AND r.name = 'ROLE_SERVICE'
  AND NOT EXISTS (
    SELECT 1
    FROM user_roles ur2
    WHERE ur2.user_id = u.id AND ur2.role_id = r.id
  );

INSERT IGNORE INTO accounts (account_number, balance, account_type, status, creation_date, user_id)
SELECT
    '9000000004', -- Número de cuenta para servicio de teléfono móvil
    3000.00,
    'SERVICE',
    'ACTIVE',
    CURRENT_TIMESTAMP(),
    u.id
FROM users u
WHERE u.email = 'movil@hyprbank.com'
AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.account_number = '9000000004');