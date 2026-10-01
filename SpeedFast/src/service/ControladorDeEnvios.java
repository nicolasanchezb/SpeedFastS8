package service;

import interfaces.Cancelable;
import interfaces.Despachable;
import interfaces.Rastreable;
import model.Pedido;
import java.util.ArrayList;

public class ControladorDeEnvios
    implements Despachable, Cancelable, Rastreable{

    private Pedido pedidoActual;
    private ArrayList<String> historial;

    public ControladorDeEnvios() {
        historial = new ArrayList<>();
    }
    public void reservarPedido(Pedido pedido) {
        pedidoActual = pedido;
        historial.add("Pedido " + pedido.getIdPedido() + " orden recibida");
    }
    @Override
    public void despachar() {
        historial.add("Pedido " + pedidoActual.getIdPedido() +" despachado");
    }
    @Override
    public void cancelar() {
        historial.add("Pedido " + pedidoActual.getIdPedido() + " cancelado");
    }
    @Override
    public void verHistorial() {
     for (String evento : historial) {
       System.out.println(evento);
     }
    }
}

