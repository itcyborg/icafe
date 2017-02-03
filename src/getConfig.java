import javax.swing.*;
import java.awt.*;
import java.io.*;

public class getConfig extends JDialog {
    BufferedReader fin;
    PrintWriter fout = null;

    public static String checksql(String dbname) {
        String sql = "CREATE TABLE IF NOT EXISTS sessions (" +
                "ID int(11) NOT NULL AUTO_INCREMENT," +
                "User varchar(20) NOT NULL," +
                "StartTime datetime NOT NULL," +
                "StopTime datetime DEFAULT NULL," +
                "Cost double NOT NULL," +
                "Duration double NOT NULL," +
                "State int(11) NOT NULL," +
                "PRIMARY KEY (ID)" +
                ") " +
                "ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
                "#" +
                "CREATE TABLE IF NOT EXISTS users (" +
                "ID int(11) NOT NULL AUTO_INCREMENT," +
                "Username varchar(60) NOT NULL," +
                "Password varchar(60) NOT NULL," +
                "PRIMARY KEY (ID)," +
                "UNIQUE KEY Username (Username)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1;" +
                "#" +
                "CREATE TABLE IF NOT EXISTS runningclients(" +
                "ID int(11) NOT NULL AUTO_INCREMENT," +
                "UserName varchar(60) NOT NULL," +
                "PRIMARY KEY ID (ID)," +
                "UNIQUE KEY UserName (UserName)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
        return sql;
    }

