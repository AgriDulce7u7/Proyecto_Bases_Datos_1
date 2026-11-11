package agencia;

public class Notif {
    // Tipos: 1 = Recordatorio de pago, 2 = Recordatorio de viaje, 3 = General
    static void crear(String cedula, Integer reserva, int tipo, String mensaje) throws Exception {
        Db.exec("INSERT INTO Notificacion (mensaje, fecha, cedula_cliente, id_reserva, id_tipo_notificacion) VALUES (?, CURDATE(), ?, ?, ?)",
                mensaje, cedula, reserva, tipo);
    }

    static boolean yaHoy(int reserva, int tipo) throws Exception {
        return !Db.query("SELECT 1 FROM Notificacion WHERE id_reserva=? AND id_tipo_notificacion=? AND fecha=CURDATE()", reserva, tipo).isEmpty();
    }

    // Se ejecuta cuando el cliente inicia sesión. Máximo un recordatorio por reserva y tipo al día.
    static void recordatorios(String cedula) throws Exception {
        // 1) Saldo pendiente de reservas no canceladas
        for (var r : Db.query("SELECT t.id_reserva, t.nombre, t.valor - t.pagado AS saldo FROM (SELECT r.id_reserva, p.nombre, r.valor, "
                + "COALESCE((SELECT SUM(pg.monto) FROM Pago pg JOIN EstadoPago e USING(id_estado_pago) WHERE pg.id_reserva=r.id_reserva AND e.nombre='Aprobado'),0) AS pagado "
                + "FROM Reserva r JOIN PaqueteTuristico p USING(id_paquete) WHERE r.cedula_cliente=? AND r.id_estado_reserva<>3) t WHERE t.valor > t.pagado", cedula)) {
            int id = ((Number) r.get("id_reserva")).intValue();
            if (!yaHoy(id, 1))
                crear(cedula, id, 1, "Recuerde cancelar el saldo de " + Pdf.fmt(r.get("saldo"), true) + " de su reserva a " + r.get("nombre") + ".");
        }
        // 2) Viajes que empiezan en los próximos 7 días
        for (var r : Db.query("SELECT r.id_reserva, p.nombre, r.fecha_viaje FROM Reserva r JOIN PaqueteTuristico p USING(id_paquete) "
                + "WHERE r.cedula_cliente=? AND r.id_estado_reserva<>3 AND r.fecha_viaje BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)", cedula)) {
            int id = ((Number) r.get("id_reserva")).intValue();
            if (!yaHoy(id, 2))
                crear(cedula, id, 2, "Su viaje a " + r.get("nombre") + " inicia el " + r.get("fecha_viaje") + ". ¡Feliz viaje!");
        }
    }
}