package model;

public class PedidoEncomienda extends Pedido {

    public PedidoEncomienda(
            int idPedido,
            String direccionEntrega,
            double distanciaKm) {

        super(idPedido, direccionEntrega, distanciaKm);
    }

    @Override
    public void asignarRepartidor() {
        repartidorAsignado = "Alexis - vehículo: Moto";
    }
    @Override
    public int calcularTiempoEntrega() {
        double tiempo = 20 + (1.5 * distanciaKm);

        return (int) Math.round(tiempo);
    }

    @Override
    public String obtenerTipoEntrega() {
        return "Pedido de encomienda";
    }

    @Override
    public String obtenerFactoresDuracion() {
        return "20 minutos base más 1,5 minutos por cada kilómetro";
    }
}