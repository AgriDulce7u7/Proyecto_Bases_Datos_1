package agencia;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class App extends Application {
    static String rol, cedula, nombre;

    interface Job { void run(File f) throws Exception; }

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage st) {
        TextField u = new TextField();
        PasswordField p = new PasswordField();
        Button b = new Button("Ingresar");
        b.setDefaultButton(true);
        GridPane g = new GridPane();
        g.setHgap(8); g.setVgap(8); g.setPadding(new Insets(24));
        g.addRow(0, new Label("Usuario"), u);
        g.addRow(1, new Label("Contraseña"), p);
        g.add(b, 1, 2);
        b.setOnAction(e -> {
            try {
                for (String[] r : new String[][]{{"asesor", "Asesor"}, {"cliente", "Cliente"}}) {
                    var x = Db.query("SELECT cedula, nombres FROM " + r[1] + " WHERE usuario=? AND clave=?", u.getText(), Db.sha(p.getText()));
                    if (!x.isEmpty()) {
                        rol = r[0]; cedula = (String) x.get(0).get("cedula"); nombre = (String) x.get(0).get("nombres");
                        panel(st);
                        return;
                    }
                }
                alert("Usuario o contraseña incorrectos");
            } catch (Exception ex) { error(ex); }
        });
        st.setTitle("Agencia de Viajes — Iniciar sesión");
        st.setScene(new Scene(g));
        st.show();
    }

    void panel(Stage st) {
        TabPane tp = new TabPane();
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        try {
            if (rol.equals("asesor")) {
                for (Crud.Cfg c : Crud.ALL) tp.getTabs().add(new Tab(c.title, new Crud(c)));
                tp.getTabs().add(new Tab("Reportes", new ReportView()));
            } else {
                var res = new TableView<Map<String, Object>>();
                fill(res, Db.query("SELECT r.id_reserva AS Reserva, p.nombre AS Paquete, r.fecha_viaje AS Viaje, r.valor AS Valor, e.nombre AS Estado "
                        + "FROM Reserva r JOIN PaqueteTuristico p USING(id_paquete) JOIN EstadoReserva e USING(id_estado_reserva) WHERE r.cedula_cliente=?", cedula));
                Button f = new Button("Descargar factura (PDF)");
                f.setOnAction(e -> {
                    var s = res.getSelectionModel().getSelectedItem();
                    if (s == null) return;
                    int id = ((Number) s.get("Reserva")).intValue();
                    guardar("factura_reserva_" + id, file -> Pdf.factura(file, id));
                });
                Notif.recordatorios(cedula);
                var nots = new TableView<Map<String, Object>>();
                fill(nots, Db.query("SELECT n.fecha AS Fecha, t.nombre AS Tipo, n.mensaje AS Mensaje FROM Notificacion n "
                        + "JOIN TipoNotificacion t USING(id_tipo_notificacion) WHERE n.cedula_cliente=? ORDER BY n.fecha DESC", cedula));
                tp.getTabs().add(new Tab("Mis reservas", new VBox(8, f, res)));
                tp.getTabs().add(new Tab("Notificaciones", nots));
            }
        } catch (Exception e) { error(e); }
        st.setTitle("Agencia de Viajes — " + nombre);
        st.setScene(new Scene(tp, 1100, 650));
        st.centerOnScreen();
    }

    //Utilidades
    static void fill(TableView<Map<String, Object>> tv, List<Map<String, Object>> rows) {
        tv.getColumns().clear();
        if (!rows.isEmpty()) for (String k : rows.get(0).keySet()) {
            TableColumn<Map<String, Object>, String> c = new TableColumn<>(k);
            c.setCellValueFactory(d -> { Object v = d.getValue().get(k); return new SimpleStringProperty(v == null ? "" : v.toString()); });
            tv.getColumns().add(c);
        }
        tv.setItems(FXCollections.observableArrayList(rows));
    }

    static void guardar(String nombre, Job j) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(nombre + ".pdf");
        File f = fc.showSaveDialog(null);
        if (f == null) return;
        try { j.run(f); alert("PDF guardado en:\n" + f.getAbsolutePath()); } catch (Exception e) { error(e); }
    }

    static void alert(String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    static void error(Exception e) { new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
}