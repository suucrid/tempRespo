package grocery;
import java.sql.SQLException;
import javax.swing.*;

public final class Grocery extends JFrame
{
    private final Login_Panel login_Panel;
    private final Main_Panel main_Panel;
    private final Functions function;
    
    public void show_Login_Panel()
    {   
        getContentPane().removeAll();
        getContentPane().add(login_Panel);
        setSize(300, 280);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }
    
    public void show_Main_Panel()
    {
        getContentPane().removeAll();
        getContentPane().add(main_Panel);
        setSize(800, 500);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }
    
    Grocery() throws SQLException
    {
        setTitle("Grocery");
        setSize(300, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        
        function = new Functions(this);
        login_Panel = new Login_Panel(this, function);
        main_Panel = new Main_Panel(this, function);
        
        function.connect();
        function.create_database();
        //show_Login_Panel();
        show_Main_Panel();
        
        setVisible(true);
    }
    
    public static void main(String[] args) throws SQLException
    {
        Grocery app = new Grocery();
    }
}