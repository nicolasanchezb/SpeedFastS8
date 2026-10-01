package dao;

import model.Pedido;

import java.sql.SQLException;

public class PruebaListadoPedidos {

    public static void main(String[] args) {
        PedidoDAO pedidoDAO = new PedidoDAO();

        try {
            for (Pedido pedido : pedidoDAO.listarTodos()) {
                System.out.println(
                        pedido.getIdPedido()
                                + " | " + pedido.getDireccionEntrega()
                                + " | " + pedido.obtenerTipoEntrega()
                                + " | " + pedido.getEstado()
                );
            }

        } catch (SQLException e) {
            System.out.println(
                    "No se pudieron listar los pedidos: "
                            + e.getMessage()
            );
        }
    }
}