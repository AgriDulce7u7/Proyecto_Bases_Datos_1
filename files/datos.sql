USE agencia_viajes;
SET NAMES utf8mb4;

INSERT INTO Pais (nombre) VALUES ('Colombia'),('México'),('Argentina'),('España'),('Estados Unidos'),('Perú');
INSERT INTO Departamento (nombre, id_pais) VALUES ('Bolívar',1),('Quindío',1),('Quintana Roo',2),('Buenos Aires',3),('Cataluña',4),('Cusco',6),('San Andrés y Providencia',1);
INSERT INTO Ciudad (nombre, id_departamento) VALUES ('Cartagena',1),('Salento',2),('Cancún',3),('Buenos Aires',4),('Barcelona',5),('Cusco',6),('San Andrés',7);
INSERT INTO Destino (descripcion, id_ciudad) VALUES
('Ciudad amurallada, playas e islas del Caribe colombiano',1),
('Paisaje cafetero, Valle del Cocora y pueblos patrimonio',2),
('Playas del Caribe mexicano, parques y cultura maya',3),
('Capital cultural: tango, gastronomía y arquitectura',4),
('Arquitectura de Gaudí, playas y vida mediterránea',5),
('Capital del Imperio Inca y puerta de entrada a Machu Picchu',6),
('Isla del mar de los siete colores',7);

INSERT INTO TipoTelefono (nombre) VALUES ('Móvil'),('Fijo');
INSERT INTO TipoProveedor (nombre) VALUES ('Hotel'),('Transporte'),('Guía');
INSERT INTO TipoPlan (nombre) VALUES ('Económico'),('Premium'),('Todo incluido');
INSERT INTO TipoTransporte (nombre) VALUES ('Avión'),('Bus'),('Van');
INSERT INTO TipoHabitacion (nombre) VALUES ('Sencilla'),('Doble'),('Suite');
INSERT INTO TipoNotificacion (nombre) VALUES ('Recordatorio de pago'),('Recordatorio de viaje'),('General');
INSERT INTO EstadoReserva (nombre) VALUES ('Pendiente'),('Pagada'),('Cancelada');
INSERT INTO EstadoPago (nombre) VALUES ('Aprobado'),('Pendiente'),('Rechazado');
INSERT INTO MedioPago (nombre) VALUES ('Tarjeta'),('PSE'),('Transferencia'),('Efectivo');

-- Contraseñas: clientes = Cliente123, asesores = Asesor123
INSERT INTO Cliente VALUES
('1094567821','Laura Valentina','Gómez Ríos','1992-03-14','laura.gomez@gmail.com','lgomez',SHA2('Cliente123',256)),
('1004321876','Andrés Felipe','Cardona Marín','1988-07-22','andres.cardona@hotmail.com','acardona',SHA2('Cliente123',256)),
('1098765432','Camila Andrea','Torres Vélez','1995-11-05','camila.torres@gmail.com','ctorres',SHA2('Cliente123',256)),
('79845123','Julián David','Ospina Henao','1985-01-30','julian.ospina@outlook.com','jospina',SHA2('Cliente123',256)),
('41912345','Marta Lucía','Herrera Castaño','1979-09-18','marta.herrera@gmail.com','mherrera',SHA2('Cliente123',256)),
('1089456123','Santiago','Rojas Pineda','1999-05-27','santiago.rojas@gmail.com','srojas',SHA2('Cliente123',256)),
('1020334455','Daniela','Restrepo Zapata','1990-12-02','daniela.restrepo@gmail.com','drestrepo',SHA2('Cliente123',256)),
('1112223334','Sebastián','Mejía Londoño','2000-08-09','sebastian.mejia@gmail.com','smejia',SHA2('Cliente123',256));
INSERT INTO Asesor VALUES
('1010111213','Carolina','Duque Arango','carolina.duque@agenciaviajes.co','2021-02-01',2800000,'cduque',SHA2('Asesor123',256)),
('1020202020','Mauricio','Salazar Giraldo','mauricio.salazar@agenciaviajes.co','2019-06-15',3100000,'msalazar',SHA2('Asesor123',256)),
('1030303030','Paola Andrea','Giraldo Muñoz','paola.giraldo@agenciaviajes.co','2022-09-05',2600000,'pgiraldo',SHA2('Asesor123',256));
INSERT INTO Nacionalidad VALUES ('1094567821',1),('1004321876',1),('1004321876',4),('1098765432',1),('79845123',1),('41912345',1),('1089456123',1),('1020334455',1),('1112223334',1),('1112223334',5);

