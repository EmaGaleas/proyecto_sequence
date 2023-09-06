package clases;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;

public class registro {

    private static String login;
    private static int cantidadJ;
    private static int codigoLogueado;
    private static String colorFicha;
    private RandomAccessFile cods, registros, modo, reportes;

    public registro() {
        try {
            File f = new File("usuarios");
            f.mkdir();
            cods = new RandomAccessFile("usuarios/codigos.emp", "rw");
            registros = new RandomAccessFile("usuarios/registrado.emp", "rw");
            initCodigo();
        } catch (IOException e) {
            System.out.println("SE TRONO");
        }
    }

    private void initCodigo() throws IOException {
        if (cods.length() == 0) {
            cods.writeInt(1);
        }
    }

    private int getCode() throws IOException {
        cods.seek(0);
        int codigo = cods.readInt();
        cods.seek(0);
        cods.writeInt(codigo + 1);
        return codigo;
    }

    public boolean usuarioExiste(String username) throws IOException {
        registros.seek(0);
        while (registros.getFilePointer() < registros.length()) {
            registros.readInt();
            registros.readUTF();
            registros.readInt();
            String existingUsername = registros.readUTF();
            registros.readLong();
            registros.readUTF();

            if (username.equals(existingUsername)) {
                JOptionPane.showMessageDialog(null, "USERNAME no disponible");
                return true;
            }
        }
        return false;
    }

    private String carpetaUsuario(int code) {
        return "usuarios/registro" + code;
    }

    private void crearCarpetaUsuario(int code) throws IOException {
        File udir = new File(carpetaUsuario(code));
        udir.mkdir();
    }

    public void agregarUsuario(String nombreCompleto, String username, String contra) throws IOException {
        if (!usuarioExiste(username)) {
            registros.seek(registros.length());
            int code = getCode();
            registros.writeInt(code);
            registros.writeUTF(nombreCompleto);
            registros.writeInt(0);
            registros.writeUTF(username);
            registros.writeLong(Calendar.getInstance().getTimeInMillis());
            registros.writeUTF(contra);
            crearCarpetaUsuario(code);

            //crea archivo que contendra datos del modo de este jugador
            RandomAccessFile modoArchivo = new RandomAccessFile(carpetaUsuario(code) + "/modo.emp", "rw");
            modoArchivo.writeInt(4);//por default es 4
            modoArchivo.writeUTF("NO APLICA");//si no aplica no pasa nada pero cuando se escoja va a cambiar sobreescirbe seek 0
            modoArchivo.close();
            cantidadJ = 4;
            colorFicha = "NO APLICA";

            //crea archivo que contendra datos de reportes de este jugador 
            RandomAccessFile repotesArchivo = new RandomAccessFile(carpetaUsuario(code) + "/reportes.emp", "rw");
            repotesArchivo.writeLong(Calendar.getInstance().getTimeInMillis());//pruebita
            repotesArchivo.writeUTF("NO HA JUGADOR");//se le va a ñadir despues
            repotesArchivo.close();

            login = username;
            codigoLogueado = code;

            JOptionPane.showMessageDialog(null, "Agregado correctamente");
        } else {
            JOptionPane.showMessageDialog(null, "USERNAME no disponible");
        }
    }

    public void imprimirModo() throws IOException {
        String path = carpetaUsuario(codigoLogueado) + "/modo.emp";
        RandomAccessFile raf = new RandomAccessFile(path, "r");
        try {
            int entero = raf.readInt();
            String cadena = raf.readUTF();
            System.out.println("modo:" + entero);
            System.out.println("ficha:" + cadena);
        } catch (EOFException e) {
            JOptionPane.showMessageDialog(null, "Voa llorar");
        } finally {
            raf.close();
        }
    }

    public void sobreModo(int nModo, String ficha) throws IOException {
        String path = carpetaUsuario(codigoLogueado) + "/modo.emp";
        RandomAccessFile raf = new RandomAccessFile(path, "rw");
        try {
            raf.seek(0); //principio del archivo
            raf.writeInt(nModo); //Sobre el int
            raf.writeUTF(ficha); //Sobre color
            cantidadJ = nModo;
            colorFicha = ficha;
        } finally {
            raf.close();
        }
    }

