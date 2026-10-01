package dao;

import model.PedidoComida;

import java.sql.SQLException;

public class PruebaPedidoDAO {

    public static void main(String[] args) {
        PedidoDAO pedidoDAO = new PedidoDAO();

        try {
            pedidoDAO.guardar(
                    new PedidoComida(
                            107,
                            "Avenida Alemania 500",
                            4.5
                    )
            );

            System.out.println("Pedido 107 guardado en MySQL.");

        } catch (SQLException e) {
            System.out.println(
                    "No se pudo guardar: " + e.getMessage()
            );
        }
    }
}