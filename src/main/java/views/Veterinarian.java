package views;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class Veterinarian {
    private JPanel VeterinarianPanel;
    private JTextField txtFullName;
    private JTextField txtPhoneNumber;
    private JTable veterinarianTable;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JPasswordField txtPassword;
    private JTextField txtUsername;

    private final DefaultTableModel tableModel;
    private Integer selectedVeterinarianId;

    private final SessionFactory factory;

    public Veterinarian(SessionFactory factory) {
        this.factory = factory;

        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre Completo", "Número de teléfono", "Usuario"}, 0);
        veterinarianTable.setModel(tableModel);
        loadVeterinarians();

        veterinarianTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectVeterinarian();
            }
        });

        saveButton.addActionListener(_ -> saveVeterinarian());

        cancelButton.addActionListener(_ -> clearFields());

        deleteButton.addActionListener(_ -> deleteVeterinarian());
    }

    private void loadVeterinarians() {
        try (Session session = factory.openSession()) {
            List<models.Veterinarian> veterinarians = session.createQuery("from models.Veterinarian", models.Veterinarian.class).list();
            for (models.Veterinarian veterinarian : veterinarians) {
                tableModel.addRow(new Object[]{veterinarian.getId(), veterinarian.getFullName(), veterinarian.getPhoneNumber(), veterinarian.getUsername()});
            }
        }
    }

    private void selectVeterinarian() {
        int selectedRow = veterinarianTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedVeterinarianId = (int) tableModel.getValueAt(selectedRow, 0);
            txtFullName.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtPhoneNumber.setText((String) tableModel.getValueAt(selectedRow, 2));
            txtUsername.setText((String) tableModel.getValueAt(selectedRow, 3));
        }
    }

    private void saveVeterinarian() {
        String fullName = txtFullName.getText();
        String phoneNumber = txtPhoneNumber.getText();
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (fullName.isEmpty() || phoneNumber.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, completá todos los campos.");
            return;
        }

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.Veterinarian veterinarian;
            if (selectedVeterinarianId == null) {
                veterinarian = new models.Veterinarian();
                veterinarian.setFullName(fullName);
                veterinarian.setPhoneNumber(phoneNumber);
                veterinarian.setUsername(username);
                veterinarian.setPassword(password);
                session.persist(veterinarian);
                tableModel.addRow(new Object[]{veterinarian.getId(), veterinarian.getFullName(), veterinarian.getPhoneNumber(), veterinarian.getUsername()});
            } else {
                veterinarian = session.get(models.Veterinarian.class, selectedVeterinarianId);
                veterinarian.setFullName(fullName);
                veterinarian.setPhoneNumber(phoneNumber);
                veterinarian.setUsername(username);
                if (!password.isEmpty())
                    veterinarian.setPassword(password);
                session.merge(veterinarian);
                int selectedRow = veterinarianTable.getSelectedRow();
                tableModel.setValueAt(veterinarian.getFullName(), selectedRow, 1);
                tableModel.setValueAt(veterinarian.getPhoneNumber(), selectedRow, 2);
                tableModel.setValueAt(veterinarian.getUsername(), selectedRow, 3);
                selectedVeterinarianId = null;
            }
            session.getTransaction().commit();
            clearFields();
        }
    }

    private void deleteVeterinarian() {
        int selectedRow = veterinarianTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Por favor, seleccioná una veterinario.");
            return;
        }

        int dialogResult = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que querés eliminar esta veterinario?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.NO_OPTION) {
            return;
        }

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.Veterinarian veterinarian = session.get(models.Veterinarian.class, selectedVeterinarianId);
            if (veterinarian != null) {
                session.remove(veterinarian);
                session.getTransaction().commit();
                tableModel.removeRow(selectedRow);
            }
            clearFields();
        }
    }

    private void clearFields() {
        txtFullName.setText("");
        txtPhoneNumber.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        selectedVeterinarianId = null;

        veterinarianTable.clearSelection();
    }

    public JPanel getPanel() {
        return VeterinarianPanel;
    }
}
