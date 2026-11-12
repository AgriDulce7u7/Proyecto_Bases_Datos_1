package agencia;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;

public class Crud extends BorderPane {
    public static class Cfg {
        String t, pk, title, doc; boolean auto; String[][] cols;
        Cfg(String t, String pk, String title, boolean auto, String doc, String[]... cols) {
            this.t = t; this.pk = pk; this.title = title; this.auto = auto; this.doc = doc; this.cols = cols;
        }
    }

    static String[] f(String n, String l, String t) { return new String[]{n, l, t}; }

    static final String DEST = "SELECT d.id_destino, CONCAT(c.nombre,', ',p.nombre) FROM Destino d JOIN Ciudad c USING(id_ciudad) "
            + "JOIN Departamento dp USING(id_departamento) JOIN Pais p USING(id_pais)";

    static final Cfg[] ALL = {
        new Cfg("Cliente", "cedula", "Clientes", false, null,
            f("cedula", "Cédula", "digits"), f("nombres", "Nombres", "text"), f("apellidos", "Apellidos", "text"),
            f("fecha_nacimiento", "Fecha de nacimiento", "date"), f("email", "Email", "text"),
            f("usuario", "Usuario", "text"), f("clave", "Contraseña", "password")),
        new Cfg("PaqueteTuristico", "id_paquete", "Paquetes", true, null,
            f("id_paquete", "Código", "text"), f("nombre", "Nombre", "text"), f("duracion_dias", "Duración (días)", "number"),
            f("precio", "Precio", "number"), f("id_plan", "Plan", "SELECT id_plan, nombre FROM `Plan`"), f("id_destino", "Destino", DEST)),
        new Cfg("Reserva", "id_reserva", "Reservas", true, "factura",
            f("id_reserva", "Código", "text"), f("fecha_reserva", "Fecha de reserva", "date"), f("fecha_viaje", "Fecha de viaje", "date"),
            f("valor", "Valor", "number"),
            f("cedula_cliente", "Cliente", "SELECT cedula, CONCAT(nombres,' ',apellidos) FROM Cliente"),
            f("cedula_asesor", "Asesor", "SELECT cedula, CONCAT(nombres,' ',apellidos) FROM Asesor"),
            f("id_paquete", "Paquete", "SELECT id_paquete, nombre FROM PaqueteTuristico"),
            f("id_estado_reserva", "Estado", "SELECT id_estado_reserva, nombre FROM EstadoReserva")),
        new Cfg("Pago", "id_pago", "Pagos", true, "comprobante",
            f("id_pago", "Código", "text"), f("monto", "Monto", "number"), f("fecha", "Fecha", "date"),
            f("id_reserva", "Reserva", "SELECT id_reserva, CONCAT('Reserva #',id_reserva) FROM Reserva"),
            f("id_medio_pago", "Medio de pago", "SELECT id_medio_pago, nombre FROM MedioPago"),
            f("id_estado_pago", "Estado", "SELECT id_estado_pago, nombre FROM EstadoPago"))
    };

    final Cfg cfg;
    final TableView<Map<String, Object>> tv = new TableView<>();
    final TextField q = new TextField();

    public Crud(Cfg cfg) {
        this.cfg = cfg;
        q.setPromptText("Consultar...");
        Button bs = new Button("Buscar"), bn = new Button("Nuevo"), be = new Button("Editar"), bd = new Button("Eliminar");
        bs.setOnAction(e -> refresh());
        bn.setOnAction(e -> form(null));
        be.setOnAction(e -> { var s = tv.getSelectionModel().getSelectedItem(); if (s != null) form(s); });
        bd.setOnAction(e -> eliminar());
        HBox top = new HBox(8, q, bs, bn, be, bd);
        top.setPadding(new Insets(10));
        if (cfg.doc != null) {
            Button bp = new Button("Generar " + cfg.doc + " (PDF)");
            bp.setOnAction(e -> documento());
            top.getChildren().add(bp);
        }
        setTop(top);
        setCenter(tv);
        refresh();
    }

    List<String> vis() {
        List<String> l = new ArrayList<>();
        for (String[] c : cfg.cols) if (!c[2].equals("password")) l.add(c[0]);
        return l;
    }

    void refresh() {
        try {
            String cols = String.join(",", vis()), sql = "SELECT " + cols + " FROM " + cfg.t, s = q.getText().trim();
            var rows = s.isEmpty() ? Db.query(sql + " ORDER BY " + cfg.pk + " DESC")
                    : Db.query(sql + " WHERE CONCAT_WS(' '," + cols + ") LIKE ? ORDER BY " + cfg.pk + " DESC", "%" + s + "%");
            App.fill(tv, rows);
        } catch (Exception e) { App.error(e); }
    }

