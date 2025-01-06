import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class seleccionarMetodo extends JFrame {
    private JComboBox<String> comboBox;
    private JButton btnSubmit;
    private JFrame mainFrame;
    public static String metodo = "";

    public seleccionarMetodo(JFrame mainFrame) {
        this.mainFrame = mainFrame;

        if (metodo.isEmpty()) {
            setTitle("Tarjeta de Almacén");
            setSize(400, 200);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 1));

            String[] opciones = {"Precio promedio", "PEPS", "UEPS"};

            comboBox = new JComboBox<>(opciones);
            btnSubmit = new JButton("Confirmar");

            panel.add(new JLabel("Seleccione el método a usar:"));
            panel.add(comboBox);
            panel.add(btnSubmit);

            add(panel);

            btnSubmit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    metodo = (String) comboBox.getSelectedItem();
                    tarjetaDeAlmacen();
                }
            });
        } else {
            tarjetaDeAlmacen();
        }
    }

    private void tarjetaDeAlmacen() {
        switch (metodo) {
            case "Precio promedio":
                new tarjetaPrecioPromedio(mainFrame).setVisible(true);
                dispose();
                break;
            case "PEPS":
                tarjetaPEPS();
                break;
            case "UEPS":
                tarjetaUEPS();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Método no válido.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    private void tarjetaPrecioPromedio() {

    }

    private void tarjetaPEPS() {
        // Implementar lógica para tarjeta PEPS
        JOptionPane.showMessageDialog(this, "Método PEPS seleccionado.");
        // Código adicional para la funcionalidad de PEPS
    }

    private void tarjetaUEPS() {
        // Implementar lógica para tarjeta UEPS
        JOptionPane.showMessageDialog(this, "Método UEPS seleccionado.");
        // Código adicional para la funcionalidad de UEPS
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new seleccionarMetodo(null).setVisible(true);
            }
        });
    }
}
