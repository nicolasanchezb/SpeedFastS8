package model;

import java.time.LocalDateTime;

public class Entrega {

    private final int id;
    private final Pedido pedido;
    private final Repartidor repartidor;
    private final LocalDateTime fechaHora;

    // Constructor para nuevas entregas, todavía sin ID generado.
    public Entrega(
            Pedido pedido,
            Repartidor repartidor,
            LocalDateTime fechaHora
    ) {
        this(0, pedido, repartidor, fechaHora);
    }

    // Constructor para entregas recuperadas de MySQL o editadas.
    public Entrega(
            int id,
            Pedido pedido,
            Repartidor repartidor,
            LocalDateTime fechaHora
    ) {
        if (id < 0) {
            throw new IllegalArgumentException(
                    "El ID de la entrega no puede ser negativo."
            );
        }

        if (pedido == null || repartidor == null || fechaHora == null) {
            throw new IllegalArgumentException(
                    "Pedido, repartidor, fecha y hora son obligatorios."
            );
        }

        if (pedido.getIdPedido() <= 0) {
            throw new IllegalArgumentException(
                    "El pedido debe estar registrado en MySQL."
            );
        }

        if (repartidor.getId() <= 0) {
            throw new IllegalArgumentException(
                    "El repartidor debe estar registrado en MySQL."
            );
        }

        this.id = id;
        this.pedido = pedido;
        this.repartidor = repartidor;
        this.fechaHora = fechaHora.withNano(0);
    }

    public int getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Repartidor getRepartidor() {
        return repartidor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}