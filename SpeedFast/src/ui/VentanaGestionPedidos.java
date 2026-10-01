package ui;

import dao.PedidoDAO;
import model.EstadoPedido;
import model.Pedido;
import model.PedidoComida;
import model.PedidoEncomienda;
import model.PedidoExpress;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class VentanaGestionPedidos extends JFrame {

    private final PedidoDAO dao = new PedidoDAO();

    private final JTextField direccion = new JTextField();
    private final JTextField distancia = new JTextField();

    private final JComboBox<String> tipo = new JComboBox<>(
            new String[]{"COMIDA", "ENCOMIENDA", "EXPRESS"}
    );

    private final JComboBox<EstadoPedido> estado =
            new JComboBox<>(EstadoPedido.values());

    private final JButton registrar = new JButton("Registrar");
    private final JButton editar = new JButton("Guardar cambios");
    private final JButton eliminar = new JButton("Eliminar");
    private final JButton nuevo = new JButton("Nuevo");
    private final JButton actualizar = new JButton("Actualizar");

    private final JLabel mensaje = new JLabel("Nuevo pedido");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Dirección", "Distancia km", "Tipo", "Estado"},
            0
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return switch (columna) {
                case 0 -> Integer.class;
                case 2 -> Double.class;
                default -> String.class;
            };
        }
    };

    private final JTable tabla = new JTable(modelo);

    private int idSeleccionado;
    private boolean ocupado;

    public VentanaGestionPedidos() {
        super("Gestión de pedidos - SpeedFast");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 480);
        setLocationRelativeTo(null);

        JPanel formulario = new JPanel(new GridLayout(4, 2, 10, 10));
        formulario.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        formulario.add(new JLabel("Dirección:"));
        formulario.add(direccion);
        formulario.add(new JLabel("Distancia en kilómetros:"));
        formulario.add(distancia);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(tipo);
        formulario.add(new JLabel("Estado:"));
        formulario.add(estado);

        JPanel botones = new JPanel();
        botones.add(registrar);
        botones.add(editar);
        botones.add(eliminar);
        botones.add(nuevo);
        botones.add(actualizar);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(mensaje, BorderLayout.SOUTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !ocupado) {
                seleccionar();
            }
        });

        registrar.addActionListener(e -> guardar(false));
        editar.addActionListener(e -> guardar(true));
        eliminar.addActionListener(e -> eliminarPedido());
        nuevo.addActionListener(e -> limpiar());
        actualizar.addActionListener(e -> cargar());

        cargar();
    }

    private void guardar(boolean esEdicion) {
        if (ocupado) {
            return;
        }

        if (esEdicion && idSeleccionado == 0) {
            aviso("Selecciona un pedido de la tabla.");
            return;
        }

        try {
            String textoDireccion = direccion.getText().trim();

            if (textoDireccion.isBlank() || textoDireccion.length() > 150) {
                throw new IllegalArgumentException(
                        "La dirección debe tener entre 1 y 150 caracteres."
                );
            }

            double km;

            try {
                km = Double.parseDouble(
                        distancia.getText().trim().replace(',', '.')
                );
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Ingresa una distancia numérica, por ejemplo 4.5."
                );
            }

            if (!Double.isFinite(km) || km <= 0) {
                throw new IllegalArgumentException(
                        "La distancia debe ser mayor que cero."
                );
            }

            int id = esEdicion ? idSeleccionado : 0;

            Pedido pedido = switch ((String) tipo.getSelectedItem()) {
                case "COMIDA" ->
                        new PedidoComida(id, textoDireccion, km);
                case "ENCOMIENDA" ->
                        new PedidoEncomienda(id, textoDireccion, km);
                case "EXPRESS" ->
                        new PedidoExpress(id, textoDireccion, km);
                default -> throw new IllegalArgumentException(
                        "Selecciona un tipo válido."
                );
            };

            EstadoPedido seleccionado =
                    (EstadoPedido) estado.getSelectedItem();

            if (seleccionado == null) {
                throw new IllegalArgumentException("Selecciona un estado.");
            }

            pedido.setEstado(seleccionado);

            ejecutar(() -> {
                if (esEdicion) {
                    if (!dao.update(pedido)) {
                        throw new SQLException(
                                "No se actualizó el pedido. Actualiza la lista."
                        );
                    }
                } else {
                    dao.create(pedido);
                }
            }, esEdicion ? "Pedido actualizado." : "Pedido registrado.");

        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
        }
    }

    private void eliminarPedido() {
        if (ocupado) {
            return;
        }

        if (idSeleccionado == 0) {
            aviso("Selecciona un pedido de la tabla.");
            return;
        }

        int id = idSeleccionado;

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar el pedido " + id + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutar(() -> {
            if (!dao.delete(id)) {
                throw new SQLException(
                        "El pedido ya no existe. Actualiza la lista."
                );
            }
        }, "Pedido eliminado.");
    }

    private void seleccionar() {
        int filaVista = tabla.getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int fila = tabla.convertRowIndexToModel(filaVista);

        idSeleccionado = (Integer) modelo.getValueAt(fila, 0);
        direccion.setText((String) modelo.getValueAt(fila, 1));
        distancia.setText(modelo.getValueAt(fila, 2).toString());
        tipo.setSelectedItem(modelo.getValueAt(fila, 3));
        estado.setSelectedItem(
                EstadoPedido.valueOf((String) modelo.getValueAt(fila, 4))
        );

        mensaje.setText("Pedido seleccionado: " + idSeleccionado);
    }

    private String obtenerTipo(Pedido pedido) {
        if (pedido instanceof PedidoComida) {
            return "COMIDA";
        }

        if (pedido instanceof PedidoEncomienda) {
            return "ENCOMIENDA";
        }

        return "EXPRESS";
    }

    private void limpiar() {
        idSeleccionado = 0;
        tabla.clearSelection();
        direccion.setText("");
        distancia.setText("");
        tipo.setSelectedIndex(0);
        estado.setSelectedItem(EstadoPedido.PENDIENTE);
        mensaje.setText("Nuevo pedido");
    }

    private void cargar() {
        if (ocupado) {
            return;
        }

        bloquear(true);

        new SwingWorker<List<Pedido>, Void>() {
            @Override
            protected List<Pedido> doInBackground() throws SQLException {
                return dao.readAll();
            }

            @Override
            protected void done() {
                try {
                    List<Pedido> pedidos = get();

                    modelo.setRowCount(0);

                    for (Pedido pedido : pedidos) {
                        modelo.addRow(new Object[]{
                                pedido.getIdPedido(),
                                pedido.getDireccionEntrega(),
                                pedido.getDistanciaKm(),
                                obtenerTipo(pedido),
                                pedido.getEstado().name()
                        });
                    }

                    limpiar();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    error(ex);
                } catch (ExecutionException ex) {
                    error(ex.getCause());
                } finally {
                    bloquear(false);
                }
            }
        }.execute();
    }

    private void ejecutar(OperacionSQL operacion, String textoExito) {
        bloquear(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws SQLException {
                operacion.ejecutar();
                return null;
            }

            @Override
            protected void done() {
                boolean completada = false;

                try {
                    get();
                    completada = true;
                    JOptionPane.showMessageDialog(
                            VentanaGestionPedidos.this, textoExito
                    );
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    error(ex);
                } catch (ExecutionException ex) {
                    error(ex.getCause());
                } finally {
                    bloquear(false);
                }

                if (completada) {
                    cargar();
                    firePropertyChange("datosActualizados", false, true);
                }
            }
        }.execute();
    }

    private void bloquear(boolean valor) {
        ocupado = valor;

        registrar.setEnabled(!valor);
        editar.setEnabled(!valor);
        eliminar.setEnabled(!valor);
        nuevo.setEnabled(!valor);
        actualizar.setEnabled(!valor);
        direccion.setEnabled(!valor);
        distancia.setEnabled(!valor);
        tipo.setEnabled(!valor);
        estado.setEnabled(!valor);
        tabla.setEnabled(!valor);

        mensaje.setText(valor
                ? "Procesando..."
                : idSeleccionado == 0
                  ? "Nuevo pedido"
                  : "Pedido seleccionado: " + idSeleccionado);
    }

    private void aviso(String texto) {
        JOptionPane.showMessageDialog(
                this, texto, "Validación", JOptionPane.WARNING_MESSAGE
        );
    }

    private void error(Throwable causa) {
        String texto = causa == null
                ? "Error desconocido."
                : causa.getMessage();

        if (causa instanceof SQLException sql
                && sql.getErrorCode() == 1451) {
            texto = "El pedido tiene entregas asociadas "
                    + "y no puede eliminarse.";
        }

        JOptionPane.showMessageDialog(
                this,
                "No se pudo completar la operación:\n" + texto,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @FunctionalInterface
    private interface OperacionSQL {
        void ejecutar() throws SQLException;
    }
}