INSERT INTO Proveedor (nombre,email,direccion,id_tipo_proveedor) VALUES
('Hoteles del Caribe S.A.S.','reservas@hotelescaribe.co','Calle del Arsenal #8-29, Cartagena',1),
('Grupo Hotelero Andino','info@hotelandino.com','Carrera 6 #3-15, Salento',1),
('Avianca Servicios Turísticos','turismo@avianca.com','Av. El Dorado #103-09, Bogotá',2),
('Transportes Turísticos del Café','contacto@transcafe.co','Carrera 14 #20-10, Armenia',2),
('Guías Aventura Colombia','guias@aventuracolombia.co','Calle 30 #12-45, Cartagena',3),
('Expediciones Andinas','info@expandinas.pe','Av. El Sol #432, Cusco',3);
INSERT INTO Telefono (numero,id_tipo_telefono,cedula_cliente,id_proveedor) VALUES
('3104567890',1,'1094567821',NULL),('3156781234',1,'1004321876',NULL),('3207894561',1,'1098765432',NULL),
('6067451234',2,'79845123',NULL),('3001239876',1,'1020334455',NULL),
('6056644321',2,NULL,1),('6067593210',2,NULL,2),('3118889900',1,NULL,5);

INSERT INTO `Plan` (nombre,descripcion,id_tipo_plan) VALUES
('Plan Mochilero','Hospedaje sencillo, traslados y actividades básicas',1),
('Plan Confort','Hotel boutique, traslados privados y tours guiados',2),
('Plan Todo Incluido','Alimentación, bebidas, actividades y traslados incluidos',3);
INSERT INTO PaqueteTuristico (nombre,duracion_dias,precio,id_plan,id_destino) VALUES
('Cartagena Colonial',4,1450000,2,1),('Eje Cafetero Esencial',3,780000,1,2),('Cancún Todo Incluido',6,4900000,3,3),
('Cusco Mágico',5,3200000,2,6),('San Andrés Paraíso',5,2300000,3,7),('Cartagena Mochilero',3,690000,1,1);

INSERT INTO Hotel (nombre,hora_checkin,hora_checkout,direccion,id_proveedor,id_destino) VALUES
('Hotel Caribe Cartagena','15:00','12:00','Carrera 1 #2-87, Bocagrande',1,1),
('Hotel Boutique Salento','14:00','11:00','Calle Real #5-20, Salento',2,2),
('Riviera Maya Resort','15:00','12:00','Blvd. Kukulcán Km 12, Cancún',1,3),
('Hotel Cusco Plaza','14:00','11:00','Plaza de Armas #115, Cusco',2,6),
('Sunrise San Andrés','15:00','12:00','Av. Colombia #1-50, San Andrés',1,7);
INSERT INTO Habitacion VALUES (1,101,2,280000),(1,201,3,520000),(2,1,1,150000),(2,2,2,210000),(3,301,3,850000),(3,302,2,600000),(4,12,2,240000),(5,10,2,310000),(5,11,3,480000);
INSERT INTO Transporte (origen,destino,fecha_salida,fecha_llegada,proposito,precio,id_proveedor,id_tipo_transporte) VALUES
('Bogotá','Cartagena','2026-12-15 07:00','2026-12-15 08:35','Traslado al hotel',420000,3,1),
('Aeropuerto Rafael Núñez','Hotel Caribe Cartagena','2026-12-15 09:30','2026-12-15 10:00','Traslado al hotel',60000,4,3),
('Armenia','Salento','2026-12-05 09:00','2026-12-05 10:00','Traslado al hotel',15000,4,2),
('Bogotá','Cancún','2026-12-27 06:00','2026-12-27 09:30','Traslado al hotel',1350000,3,1),
('Bogotá','Cusco','2026-11-28 05:30','2026-11-28 09:10','Traslado al hotel',980000,3,1),
('Medellín','San Andrés','2026-12-18 08:00','2026-12-18 10:15','Traslado al hotel',690000,3,1),
('Salento','Valle del Cocora','2026-12-06 07:00','2026-12-06 07:45','Traslado a la actividad',40000,4,3);
INSERT INTO Actividad (nombre,descripcion,duracion_horas,precio,id_destino,id_proveedor) VALUES
('Tour Ciudad Amurallada','Recorrido a pie por el centro histórico',3,90000,1,5),
('Islas del Rosario','Día de playa y snorkel en lancha',8,180000,1,5),
('Valle del Cocora','Caminata entre palmas de cera',6,120000,2,5),
('Parque Xcaret','Parque ecoarqueológico con espectáculos',9,350000,3,6),
('Machu Picchu','Visita guiada a la ciudadela inca',12,500000,6,6),
('Johnny Cay','Paseo en lancha y día de playa',5,130000,7,5);

