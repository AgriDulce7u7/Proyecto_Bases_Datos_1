package agencia;

import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

public class Db {
    // Ajuste usuario y clave de su MySQL
    static final String URL = "jdbc:mysql://localhost:3306/agencia_viajes?useUnicode=true&characterEncoding=UTF-8";
    static final String USER = "root", PASS = "";

    public static List<Map<String, Object>> query(String sql, Object... a) throws SQLException {
        try (Connection c = DriverManager.getConnection(URL, USER, PASS); PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < a.length; i++) p.setObject(i + 1, a[i]);
            ResultSet rs = p.executeQuery();
            ResultSetMetaData m = rs.getMetaData();
            List<Map<String, Object>> out = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> r = new LinkedHashMap<>();
                for (int i = 1; i <= m.getColumnCount(); i++) r.put(m.getColumnLabel(i), rs.getObject(i));
                out.add(r);
            }
            return out;
        }
    }

    // Devuelve el id autogenerado (0 si no aplica)
    public static long exec(String sql, Object... a) throws SQLException {
        try (Connection c = DriverManager.getConnection(URL, USER, PASS); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < a.length; i++) p.setObject(i + 1, a[i]);
            p.executeUpdate();
            ResultSet k = p.getGeneratedKeys();
            return k.next() ? k.getLong(1) : 0;
        }
    }

    public static String sha(String s) {
        try {
            StringBuilder b = new StringBuilder();
            for (byte x : MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))) b.append(String.format("%02x", x));
            return b.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}