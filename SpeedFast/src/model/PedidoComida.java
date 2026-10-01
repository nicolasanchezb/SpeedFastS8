package model;

public class PedidoComida extends Pedido {

    public PedidoComida(int idPedido,
                        String direccionEntrega,
                        double distanciaKm) {

        super(idPedido, direccionEntrega, distanciaKm);
    }

    @Override
    public void asignarRepartidor() {
        repartidorAsignado = "Juan - mochila térmica";
    }
    @Override
    public int calcularTiempoEntrega() {
        return (int) Math.round(15 + (2 * distanciaKm));
    }

    @Override
    public String obtenerTipoEntrega() {
        return "Pedido de comida";
    }

    @Override
    public String obtenerFactoresDuracion() {
        return "15 minutos base más 2 minutos por cada kilómetro";
    }
}