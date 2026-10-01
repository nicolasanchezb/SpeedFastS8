package dao;

import model.Repartidor;

import java.sql.SQLException;

public class PruebaRepartidorDAO {

    public static void main(String[] args) {
        RepartidorDAO dao = new RepartidorDAO();

        try {
            dao.guardar("Juan");
            dao.guardar("María");
            dao.guardar("Alexis");

            System.out.println("Repartidores registrados:");

            for (Repartidor repartidor : dao.listarTodos()) {
                System.out.println(
                        repartidor.getId()
                                + " | "
                                + repartidor.getNombre()
                );
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error de base de datos: " + e.getMessage()
            );
        }
    }
}