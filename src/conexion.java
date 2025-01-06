import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {
	private String nombreBd = "LIBRO_MAYOR";
	private String usuario = "root";
	private String password = "";
	private String url = "jdbc:mysql://localhost:3306/" + nombreBd + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

	Connection conn = null;

	public conexion() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url, usuario, password);
			if (conn != null) {
				System.out.println("Conexión exitosa a la base de datos.");
			}
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "CONEXIÓN FALLIDA, ClassNotFoundException: " + e.getMessage(), "CONEXIÓN FALLIDA...", JOptionPane.INFORMATION_MESSAGE);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "CONEXIÓN FALLIDA, SQLException: " + e.getLocalizedMessage(), "CONEXIÓN FALLIDA...", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public static void main(String[] args) {
		new conexion();
	}
}
