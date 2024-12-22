package views;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.*;

public class Login {
    private JPanel loginPanel;
    private JButton loginButton;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private final SessionFactory factory;

    public Login(SessionFactory factory) {
        this.factory = factory;

        loginButton.addActionListener(_ -> performLogin());
    }

    private void performLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, completá todos los campos.");
            return;
        }

        try (Session session = factory.openSession()) {
            Query<models.Veterinarian> query = session.createQuery("from models.Veterinarian where username = :username and password = :password", models.Veterinarian.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            models.Veterinarian veterinarian = query.uniqueResult();

            if (veterinarian != null) {
                new MainMenu(factory);
            } else {
                JOptionPane.showMessageDialog(null, "Usuario o contraseña incorrectos.");
            }
        }
    }

    public JPanel getPanel() {
        return loginPanel;
    }
}