INSERT INTO PaqueteTuristico_Habitacion VALUES
(1,1,101,3,'2026-12-15','2026-12-18'),(2,2,2,2,'2026-12-05','2026-12-07'),(3,3,301,5,'2026-12-27','2027-01-01'),
(4,4,12,4,'2026-11-28','2026-12-02'),(5,5,10,4,'2026-12-18','2026-12-22'),(6,1,101,2,'2026-12-15','2026-12-17');
INSERT INTO PaqueteTuristico_Transporte VALUES (1,1,1),(1,2,2),(2,3,1),(2,7,2),(3,4,1),(4,5,1),(5,6,1),(6,1,1);
INSERT INTO PaqueteTuristico_Actividad VALUES (1,1,2,12),(1,2,3,8),(2,3,2,15),(3,4,3,20),(4,5,3,10),(5,6,2,14),(6,1,2,10);

INSERT INTO Reserva (fecha_reserva,fecha_viaje,valor,cedula_cliente,cedula_asesor,id_paquete,id_estado_reserva) VALUES
('2026-06-10','2026-08-15',1450000,'1094567821','1010111213',1,2),
('2026-06-22','2026-09-05',780000,'1004321876','1020202020',2,2),
('2026-07-03','2026-10-20',4900000,'1098765432','1010111213',3,2),
('2026-07-18','2026-11-28',3200000,'79845123','1030303030',4,1),
('2026-08-02','2026-12-18',2300000,'41912345','1020202020',5,2),
('2026-08-14','2026-09-25',690000,'1089456123','1030303030',6,3),
('2026-09-01','2026-12-27',4900000,'1020334455','1010111213',3,1),
('2026-09-12','2027-01-10',1450000,'1112223334','1020202020',1,2),
('2026-10-05','2027-02-14',3200000,'1094567821','1030303030',4,1),
('2026-10-19','2026-12-05',780000,'1098765432','1010111213',2,2),
('2026-11-02','2027-03-06',2300000,'1004321876','1020202020',5,1),
('2026-11-05','2027-01-22',1450000,'79845123','1030303030',1,1);
INSERT INTO Pago (monto,fecha,id_reserva,id_medio_pago,id_estado_pago) VALUES
(1450000,'2026-06-10',1,1,1),(780000,'2026-06-22',2,2,1),(2450000,'2026-07-03',3,1,1),(2450000,'2026-07-20',3,3,1),
(1000000,'2026-07-18',4,2,1),(1150000,'2026-08-02',5,3,1),(1150000,'2026-08-30',5,2,1),(690000,'2026-08-14',6,1,3),
(2000000,'2026-09-01',7,1,1),(1450000,'2026-09-12',8,4,1),(1000000,'2026-10-05',9,3,1),(780000,'2026-10-19',10,1,1),
(500000,'2026-11-02',11,2,2);
INSERT INTO Factura (numero,fecha,total,id_reserva) VALUES
('FV-2026-0001','2026-06-10',1450000,1),('FV-2026-0002','2026-06-22',780000,2),('FV-2026-0003','2026-07-20',4900000,3),
('FV-2026-0004','2026-08-30',2300000,5),('FV-2026-0005','2026-09-12',1450000,8),('FV-2026-0006','2026-10-19',780000,10);
INSERT INTO Notificacion (mensaje,fecha,cedula_cliente,id_reserva,id_tipo_notificacion) VALUES
('Recuerde cancelar el saldo de $2.200.000 de su reserva a Cusco Mágico.','2026-11-02','79845123',4,1),
('Su viaje a Cancún inicia el 20 de octubre. ¡Feliz viaje!','2026-10-13','1098765432',3,2),
('Temporada de vacaciones: 15% de descuento en planes Todo Incluido.','2026-11-01','1094567821',NULL,3),
('Tiene un saldo pendiente de $2.900.000 por su reserva a Cancún.','2026-11-05','1020334455',7,1),
('Su viaje al Eje Cafetero inicia el 5 de diciembre.','2026-11-06','1098765432',10,2),
('Recuerde cancelar el saldo de $2.200.000 de su reserva a Cusco Mágico.','2026-11-07','1094567821',9,1);
