package agencia;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class Pdf {
    static final Color VERDE = new Color(20, 83, 45);
    static final Font H = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, VERDE);
    static final Font B = FontFactory.getFont(FontFactory.HELVETICA, 10);
    static final Font BH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

    static String fmt(Object v, boolean money) {
        if (v == null) return "";
        if (money && v instanceof Number n) return "$ " + String.format("%,.0f", n.doubleValue()).replace(',', '.');
        return v.toString();
    }

    static String s(Map<String, Object> m, String k) { return String.valueOf(m.get(k)); }

    // Reporte en tabla (con gráfico opcional)
    public static void tabla(File f, String titulo, String desc, List<Map<String, Object>> rows, Set<String> money, byte[] img) throws Exception {
        Document d = new Document(PageSize.A4);
        PdfWriter.getInstance(d, new FileOutputStream(f));
        d.open();
        d.add(new Paragraph("Agencia de Viajes", H));
        d.add(new Paragraph(titulo + " — " + desc + "\nGenerado: " + new Date(), B));
        d.add(new Paragraph(" "));
        if (img != null) { Image i = Image.getInstance(img); i.scaleToFit(450, 230); d.add(i); }
        if (!rows.isEmpty()) {
            List<String> cols = new ArrayList<>(rows.get(0).keySet());
            PdfPTable t = new PdfPTable(cols.size());
            t.setWidthPercentage(100);
            for (String c : cols) { PdfPCell h = new PdfPCell(new Phrase(c, BH)); h.setBackgroundColor(VERDE); t.addCell(h); }
            for (var r : rows) for (String c : cols) t.addCell(new Phrase(fmt(r.get(c), money.contains(c)), B));
            d.add(t);
        }
        d.close();
    }

    // Plantilla de documento (factura / comprobante)
    static void doc(File f, String titulo, String[][] filas, Object total) throws Exception {
        Document d = new Document(PageSize.A4);
        PdfWriter.getInstance(d, new FileOutputStream(f));
        d.open();
        d.add(new Paragraph("Agencia de Viajes", H));
        d.add(new Paragraph(titulo, H));
        d.add(new Paragraph(" "));
        PdfPTable t = new PdfPTable(new float[]{1, 3});
        t.setWidthPercentage(100);
        for (String[] r : filas) { t.addCell(new Phrase(r[0], FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10))); t.addCell(new Phrase(r[1], B)); }
        d.add(t);
        Paragraph p = new Paragraph("\nTotal: " + fmt(total, true), H);
        p.setAlignment(Element.ALIGN_RIGHT);
        d.add(p);
        d.close();
    }

    static final String FACT_SQL = "SELECT f.numero, f.fecha, f.total, r.id_reserva, r.fecha_viaje, c.cedula, CONCAT(c.nombres,' ',c.apellidos) AS cliente, c.email, "
            + "p.nombre AS paquete, e.nombre AS estado, CONCAT(a.nombres,' ',a.apellidos) AS asesor FROM Factura f JOIN Reserva r USING(id_reserva) "
            + "JOIN Cliente c ON c.cedula=r.cedula_cliente JOIN PaqueteTuristico p ON p.id_paquete=r.id_paquete "
            + "JOIN EstadoReserva e ON e.id_estado_reserva=r.id_estado_reserva "
            + "JOIN Asesor a ON a.cedula=r.cedula_asesor WHERE f.id_reserva=?";

    // Si la reserva no tiene factura, se genera automáticamente (cualquier estado de reserva)
    public static void factura(File f, int idReserva) throws Exception {
        var l = Db.query(FACT_SQL, idReserva);
        if (l.isEmpty()) {
            var r = Db.query("SELECT valor FROM Reserva WHERE id_reserva=?", idReserva);
            if (r.isEmpty()) throw new Exception("La reserva no existe.");
            long sig = ((Number) Db.query("SELECT COALESCE(MAX(id_factura),0)+1 AS n FROM Factura").get(0).get("n")).longValue();
            Db.exec("INSERT INTO Factura (numero, fecha, total, id_reserva) VALUES (?, CURDATE(), ?, ?)",
                    String.format("FV-%d-%04d", java.time.LocalDate.now().getYear(), sig), r.get(0).get("valor"), idReserva);
            l = Db.query(FACT_SQL, idReserva);
        }
        var x = l.get(0);
        doc(f, "Factura de venta " + s(x, "numero"), new String[][]{
            {"Cliente", s(x, "cliente")}, {"Cédula", s(x, "cedula")}, {"Email", s(x, "email")}, {"Reserva", "#" + s(x, "id_reserva")},
            {"Paquete", s(x, "paquete")}, {"Fecha de viaje", s(x, "fecha_viaje")}, {"Estado de la reserva", s(x, "estado")}, {"Asesor", s(x, "asesor")}, {"Fecha de emisión", s(x, "fecha")}
        }, x.get("total"));
    }

    public static void comprobante(File f, int idPago) throws Exception {
        var l = Db.query("SELECT p.id_pago, p.monto, p.fecha, m.nombre AS medio, e.nombre AS estado, r.id_reserva, c.cedula, "
                + "CONCAT(c.nombres,' ',c.apellidos) AS cliente, k.nombre AS paquete FROM Pago p JOIN MedioPago m USING(id_medio_pago) "
                + "JOIN EstadoPago e USING(id_estado_pago) JOIN Reserva r USING(id_reserva) JOIN Cliente c ON c.cedula=r.cedula_cliente "
                + "JOIN PaqueteTuristico k ON k.id_paquete=r.id_paquete WHERE p.id_pago=?", idPago);
        if (l.isEmpty()) throw new Exception("Pago no encontrado.");
        var x = l.get(0);
        doc(f, "Comprobante de pago No. " + s(x, "id_pago"), new String[][]{
            {"Cliente", s(x, "cliente")}, {"Cédula", s(x, "cedula")}, {"Reserva", "#" + s(x, "id_reserva")}, {"Paquete", s(x, "paquete")},
            {"Fecha del pago", s(x, "fecha")}, {"Medio de pago", s(x, "medio")}, {"Estado", s(x, "estado")}
        }, x.get("monto"));
    }
}