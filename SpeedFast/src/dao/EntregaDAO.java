package dao;

import model.Entrega;
import model.EstadoPedido;
import model.Pedido;
import model.PedidoComida;
import model.PedidoEncomienda;
import model.PedidoExpress;
import model.Repartidor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntregaDAO {

    public Entrega create(Entrega entrega) throws SQLException {
        validarEntrega(entrega);

        String sql = """
                INSERT INTO entrega
                    (id_pedido, id_repartidor, fecha, hora)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            cargarDatos(sentencia, entrega);

            if (sentencia.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar la entrega.");
            }

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Entrega(
                            claves.getInt(1),
                            entrega.getPedido(),
                            entrega.getRepartidor(),
                            entrega.getFechaHora()
                    );
                }
            }
        }

        throw new SQLException("No se obtuvo el ID de la entrega.");
    }

    // Los JOIN recuperan los datos del pedido y del repartidor.
    public List<Entrega> readAll() throws SQLException {
        String sql = """
                SELECT
                    e.id AS entrega_id,
                    e.fecha,
                    e.hora,
                    p.id AS pedido_id,
                    p.direccion,
                    p.distancia_km,
                    p.tipo,
                    p.estado,
                    r.id AS repartidor_id,
                    r.nombre
                FROM entrega e
                INNER JOIN pedido p ON e.id_pedido = p.id
                INNER JOIN repartidor r ON e.id_repartidor = r.id
                ORDER BY e.id
                """;

        List<Entrega> entregas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Pedido pedido = leerPedido(resultado);

                Repartidor repartidor = new Repartidor(
                        resultado.getInt("repartidor_id"),
                        resultado.getString("nombre")
                );

                Date fecha = resultado.getDate("fecha");
                Time hora = resultado.getTime("hora");

                if (fecha == null || hora == null) {
                    throw new SQLException(
                            "La entrega " + resultado.getInt("entrega_id")
                                    + " no tiene fecha u hora."
                    );
                }

                entregas.add(new Entrega(
                        resultado.getInt("entrega_id"),
                        pedido,
                        repartidor,
                        fecha.toLocalDate().atTime(hora.toLocalTime())
                ));
            }
        }

        return entregas;
    }

    public boolean update(Entrega entrega) throws SQLException {
        validarEntrega(entrega);
        validarId(entrega.getId());

        String sql = """
                UPDATE entrega
                SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ?
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            cargarDatos(sentencia, entrega);
            sentencia.setInt(5, entrega.getId());

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean delete(int idEntrega) throws SQLException {
        validarId(idEntrega);

        String sql = "DELETE FROM entrega WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idEntrega);

            return sentencia.executeUpdate() > 0;
        }
    }

    // Compatibilidad con las llamadas anteriores.
    public void guardar(Entrega entrega) throws SQLException {
        create(entrega);
    }

    public List<Entrega> listarTodos() throws SQLException {
        return readAll();
    }

    private void cargarDatos(
            PreparedStatement sentencia,
            Entrega entrega
    ) throws SQLException {

        sentencia.setInt(1, entrega.getPedido().getIdPedido());
        sentencia.setInt(2, entrega.getRepartidor().getId());

        sentencia.setDate(
                3,
                Date.valueOf(entrega.getFechaHora().toLocalDate())
        );

        sentencia.setTime(
                4,
                Time.valueOf(entrega.getFechaHora().toLocalTime())
        );
    }

    private Pedido leerPedido(ResultSet resultado) throws SQLException {
        int id = resultado.getInt("pedido_id");
        String direccion = resultado.getString("direccion");
        double distancia = resultado.getDouble("distancia_km");
        String tipo = resultado.getString("tipo");

        if (tipo == null) {
            throw new SQLException("El pedido " + id + " no tiene tipo.");
        }

        Pedido pedido = switch (tipo.trim().toUpperCase(Locale.ROOT)) {
            case "COMIDA" -> new PedidoComida(id, direccion, distancia);
            case "ENCOMIENDA" -> new PedidoEncomienda(id, direccion, distancia);
            case "EXPRESS" -> new PedidoExpress(id, direccion, distancia);
            default -> throw new SQLException(
                    "Tipo desconocido en el pedido " + id + ": " + tipo
            );
        };

        String estado = resultado.getString("estado");

        try {
            if (estado == null) {
                throw new IllegalArgumentException("Estado nulo");
            }

            pedido.setEstado(EstadoPedido.valueOf(estado));
        } catch (IllegalArgumentException e) {
            throw new SQLException(
                    "Estado no válido en el pedido " + id + ": " + estado,
                    e
            );
        }

        return pedido;
    }

    private void validarEntrega(Entrega entrega) {
        if (entrega == null) {
            throw new IllegalArgumentException(
                    "La entrega es obligatoria."
            );
        }

        validarId(entrega.getPedido().getIdPedido());
        validarId(entrega.getRepartidor().getId());
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID debe ser mayor que cero."
            );
        }
    }
}