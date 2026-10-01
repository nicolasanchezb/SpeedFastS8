package ui;

import dao.PedidoDAO;
import model.Pedido;
import service.GestorPedidos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class VentanaListaPedidos extends JFrame {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{
                    "ID",
                    "Dirección",
                    "Tipo",
                    "Km",
                    "Estado",
                    "Minutos estimados"
            },
            0
    ) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);
    private final Timer temporizador;

    public VentanaListaPedidos(GestorPedidos gestor) {
        super("Listado de pedidos - SpeedFast");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel acciones = new JPanel();

        JButton botonRefrescar = new JButton("Refrescar");
        JButton botonAsignar = new JButton("Asignar entrega");

        acciones.add(botonRefrescar);
        acciones.add(botonAsignar);

        add(acciones, BorderLayout.SOUTH);

        botonRefrescar.addActionListener(e -> actualizarTabla());

        botonAsignar.addActionListener(e ->
                new VentanaAsignarEntrega().setVisible(true)
        );

        actualizarTabla();

        // Consulta nuevamente MySQL cada dos segundos.
        temporizador = new Timer(2000, e -> actualizarTabla());
        temporizador.start();
    }

    private void actualizarTabla() {
        try {
            modelo.setRowCount(0);

            for (Pedido pedido : pedidoDAO.listarTodos()) {
                modelo.addRow(new Object[]{
                        pedido.getIdPedido(),
                        pedido.getDireccionEntrega(),
                        pedido.obtenerTipoEntrega(),
                        pedido.getDistanciaKm(),
                        pedido.getEstado(),
                        pedido.calcularTiempoEntrega()
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron cargar los pedidos: "
                            + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    public void dispose() {
        temporizador.stop();
        super.dispose();
    }
}