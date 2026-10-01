package ui;

import model.Pedido;
import model.PedidoComida;
import model.PedidoEncomienda;
import model.PedidoExpress;
import service.GestorPedidos;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class VentanaRegistroPedido extends JFrame {

    private final JTextField campoId = new JTextField();
    private final JTextField campoDireccion = new JTextField();
    private final JTextField campoDistancia = new JTextField();

    private final JComboBox<String> comboTipo = new JComboBox<>(
            new String[]{"Comida", "Encomienda", "Express"}
    );

    public VentanaRegistroPedido(GestorPedidos gestor) {
        super("Registrar pedido");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(390, 270);
        setLocationRelativeTo(null);

        JPanel formulario = new JPanel(
                new GridLayout(4, 2, 8, 8)
        );

        formulario.setBorder(
                BorderFactory.createEmptyBorder(
                        18, 18, 8, 18
                )
        );

        formulario.add(new JLabel("ID:"));
        formulario.add(campoId);

        formulario.add(new JLabel("Dirección:"));
        formulario.add(campoDireccion);

        formulario.add(new JLabel("Distancia (km):"));
        formulario.add(campoDistancia);

        formulario.add(new JLabel("Tipo:"));
        formulario.add(comboTipo);

        add(formulario, BorderLayout.CENTER);

        JButton guardar = new JButton("Guardar");
        add(guardar, BorderLayout.SOUTH);

        guardar.addActionListener(e -> {
            try {
                int id = Integer.parseInt(
                        campoId.getText().trim()
                );

                double distancia = Double.parseDouble(
                        campoDistancia.getText()
                                .trim()
                                .replace(',', '.')
                );

                String direccion =
                        campoDireccion.getText().trim();

                if (id <= 0
                        || distancia <= 0
                        || !Double.isFinite(distancia)
                        || direccion.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Ingresa un ID y distancia positivos, "
                                    + "y una dirección."
                    );
                }

                Pedido pedido;

                switch ((String) comboTipo.getSelectedItem()) {
                    case "Comida":
                        pedido = new PedidoComida(
                                id, direccion, distancia
                        );
                        break;

                    case "Encomienda":
                        pedido = new PedidoEncomienda(
                                id, direccion, distancia
                        );
                        break;

                    default:
                        pedido = new PedidoExpress(
                                id, direccion, distancia
                        );
                }

                gestor.registrar(pedido);

                JOptionPane.showMessageDialog(
                        this,
                        "Pedido " + id
                                + " registrado en MySQL."
                );

                dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "ID debe ser entero y distancia debe ser numérica.",
                        "Datos inválidos",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Datos inválidos",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo guardar en MySQL:\n"
                                + ex.getMessage(),
                        "Error de base de datos",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}