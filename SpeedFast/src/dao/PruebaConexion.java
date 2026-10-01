package dao;

import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {
        try (Connection conexion = ConexionBD.conectar()) {
            System.out.println("Conexión exitosa a speedfast_db");

        } catch (SQLException e) {
            System.out.println(
                    "Error de conexión: " + e.getMessage()
            );
        }
    }
}