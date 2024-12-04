package views;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class Person {
    private JPanel PersonPanel;
    private JTextField txtFullName;
    private JTextField txtPhoneNumber;
    private JButton saveButton;
    private JTable peopleTable;
    private JButton deleteButton;
    private JButton cancelButton;

    private final DefaultTableModel tableModel;
    private Integer selectedPersonId;

    private final SessionFactory factory;

    public Person(SessionFactory factory) {
        this.factory = factory;

        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre Completo", "Número de teléfono"}, 0);
        peopleTable.setModel(tableModel);
        loadPeople();

        peopleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectPerson();
            }
        });

        saveButton.addActionListener(_ -> savePerson());

        cancelButton.addActionListener(_ -> clearFields());

        deleteButton.addActionListener(_ -> deletePerson());
    }

    private void loadPeople() {
        try (Session session = factory.openSession()) {
            List<models.Person> people = session.createQuery("from models.Person", models.Person.class).list();
            for (models.Person person : people) {
                tableModel.addRow(new Object[]{person.getId(), person.getFullName(), person.getPhoneNumber()});
            }
        }
    }

    private void selectPerson() {
        int selectedRow = peopleTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedPersonId = (int) tableModel.getValueAt(selectedRow, 0);
            txtFullName.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtPhoneNumber.setText((String) tableModel.getValueAt(selectedRow, 2));
        }
    }

    private void savePerson() {
        String fullName = txtFullName.getText();
        String phoneNumber = txtPhoneNumber.getText();

        if (fullName.isEmpty() || phoneNumber.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, completá todos los campos.");
            return;
        }

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.Person person;
            if (selectedPersonId == null) {
                person = new models.Person();
                person.setFullName(fullName);
                person.setPhoneNumber(phoneNumber);
                session.persist(person);
                tableModel.addRow(new Object[]{person.getId(), person.getFullName(), person.getPhoneNumber()});
            } else {
                person = session.get(models.Person.class, selectedPersonId);
                person.setFullName(fullName);
                person.setPhoneNumber(phoneNumber);
                session.merge(person);
                int selectedRow = peopleTable.getSelectedRow();
                tableModel.setValueAt(person.getFullName(), selectedRow, 1);
                tableModel.setValueAt(person.getPhoneNumber(), selectedRow, 2);
                selectedPersonId = null;
            }
            session.getTransaction().commit();
            clearFields();
        }
    }

    private void deletePerson() {
        int selectedRow = peopleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Por favor, seleccioná una persona.");
            return;
        }

        int dialogResult = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que querés eliminar esta persona?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.NO_OPTION) {
            return;
        }

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.Person person = session.get(models.Person.class, selectedPersonId);
            if (person != null) {
                session.remove(person);
                session.getTransaction().commit();
                tableModel.removeRow(selectedRow);
            }
            clearFields();
        }
    }

    private void clearFields() {
        txtFullName.setText("");
        txtPhoneNumber.setText("");
        selectedPersonId = null;

        peopleTable.clearSelection();
    }

    public JPanel getPanel() {
        return PersonPanel;
    }
}
