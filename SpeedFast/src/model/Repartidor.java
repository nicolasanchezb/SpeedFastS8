package model;

public class Repartidor implements Runnable {

    private final int id;
    private final String nombre;
    private final ZonaDeCarga zonaDeCarga;

    // Constructor utilizado por la simulación de entregas.
    public Repartidor(String nombre, ZonaDeCarga zonaDeCarga) {
        this(0, nombre, zonaDeCarga);
    }

    // Constructor utilizado para los repartidores leídos desde MySQL.
    public Repartidor(int id, String nombre) {
        this(id, nombre, null);
    }

    private Repartidor(int id, String nombre, ZonaDeCarga zonaDeCarga) {
        this.id = id;
        this.nombre = nombre;
        this.zonaDeCarga = zonaDeCarga;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public void run() {
        if (zonaDeCarga == null) {
            throw new IllegalStateException(
                    "Este repartidor no tiene una zona de carga asignada."
            );
        }

        while (true) {
            Pedido pedido = zonaDeCarga.retirarPedido();

            if (pedido == null) {
                System.out.println(
                        nombre + " no encontró más pedidos pendientes."
                );
                break;
            }

            pedido.asignarRepartidor(nombre);

            System.out.println(
                    nombre + " retiró el pedido "
                            + pedido.getIdPedido()
                            + " - Estado: "
                            + pedido.getEstado()
            );

            try {
                System.out.println(
                        nombre + " está entregando el pedido "
                                + pedido.getIdPedido()
                                + " en "
                                + pedido.getDireccionEntrega()
                );

                Thread.sleep(2000);

                pedido.setEstado(EstadoPedido.ENTREGADO);

                System.out.println(
                        nombre + " entregó el pedido "
                                + pedido.getIdPedido()
                                + " - Estado: "
                                + pedido.getEstado()
                );

            } catch (InterruptedException e) {
                System.out.println(
                        "La entrega realizada por "
                                + nombre
                                + " fue interrumpida."
                );

                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}