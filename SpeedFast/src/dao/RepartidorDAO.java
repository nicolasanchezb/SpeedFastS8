package dao;

import model.Repartidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RepartidorDAO {

    // Registra un repartidor y devuelve el objeto con su ID generado.
    public Repartidor create(String nombre) throws SQLException {
        String nombreValidado = validarNombre(nombre);

        String sql = "INSERT INTO repartidor (nombre) VALUES (?)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, nombreValidado);

            if (sentencia.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el repartidor.");
            }

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Repartidor(
                            claves.getInt(1),
                            nombreValidado
                    );
                }
            }
        }

        throw new SQLException(
                "No se obtuvo el ID del repartidor registrado."
        );
    }

    // Consulta todos los repartidores, ordenados por ID.
    public List<Repartidor> readAll() throws SQLException {
        List<Repartidor> repartidores = new ArrayList<>();

        String sql = "SELECT id, nombre FROM repartidor ORDER BY id";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                repartidores.add(new Repartidor(
                        resultado.getInt("id"),
                        resultado.getString("nombre")
                ));
            }
        }

        return repartidores;
    }

    // Actualiza únicamente el repartidor identificado por su ID.
    public boolean update(int id, String nombre) throws SQLException {
        validarId(id);
        String nombreValidado = validarNombre(nombre);

        String sql = "UPDATE repartidor SET nombre = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombreValidado);
            sentencia.setInt(2, id);

            return sentencia.executeUpdate() > 0;
        }
    }

    // Las restricciones de la BD pueden impedir la eliminación
    // si existen entregas asociadas al repartidor.
    public boolean delete(int id) throws SQLException {
        validarId(id);

        String sql = "DELETE FROM repartidor WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            return sentencia.executeUpdate() > 0;
        }
    }

    // Conserva la compatibilidad con el código de la semana anterior.
    public Repartidor guardar(String nombre) throws SQLException {
        return create(nombre);
    }

    public List<Repartidor> listarTodos() throws SQLException {
        return readAll();
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del repartidor es obligatorio."
            );
        }

        String nombreValidado = nombre.trim();

        if (nombreValidado.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres."
            );
        }

        return nombreValidado;
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID del repartidor debe ser mayor que cero."
            );
        }
    }
}