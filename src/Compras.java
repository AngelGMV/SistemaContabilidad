import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Compras extends JFrame {
    private JComboBox<String> comboBox;
    private JTextField textField;
    private JButton btnSubmit;
    private JButton btnBack;
    private JFrame mainFrame;

    public Compras(JFrame mainFrame) {
        this.mainFrame = mainFrame;

        setTitle("Compras");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1)); // Ajustar el GridLayout para incluir el botón de regreso

        String[] opciones = {"Mercancias", "Edificio", "Terreno", "Acciones y valores",
                "Mobiliario y equipo de oficina", "Equipo de computo", "Equipo de reparto y entrega"};

        comboBox = new JComboBox<>(opciones);
        textField = new JTextField();
        btnSubmit = new JButton("Registrar Compra");
        btnBack = new JButton("Regresar");

        panel.add(comboBox);
        panel.add(new JLabel("Ingrese el costo total de la compra:"));
        panel.add(textField);
        panel.add(btnSubmit);
        panel.add(btnBack);

        add(panel);

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarCompra();
            }
        });

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.setVisible(true); // Reabre la ventana principal
                dispose(); // Cierra la ventana actual
            }
        });
    }

    public void registrarCompra() {
        String seleccion = (String) comboBox.getSelectedItem();
        float costoTotal;
        try {
            costoTotal = Float.parseFloat(textField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un costo válido.");
            return;
        }

        float saldoBanco = getSaldoBanco();

        conexion conectar = new conexion();

        String query;
        String insertMovimiento = "INSERT INTO MOVIMIENTOS (FECHA_MOVIMIENTO) VALUES (CURDATE())";
        switch (seleccion) {
            case "Mercancias":
                query = "INSERT INTO ALMACEN (MOVIMIENTO_ALMACEN, DEUDOR_ALMACEN) VALUES (LAST_INSERT_ID(), ?);";
                break;
            case "Edificio":
                query = "INSERT INTO EDIFICIOS (MOVIMIENTO_EDIFICIOS, DEUDOR_EDIFICIOS) VALUES (LAST_INSERT_ID(), ?);";
                break;
            case "Terreno":
                query = "INSERT INTO TERRENOS (MOVIMIENTO_TERRENOS, DEUDOR_TERRENOS) VALUES (LAST_INSERT_ID(), ?);";
                break;
            case "Acciones y valores":
                query = "INSERT INTO ACCIONES_Y_VALORES (MOVIMIENTO_ACCIONES_Y_VALORES, DEUDOR_ACCIONES_Y_VALORES) VALUES (LAST_INSERT_ID(), ?);";
                break;
            case "Mobiliario y equipo de oficina":
                query = "INSERT INTO MOB_Y_EQ_DE_OFICINA (MOVIMIENTO_MOB_Y_EQ_DE_OFICINA, DEUDOR_MOB_Y_EQ_DE_OFICINA) VALUES (LAST_INSERT_ID(), ?);";
                break;
            case "Equipo de computo":
                query = "INSERT INTO EQ_DE_COMPUTO (MOVIMIENTO_EQ_DE_COMPUTO, DEUDOR_EQ_DE_COMPUTO) VALUES (LAST_INSERT_ID(), ?);";
                break;
            case "Equipo de reparto y entrega":
                query = "INSERT INTO EQ_DE_REPARTO (MOVIMIENTO_EQ_DE_REPARTO, DEUDOR_EQ_DE_REPARTO) VALUES (LAST_INSERT_ID(), ?);";
                break;
            default:
                throw new IllegalStateException("Valor fuera de rango: " + seleccion);
        }

        try {
            conectar.conn.setAutoCommit(false);
            int movimientoId = 0;
            try (PreparedStatement stmt = conectar.conn.prepareStatement(insertMovimiento, Statement.RETURN_GENERATED_KEYS)) {
                stmt.executeUpdate();
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        movimientoId = generatedKeys.getInt(1);
                    }
                }
            }

            if (movimientoId == 0) {
                throw new SQLException("Fallo al insertar el movimiento, no se obtuvo el ID.");
            }

            if (saldoBanco >= costoTotal) {
                // Restar del saldo del banco
                String updateBanco = "INSERT INTO BANCO (MOVIMIENTO_BANCO, DEUDOR_BANCO) VALUES (?, ?)";
                try (PreparedStatement stmt = conectar.conn.prepareStatement(updateBanco)) {
                    stmt.setInt(1, movimientoId);
                    stmt.setFloat(2, costoTotal);
                    stmt.executeUpdate();
                }
            } else if (seleccion=="Mercancias") {
                // Agregar deuda a proveedores
                String updateBanco = "INSERT INTO PROVEEDORES (MOVIMIENTO_PROVEEDORES, ACREEDOR_PROVEEDORES) VALUES (?, ?)";
                try (PreparedStatement stmt = conectar.conn.prepareStatement(updateBanco)) {
                    stmt.setInt(1, movimientoId);
                    stmt.setFloat(2, costoTotal);
                    stmt.executeUpdate();
                }
            }
            else {
                String updateBanco = "INSERT INTO DOC_POR_PAGAR (MOVIMIENTO_DOC_POR_PAGAR, ACREEDOR_DOC_POR_PAGAR) VALUES (?, ?)";
                try (PreparedStatement stmt = conectar.conn.prepareStatement(updateBanco)) {
                    stmt.setInt(1, movimientoId);
                    stmt.setFloat(2, costoTotal);
                    stmt.executeUpdate();
                }
            }

            // Insertar en la tabla correspondiente
            try (PreparedStatement stmt = conectar.conn.prepareStatement(query)) {
                stmt.setFloat(1, costoTotal);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Compra registrada exitosamente.");
            }

            // Confirmar la transacción
            conectar.conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                // Revertir la transacción en caso de error
                conectar.conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                conectar.conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    public float getSaldoBanco() {
        conexion conectar = new conexion();
        float saldoBanco = 0;
        String query = "SELECT SUM(DEUDOR_BANCO) - SUM(ACREEDOR_BANCO) AS saldo FROM BANCO";

        try (Statement st = conectar.conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                saldoBanco = rs.getFloat("saldo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return saldoBanco;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Compras(null).setVisible(true);
            }
        });
    }
}
