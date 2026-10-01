package dao;

import model.EstadoPedido;
import model.Pedido;
import model.PedidoComida;
import model.PedidoEncomienda;
import model.PedidoExpress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidoDAO {

    // Registra sin enviar el ID: MySQL lo genera automáticamente.
    public Pedido create(Pedido pedido) throws SQLException {
        validarPedido(pedido);

        String sql = """
                INSERT INTO pedido (direccion, distancia_km, tipo, estado)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            cargarDatos(sentencia, pedido);

            if (sentencia.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el pedido.");
            }

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    Pedido registrado = crearPedido(
                            claves.getInt(1),
                            pedido.getDireccionEntrega().trim(),
                            pedido.getDistanciaKm(),
                            obtenerTipo(pedido)
                    );

                    registrado.setEstado(pedido.getEstado());
                    return registrado;
                }
            }
        }

        throw new SQLException("No se obtuvo el ID del pedido registrado.");
    }

    // Recupera los registros y reconstruye sus subclases.
    public List<Pedido> readAll() throws SQLException {
        String sql = """
                SELECT id, direccion, distancia_km, tipo, estado
                FROM pedido
                ORDER BY id
                """;

        List<Pedido> pedidos = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Pedido pedido = crearPedido(
                        resultado.getInt("id"),
                        resultado.getString("direccion"),
                        resultado.getDouble("distancia_km"),
                        resultado.getString("tipo")
                );

                String estado = resultado.getString("estado");

                try {
                    if (estado == null) {
                        throw new IllegalArgumentException("Estado nulo");
                    }

                    EstadoPedido estadoPedido = EstadoPedido.valueOf(estado);
                    validarEstado(estadoPedido);
                    pedido.setEstado(estadoPedido);
                } catch (IllegalArgumentException e) {
                    throw new SQLException(
                            "Estado no válido en el pedido "
                                    + pedido.getIdPedido() + ": " + estado,
                            e
                    );
                }

                pedidos.add(pedido);
            }
        }

        return pedidos;
    }

    // Actualiza dirección, distancia, tipo y estado del pedido seleccionado.
    public boolean update(Pedido pedido) throws SQLException {
        validarPedido(pedido);
        validarId(pedido.getIdPedido());

        String sql = """
                UPDATE pedido
                SET direccion = ?, distancia_km = ?, tipo = ?, estado = ?
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            cargarDatos(sentencia, pedido);
            sentencia.setInt(5, pedido.getIdPedido());

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean delete(int idPedido) throws SQLException {
        validarId(idPedido);

        String sql = "DELETE FROM pedido WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            return sentencia.executeUpdate() > 0;
        }
    }

    // Compatibilidad con los registros de semanas anteriores.
    // Este método conserva el ID que trae el objeto.
    public void guardar(Pedido pedido) throws SQLException {
        validarPedido(pedido);
        validarId(pedido.getIdPedido());

        String sql = """
                INSERT INTO pedido
                    (direccion, distancia_km, tipo, estado, id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            cargarDatos(sentencia, pedido);
            sentencia.setInt(5, pedido.getIdPedido());
            sentencia.executeUpdate();
        }
    }

    public List<Pedido> listarTodos() throws SQLException {
        return readAll();
    }

    public void actualizarEstado(int idPedido, EstadoPedido estado)
            throws SQLException {

        validarId(idPedido);
        validarEstado(estado);

        String sql = "UPDATE pedido SET estado = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado.name());
            sentencia.setInt(2, idPedido);

            if (sentencia.executeUpdate() == 0) {
                throw new SQLException(
                        "No se actualizó el pedido con ID " + idPedido
                );
            }
        }
    }

    // Reutiliza el mismo orden de parámetros en INSERT y UPDATE.
    private void cargarDatos(
            PreparedStatement sentencia,
            Pedido pedido
    ) throws SQLException {

        sentencia.setString(1, pedido.getDireccionEntrega().trim());
        sentencia.setDouble(2, pedido.getDistanciaKm());
        sentencia.setString(3, obtenerTipo(pedido));
        sentencia.setString(4, pedido.getEstado().name());
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido es obligatorio.");
        }

        String direccion = pedido.getDireccionEntrega();

        if (direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException(
                    "La dirección es obligatoria."
            );
        }

        if (direccion.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "La dirección no puede superar los 150 caracteres."
            );
        }

        double distancia = pedido.getDistanciaKm();

        if (!Double.isFinite(distancia) || distancia <= 0) {
            throw new IllegalArgumentException(
                    "La distancia debe ser un número mayor que cero."
            );
        }

        obtenerTipo(pedido);
        validarEstado(pedido.getEstado());
    }

    private void validarEstado(EstadoPedido estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }

        switch (estado.name()) {
            case "PENDIENTE", "EN_REPARTO", "ENTREGADO" -> {
                // Estados permitidos por la actividad.
            }
            default -> throw new IllegalArgumentException(
                    "Estado no permitido: " + estado.name()
            );
        }
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID debe ser mayor que cero."
            );
        }
    }

    private String obtenerTipo(Pedido pedido) {
        if (pedido instanceof PedidoComida) {
            return "COMIDA";
        }

        if (pedido instanceof PedidoEncomienda) {
            return "ENCOMIENDA";
        }

        if (pedido instanceof PedidoExpress) {
            return "EXPRESS";
        }

        throw new IllegalArgumentException("Tipo de pedido no válido.");
    }

    private Pedido crearPedido(
            int id,
            String direccion,
            double distanciaKm,
            String tipo
    ) throws SQLException {

        if (tipo == null) {
            throw new SQLException("El pedido " + id + " no tiene tipo.");
        }

        return switch (tipo.trim().toUpperCase(Locale.ROOT)) {
            case "COMIDA" ->
                    new PedidoComida(id, direccion, distanciaKm);
            case "ENCOMIENDA" ->
                    new PedidoEncomienda(id, direccion, distanciaKm);
            case "EXPRESS" ->
                    new PedidoExpress(id, direccion, distanciaKm);
            default -> throw new SQLException(
                    "Tipo desconocido en el pedido " + id + ": " + tipo
            );
        };
    }
}