package dao;

import model.Entrega;
import model.Pedido;
import model.Repartidor;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class PruebaEntregaDAO {

    public static void main(String[] args) {
        PedidoDAO pedidoDAO = new PedidoDAO();
        RepartidorDAO repartidorDAO = new RepartidorDAO();
        EntregaDAO entregaDAO = new EntregaDAO();

        try {
            Pedido pedido = null;
            Repartidor repartidor = null;

            for (Pedido actual : pedidoDAO.listarTodos()) {
                if (actual.getIdPedido() == 107) {
                    pedido = actual;
                    break;
                }
            }

            for (Repartidor actual : repartidorDAO.listarTodos()) {
                if (actual.getId() == 1) {
                    repartidor = actual;
                    break;
                }
            }

            if (pedido == null || repartidor == null) {
                System.out.println(
                        "No se encontró el pedido 107 o el repartidor 1."
                );
                return;
            }

            Entrega entrega = new Entrega(
                    pedido,
                    repartidor,
                    LocalDateTime.now()
            );

            entregaDAO.guardar(entrega);

            System.out.println(
                    "Entrega del pedido 107 asignada a "
                            + repartidor.getNombre()
                            + " y guardada en MySQL."
            );

        } catch (SQLException e) {
            System.out.println(
                    "No se pudo guardar la entrega: "
                            + e.getMessage()
            );
        }
    }
}