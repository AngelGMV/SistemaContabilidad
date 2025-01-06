import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class tarjetaPrecioPromedio extends JFrame {
    private JComboBox<String> comboBox;
    private JTextField cantidad, precioAdq;
    private JButton btnBack;
    private JButton btnSubmit;
    private JFrame mainFrame;
    private conexion conectar = new conexion();
    float ultimaVenta = 1880;
    public tarjetaPrecioPromedio(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        setTitle("Tarjeta de Almacén Precio Promedio");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        String[] opciones = {"Comprar", "Vender", "Devolucion a proveedor", "Devolucion de cliente"};

        comboBox = new JComboBox<>(opciones);
        btnSubmit = new JButton("Confirmar");

        panel.add(new JLabel("¿Qué hacemos hoy?"));
        panel.add(comboBox);
        panel.add(btnSubmit);

        add(panel);

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String opcion = (String) comboBox.getSelectedItem();

                switch (Objects.requireNonNull(opcion)) {
                    case "Comprar":
                        dispose(); // Cierra la ventana actual
                        comprar(mainFrame);
                        break;
                    case "Vender":
                        dispose();
                        vender();
                        break;
                    case "Devolucion a proveedor":
                        dispose();
                        devProv();
                        break;
                    case "Devolucion de cliente":
                        dispose();
                        devClie();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + Objects.requireNonNull(opcion));
                }
            }
        });
    }

    public void comprar(JFrame mainFrame) {
        JFrame compraFrame = new JFrame("Compras");
        compraFrame.setSize(400, 300);
        compraFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        compraFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        cantidad = new JTextField();
        precioAdq = new JTextField();
        btnSubmit = new JButton("Registrar Compra");
        btnBack = new JButton("Regresar");

        panel.add(new JLabel("Ingrese la cantidad a comprar:"));
        panel.add(cantidad);
        panel.add(new JLabel("Ingrese el precio de adquisición unitario:"));
        panel.add(precioAdq);
        panel.add(btnSubmit);
        panel.add(btnBack);

        compraFrame.add(panel);
        compraFrame.setVisible(true);

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int entradas = Integer.parseInt(cantidad.getText());
                float precio = Float.parseFloat(precioAdq.getText());
                float saldoBanco = getSaldoBanco();

                String insertMovimiento = "INSERT INTO MOVIMIENTOS (FECHA_MOVIMIENTO) VALUES (CURDATE())";
                String query = "INSERT INTO TARJETA_ALMACEN (MOVIMIENTO_TARJETA,ENTRADAS_TARJETA, SALIDAS_TARJETA, EXISTENCIA_TARJETA, PRECIO_ADQUISICION,PRECIO_PROMEDIO,DEUDOR_TARJETA, ACREEDOR_TARJETA) VALUES (LAST_INSERT_ID(),?,?,?,?,?,?,?)";
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
                    try (PreparedStatement stmt = conectar.conn.prepareStatement(query)) {
                        stmt.setInt(1, entradas);
                        stmt.setInt(2, 0);
                        stmt.setInt(3, (getExistencia()+entradas));
                        stmt.setFloat(4, precio);
                        stmt.setFloat(5, getPrecioPromedio());
                        stmt.setFloat(6, (entradas * precio));
                        stmt.setFloat(7, 0);
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(mainFrame, "Compra registrada exitosamente.");
                    }
                    if (saldoBanco >= (precio * entradas)) {
                        String updateBanco = "INSERT INTO BANCO (MOVIMIENTO_BANCO, DEUDOR_BANCO) VALUES (?, ?)";
                        try (PreparedStatement stmt = conectar.conn.prepareStatement(updateBanco)) {
                            stmt.setInt(1, movimientoId);
                            stmt.setFloat(2, (entradas * precio));
                            stmt.executeUpdate();
                        }
                    } else {
                        String updateBanco = "INSERT INTO PROVEEDORES (MOVIMIENTO_PROVEEDORES, ACREEDOR_PROVEEDORES) VALUES (?, ?)";
                        try (PreparedStatement stmt = conectar.conn.prepareStatement(updateBanco)) {
                            stmt.setInt(1, movimientoId);
                            stmt.setFloat(2, (entradas * precio));
                            stmt.executeUpdate();
                        }
                    }
                    conectar.conn.commit();
                } catch (SQLException ex) {
                    try {
                        conectar.conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                    ex.printStackTrace();
                }
            }
        });

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new seleccionarMetodo(mainFrame).setVisible(true);
                compraFrame.dispose(); // Cierra la ventana de compra
            }
        });
    }

    public void vender() {
        JFrame compraFrame = new JFrame("Ventas");
        compraFrame.setSize(400, 300);
        compraFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        compraFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        cantidad = new JTextField();
        btnSubmit = new JButton("Registrar venta");
        btnBack = new JButton("Regresar");

        panel.add(new JLabel("Ingrese la cantidad a vender:"));
        panel.add(cantidad);
        panel.add(btnSubmit);
        panel.add(btnBack);

        compraFrame.add(panel);
        compraFrame.setVisible(true);

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int entradas = Integer.parseInt(cantidad.getText());
                float precio = getPrecioPromedio();

                String insertMovimiento = "INSERT INTO MOVIMIENTOS (FECHA_MOVIMIENTO) VALUES (CURDATE())";
                String query = "INSERT INTO TARJETA_ALMACEN (MOVIMIENTO_TARJETA,ENTRADAS_TARJETA, SALIDAS_TARJETA, EXISTENCIA_TARJETA, PRECIO_ADQUISICION,PRECIO_PROMEDIO,DEUDOR_TARJETA, ACREEDOR_TARJETA) VALUES (LAST_INSERT_ID(),?,?,?,?,?,?,?)";
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
                    try (PreparedStatement stmt = conectar.conn.prepareStatement(query)) {
                        stmt.setInt(1, 0);
                        stmt.setInt(2, entradas);
                        stmt.setInt(3,(getExistencia()-entradas) );
                        stmt.setFloat(4, 0);
                        stmt.setFloat(5, precio);
                        stmt.setFloat(6, 0);
                        stmt.setFloat(7, (entradas * precio));
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(mainFrame, "Venta registrada exitosamente.");
                    }

                    conectar.conn.commit();
                } catch (SQLException ex) {
                    try {
                        conectar.conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                    ex.printStackTrace();
                }
            }
        });

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new seleccionarMetodo(mainFrame).setVisible(true);
                dispose(); // Cierra la ventana de compra
            }
        });
    }

    public void devProv() {
        JFrame compraFrame = new JFrame("Devolucion a proveedor");
        compraFrame.setSize(400, 300);
        compraFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        compraFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        cantidad = new JTextField();
        btnSubmit = new JButton("Registrar devolucion");
        btnBack = new JButton("Regresar");

        panel.add(new JLabel("Ingrese la cantidad a devolver:"));
        panel.add(cantidad);
        panel.add(btnSubmit);
        panel.add(btnBack);

        compraFrame.add(panel);
        compraFrame.setVisible(true);

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int entradas = Integer.parseInt(cantidad.getText());
                float precio = getPrecioMayor();

                String insertMovimiento = "INSERT INTO MOVIMIENTOS (FECHA_MOVIMIENTO) VALUES (CURDATE())";
                String query = "INSERT INTO TARJETA_ALMACEN (MOVIMIENTO_TARJETA,ENTRADAS_TARJETA, SALIDAS_TARJETA, EXISTENCIA_TARJETA, PRECIO_ADQUISICION,PRECIO_PROMEDIO,DEUDOR_TARJETA, ACREEDOR_TARJETA) VALUES (LAST_INSERT_ID(),?,?,?,?,?,?,?)";
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
                    try (PreparedStatement stmt = conectar.conn.prepareStatement(query)) {
                        stmt.setInt(1, 0);
                        stmt.setInt(2, entradas);
                        stmt.setInt(3,getExistencia()-entradas);
                        stmt.setFloat(4, precio);
                        stmt.setFloat(5, getPrecioPromedio());
                        stmt.setFloat(6, 0);
                        stmt.setFloat(7, (entradas * precio));
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(mainFrame, "Devolucion a proveedor registrada exitosamente.");
                    }

                    conectar.conn.commit();
                } catch (SQLException ex) {
                    try {
                        conectar.conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                    ex.printStackTrace();
                }
            }
        });

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new seleccionarMetodo(mainFrame).setVisible(true);
                dispose(); // Cierra la ventana de compra
            }
        });
    }

    public void devClie() {
        JFrame compraFrame = new JFrame("Devolucion a cliente");
        compraFrame.setSize(400, 300);
        compraFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        compraFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        cantidad = new JTextField();
        btnSubmit = new JButton("Registrar devolucion");
        btnBack = new JButton("Regresar");

        panel.add(new JLabel("Ingrese la cantidad devuelta:"));
        panel.add(cantidad);
        panel.add(btnSubmit);
        panel.add(btnBack);

        compraFrame.add(panel);
        compraFrame.setVisible(true);

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int entradas = Integer.parseInt(cantidad.getText());
                float precio = getPrecioMenor();

                String insertMovimiento = "INSERT INTO MOVIMIENTOS (FECHA_MOVIMIENTO) VALUES (CURDATE())";
                String query = "INSERT INTO TARJETA_ALMACEN (MOVIMIENTO_TARJETA,ENTRADAS_TARJETA, SALIDAS_TARJETA, EXISTENCIA_TARJETA, PRECIO_ADQUISICION,PRECIO_PROMEDIO,DEUDOR_TARJETA, ACREEDOR_TARJETA) VALUES (LAST_INSERT_ID(),?,?,?,?,?,?,?)";
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
                    try (PreparedStatement stmt = conectar.conn.prepareStatement(query)) {
                        stmt.setInt(1, entradas);
                        stmt.setInt(2, 0);
                        stmt.setInt(3,getExistencia()+entradas);
                        stmt.setFloat(4, 0);
                        stmt.setFloat(5, ultimaVenta);
                        stmt.setFloat(6, (entradas * ultimaVenta));
                        stmt.setFloat(7, 0);
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(mainFrame, "Devolucion a proveedor registrada exitosamente.");
                    }

                    conectar.conn.commit();
                } catch (SQLException ex) {
                    try {
                        conectar.conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                    ex.printStackTrace();
                }
            }
        });

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new seleccionarMetodo(mainFrame).setVisible(true);
                dispose(); // Cierra la ventana de compra
            }
        });
    }

    public float getPrecioPromedio() {
        float precioPromedio = 0;
        String query = "SELECT SUM(DEUDOR_TARJETA) / (SUM(ENTRADAS_TARJETA) - SUM(SALIDAS_TARJETA)) AS precioPromedio FROM TARJETA_ALMACEN";

        try (Statement st = conectar.conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                precioPromedio = rs.getFloat("precioPromedio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return precioPromedio;
    }
    public float getPrecioMayor() {
        String selectQuery = "SELECT MOVIMIENTO_TARJETA, PRECIO_ADQUISICION, EXISTENCIA_TARJETA FROM TARJETA_ALMACEN WHERE EXISTENCIA_TARJETA > 0 ORDER BY PRECIO_ADQUISICION DESC LIMIT 1";
        float precio = 0;
        try (Statement st = conectar.conn.createStatement(); ResultSet rs = st.executeQuery(selectQuery)) {
            if (rs.next()) {
                int id = rs.getInt("MOVIMIENTO_TARJETA");
                precio = rs.getFloat("PRECIO_ADQUISICION");
                int existencia = rs.getInt("EXISTENCIA_TARJETA");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return precio;
    }
    public float getPrecioMenor() {
        String selectQuery = "SELECT MOVIMIENTO_TARJETA, PRECIO_ADQUISICION, EXISTENCIA_TARJETA FROM TARJETA_ALMACEN WHERE EXISTENCIA_TARJETA > 0 ORDER BY PRECIO_ADQUISICION ASC LIMIT 1";
        float precio = 0;
        try (Statement st = conectar.conn.createStatement(); ResultSet rs = st.executeQuery(selectQuery)) {
            if (rs.next()) {
                int id = rs.getInt("MOVIMIENTO_TARJETA");
                precio = rs.getFloat("PRECIO_ADQUISICION");
                int existencia = rs.getInt("EXISTENCIA_TARJETA");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return precio;
    }



    public int getExistencia() {
        int existencia = 0;

        String query = "SELECT SUM(ENTRADAS_TARJETA) - SUM(SALIDAS_TARJETA) AS EXISTENCIA FROM TARJETA_ALMACEN";

        try (Statement st = conectar.conn.createStatement(); ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                existencia = rs.getInt("EXISTENCIA");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return existencia;
    }

    public float getSaldoBanco() {
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
                new tarjetaPrecioPromedio(null).setVisible(true);
            }
        });
    }
}