    public int getCantidadJ() throws IOException {
        String path = carpetaUsuario(codigoLogueado) + "/modo.emp";
        RandomAccessFile raf = new RandomAccessFile(path, "r");
        int entero = 4;
        try {
            entero = raf.readInt();
            raf.readUTF();
        } catch (EOFException e) {
            JOptionPane.showMessageDialog(null, "Voa llorar");
        } finally {
            raf.close();
        }
        return entero;
    }

    public String getColorFicha() throws IOException {
        String path = carpetaUsuario(codigoLogueado) + "/modo.emp";
        RandomAccessFile raf = new RandomAccessFile(path, "r");
        String ficha = "";
        try {
            raf.readInt();
            ficha = raf.readUTF();
        } catch (EOFException e) {
            JOptionPane.showMessageDialog(null, "Voa llorar");
        } finally {
            raf.close();
        }
        return ficha;
    }

    public String imprimirReportes() throws IOException {
        String path = carpetaUsuario(codigoLogueado) + "/reportes.emp";
        RandomAccessFile raf = new RandomAccessFile(path, "r");
        String r = "";
        try {
            while (raf.getFilePointer() < raf.length()) {
                Date fecha = new Date(raf.readLong());
                String dat = raf.readUTF();
                r += fecha + "\t" + dat + "\n";
            }
            //esto lo podes usar para insertarlo despues en un text area la letra esta
            System.out.println(r);
            return r;
        } catch (EOFException e) {
            JOptionPane.showMessageDialog(null, "INTENTO 5 PQ SE LE OLVIDO EL LLAMADO JEJE");
        } finally {
            raf.close();
        }
        System.out.println(r);
        return "";
    }

    public void agregarReportes(String sms) throws IOException {
        String path = carpetaUsuario(codigoLogueado) + "/reportes.emp";
        RandomAccessFile raf = new RandomAccessFile(path, "rw");
        try {
            raf.seek(raf.length());
            raf.writeLong(Calendar.getInstance().getTimeInMillis());
            raf.writeUTF(sms);
            JOptionPane.showMessageDialog(null, "REPORTE AGREGADO");
        } finally {
            raf.close();
        }
    }

//
//    public void listarUsuariosT() throws IOException {//COMENTAR AL TERMINAR
//        registros.seek(0);
//        while (registros.getFilePointer() < registros.length()) {
//            int codigo = registros.readInt();
//            String nombre = registros.readUTF();
//            int puntos = registros.readInt();
//            String username = registros.readUTF();
//            Date fechaRegistro = new Date(registros.readLong());
//            String contrasena = registros.readUTF();
//            System.out.println(codigo + " - " + nombre + " - " + puntos + " - " + username + " - " + fechaRegistro + " - " + contrasena);
//        }
//    }
    public String listarUsuarios() throws IOException {//COMENTAR AL TERMINAR
        registros.seek(0);
        String j = "";
        while (registros.getFilePointer() < registros.length()) {
            registros.readInt();
            registros.readUTF();
            registros.readInt();
            String username = registros.readUTF();
            registros.readLong();
            registros.readUTF();
            j += username + "\n";
        }
        return j;
    }

    public boolean login(String username, String contraseña) throws IOException {
        registros.seek(0);
        while (registros.getFilePointer() < registros.length()) {
            int code = registros.readInt();
            registros.readUTF();
            registros.readInt();
            String user = registros.readUTF();
            Date fc = new Date(registros.readLong());
            String contra = registros.readUTF();
            if (username.equals(user) && contraseña.equals(contra)) {
                login = username;
                codigoLogueado = code;
                JOptionPane.showMessageDialog(null, "Bienvenido de vuelta");
                return true;
            }
        }
        JOptionPane.showMessageDialog(null, "Acceso denegado");
        return false;
    }

    public static String getLogin() {
        return login;
    }

    public int contarUsuarios() throws IOException {
        int contador = 0;
        registros.seek(0);
        while (registros.getFilePointer() < registros.length()) {
            registros.readInt();
            registros.readUTF();
            registros.readInt();
            registros.readUTF();
            registros.readLong();
            registros.readUTF();
            contador++;
        }
        return contador;
    }

}