    public String getClientConfig() {
        String data = "";
        try {
            fin = new BufferedReader(new FileReader("config"));
            //read the file
            String fileData = fin.readLine();
            if (fileData.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Invalid settings found!\nPlease enter the settings again");
            } else {
                data = fileData.trim();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No/invalid client configuration found. Please enter one.");
            String user = JOptionPane.showInputDialog(null, "Enter client name\neg. user 7");
            String ip = JOptionPane.showInputDialog(null, "Enter ip address of the server", "Server Ip");
            writeClientConfig(user, ip);
            new logger(e.toString()).writelog();
        }
        return data;
    }

    public boolean writeClientConfig(String user, String serverIp) {
        boolean status = false;
        try {
            fout = new PrintWriter(new FileOutputStream("config"));
            fout.println(user + "," + serverIp);
            fout.close();
            status = true;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return status;
    }

    public boolean writeServerConfig(String price, String minimum) {
        boolean status = false;
        double min = Double.valueOf(minimum);
        double rate = Double.valueOf(price);
        try {
            fout = new PrintWriter(new FileOutputStream("serverConfig"));
            fout.println(rate + "," + minimum);
            fout.close();
            status = true;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return status;
    }

    public String getServerConfig() {
        String data = null;
        try {
            fin = new BufferedReader(new FileReader("serverConfig"));
            String conf = fin.readLine();
            fin.close();
            data = conf;
        } catch (IOException e) {
            data = "0,0";
            new logger(e.toString()).writelog();
        }
        return data;
    }

    //new server
    public boolean writeInitClientConfig(String usersData) {
        boolean status = false;
        String[] usersArray = usersData.split(",");
        int userNo = usersArray.length;
        try {
            fout = new PrintWriter(new FileOutputStream("usersInitConfig"));
            fout.write("No;" + userNo + "\n");
            for (int i = 0; i < userNo; i++) {
                fout.write(usersArray[i] + ";");
            }
            fout.close();
            status = true;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return status;
    }

    public String getInitClientConfig() {
        String data = null;
        try {
            fin = new BufferedReader(new FileReader("usersInitConfig"));
            String userNo = fin.readLine();
            String users = fin.readLine();
            fin.close();
            if (userNo.trim().length() == 0 || users.trim().length() == 0) {
                data = "null";
            } else {
                data = userNo + "," + users;
            }
        } catch (IOException e) {
            data = "null";
            new logger(e.toString()).writelog();
        } catch (NullPointerException e) {
            data = "null";
            new logger(e.toString()).writelog();
        }
        return data;
    }

    public String checkconfigure() {
        String con = "";
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader("serverconfigure"));
            String db = inputStream.readLine();
            String dbpass = inputStream.readLine();
            String dbuser = inputStream.readLine();
            if (db.length() < 1 || dbuser.length() < 1) {
                configure();
            } else {
                con = db + "," + dbpass + "," + dbuser;
            }
        } catch (FileNotFoundException e) {
            configure();
            new logger(e.toString()).writelog();
        } catch (IOException e) {
            //e.printStackTrace();
            new logger(e.toString()).writelog();
        }
        return con;
    }

    public void configure() {
        setTitle("First Run");
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL); //set the type of dialog
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();      //get the screen size
        setLocation((screenSize.width / 2) - 390, (screenSize.height / 2) - 360);        //set the window location
        setSize(700, 600);

        //labels
        JLabel userdata = new JLabel("User Data");
        JLabel title = new JLabel("Configure for you device");
        JLabel regLabel = new JLabel("Registration Number:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel serverdata = new JLabel("MySql Server Config");
        JLabel serverlocationlabel = new JLabel("Server Location:");
        JLabel dbportlabel = new JLabel("Port:");
        JLabel dbnamelabel = new JLabel("Database Name:");
        JLabel usernamelabel = new JLabel("Database Username:");
        JLabel dbpasslabel = new JLabel("Database Password:");
        JLabel existinga = new JLabel("New/Old");

        //textfields
        JTextField regField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField serverlocationField = new JTextField();
        JTextField dbportField = new JTextField();
        JTextField dbnameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField dbpassField = new JPasswordField();

        //JCombobox
        JComboBox newold = new JComboBox();
        newold.addItem("New (Recommended)");
        newold.addItem("Existing");

        //buttons
        JButton save = new JButton("Save Configurations");
        JButton exit = new JButton("Cancel and Quit");
        JButton create = new JButton("Create Db");

        //jcombobox events
        newold.addActionListener(e -> {
            if (String.valueOf(newold.getSelectedItem()).equals("New (Recommended)")) {
                create.setEnabled(true);
            } else {
                create.setEnabled(false);
            }
        });
        //button actions
        exit.addActionListener(e -> {
            System.exit(0);
        });

        create.addActionListener(e -> {
            String host = String.valueOf(serverlocationField.getText()).trim() + ":" + String.valueOf(dbportField.getText()).trim();
            String db = String.valueOf(dbnameField.getText()).trim();
            String pass = String.valueOf(dbpassField.getPassword()).trim();
            String user = String.valueOf(usernameField.getText()).trim();
            new createDB().createDb(db, host, user, pass);
        });

        save.addActionListener(e -> {
            String db = String.valueOf(serverlocationField.getText()).trim() + ":" + String.valueOf(dbportField.getText()).trim() + "/" + String.valueOf(dbnameField.getText()).trim();
            String pass = String.valueOf(dbpassField.getPassword()).trim();
            String user = String.valueOf(usernameField.getText()).trim();
            String userreg = String.valueOf(regField.getText()).trim();
            String userpass = String.valueOf(passField.getPassword()).trim();
            writeconfig(db.trim(), pass.trim(), user.trim(), userreg.trim(), userpass.trim());
        });

        //panels
        JPanel main = new JPanel();       //main panel
        JPanel top = new JPanel();        //top panel
        JPanel content = new JPanel();    //content panel
        JPanel bottom = new JPanel();     //bottom panel
        JPanel buttons = new JPanel();    //buttons panel

        //add the elements to the panels
        top.add(title, BorderLayout.CENTER);
        content.add(serverlocationlabel).setPreferredSize(new Dimension(200, 30));
        content.add(serverlocationField).setPreferredSize(new Dimension(400, 30));
        content.add(dbportlabel).setPreferredSize(new Dimension(200, 30));
        content.add(dbportField).setPreferredSize(new Dimension(400, 30));
        content.add(existinga).setPreferredSize(new Dimension(200, 30));
        content.add(newold).setPreferredSize(new Dimension(400, 30));
        content.add(dbnamelabel).setPreferredSize(new Dimension(200, 30));
        content.add(dbnameField).setPreferredSize(new Dimension(400, 30));
        content.add(usernamelabel).setPreferredSize(new Dimension(200, 30));
        content.add(usernameField).setPreferredSize(new Dimension(400, 30));
        content.add(dbpasslabel).setPreferredSize(new Dimension(200, 30));
        content.add(dbpassField).setPreferredSize(new Dimension(250, 30));
        content.add(create).setPreferredSize(new Dimension(150, 30));
        bottom.add(regLabel).setPreferredSize(new Dimension(200, 30));
        bottom.add(regField).setPreferredSize(new Dimension(400, 30));
        bottom.add(passwordLabel).setPreferredSize(new Dimension(200, 30));
        bottom.add(passField).setPreferredSize(new Dimension(400, 30));
        buttons.add(exit, BorderLayout.WEST);
        buttons.add(save, BorderLayout.EAST);

        //set panel sizes
        main.setPreferredSize(new Dimension(700, 600));
        top.setPreferredSize(new Dimension(700, 100));
        content.setPreferredSize(new Dimension(700, 250));
        bottom.setPreferredSize(new Dimension(700, 100));
        buttons.setPreferredSize(new Dimension(700, 150));

        //add the panels to the main panel
        main.add(top);
        main.add(content);
        main.add(bottom);
        main.add(buttons);

        //set the content pane
        getContentPane().add(main);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        //show dialog window
        setVisible(true);
    }

    private boolean writeconfig(String db, String dbpass, String dbuser, String user, String pass) {
        PrintWriter outputStream = null;
        int result = 0;
        try {
            outputStream = new PrintWriter(new FileOutputStream("serverconfigure"));
            outputStream.println(db);
            outputStream.println(dbpass);
            outputStream.println(dbuser);
            outputStream.close();
            if (user.length() > 0 && user.length() > 0) {
                result = new dbPutter(new dbConnector().getConnection(), "INSERT INTO users (Username,Password) VALUES ('" + user + "','" + pass + "')").putter();
                if (result == 0) {
                } else {
                    setVisible(false);
                }
            } else {
                setVisible(false);
            }
        } catch (FileNotFoundException e) {
            new logger(e.toString()).writelog();
        }
        return false;
    }
}
