package service;

import dao.PedidoDAO;
import model.EstadoPedido;
import model.Pedido;
import model.Repartidor;
import model.ZonaDeCarga;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GestorPedidos {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final List<Pedido> pedidos = new ArrayList<>();
    private final ZonaDeCarga zonaDeCarga = new ZonaDeCarga();

    private ExecutorService executor;

    public GestorPedidos() throws SQLException {
        // Recupera los pedidos guardados en ejecuciones anteriores.
        for (Pedido pedido : pedidoDAO.listarTodos()) {
            pedidos.add(pedido);

            if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
                zonaDeCarga.agregarPedido(pedido);
            }
        }
    }

    public synchronized void registrar(Pedido pedido)
            throws SQLException {

        for (Pedido existente : pedidos) {
            if (existente.getIdPedido() == pedido.getIdPedido()) {
                throw new IllegalArgumentException(
                        "Ya existe un pedido con ese ID."
                );
            }
        }

        // Primero se guarda en MySQL. Si falla, no se añade a la lista.
        pedidoDAO.guardar(pedido);

        pedidos.add(pedido);
        zonaDeCarga.agregarPedido(pedido);
    }

    public synchronized List<Pedido> listar() {
        return new ArrayList<>(pedidos);
    }

    public synchronized boolean existePedido(int id) {
        for (Pedido pedido : pedidos) {
            if (pedido.getIdPedido() == id) {
                return true;
            }
        }

        return false;
    }

    public synchronized void asignar(Pedido pedido) {
        if (!pedidos.contains(pedido)
                || pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Selecciona un pedido pendiente."
            );
        }

        pedido.asignarRepartidor();
    }

    public synchronized void iniciarEntregas() {
        if (executor != null && !executor.isTerminated()) {
            throw new IllegalStateException(
                    "Las entregas ya están en curso."
            );
        }

        boolean hayPendientes = pedidos.stream()
                .anyMatch(
                        p -> p.getEstado() == EstadoPedido.PENDIENTE
                );

        if (!hayPendientes) {
            throw new IllegalStateException(
                    "No hay pedidos pendientes."
            );
        }

        executor = Executors.newFixedThreadPool(3);

        executor.execute(
                new Repartidor("Juan", zonaDeCarga)
        );

        executor.execute(
                new Repartidor("María", zonaDeCarga)
        );

        executor.execute(
                new Repartidor("Alexis", zonaDeCarga)
        );

        executor.shutdown();
    }
}
