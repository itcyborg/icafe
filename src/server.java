import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Created by itcyb on 1/10/2017.
 */
public class server {
    static String Admin = "";
    java.util.Date dt = new java.util.Date();
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String currentTime = sdf.format(dt);
    //static SimpleDateFormat localDateFormat = new SimpleDateFormat("YYYY/MM/DD HH:mm:ss");
    boolean usersEntered;
    DataOutputStream dout;
    DataInputStream din;
    Hashtable<String, String> runningClients;
    Hashtable<String, String> stoppedClients;
    Hashtable<Integer, String> allClients;
    Hashtable<String, Integer> connectedClients;
    Thread r;
    JLabel costLab = new JLabel();
    JLabel durationLab = new JLabel();
    JLabel userLab = new JLabel();
    String activeClient = "";
    JButton stopClient = new JButton("Stop");
    String userj = "";
    boolean stop;
    int id;
    Connection conn = new dbConnector().getConnection();

    public server() {
        new Thread() {
            @Override
            public void run() {
                gui();
            }
        }.start();
        runningClients = new Hashtable<>();
        stoppedClients = new Hashtable<>();
        allClients = new Hashtable<>();
        connectedClients = new Hashtable<>();
    }

    public static void main(String[] args) {
        new getConfig().checkconfigure();
        login();
        //new server();
    }

