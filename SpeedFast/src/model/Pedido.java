package model;

public abstract class Pedido {

    protected int idPedido;
    protected String direccionEntrega;
    protected double distanciaKm;
    protected String repartidorAsignado;
    private EstadoPedido estado;

    public Pedido(int idPedido, String direccionEntrega, double distanciaKm) {
        this.idPedido = idPedido;
        this.direccionEntrega = direccionEntrega;
        this.distanciaKm = distanciaKm;
        this.repartidorAsignado = "Sin asignar";
        this.estado = EstadoPedido.PENDIENTE;
    }

    public int getIdPedido() {
        return idPedido;
    }
    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public void setEstado(String nuevoEstado) {
        this.estado = EstadoPedido.valueOf(nuevoEstado.toUpperCase());
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }
    public double getDistanciaKm() {
        return distanciaKm;
    }

    public String getRepartidorAsignado() {
        return repartidorAsignado;
    }

    public void mostrarResumen() {
        System.out.println("ID del pedido: " + idPedido);
        System.out.println("Dirección de entrega: " + direccionEntrega);
        System.out.println("Distancia: " + distanciaKm + " km");
        System.out.println("Tipo de entrega: " + obtenerTipoEntrega());
        System.out.println("Factores que afectan su duración: "
                + obtenerFactoresDuracion());
        System.out.println("Repartidor asignado: " + repartidorAsignado);
    }

    public abstract void asignarRepartidor();

    public void asignarRepartidor(String nombre) {
        this.repartidorAsignado = nombre;
    }

    public abstract int calcularTiempoEntrega();

    public abstract String obtenerTipoEntrega();

    public abstract String obtenerFactoresDuracion();
}