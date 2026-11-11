DROP DATABASE IF EXISTS agencia_viajes;
CREATE DATABASE agencia_viajes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agencia_viajes;

-- Catálogos y ubicación
CREATE TABLE Pais (id_pais INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(60) NOT NULL UNIQUE);
CREATE TABLE Departamento (id_departamento INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(60) NOT NULL,
  id_pais INT NOT NULL, FOREIGN KEY (id_pais) REFERENCES Pais(id_pais));
CREATE TABLE Ciudad (id_ciudad INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(60) NOT NULL,
  id_departamento INT NOT NULL, FOREIGN KEY (id_departamento) REFERENCES Departamento(id_departamento));
CREATE TABLE Destino (id_destino INT AUTO_INCREMENT PRIMARY KEY, descripcion VARCHAR(255) NOT NULL,
  id_ciudad INT NOT NULL, FOREIGN KEY (id_ciudad) REFERENCES Ciudad(id_ciudad));
CREATE TABLE TipoTelefono (id_tipo_telefono INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(30) NOT NULL);
CREATE TABLE TipoProveedor (id_tipo_proveedor INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(30) NOT NULL);
CREATE TABLE TipoPlan (id_tipo_plan INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(30) NOT NULL);
CREATE TABLE TipoTransporte (id_tipo_transporte INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(30) NOT NULL);
CREATE TABLE TipoHabitacion (id_tipo_habitacion INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(30) NOT NULL);
CREATE TABLE TipoNotificacion (id_tipo_notificacion INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(40) NOT NULL);
CREATE TABLE EstadoReserva (id_estado_reserva INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(20) NOT NULL);
CREATE TABLE EstadoPago (id_estado_pago INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(20) NOT NULL);
CREATE TABLE MedioPago (id_medio_pago INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(30) NOT NULL);

-- Personas
CREATE TABLE Cliente (cedula VARCHAR(15) PRIMARY KEY, nombres VARCHAR(60) NOT NULL, apellidos VARCHAR(60) NOT NULL,
  fecha_nacimiento DATE NOT NULL, email VARCHAR(80) NOT NULL UNIQUE,
  usuario VARCHAR(30) NOT NULL UNIQUE, clave CHAR(64) NOT NULL);
CREATE TABLE Asesor (cedula VARCHAR(15) PRIMARY KEY, nombres VARCHAR(60) NOT NULL, apellidos VARCHAR(60) NOT NULL,
  email VARCHAR(80) NOT NULL UNIQUE, fecha_ingreso DATE NOT NULL, salario DECIMAL(12,2) NOT NULL,
  usuario VARCHAR(30) NOT NULL UNIQUE, clave CHAR(64) NOT NULL);
CREATE TABLE Nacionalidad (cedula_cliente VARCHAR(15), id_pais INT, PRIMARY KEY (cedula_cliente, id_pais),
  FOREIGN KEY (cedula_cliente) REFERENCES Cliente(cedula) ON DELETE CASCADE,
  FOREIGN KEY (id_pais) REFERENCES Pais(id_pais));
CREATE TABLE Proveedor (id_proveedor INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(80) NOT NULL,
  email VARCHAR(80), direccion VARCHAR(120), id_tipo_proveedor INT NOT NULL,
  FOREIGN KEY (id_tipo_proveedor) REFERENCES TipoProveedor(id_tipo_proveedor));
CREATE TABLE Telefono (id_telefono INT AUTO_INCREMENT PRIMARY KEY, numero VARCHAR(20) NOT NULL,
  id_tipo_telefono INT NOT NULL, cedula_cliente VARCHAR(15) NULL, id_proveedor INT NULL,
  FOREIGN KEY (id_tipo_telefono) REFERENCES TipoTelefono(id_tipo_telefono),
  FOREIGN KEY (cedula_cliente) REFERENCES Cliente(cedula) ON DELETE CASCADE,
  FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor));

-- Oferta turística
CREATE TABLE `Plan` (id_plan INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(60) NOT NULL, descripcion VARCHAR(255),
  id_tipo_plan INT NOT NULL, FOREIGN KEY (id_tipo_plan) REFERENCES TipoPlan(id_tipo_plan));
CREATE TABLE PaqueteTuristico (id_paquete INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(80) NOT NULL,
  duracion_dias INT NOT NULL, precio DECIMAL(12,2) NOT NULL, id_plan INT NOT NULL, id_destino INT NOT NULL,
  FOREIGN KEY (id_plan) REFERENCES `Plan`(id_plan), FOREIGN KEY (id_destino) REFERENCES Destino(id_destino));
CREATE TABLE Hotel (id_hotel INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(80) NOT NULL, hora_checkin TIME NOT NULL,
  hora_checkout TIME NOT NULL, direccion VARCHAR(120), id_proveedor INT NOT NULL, id_destino INT NOT NULL,
  FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor), FOREIGN KEY (id_destino) REFERENCES Destino(id_destino));
CREATE TABLE Habitacion (id_hotel INT, numero_habitacion INT, id_tipo_habitacion INT NOT NULL, precio DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id_hotel, numero_habitacion),  -- entidad débil
  FOREIGN KEY (id_hotel) REFERENCES Hotel(id_hotel) ON DELETE CASCADE,
  FOREIGN KEY (id_tipo_habitacion) REFERENCES TipoHabitacion(id_tipo_habitacion));
