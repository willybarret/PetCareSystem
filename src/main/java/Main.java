import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import models.*;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import utils.DatabaseSeeder;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Person.class)
                .addAnnotatedClass(Veterinarian.class)
                .addAnnotatedClass(Pet.class)
                .addAnnotatedClass(Appointment.class)
                .addAnnotatedClass(MedicalRecord.class)
                .buildSessionFactory();

        DatabaseSeeder.seedAdminUser(factory);

        FlatMacDarkLaf.setup();
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("PetCare System");
            frame.setContentPane(new views.Login(factory).getPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
