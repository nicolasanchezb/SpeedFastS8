package ui;

import dao.EntregaDAO;
import dao.PedidoDAO;
import dao.RepartidorDAO;
import model.Entrega;
import model.Pedido;
import model.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class VentanaAsignarEntrega extends JFrame {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final EntregaDAO entregaDAO = new EntregaDAO();

    private final JComboBox<Pedido> comboPedidos = new JComboBox<>();
    private final JComboBox<Repartidor> comboRepartidores = new JComboBox<>();

    private final JComboBox<OpcionFiltro> filtroPedido = new JComboBox<>();
    private final JComboBox<OpcionFiltro> filtroRepartidor = new JComboBox<>();

    private final JTextField campoFecha = new JTextField();
    private final JTextField campoHora = new JTextField();

    private final JLabel etiquetaEstado = new JLabel("Nueva entrega");

    private final JButton registrar = new JButton("Registrar");
    private final JButton editar = new JButton("Guardar cambios");
    private final JButton eliminar = new JButton("Eliminar");
    private final JButton nuevo = new JButton("Nuevo");
    private final JButton actualizar = new JButton("Actualizar");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{
                    "ID", "Pedido", "Dirección",
                    "ID repartidor", "Repartidor", "Fecha", "Hora"
            }, 0
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return switch (columna) {
                case 0, 1, 3 -> Integer.class;
                case 5 -> LocalDate.class;
                case 6 -> LocalTime.class;
                default -> String.class;
            };
        }
    };

    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenador =
            new TableRowSorter<>(modelo);

    private List<Entrega> entregas = List.of();

    private int idSeleccionado;
    private boolean ocupado;
    private boolean recargaPendiente;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withResolverStyle(ResolverStyle.STRICT);

    public VentanaAsignarEntrega() {
        super("Gestión de entregas - SpeedFast");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 560);
        setLocationRelativeTo(null);

        JPanel formulario = new JPanel(new GridLayout(4, 2, 10, 10));
        formulario.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        formulario.add(new JLabel("Pedido:"));
        formulario.add(comboPedidos);
        formulario.add(new JLabel("Repartidor:"));
        formulario.add(comboRepartidores);
        formulario.add(new JLabel("Fecha (AAAA-MM-DD):"));
        formulario.add(campoFecha);
        formulario.add(new JLabel("Hora (HH:mm:ss):"));
        formulario.add(campoHora);

        JPanel botones = new JPanel();
        botones.add(registrar);
        botones.add(editar);
        botones.add(eliminar);
        botones.add(nuevo);
        botones.add(actualizar);

        JPanel filtros = new JPanel(new FlowLayout());
        filtros.add(new JLabel("Filtrar por pedido:"));
        filtros.add(filtroPedido);
        filtros.add(new JLabel("Por repartidor:"));
        filtros.add(filtroRepartidor);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(formulario, BorderLayout.NORTH);
        superior.add(botones, BorderLayout.CENTER);
        superior.add(filtros, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(etiquetaEstado, BorderLayout.SOUTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowSorter(ordenador);

        configurarCombos();

        registrar.addActionListener(e -> guardar(false));
        editar.addActionListener(e -> guardar(true));
        eliminar.addActionListener(e -> eliminarEntrega());
        nuevo.addActionListener(e -> limpiarFormulario());
        actualizar.addActionListener(e -> cargarDatos());

        filtroPedido.addActionListener(e -> {
            if (!ocupado) {
                limpiarFormulario();
                aplicarFiltros();
            }
        });

        filtroRepartidor.addActionListener(e -> {
            if (!ocupado) {
                limpiarFormulario();
                aplicarFiltros();
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !ocupado) {
                cargarSeleccion();
            }
        });

        limpiarFormulario();
        cargarDatos();
    }

    private void configurarCombos() {
        comboPedidos.setRenderer((lista, pedido, indice, seleccionado, foco) -> {
            JLabel etiqueta = (JLabel) new DefaultListCellRenderer()
                    .getListCellRendererComponent(
                            lista, pedido, indice, seleccionado, foco
                    );

            etiqueta.setText(pedido == null ? ""
                    : pedido.getIdPedido() + " - "
                      + pedido.getDireccionEntrega());

            return etiqueta;
        });

        comboRepartidores.setRenderer(
                (lista, repartidor, indice, seleccionado, foco) -> {
                    JLabel etiqueta = (JLabel) new DefaultListCellRenderer()
                            .getListCellRendererComponent(
                                    lista, repartidor, indice, seleccionado, foco
                            );

                    etiqueta.setText(repartidor == null ? ""
                            : repartidor.getId() + " - "
                              + repartidor.getNombre());

                    return etiqueta;
                }
        );
    }

    public void cargarDatos() {
        if (ocupado) {
            // Conserva los avisos recibidos durante otra operación.
            recargaPendiente = true;
            return;
        }

        recargaPendiente = false;

        int pedidoFiltrado = obtenerIdFiltro(filtroPedido);
        int repartidorFiltrado = obtenerIdFiltro(filtroRepartidor);

        bloquear(true);

        new SwingWorker<Datos, Void>() {
            @Override
            protected Datos doInBackground() throws SQLException {
                return new Datos(
                        pedidoDAO.readAll(),
                        repartidorDAO.readAll(),
                        entregaDAO.readAll()
                );
            }

            @Override
            protected void done() {
                try {
                    Datos datos = get();

                    comboPedidos.removeAllItems();
                    comboRepartidores.removeAllItems();
                    filtroPedido.removeAllItems();
                    filtroRepartidor.removeAllItems();

                    filtroPedido.addItem(new OpcionFiltro(0, "Todos"));
                    filtroRepartidor.addItem(new OpcionFiltro(0, "Todos"));

                    for (Pedido pedido : datos.pedidos()) {
                        comboPedidos.addItem(pedido);
                        filtroPedido.addItem(new OpcionFiltro(
                                pedido.getIdPedido(),
                                pedido.getIdPedido() + " - "
                                        + pedido.getDireccionEntrega()
                        ));
                    }

                    for (Repartidor repartidor : datos.repartidores()) {
                        comboRepartidores.addItem(repartidor);
                        filtroRepartidor.addItem(new OpcionFiltro(
                                repartidor.getId(),
                                repartidor.getId() + " - "
                                        + repartidor.getNombre()
                        ));
                    }

                    seleccionarFiltro(filtroPedido, pedidoFiltrado);
                    seleccionarFiltro(filtroRepartidor, repartidorFiltrado);

                    entregas = datos.entregas();
                    modelo.setRowCount(0);

                    for (Entrega entrega : entregas) {
                        modelo.addRow(new Object[]{
                                entrega.getId(),
                                entrega.getPedido().getIdPedido(),
                                entrega.getPedido().getDireccionEntrega(),
                                entrega.getRepartidor().getId(),
                                entrega.getRepartidor().getNombre(),
                                entrega.getFechaHora().toLocalDate(),
                                entrega.getFechaHora().toLocalTime()
                        });
                    }

                    limpiarFormulario();
                    aplicarFiltros();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarError(ex);
                } catch (ExecutionException ex) {
                    mostrarError(ex.getCause());
                } finally {
                    bloquear(false);
                }

                if (recargaPendiente) {
                    cargarDatos();
                }
            }
        }.execute();
    }

    // Filtra los registros cargados usando sus IDs exactos.
    private void aplicarFiltros() {
        int idPedido = obtenerIdFiltro(filtroPedido);
        int idRepartidor = obtenerIdFiltro(filtroRepartidor);

        ordenador.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel,
                            ? extends Integer> fila
            ) {
                int pedido = ((Number) fila.getValue(1)).intValue();
                int repartidor = ((Number) fila.getValue(3)).intValue();

                return (idPedido == 0 || pedido == idPedido)
                        && (idRepartidor == 0 || repartidor == idRepartidor);
            }
        });
    }

    private int obtenerIdFiltro(JComboBox<OpcionFiltro> combo) {
        OpcionFiltro opcion = (OpcionFiltro) combo.getSelectedItem();
        return opcion == null ? 0 : opcion.id();
    }

    private void seleccionarFiltro(
            JComboBox<OpcionFiltro> combo,
            int id
    ) {
        combo.setSelectedIndex(0);

        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id() == id) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void guardar(boolean esEdicion) {
        if (ocupado) {
            return;
        }

        if (esEdicion && idSeleccionado == 0) {
            aviso("Selecciona una entrega de la tabla.");
            return;
        }

        try {
            Pedido pedido = (Pedido) comboPedidos.getSelectedItem();
            Repartidor repartidor =
                    (Repartidor) comboRepartidores.getSelectedItem();

            if (pedido == null || repartidor == null) {
                throw new IllegalArgumentException(
                        "Selecciona un pedido y un repartidor."
                );
            }

            LocalDate fecha = LocalDate.parse(
                    campoFecha.getText().trim(), FORMATO_FECHA
            );

            LocalTime hora = LocalTime.parse(
                    campoHora.getText().trim(), FORMATO_HORA
            );

            if (fecha.getYear() < 1000 || fecha.getYear() > 9999) {
                throw new IllegalArgumentException(
                        "El año debe estar entre 1000 y 9999."
                );
            }

            Entrega entrega = new Entrega(
                    esEdicion ? idSeleccionado : 0,
                    pedido,
                    repartidor,
                    LocalDateTime.of(fecha, hora)
            );

            ejecutar(() -> {
                if (esEdicion) {
                    if (!entregaDAO.update(entrega)) {
                        throw new SQLException(
                                "No se actualizó la entrega. Actualiza la lista."
                        );
                    }
                } else {
                    entregaDAO.create(entrega);
                }
            }, esEdicion ? "Entrega actualizada." : "Entrega registrada.");

        } catch (DateTimeParseException ex) {
            aviso(
                    "Ingresa una fecha válida como 2026-10-01 "
                            + "y una hora como 14:30:00."
            );
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
        }
    }

    private void eliminarEntrega() {
        if (ocupado) {
            return;
        }

        if (idSeleccionado == 0) {
            aviso("Selecciona una entrega de la tabla.");
            return;
        }

        int id = idSeleccionado;

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar la entrega " + id + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutar(() -> {
            if (!entregaDAO.delete(id)) {
                throw new SQLException(
                        "La entrega ya no existe. Actualiza la lista."
                );
            }
        }, "Entrega eliminada.");
    }

    private void cargarSeleccion() {
        int filaVista = tabla.getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        // Convierte la fila filtrada/ordenada a su posición original.
        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        Entrega entrega = entregas.get(filaModelo);

        idSeleccionado = entrega.getId();

        comboPedidos.setSelectedIndex(-1);
        for (int i = 0; i < comboPedidos.getItemCount(); i++) {
            if (comboPedidos.getItemAt(i).getIdPedido()
                    == entrega.getPedido().getIdPedido()) {
                comboPedidos.setSelectedIndex(i);
                break;
            }
        }

        comboRepartidores.setSelectedIndex(-1);
        for (int i = 0; i < comboRepartidores.getItemCount(); i++) {
            if (comboRepartidores.getItemAt(i).getId()
                    == entrega.getRepartidor().getId()) {
                comboRepartidores.setSelectedIndex(i);
                break;
            }
        }

        campoFecha.setText(
                entrega.getFechaHora().toLocalDate().format(FORMATO_FECHA)
        );

        campoHora.setText(
                entrega.getFechaHora().toLocalTime().format(FORMATO_HORA)
        );

        etiquetaEstado.setText("Entrega seleccionada: " + idSeleccionado);
    }

    private void limpiarFormulario() {
        idSeleccionado = 0;
        tabla.clearSelection();

        comboPedidos.setSelectedIndex(-1);
        comboRepartidores.setSelectedIndex(-1);

        LocalDateTime ahora = LocalDateTime.now();
        campoFecha.setText(ahora.toLocalDate().format(FORMATO_FECHA));
        campoHora.setText(ahora.toLocalTime().format(FORMATO_HORA));

        etiquetaEstado.setText("Nueva entrega");
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
                            VentanaAsignarEntrega.this, textoExito
                    );

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarError(ex);
                } catch (ExecutionException ex) {
                    mostrarError(ex.getCause());
                } finally {
                    bloquear(false);
                }

                if (completada || recargaPendiente) {
                    cargarDatos();
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

        comboPedidos.setEnabled(!valor);
        comboRepartidores.setEnabled(!valor);
        filtroPedido.setEnabled(!valor);
        filtroRepartidor.setEnabled(!valor);

        campoFecha.setEnabled(!valor);
        campoHora.setEnabled(!valor);
        tabla.setEnabled(!valor);

        etiquetaEstado.setText(valor
                ? "Procesando..."
                : idSeleccionado == 0
                  ? "Nueva entrega"
                  : "Entrega seleccionada: " + idSeleccionado);
    }

    private void aviso(String texto) {
        JOptionPane.showMessageDialog(
                this, texto, "Validación", JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarError(Throwable causa) {
        String texto = causa == null
                ? "Error desconocido."
                : causa.getMessage();

        if (causa instanceof SQLException sql) {
            if (sql.getErrorCode() == 1452) {
                texto = "El pedido o repartidor ya no existe. "
                        + "Actualiza las listas.";
            } else if (sql.getErrorCode() == 1451) {
                texto = "El registro tiene datos asociados "
                        + "y no puede eliminarse.";
            }
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

    private record Datos(
            List<Pedido> pedidos,
            List<Repartidor> repartidores,
            List<Entrega> entregas
    ) {
    }

    private record OpcionFiltro(int id, String texto) {
        @Override
        public String toString() {
            return texto;
        }
    }
}