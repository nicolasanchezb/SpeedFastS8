package ui;

import dao.RepartidorDAO;
import model.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class VentanaRepartidores extends JFrame {

    private final RepartidorDAO repartidorDAO = new RepartidorDAO();

    private final JTextField campoNombre = new JTextField(20);
    private final JLabel etiquetaEstado = new JLabel("Nuevo repartidor");

    private final JButton registrar = new JButton("Registrar");
    private final JButton editar = new JButton("Guardar cambios");
    private final JButton eliminar = new JButton("Eliminar");
    private final JButton nuevo = new JButton("Nuevo");
    private final JButton actualizar = new JButton("Actualizar");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nombre"}, 0
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 0 ? Integer.class : String.class;
        }
    };

    private final JTable tabla = new JTable(modelo);

    private int idSeleccionado;
    private boolean ocupado;

    public VentanaRepartidores() {
        super("Gestión de repartidores - SpeedFast");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(650, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new FlowLayout());
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        JPanel botones = new JPanel(new FlowLayout());
        botones.add(registrar);
        botones.add(editar);
        botones.add(eliminar);
        botones.add(nuevo);
        botones.add(actualizar);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(formulario, BorderLayout.NORTH);
        superior.add(botones, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(etiquetaEstado, BorderLayout.SOUTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !ocupado) {
                cargarSeleccion();
            }
        });

        registrar.addActionListener(e -> guardar(false));
        editar.addActionListener(e -> guardar(true));
        eliminar.addActionListener(e -> eliminarSeleccionado());
        nuevo.addActionListener(e -> limpiarFormulario());
        actualizar.addActionListener(e -> cargarRepartidores());

        cargarRepartidores();
    }

    private void guardar(boolean esEdicion) {
        if (ocupado) {
            return;
        }

        String nombre = campoNombre.getText().trim();

        if (nombre.isBlank() || nombre.length() > 100) {
            mostrarAviso(
                    "Ingresa un nombre de entre 1 y 100 caracteres."
            );
            return;
        }

        if (esEdicion && idSeleccionado == 0) {
            mostrarAviso("Selecciona un repartidor de la tabla.");
            return;
        }

        int id = idSeleccionado;

        ejecutarOperacion(() -> {
            if (esEdicion) {
                if (!repartidorDAO.update(id, nombre)) {
                    throw new SQLException(
                            "No se actualizó el repartidor. Actualiza la lista."
                    );
                }
            } else {
                repartidorDAO.create(nombre);
            }
        }, esEdicion
                ? "Repartidor actualizado."
                : "Repartidor registrado.");
    }

    private void eliminarSeleccionado() {
        if (ocupado) {
            return;
        }

        if (idSeleccionado == 0) {
            mostrarAviso("Selecciona un repartidor de la tabla.");
            return;
        }

        int id = idSeleccionado;

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar el repartidor " + id + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutarOperacion(() -> {
            if (!repartidorDAO.delete(id)) {
                throw new SQLException(
                        "El repartidor ya no existe. Actualiza la lista."
                );
            }
        }, "Repartidor eliminado.");
    }

    private void cargarSeleccion() {
        int filaVista = tabla.getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);

        idSeleccionado = (Integer) modelo.getValueAt(filaModelo, 0);
        campoNombre.setText(
                (String) modelo.getValueAt(filaModelo, 1)
        );

        etiquetaEstado.setText(
                "Repartidor seleccionado: " + idSeleccionado
        );
    }

    private void limpiarFormulario() {
        idSeleccionado = 0;
        tabla.clearSelection();
        campoNombre.setText("");
        etiquetaEstado.setText("Nuevo repartidor");
    }

    private void cargarRepartidores() {
        if (ocupado) {
            return;
        }

        cambiarOcupado(true);

        new SwingWorker<List<Repartidor>, Void>() {
            @Override
            protected List<Repartidor> doInBackground()
                    throws SQLException {
                return repartidorDAO.readAll();
            }

            @Override
            protected void done() {
                try {
                    List<Repartidor> repartidores = get();

                    modelo.setRowCount(0);

                    for (Repartidor repartidor : repartidores) {
                        modelo.addRow(new Object[]{
                                repartidor.getId(),
                                repartidor.getNombre()
                        });
                    }

                    limpiarFormulario();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarError(ex);
                } catch (ExecutionException ex) {
                    mostrarError(ex.getCause());
                } finally {
                    cambiarOcupado(false);
                }
            }
        }.execute();
    }

    private void ejecutarOperacion(
            OperacionSQL operacion,
            String mensaje
    ) {
        cambiarOcupado(true);

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
                            VentanaRepartidores.this, mensaje
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarError(ex);
                } catch (ExecutionException ex) {
                    mostrarError(ex.getCause());
                } finally {
                    cambiarOcupado(false);
                }

                if (completada) {
                    cargarRepartidores();

                    // Avisa al menú para refrescar los combos de entregas.
                    firePropertyChange("datosActualizados", false, true);
                }
            }
        }.execute();
    }

    private void cambiarOcupado(boolean valor) {
        ocupado = valor;

        registrar.setEnabled(!valor);
        editar.setEnabled(!valor);
        eliminar.setEnabled(!valor);
        nuevo.setEnabled(!valor);
        actualizar.setEnabled(!valor);
        campoNombre.setEnabled(!valor);
        tabla.setEnabled(!valor);

        etiquetaEstado.setText(valor
                ? "Procesando..."
                : idSeleccionado == 0
                  ? "Nuevo repartidor"
                  : "Repartidor seleccionado: " + idSeleccionado);
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(
                this, mensaje, "Validación", JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarError(Throwable error) {
        String mensaje = error == null
                ? "Error desconocido."
                : error.getMessage();

        if (error instanceof SQLException sql
                && sql.getErrorCode() == 1451) {
            mensaje = "El repartidor tiene entregas asociadas "
                    + "y no puede eliminarse.";
        }

        JOptionPane.showMessageDialog(
                this,
                "No se pudo completar la operación:\n" + mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @FunctionalInterface
    private interface OperacionSQL {
        void ejecutar() throws SQLException;
    }
}