package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class ZonaDeCarga {

    private final List<Pedido> pedidos;

    public ZonaDeCarga() {
        pedidos = new ArrayList<>();
    }

    public synchronized void agregarPedido(Pedido pedido) {
        pedidos.add(pedido);

        System.out.println(
                "Pedido " + pedido.getIdPedido()
                        + " agregado a la zona de carga."
        );
    }

    public synchronized Pedido retirarPedido() {

        Iterator<Pedido> iterator = pedidos.iterator();

        while (iterator.hasNext()) {
            Pedido pedido = iterator.next();

            if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
                pedido.setEstado(EstadoPedido.EN_REPARTO);
                iterator.remove();
                return pedido;
            }
        }

        return null;
    }
}