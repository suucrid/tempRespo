package grocery;
import static grocery.Main_Panel.isValidEmailFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class Functions
{
    Grocery grocery;
    Connection conn;
    DefaultTableModel table_product_function, table_customer_function, table_sales_function, table_checkout_function, table_items_sold_function;
    Session newSession = null;
    MimeMessage mimeMessage = null;
    Main_Panel mainPanel;
    String DATABASE_NAME = "inventory";
    
    Functions(Grocery grocery)
    {
        this.grocery = grocery;
        
    }
    
    public void main_Panel(Main_Panel mainPanel)
    {
        this.mainPanel = mainPanel;
    }
    
    public void connect()
    {
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
        } catch (SQLException ex) {
            Logger.getLogger(Login_Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void validate(String username, String password)
    {   
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE USERNAME = ? AND PASSWORD = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                grocery.show_Main_Panel();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Login_Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void create_database() throws SQLException
    {
        try (
            Statement statement = conn.createStatement()) {

            String checkDatabaseQuery = "SHOW DATABASES LIKE '" + DATABASE_NAME + "'";
            if (!statement.executeQuery(checkDatabaseQuery).next()) {
                String createDatabaseQuery = "CREATE DATABASE " + DATABASE_NAME;
                statement.executeUpdate(createDatabaseQuery);
                
                create_table();
            }
            else
            {
                statement.execute("USE " + DATABASE_NAME);
            }
        }
    }
    
    private void create_table() throws SQLException
    {
        try (
            Statement statement = conn.createStatement()) {
            
            statement.execute("USE " + DATABASE_NAME);
            
            String createUserTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "USER_ID INT, "
                    + "USERNAME VARCHAR(255), "
                    + "PASSWORD VARCHAR(255), "
                    + "PRIMARY KEY (USER_ID) "
                    + ")";
            statement.executeUpdate(createUserTable);
            
            String createProductTable = "CREATE TABLE IF NOT EXISTS products ("
                    + "PRODUCT_ID INT AUTO_INCREMENT PRIMARY KEY, "
                    + "PRODUCT_DESCRIPTION VARCHAR(255), "
                    + "PRODUCT_AVAILABLE_QUANTITY VARCHAR(255), "
                    + "PRODUCT_UNIT VARCHAR(255), "
                    + "PRODUCT_PRICE DOUBLE "
                    + ")";
            statement.executeUpdate(createProductTable);
            
            String createCustomerTable = "CREATE TABLE IF NOT EXISTS customer ("
                    + "CUSTOMER_ID INT AUTO_INCREMENT PRIMARY KEY, "
                    + "CUSTOMER_NAME VARCHAR(255), "
                    + "CUSTOMER_EMAIL VARCHAR(255) "
                    + ")";
            statement.executeUpdate(createCustomerTable);
            
            String createSalesTable = "CREATE TABLE IF NOT EXISTS sales ("
                    + "DATE DATE, "
                    + "TIME TIME, "
                    + "SALES_ID INT AUTO_INCREMENT PRIMARY KEY, "
                    + "CUSTOMER_ID INT, "
                    + "CUSTOMER_NAME VARCHAR(255), "
                    + "TOTAL_SALES DOUBLE, "
                    + "FOREIGN KEY (CUSTOMER_ID) REFERENCES customer(CUSTOMER_ID) "
                    + ")";
            statement.executeUpdate(createSalesTable);
            
            String createItemsSoldTable = "CREATE TABLE IF NOT EXISTS items_sold ("
                    + "SALES_ID INT, "
                    + "PRODUCT_ID INT, "
                    + "SOLD_QUANTITY INT, "
                    + "FOREIGN KEY (SALES_ID) REFERENCES sales(SALES_ID), "
                    + "FOREIGN KEY (PRODUCT_ID) REFERENCES products(PRODUCT_ID) "
                    + ")";
            statement.executeUpdate(createItemsSoldTable);
        }
    }
    
    public void create_product(String productDescription, int productAvailableQuantity, String productUnit, double productPrice)
    {
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO products (PRODUCT_DESCRIPTION, PRODUCT_AVAILABLE_QUANTITY, PRODUCT_UNIT, PRODUCT_PRICE) VALUES (?, ?, ?, ?)");
            stmt.setString(1, productDescription);
            stmt.setInt(2, productAvailableQuantity);
            stmt.setString(3, productUnit);
            stmt.setDouble(4, productPrice);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Main_Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void create_customer(String customerName, String customerEmail)
    {
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO customer (CUSTOMER_NAME, CUSTOMER_EMAIL) VALUES (?, ?)");
            stmt.setString(1, customerName);
            stmt.setString(2, customerEmail);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Main_Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void create_sales(int customerID, String name, double total)
    {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO sales (DATE, TIME, CUSTOMER_ID, CUSTOMER_NAME, TOTAL_SALES) VALUES ( ?, ?, ?, ?, ?)");
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String dateString = dateFormat.format(currentDate);
            String timeString = timeFormat.format(currentDate);
            
            preparedStatement.setString(1, dateString);
            preparedStatement.setString(2, timeString);
            preparedStatement.setInt(3, customerID);
            preparedStatement.setString(4, name);
            preparedStatement.setDouble(5,total);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void create_items_sold(int[] productID, int[] soldQuantity)
    {
        read_sales();
        String [][] salesData = new String [table_sales_function.getRowCount()][table_sales_function.getColumnCount()];
        int latestSalesID = 0;
        for(int i = 0; i < table_sales_function.getRowCount(); i++)
        {
            for(int j = 0; j < table_sales_function.getColumnCount(); j++)
            {
                salesData[i][j] = String.valueOf(table_sales_function.getValueAt(i,j));
            }
        }
        
        latestSalesID = Integer.parseInt(salesData[table_sales_function.getRowCount() - 1][2]);
        
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO items_sold (SALES_ID, PRODUCT_ID, SOLD_QUANTITY) VALUES (?, ?, ?)");

            conn.setAutoCommit(false);

            for(int i = 0;i < productID.length;i++)
            {
                preparedStatement.setInt(1, latestSalesID);
                preparedStatement.setInt(2, productID[i]);
                preparedStatement.setInt(3, soldQuantity[i]);
                preparedStatement.addBatch();
            }

            int [] count = preparedStatement.executeBatch();

            conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void read_product()
    {
        try {
            try (Statement statement = conn.createStatement()) {
                String query = "SELECT * FROM products";
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    int rowCount = table_product_function.getRowCount();
                    for(int i = rowCount - 1; i >= 0; i--)
                    {
                        table_product_function.removeRow(i);
                    }

                    while (resultSet.next()) {
                        Object[] row = new Object[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            row[i - 1] = resultSet.getObject(i);
                        }
                        table_product_function.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching data from the database.");
        }
    }
    
    public void read_customer()
    {
        try {
            try (Statement statement = conn.createStatement()) {
                String query = "SELECT * FROM customer";
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    int rowCount = table_customer_function.getRowCount();
                    for(int i = rowCount - 1; i >= 0; i--)
                    {
                        table_customer_function.removeRow(i);
                    }

                    while (resultSet.next()) {
                        Object[] row = new Object[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            row[i - 1] = resultSet.getObject(i);
                        }
                        table_customer_function.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching data from the database.");
        }
    }
    
    public void read_sales()
    {
        try {
            try (Statement statement = conn.createStatement()) {
                String query = "SELECT * FROM sales";
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    int rowCount = table_sales_function.getRowCount();
                    for(int i = rowCount - 1; i >= 0; i--)
                    {
                        table_sales_function.removeRow(i);
                    }

                    while (resultSet.next()) {
                        Object[] row = new Object[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            row[i - 1] = resultSet.getObject(i);
                        }
                        table_sales_function.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching data from the database.");
        }
    }
    
    public void read_items_sold(int salesID, JTable product_table)
    {
        try {
            try (Statement statement = conn.createStatement()) {
                String query = "SELECT * FROM items_sold WHERE SALES_ID = " + salesID;

                try (ResultSet resultSet = statement.executeQuery(query)) {

                    table_items_sold_function.setRowCount(0);

                    while (resultSet.next()) {
                        String databaseProductID = resultSet.getString(2);
                        for (int i = 0; i < product_table.getRowCount(); i++) {
                            String productID = String.valueOf(product_table.getValueAt(i, 0));
                            if (databaseProductID.equals(productID)) {
                                Object[] row = new Object[6];
                                row[0] = product_table.getValueAt(i, 0);
                                row[1] = product_table.getValueAt(i, 1);
                                row[2] = resultSet.getObject(3);
                                row[3] = product_table.getValueAt(i, 3);
                                row[4] = product_table.getValueAt(i, 4);
                                row[5] = (Double.parseDouble(String.valueOf(resultSet.getObject(3))) * Double.parseDouble(String.valueOf(product_table.getValueAt(i, 4))));
                                table_items_sold_function.addRow(row);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching data from the database: " + e.getMessage());
        }
    }
    
    public void update(int productID, String productDescription, int productAvailableQuantity, String productUnit, double productPrice)
    {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE products SET PRODUCT_DESCRIPTION = ?, PRODUCT_AVAILABLE_QUANTITY = ?, PRODUCT_UNIT = ?, PRODUCT_PRICE = ? WHERE PRODUCT_ID = ?");
            stmt.setString(1, productDescription);
            stmt.setInt(2, productAvailableQuantity);
            stmt.setString(3, productUnit);
            stmt.setDouble(4, productPrice);
            stmt.setInt(5, productID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Main_Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void delete(int productID)
    {
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE PRODUCT_ID = ?");
            stmt.setInt(1, productID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Main_Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void setUpServerProperties()
    {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.port","587");
        properties.put("mail.smtp.auth","true");
        properties.put("mail.smtp.starttls.enable","true");
        newSession = Session.getDefaultInstance(properties,null);
    }

    private void sendEmail() throws MessagingException
    {
        String fromUser = "kentlauron.acc@gmail.com"; //change to your email
        String fromUserPassword = "alternate0"; //and password
        String emailHost = "smtp.gmail.com";
        try (Transport transport = newSession.getTransport("smtp"))
        {
            transport.connect(emailHost, fromUser, fromUserPassword);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        }
        JOptionPane.showOptionDialog(null, "Email sent successfully.", "",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{},
                null);
    }
     
    protected MimeMessage draftEmail(int Customer_ID,String name, String email, String[][] order) throws AddressException, MessagingException, IOException
    {
        read_sales();
        int orderNumber = table_sales_function.getRowCount() + 1;
        
        String emailSubject = "Order #" + orderNumber;
        String orderList = "";
        double total = 0;
        for (String[] order1 : order)
        {
            for (int j = 0; j < order[0].length; j++)
            {
                if(j == 0)
                {
                    orderList += "\n<tr>";
                }
                orderList += "<td>" + order1[j] + "</td>";
                if(j == 4)
                {
                    orderList += "\n</tr>";
                    total += (Integer.valueOf(order1[2]) * Double.valueOf(order1[j]));
                }
            }
        }
        String emailBody = 
                "<html>Good Day " + name + ",<br><br>"
                + """
                Here are the list of items you have bought<br><br>
                <table style=\"width:100%\">
                    <tr>
                        <th>Product ID</th>
                        <th>Product Description</th>
                        <th>Quantity</th>
                        <th>Product Unit</th>
                        <th>Price</th>
                    </tr>
                """
                + orderList
                + """
                    <tr>
                        <th></th><th></th><th></th>
                        <th>TOTAL AMOUNT</th>
                """
                +      "<th>" + total + "</th>"
                + """
                    </tr>
                </table>
                <br><br>Thank you for purchasing with us!
                </html>
                """;
        mimeMessage = new MimeMessage(newSession);
        
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        mimeMessage.setSubject(emailSubject);

        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(emailBody,"text/html");
        MimeMultipart multiPart = new MimeMultipart();
        multiPart.addBodyPart(bodyPart);
        mimeMessage.setContent(multiPart);
        
        create_sales(Customer_ID, name, total);
        
        JOptionPane firstOptionPane = new JOptionPane("This might take a while",
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        
        JDialog dialog = firstOptionPane.createDialog("Processing...");
        dialog.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        Timer timer;
        timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        timer.setRepeats(false);
        timer.start();

        dialog.setVisible(true);
        return mimeMessage;
    }
    
    public void purchase(JTable product_table,JTable checkout_table,JTable customer_table)
    {
        try {
            String name = "";
            String email = "";
            String[][] order = new String[checkout_table.getRowCount()][checkout_table.getColumnCount()];

            while (true) {
                name = JOptionPane.showInputDialog("Please enter your name:");
                if(name == null)
                {
                    break;
                }
                else if(name.isEmpty())
                {
                    JOptionPane.showMessageDialog(null, "Please fill out the window", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    while (true)
                    {
                        email = JOptionPane.showInputDialog("Please enter your email address:");
                        if (email == null)
                        {
                            break;
                        }
                        else if (!isValidEmailFormat(email))
                        {
                            JOptionPane.showMessageDialog(null, "Please enter a valid email", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        else
                        {
                            break;
                        }
                    }
                    break;
                }
            }
            
            String [] customerRow = new String [3];
            int customerID = 0;
            boolean found = false;
            read_customer();
            if (name != null && !name.isEmpty() && email != null && !email.isEmpty())
            {
                for(int i = 0;i < customer_table.getRowCount();i++)
                {
                    for(int j = 0;j < customer_table.getColumnCount();j++)
                    {
                        customerRow[j] = String.valueOf(customer_table.getValueAt(i, j));
                    }

                    if(customerRow[2].equals(email))
                    {
                        customerID = Integer.parseInt(customerRow[0]);
                        found = true;
                    }
                }

                if(!found)
                {
                    System.out.println("Email saved into the database");
                    create_customer(name, email);
                    customerID += 1;
                }

                int[] productID_checkout = new int[checkout_table.getRowCount()];
                int[] soldQuantity = new int[checkout_table.getRowCount()];
                for(int i = 0;i < checkout_table.getRowCount();i++)
                {
                    for(int j = 0;j < checkout_table.getColumnCount();j++)
                    {
                        order[i][j] = String.valueOf(checkout_table.getValueAt(i, j));
                        if(j == 0)
                        {
                            productID_checkout[i] = Integer.parseInt(String.valueOf(checkout_table.getValueAt(i, j)));
                        }
                        else if(j == 2)
                        {
                            soldQuantity[i] = Integer.parseInt(String.valueOf(checkout_table.getValueAt(i, j)));
                        }
                    }
                }

                mainPanel.back_button[1].setEnabled(false);
                mainPanel.purchase_button[0].setEnabled(false);
                mainPanel.purchase_button[1].setEnabled(false);
                mainPanel.purchase_button[2].setEnabled(false);
                
                setUpServerProperties();
                draftEmail(customerID, name, email, order);
                create_items_sold(productID_checkout, soldQuantity);
                sendEmail();
                
                mainPanel.back_button[1].setEnabled(true);
                mainPanel.purchase_button[0].setEnabled(true);
                mainPanel.purchase_button[1].setEnabled(true);
                mainPanel.purchase_button[2].setEnabled(true);
                
                Object[][] orderData = new Object[order.length][order[0].length];

                for (int i = 0; i < order.length; i++) {
                    for (int j = 0; j < order[0].length; j++) {
                        orderData[i][j] = order[i][j];
                    }
                }
                
                Arrays.sort(orderData, (row1, row2) -> Integer.compare(
                                                       Integer.parseInt(String.valueOf(row1[0])),
                                                       Integer.parseInt(String.valueOf(row2[0]))));

                
                for(int i = 0; i < product_table.getRowCount();i++)
                {
                    String productID = String.valueOf(product_table.getValueAt(i, 0));
                    int productQuantity = Integer.parseInt(String.valueOf(product_table.getValueAt(i, 2)));
                    for(int j = 0;j < order.length;j++)
                    {
                        if(productID.equals(orderData[j][0]))
                        {
                            update(Integer.parseInt(String.valueOf(orderData[j][0])),
                            String.valueOf(orderData[j][1]),
                        productQuantity,
                                  String.valueOf(orderData[j][3]),
                                 Double.parseDouble(String.valueOf(orderData[j][4])));
                        }
                    }
                }
                
                if(checkout_table.getRowCount() != 0)
                {
                    for(int i = checkout_table.getRowCount() - 1; i >= 0;i--)
                    {
                        ((DefaultTableModel) checkout_table.getModel()).removeRow(i);
                    }
                }
            }
        }
        catch (MessagingException | IOException ex)
        {
            Logger.getLogger(Main_Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
    public void report() throws FileNotFoundException, IOException
    {
        /*try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products");
            ResultSet rs = stmt.executeQuery();
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Products");
            int rowNum = 0;
            while (rs.next()) {
                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getInt("PRODUCT_ID"));
                row.createCell(1).setCellValue(rs.getString("PRODUCT_DESCRIPTION"));
                row.createCell(2).setCellValue(rs.getInt("PRODUCT_AVAILABLE_QUANTITY"));
                row.createCell(3).setCellValue(rs.getString("PRODUCT_UNIT"));
                row.createCell(4).setCellValue(rs.getDouble("PRODUCT_PRICE"));
            }
            FileOutputStream outputStream = new FileOutputStream("products.xlsx");
            workbook.write(outputStream);
            outputStream.close();
        } catch (SQLException ex) {
            Logger.getLogger(Main_Panel.class.getName()).log(Level.SEVERE, null, ex);
        }*
    }
    */
}
