import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class clientC {
    String user;
    String ip;
    Socket socket;
    DataOutputStream dout;
    DataInputStream din;
    boolean connected;

    public clientC() {
        this.connected = false;
        getConfig();
        gui_locked();
    }

    public static void main(String[] args) {
        new clientC();
    }

    public void gui_locked() {
        JFrame window = new JFrame();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.setSize(screen);
        window.setUndecorated(true);
        JPanel main = new JPanel();
        JPanel panel = new JPanel();
        window.getContentPane().add(panel);
        JButton connectionStatus = new JButton();
        JLabel connectionStext = new JLabel();
        panel.setLayout(null);
        connectionStatus.setPreferredSize(new Dimension(10, 10));
        connectionStext.setPreferredSize(new Dimension(170, 10));
        JLabel info = new JLabel("Ask the Administrator to start the session.");
        panel.add(new JLabel(user)).setBounds(screen.width / 2 - 100, 100, 200, 30);
        panel.add(info).setBounds(screen.width / 2 - 250, screen.height / 2 - 100, 600, 30);
        panel.add(connectionStatus).setBounds(screen.width - 400, 30, 10, 10);
        panel.add(connectionStext).setBounds(screen.width - 380, 30, 170, 10);
        window.setVisible(true);
        window.setAlwaysOnTop(true);

        //frame unlocked
        JFrame unlocked = new JFrame();
        JButton connectionStatus1 = new JButton();
        JLabel connectionStext1 = new JLabel();
        unlocked.setSize(350, 120);
        unlocked.setUndecorated(true);
        unlocked.setLocation(screen.width - 400, 0);
        JPanel session = new JPanel();
        JLabel price = new JLabel();
        JLabel duration = new JLabel();
        session.add(connectionStatus1).setPreferredSize(new Dimension(10, 10));
        session.add(connectionStext1).setPreferredSize(new Dimension(270, 25));
        session.add(new JLabel("User:")).setPreferredSize(new Dimension(70, 30));
        session.add(new JLabel(user)).setPreferredSize(new Dimension(70, 30));
        session.add(new JLabel("Duration:")).setPreferredSize(new Dimension(70, 30));
        session.add(duration).setPreferredSize(new Dimension(50, 30));
        session.add(new JLabel("Price:")).setPreferredSize(new Dimension(70, 30));
        session.add(price).setPreferredSize(new Dimension(50, 30));
        session.setPreferredSize(new Dimension(350, 130));
        unlocked.getContentPane().add(session);
        unlocked.setVisible(false);

        while (true) {
            if (!connected) {
                connectionStatus.setBackground(Color.RED);
                connectionStatus.setContentAreaFilled(false);
                connectionStatus.setOpaque(true);
                connectionStext.setText("Connection not established");
                connectionStext.setForeground(Color.RED);
                connectionStatus1.setBackground(Color.RED);
                connectionStatus1.setContentAreaFilled(false);
                connectionStatus1.setOpaque(true);
                connectionStext1.setText("Connection not established");
                connectionStext1.setForeground(Color.RED);
                info.setText("Cannot establish connection to server.Check your network configurations.");
                try {
                    socket = new Socket(ip, 3304);
                    dout = new DataOutputStream(socket.getOutputStream());
                    din = new DataInputStream(socket.getInputStream());
                    dout.writeUTF("client:" + user);
                    connected = true;
                } catch (IOException e) {
                    new logger(e.toString()).writelog();
                    connected = false;
                    continue;
                }
            } else {
                connectionStext.setText("Connection Established");
                connectionStext.setForeground(Color.green);
                connectionStatus.setBackground(Color.GREEN);
                connectionStatus.setContentAreaFilled(false);
                connectionStatus.setOpaque(true);
                connectionStext1.setText("Connection Established");
                connectionStext1.setForeground(Color.green);
                connectionStatus1.setBackground(Color.GREEN);
                connectionStatus1.setContentAreaFilled(false);
                connectionStatus1.setOpaque(true);
                info.setText("Ask the Administrator to start the session.");
                try {
                    dout.writeUTF(user);
                    while (din.available() != 0) {
                        String data = din.readUTF();
                        System.out.println(data);
                        if (data.startsWith(user)) {
                            window.setVisible(false);
                            unlocked.setVisible(true);
                        }
                        if (data.startsWith("Price:")) {
                            price.setText(data.split(":")[1]);
                        }
                        if (data.startsWith("Duration:")) {
                            duration.setText(data.split(":")[1]);
                        }
                        if (data.startsWith("stop:" + user)) {
                            unlocked.setVisible(false);
                            window.setVisible(true);
                        }
                    }
                    window.repaint();
                    window.revalidate();
                } catch (IOException e) {
                    new logger(e.toString()).writelog();
                    connected = false;
                    continue;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                new logger(e.toString()).writelog();
            }
        }
    }

    public boolean getConfig() {
        boolean status = false;
        String data = new getConfig().getClientConfig();
        if (!data.isEmpty()) {
            status = true;
            user = data.split(",")[0];
            ip = data.split(",")[1];
        } else {
            getConfig();
        }
        return status;
    }
}