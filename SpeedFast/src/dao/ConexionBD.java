package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/speedfast_db";

    private static final String USUARIO = "root";

    public static Connection conectar() throws SQLException {
        String contrasena = System.getenv("SPEEDFAST_DB_PASSWORD");

        if (contrasena == null) {
            throw new SQLException(
                    "Falta configurar SPEEDFAST_DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(
                URL,
                USUARIO,
                contrasena
        );
    }
}