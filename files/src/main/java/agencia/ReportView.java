package agencia;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class ReportView extends BorderPane {
    static class R {
        String n, t, d, cl, cv, sql; Set<String> money;
        R(String n, String t, String d, String cl, String cv, String money, String sql) {
            this.n = n; this.t = t; this.d = d; this.cl = cl; this.cv = cv; this.sql = sql;
            this.money = new HashSet<>(Arrays.asList(money.split(",")));
        }
        public String toString() { return n + " · " + t; }
    }

    static final R[] ALL = {
        //Simples (una sola tabla)
        new R("Simple", "Listado de clientes", "Clientes registrados en la agencia.", null, null, "",
            "SELECT cedula AS Cédula, CONCAT(nombres,' ',apellidos) AS Cliente, email AS Email, fecha_nacimiento AS Nacimiento FROM Cliente ORDER BY apellidos"),
        new R("Simple", "Asesores y salarios", "Asesores ordenados por salario.", null, null, "Salario",
            "SELECT cedula AS Cédula, CONCAT(nombres,' ',apellidos) AS Asesor, fecha_ingreso AS Ingreso, salario AS Salario FROM Asesor ORDER BY salario DESC"),
        new R("Simple", "Catálogo de paquetes", "Paquetes ordenados por precio.", null, null, "Precio",
            "SELECT id_paquete AS Código, nombre AS Paquete, duracion_dias AS Días, precio AS Precio FROM PaqueteTuristico ORDER BY precio DESC"),
        //Intermedios (multitabla)
        new R("Intermedio", "Reservas por mes", "Reservas no canceladas y valor vendido mes a mes.", "Mes", "Total", "Total",
            "SELECT DATE_FORMAT(r.fecha_reserva,'%Y-%m') AS Mes, COUNT(*) AS Reservas, SUM(r.valor) AS Total FROM Reserva r "
            + "JOIN EstadoReserva e USING(id_estado_reserva) WHERE e.nombre<>'Cancelada' GROUP BY Mes ORDER BY Mes"),
        new R("Intermedio", "Ingresos por destino", "Facturación por destino (sin canceladas).", "Destino", "Ingresos", "Ingresos",
            "SELECT c.nombre AS Destino, COUNT(r.id_reserva) AS Reservas, SUM(r.valor) AS Ingresos FROM Reserva r "
            + "JOIN PaqueteTuristico p USING(id_paquete) JOIN Destino d USING(id_destino) JOIN Ciudad c USING(id_ciudad) "
            + "JOIN EstadoReserva e ON e.id_estado_reserva=r.id_estado_reserva WHERE e.nombre<>'Cancelada' "
            + "GROUP BY c.nombre HAVING COUNT(r.id_reserva)>=1 ORDER BY Ingresos DESC"),
        new R("Intermedio", "Recaudo por medio de pago", "Pagos aprobados agrupados por medio de pago.", null, null, "Recaudado",
            "SELECT m.nombre AS Medio, COUNT(*) AS Pagos, SUM(p.monto) AS Recaudado FROM Pago p JOIN MedioPago m USING(id_medio_pago) "
            + "JOIN EstadoPago e USING(id_estado_pago) WHERE e.nombre='Aprobado' GROUP BY m.nombre HAVING SUM(p.monto)>0 ORDER BY Recaudado DESC"),
        new R("Intermedio", "Desempeño de asesores", "Reservas y ventas por asesor (sin canceladas).", null, null, "Vendido",
            "SELECT CONCAT(a.nombres,' ',a.apellidos) AS Asesor, COUNT(r.id_reserva) AS Reservas, SUM(r.valor) AS Vendido FROM Asesor a "
            + "JOIN Reserva r ON r.cedula_asesor=a.cedula JOIN EstadoReserva e ON e.id_estado_reserva=r.id_estado_reserva "
            + "WHERE e.nombre<>'Cancelada' GROUP BY a.cedula, a.nombres, a.apellidos HAVING COUNT(*)>=1 ORDER BY Vendido DESC"),
        //Avanzados (subconsultas)
        new R("Avanzado", "Reservas sobre el valor promedio", "Reservas cuyo valor supera el promedio general.", null, null, "Valor",
            "SELECT r.id_reserva AS Reserva, CONCAT(c.nombres,' ',c.apellidos) AS Cliente, p.nombre AS Paquete, r.valor AS Valor FROM Reserva r "
            + "JOIN Cliente c ON c.cedula=r.cedula_cliente JOIN PaqueteTuristico p ON p.id_paquete=r.id_paquete "
            + "WHERE r.valor > (SELECT AVG(valor) FROM Reserva) ORDER BY r.valor DESC"),
        new R("Avanzado", "Reservas con saldo pendiente", "Valor, pagos aprobados y saldo por cobrar.", null, null, "Valor,Pagado,Saldo",
            "SELECT t.id_reserva AS Reserva, t.Cliente, t.valor AS Valor, t.Pagado, t.valor - t.Pagado AS Saldo FROM ("
            + "SELECT r.id_reserva, r.valor, CONCAT(c.nombres,' ',c.apellidos) AS Cliente, "
            + "COALESCE((SELECT SUM(p.monto) FROM Pago p JOIN EstadoPago e USING(id_estado_pago) WHERE p.id_reserva=r.id_reserva AND e.nombre='Aprobado'),0) AS Pagado "
            + "FROM Reserva r JOIN Cliente c ON c.cedula=r.cedula_cliente JOIN EstadoReserva s ON s.id_estado_reserva=r.id_estado_reserva "
            + "WHERE s.nombre<>'Cancelada') t WHERE t.valor > t.Pagado ORDER BY Saldo DESC"),
        new R("Avanzado", "Paquetes con ingresos sobre el promedio", "Paquetes cuyas ventas superan el promedio por paquete.", "Paquete", "Ingresos", "Ingresos",
            "SELECT p.nombre AS Paquete, COUNT(*) AS Reservas, SUM(r.valor) AS Ingresos FROM Reserva r JOIN PaqueteTuristico p ON p.id_paquete=r.id_paquete "
            + "WHERE r.id_estado_reserva<>3 GROUP BY p.id_paquete, p.nombre HAVING SUM(r.valor) > (SELECT AVG(s) FROM ("
            + "SELECT SUM(valor) AS s FROM Reserva WHERE id_estado_reserva<>3 GROUP BY id_paquete) x) ORDER BY Ingresos DESC")
    };

    final ComboBox<R> cb = new ComboBox<>(FXCollections.observableArrayList(ALL));
    final TableView<Map<String, Object>> tv = new TableView<>();
    final VBox box = new VBox(10);
    BarChart<String, Number> chart;
    List<Map<String, Object>> rows = List.of();

    public ReportView() {
        cb.getSelectionModel().selectFirst();
        Button bVer = new Button("Ver reporte"), bPdf = new Button("Exportar a PDF");
        bVer.setOnAction(e -> ver());
        bPdf.setOnAction(e -> exportar());
        HBox top = new HBox(10, new Label("Reporte:"), cb, bVer, bPdf);
        top.setPadding(new Insets(10));
        box.setPadding(new Insets(10));
        setTop(top);
        setCenter(box);
        ver();
    }

    void ver() {
        R r = cb.getValue();
        try {
            rows = Db.query(r.sql);
            chart = null;
            box.getChildren().setAll(new Label(r.d));
            if (r.cl != null && !rows.isEmpty()) {
                chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
                chart.setAnimated(false);
                chart.setLegendVisible(false);
                chart.setTitle(r.t);
                chart.setPrefHeight(260);
                var s = new XYChart.Series<String, Number>();
                for (var x : rows) s.getData().add(new XYChart.Data<>(String.valueOf(x.get(r.cl)), ((Number) x.get(r.cv)).doubleValue()));
                chart.getData().add(s);
                box.getChildren().add(chart);
            }
            App.fill(tv, rows);
            VBox.setVgrow(tv, Priority.ALWAYS);
            box.getChildren().add(tv);
        } catch (Exception e) { App.error(e); }
    }

    void exportar() {
        R r = cb.getValue();
        App.guardar("reporte_" + r.t.replace(' ', '_'), f -> {
            byte[] img = null;
            if (chart != null) {
                var bi = SwingFXUtils.fromFXImage(chart.snapshot(new SnapshotParameters(), null), null);
                var o = new ByteArrayOutputStream();
                ImageIO.write(bi, "png", o);
                img = o.toByteArray();
            }
            Pdf.tabla(f, r.t, r.n + ". " + r.d, rows, r.money, img);
        });
    }
}
