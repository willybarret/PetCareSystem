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

    private static final String APPOINTMENTS_BUTTON = "Gestión de citas";
    private static final String MEDICAL_RECORDS_BUTTON = "Historial médico";
    private static final String PETS_BUTTON = "Mascotas";
    private static final String OWNERS_BUTTON = "Propietarios";
    private static final String VETERINARIANS_BUTTON = "Veterinarios";

    public MainMenu(SessionFactory factory) {
        appointmentsButton.addActionListener(_ -> createAndShowFrame(APPOINTMENTS_BUTTON, new Appointment(factory).getPanel()));
        medicalRecordsButton.addActionListener(_ -> createAndShowFrame(MEDICAL_RECORDS_BUTTON, new MedicalRecord(factory).getPanel()));
        petsButton.addActionListener(_ -> createAndShowFrame(PETS_BUTTON, new Pet(factory).getPanel()));
        ownersButton.addActionListener(_ -> createAndShowFrame(OWNERS_BUTTON, new Person(factory).getPanel()));
        veterinariansButton.addActionListener(_ -> createAndShowFrame(VETERINARIANS_BUTTON, new Veterinarian(factory).getPanel()));

        JMenu menu1 = new JMenu("Gestión");
        JMenuItem appointmentsMenuItem = new JMenuItem(APPOINTMENTS_BUTTON);
        appointmentsMenuItem.addActionListener(_ -> createAndShowFrame(APPOINTMENTS_BUTTON, new Appointment(factory).getPanel()));
        menu1.add(appointmentsMenuItem);

        JMenuItem medicalRecordsMenuItem = new JMenuItem(MEDICAL_RECORDS_BUTTON);
        medicalRecordsMenuItem.addActionListener(_ -> createAndShowFrame(MEDICAL_RECORDS_BUTTON, new MedicalRecord(factory).getPanel()));
        menu1.add(medicalRecordsMenuItem);

        JMenu menu2 = new JMenu("Registros");
        JMenuItem petsMenuItem = new JMenuItem(PETS_BUTTON);
        petsMenuItem.addActionListener(_ -> createAndShowFrame(PETS_BUTTON, new Pet(factory).getPanel()));
        menu2.add(petsMenuItem);

        JMenuItem ownersMenuItem = new JMenuItem(OWNERS_BUTTON);
        ownersMenuItem.addActionListener(_ -> createAndShowFrame(OWNERS_BUTTON, new Person(factory).getPanel()));
        menu2.add(ownersMenuItem);

        JMenuItem veterinariansMenuItem = new JMenuItem(VETERINARIANS_BUTTON);
        veterinariansMenuItem.addActionListener(_ -> createAndShowFrame(VETERINARIANS_BUTTON, new Veterinarian(factory).getPanel()));
        menu2.add(veterinariansMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu1);
        menuBar.add(menu2);

        JFrame frame = new JFrame("Menú principal");
        frame.setJMenuBar(menuBar);
        frame.setContentPane(invokerPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createAndShowFrame(String title, JPanel panel) {
        JFrame frame = openFrames.get(title);
        if (frame == null) {
            frame = new JFrame(title);
            frame.setContentPane(panel);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    openFrames.remove(title);
                }
            });
            frame.pack();
            frame.setLocationRelativeTo(null);
            openFrames.put(title, frame);
        }
        frame.setVisible(true);
        frame.toFront();
    }
}