    public static boolean login() {
        JFrame login = new JFrame();
        login.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        login.setTitle("Login");
        login.setSize(420, 300);
        login.setLocationRelativeTo(null);
        login.setVisible(true);

        JPanel panel = new JPanel();
        JPanel top = new JPanel();
        JPanel center = new JPanel();
        JPanel bottom = new JPanel();

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        JButton btn = new JButton("Login");
        JButton cancel = new JButton("Cancel");

        JLabel title = new JLabel("Mavell's iCafe Login");
        title.setPreferredSize(new Dimension(200, 30));

        top.add(title, BorderLayout.CENTER);
        center.add(new JLabel("Username")).setPreferredSize(new Dimension(150, 30));
        center.add(username).setPreferredSize(new Dimension(150, 30));
        center.add(new JLabel("Password")).setPreferredSize(new Dimension(150, 30));
        center.add(password).setPreferredSize(new Dimension(150, 30));
        bottom.add(cancel);
        bottom.add(btn);

        login.getContentPane().add(panel);

        btn.addActionListener(e -> {
            String usern = username.getText().trim();
            String userp = String.valueOf(password.getPassword()).trim();
            String query = "SELECT * FROM users WHERE Username='" + usern + "' AND Password='" + userp + "'";
            boolean loggedin = new dbGetter(new dbConnector().getConnection(), query).verifyuser();
            if (loggedin) {
                Admin = usern;
                login.dispose();
                new server();
            } else {
                JOptionPane.showMessageDialog(login, "Username/password combination wrong", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(top).setPreferredSize(new Dimension(400, 70));
        panel.add(center).setPreferredSize(new Dimension(400, 120));
        panel.add(bottom).setPreferredSize(new Dimension(400, 70));
        login.repaint();
        login.revalidate();

        return true;
    }

    public void gui() {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            new logger("Line: 71: " + e.toString()).writelog();
        } catch (InstantiationException e) {
            //e.printStackTrace();
            new logger("Line: 74: " + e.toString()).writelog();
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
            new logger("Line: 77: " + e.toString()).writelog();
        } catch (UnsupportedLookAndFeelException e) {
            //e.printStackTrace();
            new logger("Line: 80: " + e.toString()).writelog();
        }
        window.setSize(screen);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setTitle("Server");
        JPanel panel = new JPanel();
        JPanel clientArea = new JPanel();
        JPanel pricingArea = new JPanel();
        JPanel stoppedClientsArea = new JPanel();
        panel.setSize(window.getSize());
        clientArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        pricingArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        stoppedClientsArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        //create menu
        JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem configure = new JMenuItem("Configure Clients");
        JMenuItem report = new JMenuItem("Reports");
        configure.addActionListener(e -> {
            configureSettings();
        });
        report.addActionListener(e -> {
            reports();
        });
        file.add(configure);
        file.add(report);
        menubar.add(file);
        window.setJMenuBar(menubar);
        clientArea.setPreferredSize(new Dimension(panel.getWidth() * 2 / 3, panel.getHeight() / 2));
        pricingArea.setPreferredSize(new Dimension(panel.getWidth() / 4, panel.getHeight() / 2));

        //get clients if present
        String clients = getclients();
        String[] clientstemp = clients.split(",");
        String[] newClients = clientstemp[1].split(";");
        JButton[] client = new JButton[newClients.length];
        if (!clients.contains("no user")) {
            for (int i = 0; i < newClients.length; i++) {
                client[i] = new JButton(newClients[i]);
                clientArea.add(client[i]).setPreferredSize(new Dimension(panel.getWidth() * 2 / 3 / 7, 100));
                final String chuser = newClients[i];
                final JButton clbutton = client[i];
                //check if users were already running
                try {
                    Connection c = new dbConnector().getConnection();
                    Statement st = c.createStatement();
                    String d = "SELECT * FROM sessions WHERE State='1'";
                    ResultSet r = st.executeQuery(d);
                    while (r.next()) {
                        int ds = r.getInt("ID");
                        Date date = r.getTimestamp("StartTime");
                        String nowdate = sdf.format(new Date());
                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        long diff = 0;
                        Date pdate = null;
                        Date nowDate = null;
                        try {
                            nowDate = sdf1.parse(nowdate);
                            pdate = date;
                            diff = nowDate.getTime() - pdate.getTime();
                        } catch (ParseException e) {
                            //e.printStackTrace();
                            new logger("Line: 143: " + e.toString()).writelog();
                        }
                        String ccc = "0," + (diff / 1000);
                        if (r.getString("User").equals(chuser)) {
                            new Thread() {
                                @Override
                                public void run() {
                                    runningClients.put(chuser, ccc);
                                    running(chuser, clbutton, ds, stopClient);
                                }
                            }.start();
                        }
                    }
                } catch (SQLException e) {
                    //e.printStackTrace();
                    new logger("Line: 158: " + e.toString()).writelog();
                }
                client[i].addActionListener(e -> {
                    if (runningClients.containsKey(chuser)) {
                        activeClient = chuser;
                        if (activeClient.equals(chuser)) {
                            userLab.setText(chuser);
                            String[] temp = runningClients.get(chuser).split(",");
                            durationLab.setText(String.valueOf(temp[1]));
                            costLab.setText(String.valueOf(temp[0]));
                            stopClient.setVisible(true);
                        }
                    } else {
                        runningClients.put(chuser, "0,0");
                        String query = "INSERT INTO sessions(User,StartTime,Cost,Duration,State) VALUES ('" + chuser + "','" + sdf.format(new Date()) + "','0','0','1') ";
                        id = new dbPutter(new dbConnector().getConnection(), query).putter();
                        activeClient = chuser;
                        r = new Thread() {
                            @Override
                            public void run() {
                                running(chuser, clbutton, id, stopClient);
                            }
                        };
                        r.setName(chuser);
                        r.start();
                    }
                    if (connectedClients.containsKey(chuser)) {
                        try {
                            dout.writeUTF("start," + chuser);
                            new Thread() {
                                @Override
                                public void run() {
                                    outputprice(chuser, dout);
                                }
                            }.start();
                        } catch (IOException e1) {
                            //e1.printStackTrace();
                            new logger("Line: 195: " + e.toString()).writelog();
                        }
                    }
                });
            }
        } else {
            //button to configure clients
            JButton configureClients = new JButton("Configure Clients");
            clientArea.add(new JLabel("No clients/users have been added please configure clients.")).setPreferredSize(new Dimension((panel.getWidth() * 2 / 3) - 150, 30));
            clientArea.add(configureClients).setPreferredSize(new Dimension(170, 50));
            configureClients.addActionListener(e -> {
                configureSettings();
            });
        }

        //pricing panel
        JLabel costLabel = new JLabel("Cost:");
        JLabel durationLabel = new JLabel("Duration:");
        JLabel userLabel = new JLabel("User:");

        pricingArea.add(userLabel).setPreferredSize(new Dimension(150, 30));
        pricingArea.add(userLab).setPreferredSize(new Dimension(100, 30));
        pricingArea.add(durationLabel).setPreferredSize(new Dimension(150, 30));
        pricingArea.add(durationLab).setPreferredSize(new Dimension(100, 30));
        pricingArea.add(costLabel).setPreferredSize(new Dimension(150, 30));
        pricingArea.add(costLab).setPreferredSize(new Dimension(100, 30));
        pricingArea.add(stopClient).setPreferredSize(new Dimension(150, 40));
        stopClient.setVisible(false);

        panel.add(clientArea);
        panel.add(pricingArea);
        panel.add(stoppedClientsArea).setPreferredSize(new Dimension(1200, 300));
        window.getContentPane().add(panel);
        window.setVisible(true);

        stopClient.addActionListener(e -> {
            stoppedClients.put(activeClient, runningClients.get(activeClient));
            runningClients.remove(activeClient);
            Thread.currentThread().interrupt();
        });

        boolean i = true;
        new Thread() {
            @Override
            public void run() {
                while (i) {
                    try {
                        Connection conn = new dbConnector().getConnection();
                        Statement stmt = conn.createStatement();
                        String query = "SELECT * FROM sessions WHERE State='0'";
                        ResultSet rs = stmt.executeQuery(query);
                        while (rs.next()) {
                            String us = rs.getString("User");
                            JButton btn = new JButton(rs.getString("User"));
                            String ids = rs.getString("ID");
                            String startDate = rs.getString("StartTime");
                            String stopDate = rs.getString("StopTime");
                            String cost = rs.getString("Cost");
                            String duration = rs.getString("Duration");
                            btn.setName(ids);
                            btn.addActionListener(e -> {
                                clearClient(ids, startDate, stopDate, cost, duration, us);
                            });
                            stoppedClientsArea.add(btn);
                        }
                        stmt.close();
                        conn.close();
                        window.repaint();
                        window.revalidate();
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(null,
                                "Cannot Establish Connection to MySQl Database.\n" +
                                        "Ensure that it is installed, running and configured correctly\n " +
                                        "then restart this application.");
                    }
                    try {
                        Thread.sleep(1000);
                        stoppedClientsArea.removeAll();
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                        new logger("Line: 275: " + e.toString()).writelog();
                    }
                }
            }
        }.start();

        //create a socket connection and listen
        try {
            ServerSocket ssocket = new ServerSocket(3304);
            while (true) {
                Socket csocket = ssocket.accept();
                dout = new DataOutputStream(csocket.getOutputStream());
                din = new DataInputStream(csocket.getInputStream());
                new Thread() {
                    @Override
                    public void run() {
                        boolean r = false;
                        String usersList = new getConfig().getInitClientConfig().split(",")[1];
                        String[] users = usersList.split(";");
                        String user = "";
                        try {
                            while (r) {
                                dout.writeUTF(String.valueOf(runningClients.get(user)));
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                    new logger("Line: 302: " + e.toString()).writelog();
                                }
                            }
                            while (din.available() != 0) {
                                String data = din.readUTF();
                                if (runningClients.containsKey(data)) {
                                    //dout.writeUTF(data);
                                    dout.writeUTF("running:" + data + ",Price:" + runningClients.get(data));
                                    user = data;
                                }
                                if (data.startsWith("client:")) {
                                    user = data.split(":")[1];
                                }
                                if (data.startsWith("stop")) {
                                    dout.writeUTF("stop");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < newClients.length; i++) {
                            if (client[i].getText().equals(user)) {
                                connectedClients.put(user, 1);
                                client[i].setBackground(new Color(52, 165, 120));
                                client[i].setContentAreaFilled(false);
                                client[i].setOpaque(true);
                            }
                        }
                        userj = user;
                        new Thread() {
                            @Override
                            public void run() {
                                outputprice(userj, dout);
                            }
                        }.start();
                    }
                }.start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    new logger("Line: 343: " + e.toString()).writelog();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot start program. Another instance is already running.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void running(String user, JButton userButton, int cid, JButton stopc) {
        String[] initv = runningClients.get(user).split(",");
        double dd = Double.valueOf(initv[1]);
        int duration = (int) dd;
        userButton.addActionListener(e -> {
            activeClient = user;
        });

        stopc.addActionListener(e -> {
            if (user.equals(activeClient)) {
                if (connectedClients.containsKey(user)) {
                    try {
                        dout.writeUTF("stop:" + user);
                    } catch (IOException e1) {
                        //e1.printStackTrace();
                        new logger("Line: 366: " + e.toString()).writelog();
                    }
                }
                String query = "UPDATE sessions SET State='0',StopTime='" + sdf.format(new Date()) + "' WHERE ID='" + cid + "' AND User='" + activeClient + "'";
                new dbPutter(conn, query).updater();
                Thread.currentThread().interrupt();
            }
        });
        while (true) {
            stop = false;
            if (runningClients.containsKey(user)) {
                userButton.setBackground(Color.GREEN);
                userButton.setBorderPainted(false);
                userButton.setContentAreaFilled(false);
                userButton.setOpaque(true);
                double rate = Double.valueOf(new getConfig().getServerConfig().split(",")[0]);
                double min = Double.valueOf(new getConfig().getServerConfig().split(",")[1]);
                double dur = Math.round(duration / 60.0 * 100.0) / 100.0;
                double cost = Math.round(dur * rate * 100.0) / 100.0;
                if (cost < min) {
                    cost = min;
                }
                if (activeClient.equals(user)) {
                    userLab.setText(user);
                    durationLab.setText(String.valueOf(dur));
                    costLab.setText(String.valueOf(cost));
                }
                String cl = cost + "," + dur;
                runningClients.put(user, cl);
                String query = "UPDATE sessions SET Cost='" + cost + "', Duration='" + dur + "' WHERE ID='" + cid + "'";
                try {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    stmt.close();
                } catch (SQLException e) {
                    //e.printStackTrace();
                    new logger("Line: 402: " + e.toString()).writelog();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    new logger("Line: 408: " + e.toString()).writelog();
                }
                ++duration;
            } else {
                if (stoppedClients.containsKey(user)) {
                    userButton.setBackground(Color.ORANGE);
                    userButton.setBorderPainted(false);
                    userButton.setContentAreaFilled(false);
                    userButton.setOpaque(true);
                }
                break;
            }
        }
    }

    public String getclients() {
        String userData = new getConfig().getInitClientConfig().trim();
        if (userData.equals(null) || userData.equals("") || userData.equals("null")) {
            return "no user,no user";
        } else {
            String[] temp = userData.split(",");
            String[] users = temp[1].split(";");
            return users.length + "," + temp[1];
        }
    }

    public void configureSettings() {
        JDialog userConfig = new JDialog();
        userConfig.setSize(600, 600);
        userConfig.setLocationRelativeTo(null);
        userConfig.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        userConfig.setTitle("Configure users");

        //panels
        JPanel panel = new JPanel();
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel top = new JPanel();
        top.setPreferredSize(new Dimension(600, 100));

        //Buttons
        JButton saveusers = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        JButton cancel1 = new JButton("Cancel");
        JButton configPricing = new JButton("Configure Pricing");
        JButton savePricing = new JButton("Save");

        String usersData1 = new getConfig().getInitClientConfig().trim();
        String usersText = "";
        if (!usersData1.equals(null) || !usersData1.equals("null") || !usersData1.equals("")) {
            try {
                String[] usersData = usersData1.split(",");
                if (!usersData[1].isEmpty()) {
                    usersText = usersData[1].replace(";", ",");
                } else {
                    usersText = "Enter users separated by comma(,)";
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                usersText = "Enter users separated by comma(,)";
                new logger("Line: 467: " + e.toString()).writelog();
            }
        } else {
            usersText = "Enter users separated by comma(,)";
        }

        //get the saved values if present
        String defaults = new getConfig().getServerConfig();
        String dprice = "0.0";
        String dmin = "0.0";
        if (!defaults.equals(null)) {
            dprice = defaults.split(",")[0];
            dmin = defaults.split(",")[1];
        }

        //textfields
        JTextArea user = new JTextArea(usersText);
        JTextField price = new JTextField(dprice);
        JTextField minPrice = new JTextField(dmin);

        user.setLineWrap(true);
        user.setWrapStyleWord(true);

        //button actions
        configPricing.addActionListener(e -> {
            panel.removeAll();
            panel.add(panel2);
            panel.revalidate();
            panel.repaint();
        });

        saveusers.addActionListener(e -> {
            if (usersEntered || !user.getText().equals("Enter users separated by comma(,)")) {
                boolean ok = new getConfig().writeInitClientConfig(user.getText().trim());
                if (ok) {
                    int o = JOptionPane.showConfirmDialog(userConfig, "changes has been successfully made. You will have to restart the server to reflect changes made.", "Success", JOptionPane.OK_OPTION);
                }
            }
        });

        savePricing.addActionListener(e -> {
            String priceset = price.getText().trim();
            String minPriceset = minPrice.getText().trim();
            boolean status = new getConfig().writeServerConfig(priceset, minPriceset);
            if (status) {
                JOptionPane.showMessageDialog(null, "Changes have been made successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error making changes", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e -> {
            userConfig.dispose();
        });

        cancel1.addActionListener(e -> {
            userConfig.dispose();
        });

        user.setToolTipText("Enter users separated by comma(,)");
        user.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (user.getText().startsWith("Enter users")) {
                    user.setText("");
                }
                usersEntered = true;
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (user.getText().trim().isEmpty()) {
                    user.setText("Enter users separated by comma(,)");
                    usersEntered = false;
                }
            }
        });

        //labels
        JLabel userLabel = new JLabel("Users");
        JLabel priceLabel = new JLabel("Price /min");
        JLabel minPriceLabel = new JLabel("Minimum Price");

        //panel sizing
        panel1.setPreferredSize(new Dimension(450, 600));
        panel2.setPreferredSize(new Dimension(450, 600));

        //add panels
        panel.add(panel1);
        top.add(configPricing).setPreferredSize(new Dimension(150, 30));
        panel1.add(top);
        panel1.add(new JLabel("Enter users separated by comma(,)")).setPreferredSize(new Dimension(200, 30));
        panel1.add(userLabel).setPreferredSize(new Dimension(200, 30));
        panel1.add(user).setPreferredSize(new Dimension(450, 150));
        panel1.add(cancel).setPreferredSize(new Dimension(100, 30));
        panel1.add(saveusers).setPreferredSize(new Dimension(100, 30));

        panel2.add(priceLabel).setPreferredSize(new Dimension(200, 30));
        panel2.add(price).setPreferredSize(new Dimension(200, 30));
        panel2.add(minPriceLabel).setPreferredSize(new Dimension(200, 30));
        panel2.add(minPrice).setPreferredSize(new Dimension(200, 30));
        panel2.add(cancel1).setPreferredSize(new Dimension(100, 30));
        panel2.add(savePricing).setPreferredSize(new Dimension(100, 30));

        userConfig.getContentPane().add(panel);
        userConfig.setVisible(true);
    }

    private void outputprice(String user, DataOutputStream dout) {
        while (true) {

        }
    }

    private void clearClient(String ij, String startDate, String stopDate, String cost, String duration, String us) {
        JDialog wdialog = new JDialog();
        wdialog.setSize(600, 600);
        JPanel panel = new JPanel();
        JPanel toppanel = new JPanel();
        JPanel centerPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        panel.add(toppanel).setPreferredSize(new Dimension(600, 100));
        panel.add(centerPanel).setPreferredSize(new Dimension(600, 400));
        panel.add(bottomPanel).setPreferredSize(new Dimension(600, 100));

        //Labels
        toppanel.add(new JLabel(String.valueOf(ij)), BorderLayout.CENTER);

        //TextAreas
        JTextField starttime = new JTextField(startDate);
        JTextField stoptime = new JTextField(stopDate);
        JTextField lduration = new JTextField(duration);
        JTextField lcost = new JTextField(cost);
        starttime.setEditable(false);
        stoptime.setEditable(false);
        lduration.setEditable(false);
        lcost.setEditable(false);

        JButton btn = new JButton("Clear Client");
        JButton cancel = new JButton("Cancel");

        centerPanel.add(new JLabel("Start Time:")).setPreferredSize(new Dimension(200, 30));
        centerPanel.add(starttime).setPreferredSize(new Dimension(300, 30));
        centerPanel.add(new JLabel("Stop Time:")).setPreferredSize(new Dimension(200, 30));
        centerPanel.add(stoptime).setPreferredSize(new Dimension(300, 30));
        centerPanel.add(new JLabel("Duration:")).setPreferredSize(new Dimension(200, 30));
        centerPanel.add(lduration).setPreferredSize(new Dimension(300, 30));
        centerPanel.add(new JLabel("Cost:")).setPreferredSize(new Dimension(200, 30));
        centerPanel.add(lcost).setPreferredSize(new Dimension(300, 30));

        bottomPanel.add(btn).setPreferredSize(new Dimension(120, 30));
        bottomPanel.add(cancel).setPreferredSize(new Dimension(80, 30));

        wdialog.getContentPane().add(panel);
        wdialog.setVisible(true);
        wdialog.setLocationRelativeTo(null);

        btn.addActionListener(e -> {
            String q = "UPDATE sessions SET State='11' WHERE ID='" + ij + "'";
            boolean j = new dbPutter(new dbConnector().getConnection(), q).updater();
            if (j) {
                wdialog.dispose();
            } else {
                JOptionPane.showMessageDialog(wdialog, "Error! Try again after sometime.", "Error", JOptionPane.OK_OPTION);
            }
        });

        cancel.addActionListener(e -> {
            wdialog.dispose();
        });
    }

    public void reports() {
        JDialog reports = new JDialog();

        //get the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        reports.setSize(screenSize.width, screenSize.height - 20);
        reports.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        reports.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        JPanel panel = new JPanel();
        JPanel topPanel = new JPanel();
        JPanel mainPanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        topPanel.setPreferredSize(new Dimension(screenSize.width - 100, 100));

        //text field for filtering
        JTextField amount = new JTextField();
        JDateChooser startchooser = new JDateChooser();
        startchooser.setLocale(Locale.US);
        startchooser.setDate(new Date());

        JDateChooser stopchooser = new JDateChooser();
        stopchooser.setLocale(Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        stopchooser.setDate(cal.getTime());

        JRadioButton all = new JRadioButton("Summarised and Detailed", true);
        JRadioButton summarised = new JRadioButton("Summarised only");
        JRadioButton detailed = new JRadioButton("Detailed only");

        ButtonGroup rgroup = new ButtonGroup();
        rgroup.add(all);
        rgroup.add(summarised);
        rgroup.add(detailed);
        // Spinner with Dates

        JLabel startDateFilterLabel = new JLabel("Select Start Date");
        JLabel stopDateFilterLabel = new JLabel("Select End Date");
        JLabel amountLabel = new JLabel("Amount");

        JButton cancel = new JButton("Close");
        JButton export = new JButton("Export to PDF");
        JButton filter = new JButton("Filter");
        cancel.addActionListener(e -> {
            reports.dispose();
        });
        export.addActionListener(e -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date s1 = startchooser.getDate();
                Date s2 = stopchooser.getDate();
                String sdate = String.valueOf(sdf.format(s1));
                String fdate = String.valueOf(sdf.format(s2));
                String query = "SELECT * FROM sessions WHERE StartTime BETWEEN '" + sdate + "' AND '" + fdate + "' AND State='11'";
                if (all.isSelected()) {
                    boolean status = new exportToPdf(sdf.format(new Date()), query, Admin, 1).createPdf1();
                    boolean status1 = new exportToPdf(sdf.format(new Date()), query, Admin, 0).createPdf();
                    if (status && status1) {
                        JOptionPane.showMessageDialog(null, "Export Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                if (detailed.isSelected()) {
                    boolean status1 = new exportToPdf(sdf.format(new Date()), query, Admin, 0).createPdf();
                    if (status1) {
                        JOptionPane.showMessageDialog(null, "Export Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                if (summarised.isSelected()) {
                    boolean status1 = new exportToPdf(sdf.format(new Date()), query, Admin, 1).createPdf1();
                    if (status1) {
                        JOptionPane.showMessageDialog(null, "Export Successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (IOException e1) {
                //e1.printStackTrace();
                new logger("Line: 732: " + e.toString()).writelog();
            }
        });
        //create a table

        //create table column headings
        Object colHeads[] = {"ID", "User", "StartTime", "StopTime", "Duration", "Cost"};
        DefaultTableModel model = new DefaultTableModel(colHeads, 0);

        //get the rows
        double fcost = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date is1 = startchooser.getDate();
        Date is2 = stopchooser.getDate();
        String isdate = String.valueOf(sdf.format(is1));
        String ifdate = String.valueOf(sdf.format(is2));
        fcost = tablegenerate("SELECT * FROM sessions WHERE StartTime BETWEEN '" + isdate + "' AND '" + ifdate + "' AND State='11'", model);

        JTable tableReports = new JTable(model);
        amount.setText(String.valueOf(fcost));
        amount.setEditable(false);

        filter.addActionListener(e -> {
            Date s1 = startchooser.getDate();
            Date s2 = stopchooser.getDate();
            String sdate = String.valueOf(sdf.format(s1));
            String fdate = String.valueOf(sdf.format(s2));
            String query = "SELECT * FROM sessions WHERE StartTime BETWEEN '" + sdate + "' AND '" + fdate + "' AND State='11'";
            double s = tablegenerate(query, model);
            amount.setText(String.valueOf(s));
            reports.repaint();
            reports.revalidate();
        });

        //add elements
        topPanel.add(startDateFilterLabel);
        startDateFilterLabel.setPreferredSize(new Dimension(100, 30));
        topPanel.add(startchooser).setPreferredSize(new Dimension(200, 30));
        topPanel.add(new JLabel("")).setPreferredSize(new Dimension(100, 30));
        topPanel.add(stopDateFilterLabel);
        stopDateFilterLabel.setPreferredSize(new Dimension(100, 30));
        topPanel.add(stopchooser).setPreferredSize(new Dimension(200, 30));
        topPanel.add(filter);
        mainPanel.add(new JScrollPane(tableReports)).setPreferredSize(new Dimension(screenSize.width - 200, 500));
        bottomPanel.add(amountLabel).setPreferredSize(new Dimension(170, 30));
        bottomPanel.add(amount).setPreferredSize(new Dimension(230, 30));
        bottomPanel.add(cancel);
        bottomPanel.add(all);
        bottomPanel.add(summarised);
        bottomPanel.add(detailed);
        bottomPanel.add(export);


        panel.add(topPanel);
        panel.add(mainPanel);
        panel.add(bottomPanel).setPreferredSize(new Dimension(screenSize.width - 100, 100));
        reports.getContentPane().add(panel);
        reports.setVisible(true);
    }

    public double tablegenerate(String param, DefaultTableModel model) {
        if (model.getRowCount() > 0) {
            for (int i = model.getRowCount() - 1; i > -1; i--) {
                model.removeRow(i);
            }
        }
        String query;
        if (!param.trim().equals("")) {
            query = param;
        } else {
            query = "SELECT * FROM sessions WHERE State=11";
        }
        ResultSet rs = new dbGetter(new dbConnector().getConnection(), query).getter();
        double fcost = 0;
        try {
            while (rs.next()) {
                String id = String.valueOf(rs.getInt(1));
                String user = rs.getString(2);
                String startDate = String.valueOf(rs.getDate(3));
                String stopDate = String.valueOf(rs.getDate(4));
                String cost = String.valueOf(rs.getInt(5));
                String duration = String.valueOf(rs.getDouble(6));
                model.addRow(new Object[]{id, user, startDate, stopDate, duration, cost});
                fcost = fcost + Double.valueOf(cost);
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            new logger("Line: 819: " + e.toString()).writelog();
        }
        return fcost;
    }
}
