import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {
    private final JButton btnComprar;
    private final JButton btnVender;
    private final JButton btnCobrar;
    private final JButton btnPagar;
    private final JButton btnAmortizar;
    private final JButton btnDepositar;
    private final JButton btnRetirar;
    private final JButton btnTarjetaDeAlmacen;
    private final JButton btnSalir;

    public Main() {
        setTitle("Sistema de Contabilidad");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 1));

        btnComprar = new JButton("Comprar");
        btnVender = new JButton("Vender");
        btnCobrar = new JButton("Cobrar");
        btnPagar = new JButton("Pagar");
        btnAmortizar = new JButton("Amortizar");
        btnDepositar = new JButton("Depositar");
        btnRetirar = new JButton("Retirar");
        btnTarjetaDeAlmacen = new JButton("Tarjeta de almacen");
        btnSalir = new JButton("Salir");

        panel.add(btnComprar);
        panel.add(btnVender);
        panel.add(btnCobrar);
        panel.add(btnPagar);
        panel.add(btnAmortizar);
        panel.add(btnDepositar);
        panel.add(btnRetirar);
        panel.add(btnTarjetaDeAlmacen);
        panel.add(btnSalir);

        add(panel);

        btnComprar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Compras(Main.this).setVisible(true);
                Main.this.dispose(); // Cierra la ventana principal
            }
        });

        btnVender.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        btnCobrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        btnPagar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        btnAmortizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        btnDepositar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        btnRetirar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        btnTarjetaDeAlmacen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new seleccionarMetodo(Main.this).setVisible(true);
                Main.this.dispose(); // Cierra la ventana principal
            }
        });

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