CREATE TABLE Transporte (id_transporte INT AUTO_INCREMENT PRIMARY KEY, origen VARCHAR(80) NOT NULL, destino VARCHAR(80) NOT NULL,
  fecha_salida DATETIME NOT NULL, fecha_llegada DATETIME NOT NULL,
  proposito ENUM('Traslado al hotel','Traslado a la actividad') NOT NULL, precio DECIMAL(12,2) NOT NULL,
  id_proveedor INT NOT NULL, id_tipo_transporte INT NOT NULL,
  FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor),
  FOREIGN KEY (id_tipo_transporte) REFERENCES TipoTransporte(id_tipo_transporte));
CREATE TABLE Actividad (id_actividad INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(80) NOT NULL, descripcion VARCHAR(255),
  duracion_horas INT NOT NULL, precio DECIMAL(12,2) NOT NULL, id_destino INT NOT NULL, id_proveedor INT NOT NULL,
  FOREIGN KEY (id_destino) REFERENCES Destino(id_destino), FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor));

-- Tablas intermedias N:M
CREATE TABLE PaqueteTuristico_Habitacion (id_paquete INT, id_hotel INT, numero_habitacion INT,
  noches INT NOT NULL, fecha_ingreso DATE NOT NULL, fecha_salida DATE NOT NULL,
  PRIMARY KEY (id_paquete, id_hotel, numero_habitacion),
  FOREIGN KEY (id_paquete) REFERENCES PaqueteTuristico(id_paquete) ON DELETE CASCADE,
  FOREIGN KEY (id_hotel, numero_habitacion) REFERENCES Habitacion(id_hotel, numero_habitacion));
CREATE TABLE PaqueteTuristico_Transporte (id_paquete INT, id_transporte INT, orden_tramo INT NOT NULL,
  PRIMARY KEY (id_paquete, id_transporte),
  FOREIGN KEY (id_paquete) REFERENCES PaqueteTuristico(id_paquete) ON DELETE CASCADE,
  FOREIGN KEY (id_transporte) REFERENCES Transporte(id_transporte));
CREATE TABLE PaqueteTuristico_Actividad (id_paquete INT, id_actividad INT, dia_viaje INT NOT NULL, cupos_disponibles INT NOT NULL,
  PRIMARY KEY (id_paquete, id_actividad),
  FOREIGN KEY (id_paquete) REFERENCES PaqueteTuristico(id_paquete) ON DELETE CASCADE,
  FOREIGN KEY (id_actividad) REFERENCES Actividad(id_actividad));

-- Ventas
CREATE TABLE Reserva (id_reserva INT AUTO_INCREMENT PRIMARY KEY, fecha_reserva DATE NOT NULL, fecha_viaje DATE NOT NULL,
  valor DECIMAL(12,2) NOT NULL, cedula_cliente VARCHAR(15) NOT NULL, cedula_asesor VARCHAR(15) NOT NULL,
  id_paquete INT NOT NULL, id_estado_reserva INT NOT NULL,
  FOREIGN KEY (cedula_cliente) REFERENCES Cliente(cedula), FOREIGN KEY (cedula_asesor) REFERENCES Asesor(cedula),
  FOREIGN KEY (id_paquete) REFERENCES PaqueteTuristico(id_paquete),
  FOREIGN KEY (id_estado_reserva) REFERENCES EstadoReserva(id_estado_reserva));
CREATE TABLE Factura (id_factura INT AUTO_INCREMENT PRIMARY KEY, numero VARCHAR(20) NOT NULL UNIQUE, fecha DATE NOT NULL,
  total DECIMAL(12,2) NOT NULL, id_reserva INT NOT NULL UNIQUE,  -- máximo una factura por reserva
  FOREIGN KEY (id_reserva) REFERENCES Reserva(id_reserva));
CREATE TABLE Pago (id_pago INT AUTO_INCREMENT PRIMARY KEY, monto DECIMAL(12,2) NOT NULL, fecha DATE NOT NULL,
  id_reserva INT NOT NULL, id_medio_pago INT NOT NULL, id_estado_pago INT NOT NULL,
  FOREIGN KEY (id_reserva) REFERENCES Reserva(id_reserva), FOREIGN KEY (id_medio_pago) REFERENCES MedioPago(id_medio_pago),
  FOREIGN KEY (id_estado_pago) REFERENCES EstadoPago(id_estado_pago));
CREATE TABLE Notificacion (id_notificacion INT AUTO_INCREMENT PRIMARY KEY, mensaje VARCHAR(255) NOT NULL, fecha DATE NOT NULL,
  cedula_cliente VARCHAR(15) NOT NULL, id_reserva INT NULL, id_tipo_notificacion INT NOT NULL,
  FOREIGN KEY (cedula_cliente) REFERENCES Cliente(cedula), FOREIGN KEY (id_reserva) REFERENCES Reserva(id_reserva),
  FOREIGN KEY (id_tipo_notificacion) REFERENCES TipoNotificacion(id_tipo_notificacion));