    static Callback<DatePicker, DateCell> lim(Supplier<LocalDate> min, Supplier<LocalDate> max) {
        return p -> new DateCell() {
            @Override public void updateItem(LocalDate d, boolean e) {
                super.updateItem(d, e);
                if (d == null || e) return;
                LocalDate a = min == null ? null : min.get(), b = max == null ? null : max.get();
                if ((a != null && d.isBefore(a)) || (b != null && d.isAfter(b))) { setDisable(true); setStyle("-fx-background-color:#ddd;"); }
            }
        };
    }

    void form(Map<String, Object> row) {
        boolean edit = row != null;
        LocalDate hoy = LocalDate.now();
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle((edit ? "Editar " : "Nuevo ") + cfg.title);
        GridPane g = new GridPane();
        g.setHgap(8); g.setVgap(8); g.setPadding(new Insets(16));
        Map<String, Control> ctl = new LinkedHashMap<>();
        int i = 0;
        try {
            for (String[] c : cfg.cols) {
                if (cfg.auto && c[0].equals(cfg.pk)) continue;
                Object cur = edit ? row.get(c[0]) : null;
                Control x;
                if (c[2].startsWith("SELECT")) {
                    ComboBox<String[]> cb = new ComboBox<>();
                    for (var r : Db.query(c[2])) {
                        var v = new ArrayList<>(r.values());
                        String[] o = {String.valueOf(v.get(0)), String.valueOf(v.get(1))};
                        cb.getItems().add(o);
                        if (cur != null && o[0].equals(cur.toString())) cb.setValue(o);
                    }
                    cb.setConverter(new StringConverter<>() {
                        public String toString(String[] o) { return o == null ? "" : o[1]; }
                        public String[] fromString(String s) { return null; }
                    });
                    x = cb;
                } else if (c[2].equals("date")) {
                    DatePicker dp = new DatePicker();
                    dp.setEditable(false);
                    if (cur != null) dp.setValue(LocalDate.parse(cur.toString()));
                    switch (c[0]) {
                        case "fecha_nacimiento":   //Mayor de 18 años
                            dp.setDayCellFactory(lim(() -> hoy.minusYears(100), () -> hoy.minusYears(18)));
                            if (cur == null) dp.setValue(hoy.minusYears(18));
                            break;
                        case "fecha_reserva": case "fecha":   //Fecha de hoy, no editable
                            if (!edit) dp.setValue(hoy);
                            dp.setDisable(true);
                            break;
                        case "fecha_viaje":   //Posterior a la fecha de reserva
                            dp.setDayCellFactory(lim(() -> {
                                var r = (DatePicker) ctl.get("fecha_reserva");
                                return (r == null || r.getValue() == null ? hoy : r.getValue()).plusDays(1);
                            }, null));
                            break;
                    }
                    x = dp;
                } else if (c[2].equals("password")) {
                    x = new PasswordField();
                } else {
                    String init = cur == null ? "" : cur.toString();
                    boolean num = c[2].equals("number"), dig = c[2].equals("digits");
                    if (num && !init.isEmpty()) init = new BigDecimal(init).toBigInteger().toString();
                    TextField tf = new TextField(init);
                    if (num || dig) {   // solo números
                        String rx = dig ? "\\d{0,12}" : "\\d{0,10}";
                        tf.setTextFormatter(new TextFormatter<String>(ch -> ch.getControlNewText().matches(rx) ? ch : null));
                    }
                    if (edit && c[0].equals(cfg.pk)) tf.setDisable(true);
                    x = tf;
                }
                ctl.put(c[0], x);
                g.addRow(i++, new Label(c[1]), x);
            }
        } catch (Exception e) { App.error(e); return; }
        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) d.getDialogPane().lookupButton(ButtonType.OK)).addEventFilter(ActionEvent.ACTION, ev -> {
            String err = validar(edit, ctl);
            if (err == null) err = guardar(edit, row, ctl);
            if (err != null) { App.alert(err); ev.consume(); }
        });
        d.showAndWait();
        refresh();
    }

    static Object val(Control c) {
        if (c instanceof DatePicker d) return d.getValue() == null ? null : d.getValue().toString();
        if (c instanceof ComboBox<?> cb) return cb.getValue() == null ? null : ((String[]) cb.getValue())[0];
        String t = ((TextInputControl) c).getText().trim();
        return t.isEmpty() ? null : t;
    }

    boolean isPwd(String n) {
        for (String[] c : cfg.cols) if (c[0].equals(n) && c[2].equals("password")) return true;
        return false;
    }

    String validar(boolean edit, Map<String, Control> ctl) {
        for (String[] c : cfg.cols) {
            Control x = ctl.get(c[0]);
            if (x == null) continue;
            Object v = val(x);
            if (v == null) {
                if (edit && c[2].equals("password")) continue; 
                return "El campo \"" + c[1] + "\" es obligatorio.";
            }
            String s = v.toString();
            if (c[2].equals("number") && new BigInteger(s).signum() <= 0) return "\"" + c[1] + "\" debe ser mayor que cero.";
            if (c[2].equals("digits") && s.length() < 6) return "La cédula debe tener al menos 6 dígitos.";
            if (c[0].equals("email") && !s.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) return "El email no es válido.";
        }
        LocalDate hoy = LocalDate.now();
        if (ctl.containsKey("fecha_nacimiento") && LocalDate.parse(val(ctl.get("fecha_nacimiento")).toString()).isAfter(hoy.minusYears(18)))
            return "El cliente debe ser mayor de 18 años.";
        if (ctl.containsKey("fecha_viaje")) {
            LocalDate r = LocalDate.parse(val(ctl.get("fecha_reserva")).toString()), v = LocalDate.parse(val(ctl.get("fecha_viaje")).toString());
            if (!v.isAfter(r)) return "La fecha de viaje debe ser posterior a la fecha de reserva.";
        }
        return null;
    }


    String guardar(boolean edit, Map<String, Object> row, Map<String, Control> ctl) {
        List<String> names = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for (var e : ctl.entrySet()) {
            String n = e.getKey();
            if (edit && n.equals(cfg.pk)) continue;
            Object v = val(e.getValue());
            if (isPwd(n)) { if (v == null && edit) continue; v = Db.sha(v == null ? "" : v.toString()); }
            names.add(n); vals.add(v);
        }
        try {
            if (edit) {
                vals.add(row.get(cfg.pk));
                Db.exec("UPDATE " + cfg.t + " SET " + String.join("=?,", names) + "=? WHERE " + cfg.pk + "=?", vals.toArray());
            } else {
                long id = Db.exec("INSERT INTO " + cfg.t + " (" + String.join(",", names) + ") VALUES (" + "?,".repeat(names.size() - 1) + "?)", vals.toArray());
                notificar(id, names, vals);
            }
            return null;
        } catch (Exception ex) {
            String m = String.valueOf(ex.getMessage());
            return m.contains("Duplicate") ? "Ya existe un registro con esos datos (cédula, email o usuario repetido)." : "Error: " + m;
        }
    }

    //Notificaciones automáticas al crear reservas y pagos
    void notificar(long id, List<String> n, List<Object> v) throws Exception {
        if (cfg.t.equals("Reserva")) {
            Notif.crear((String) v.get(n.indexOf("cedula_cliente")), (int) id, 3, "Su reserva #" + id + " fue registrada para viajar el "
                    + v.get(n.indexOf("fecha_viaje")) + " por " + Pdf.fmt(new BigDecimal(v.get(n.indexOf("valor")).toString()), true) + ".");
        } else if (cfg.t.equals("Pago")) {
            String res = (String) v.get(n.indexOf("id_reserva"));
            var r = Db.query("SELECT cedula_cliente FROM Reserva WHERE id_reserva=?", res);
            if (!r.isEmpty()) Notif.crear((String) r.get(0).get("cedula_cliente"), Integer.valueOf(res), 3,
                    "Registramos un pago de " + Pdf.fmt(new BigDecimal(v.get(n.indexOf("monto")).toString()), true) + " para su reserva #" + res + ".");
        }
    }

    void eliminar() {
        var s = tv.getSelectionModel().getSelectedItem();
        if (s == null || new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el registro seleccionado?").showAndWait().orElse(null) != ButtonType.OK) return;
        try { Db.exec("DELETE FROM " + cfg.t + " WHERE " + cfg.pk + "=?", s.get(cfg.pk)); refresh(); }
        catch (Exception e) { App.alert("No se puede eliminar: tiene registros asociados."); }
    }

    void documento() {
        var s = tv.getSelectionModel().getSelectedItem();
        if (s == null) return;
        int id = ((Number) s.get(cfg.pk)).intValue();
        App.guardar(cfg.doc + "_" + id, f -> { if (cfg.doc.equals("factura")) Pdf.factura(f, id); else Pdf.comprobante(f, id); });
    }
}