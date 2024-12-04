package views;

import org.hibernate.SessionFactory;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class MainMenu {
    private JPanel invokerPanel;
    private JButton appointmentsButton;
    private JButton medicalRecordsButton;
    private JButton petsButton;
    private JButton ownersButton;
    private JButton veterinariansButton;
    private final Map<String, JFrame> openFrames = new HashMap<>();

    public MainMenu(SessionFactory factory) {
        appointmentsButton.addActionListener(_ -> createAndShowFrame("Gestión de citas", new Appointment(factory).getPanel()));

        medicalRecordsButton.addActionListener(_ -> createAndShowFrame("Registros médicos", new MedicalRecord(factory).getPanel()));

        petsButton.addActionListener(_ -> createAndShowFrame("Colección de mascotas", new Pet(factory).getPanel()));

        ownersButton.addActionListener(_ -> createAndShowFrame("Propietarios", new Person(factory).getPanel()));

        veterinariansButton.addActionListener(_ -> createAndShowFrame("Veterinarios", new Veterinarian(factory).getPanel()));
    }

    private void createAndShowFrame(String title, JPanel panel) {
        JFrame frame = openFrames.get(title);
        if (frame == null) {
            frame = new JFrame(title);
            frame.setContentPane(panel);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            openFrames.put(title, frame);
        }
        frame.setVisible(true);
        frame.toFront();
    }

    public JPanel getPanel() {
        return invokerPanel;
    }
}
