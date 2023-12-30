package grocery;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Login_Panel extends JPanel
{
    Grocery grocery;
    Functions function;
    
    JLabel[] label = new JLabel[3];
    JTextField text;
    JPasswordField pass;
    JButton button;
        
    Font font = new Font(null,Font.PLAIN,25);
    Font label_font = new Font(null,Font.PLAIN,15);
    
    Login_Panel(Grocery grocery, Functions function)
    {
        this.grocery = grocery; 
        this.function = function;
        
        setLayout(null);
        add(label[0] = new JLabel("Grocery Inventory"));
        add(label[1] = new JLabel("Username: "));
        add(label[2] = new JLabel("Password: "));
        add(text = new JTextField());
        add(pass = new JPasswordField());
        add(button = new JButton("Log-in"));
        
        label[0].setFont(font);
        label[1].setFont(label_font);
        label[2].setFont(label_font);
        
        label[0].setBounds(40,20,250,30);
        label[1].setBounds(20,70,100,30);
        label[2].setBounds(20,120,100,30);
        text.setBounds(110,70,150,30);
        pass.setBounds(110,120,150,30);
        button.setBounds(90,180,100,30);
        
        button.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(text.getText().matches("") && String.valueOf(pass.getPassword()).matches(""))
                {
                    JOptionPane.showMessageDialog(null, "Please fill out the form", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    function.validate(text.getText(),String.valueOf(pass.getPassword()));
                }
            }
        });
    }
}