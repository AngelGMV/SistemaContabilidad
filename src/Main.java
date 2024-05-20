import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        int opcion = 0;
        menuOpciones menu = new menuOpciones();
        do {
            menu.menuPrinciapl();
            opcion = sc.nextInt();
        }while (opcion!=6);
        
    }
}