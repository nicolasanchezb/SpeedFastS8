package ui;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private VentanaGestionPedidos ventanaPedidos;
    private VentanaRepartidores ventanaRepartidores;
    private VentanaAsignarEntrega ventanaEntregas;

    public VentanaPrincipal() {
        super("SpeedFast - Gestión");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 300);
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel(
                "Sistema SpeedFast", SwingConstants.CENTER
        );

        titulo.setFont(
                titulo.getFont().deriveFont(Font.BOLD, 22f)
        );

        JPanel botones = new JPanel(
                new GridLayout(3, 1, 10, 10)
        );

        botones.setBorder(
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        );

        JButton pedidos = new JButton("Gestionar pedidos");
        JButton repartidores = new JButton("Gestionar repartidores");
        JButton entregas = new JButton("Gestionar entregas");

        botones.add(pedidos);
        botones.add(repartidores);
        botones.add(entregas);

        add(titulo, BorderLayout.NORTH);
        add(botones, BorderLayout.CENTER);

        pedidos.addActionListener(e -> abrirPedidos());
        repartidores.addActionListener(e -> abrirRepartidores());
        entregas.addActionListener(e -> abrirEntregas());
    }

    private void abrirPedidos() {
        if (ventanaPedidos == null
                || !ventanaPedidos.isDisplayable()) {

            ventanaPedidos = new VentanaGestionPedidos();

            ventanaPedidos.addPropertyChangeListener(
                    "datosActualizados",
                    e -> refrescarEntregas()
            );
        }

        ventanaPedidos.setVisible(true);
        ventanaPedidos.toFront();
    }

    private void abrirRepartidores() {
        if (ventanaRepartidores == null
                || !ventanaRepartidores.isDisplayable()) {

            ventanaRepartidores = new VentanaRepartidores();

            // Recibe el aviso emitido después de cada operación CRUD.
            ventanaRepartidores.addPropertyChangeListener(
                    "datosActualizados",
                    e -> refrescarEntregas()
            );
        }

        ventanaRepartidores.setVisible(true);
        ventanaRepartidores.toFront();
    }

    private void abrirEntregas() {
        if (ventanaEntregas == null
                || !ventanaEntregas.isDisplayable()) {

            ventanaEntregas = new VentanaAsignarEntrega();
        } else {
            ventanaEntregas.cargarDatos();
        }

        ventanaEntregas.setVisible(true);
        ventanaEntregas.toFront();
    }

    private void refrescarEntregas() {
        if (ventanaEntregas != null
                && ventanaEntregas.isDisplayable()) {

            ventanaEntregas.cargarDatos();
        }
    }
}