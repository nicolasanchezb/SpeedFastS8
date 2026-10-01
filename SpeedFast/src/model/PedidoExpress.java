package model;

public class PedidoExpress extends Pedido {

    public PedidoExpress(int idPedido,
                         String direccionEntrega,
                         double distanciaKm) {

        super(idPedido, direccionEntrega, distanciaKm);
    }

    @Override
    public void asignarRepartidor() {
        repartidorAsignado = "María - cercana a " + direccionEntrega
                + " - espacio disponible para pedido adicional";
    }
    @Override
    public int calcularTiempoEntrega() {
        int tiempo = 10;

        if (distanciaKm > 5) {
            tiempo = tiempo + 5;
        }

        return tiempo;
    }

    @Override
    public String obtenerTipoEntrega() {
        return "Pedido express";
    }

    @Override
    public String obtenerFactoresDuracion() {
        if (distanciaKm > 5) {
            return "10 minutos base más 5 minutos adicionales "
                    + "por superar los 5 kilómetros";
        }

        return "10 minutos base, sin tiempo adicional";
    }
}