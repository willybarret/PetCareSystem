package views;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import raven.datetime.component.date.DatePicker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class MedicalRecord {
    private JPanel MedicalRecordPanel;
    private JTable medicalRecordsTable;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JComboBox<ComboBoxItem> cbVeterinarian;
    private JComboBox<ComboBoxItem> cbPet;
    private JFormattedTextField dateField;
    private JTextArea txtDescription;

    private final DefaultTableModel tableModel;
    private Integer selectedMedicalRecordId;

    private final SessionFactory factory;
    private final DatePicker datePicker = new DatePicker();

    public MedicalRecord(SessionFactory factory) {
        this.factory = factory;

        tableModel = new DefaultTableModel(new Object[]{"ID", "Fecha", "Mascota", "ID Mascota", "Veterinario", "ID Veterinario","Detalles"}, 0);
        medicalRecordsTable.setModel(tableModel);
        loadMedicalRecords();
        loadPets();
        loadVeterinarians();

        medicalRecordsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectMedicalRecord();
            }
        });

        saveButton.addActionListener(_ -> saveMedicalRecord());

        cancelButton.addActionListener(_ -> clearFields());

        deleteButton.addActionListener(_ -> deleteMedicalRecord());

        datePicker.setEditor(dateField);
    }

    private void loadMedicalRecords() {
        try (Session session = factory.openSession()) {
            List<models.MedicalRecord> medicalRecords = session.createQuery("from models.MedicalRecord", models.MedicalRecord.class).list();
            for (models.MedicalRecord medicalRecord : medicalRecords) {
                tableModel.addRow(new Object[]{
                        medicalRecord.getId(),
                        medicalRecord.getDate(),
                        medicalRecord.getPet().getName(),
                        medicalRecord.getPet().getId(),
                        medicalRecord.getVeterinarian().getFullName(),
                        medicalRecord.getVeterinarian().getId(),
                        medicalRecord.getDescription(),
                });
            }
        }
    }

    private void loadPets() {
        try (Session session = factory.openSession()) {
            List<models.Pet> pets = session.createQuery("from models.Pet", models.Pet.class).list();
            for (models.Pet pet : pets) {
                cbVeterinarian.addItem(new ComboBoxItem(pet.getId(), pet.getName()));
            }
        }
    }

    private void loadVeterinarians() {
        try (Session session = factory.openSession()) {
            List<models.Veterinarian> veterinarians = session.createQuery("from models.Veterinarian", models.Veterinarian.class).list();
            for (models.Veterinarian veterinarian : veterinarians) {
                cbPet.addItem(new ComboBoxItem(veterinarian.getId(), veterinarian.getFullName()));
            }
        }
    }

    private void selectMedicalRecord() {
        int selectedRow = medicalRecordsTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedMedicalRecordId = (int) tableModel.getValueAt(selectedRow, 0);
            Date date = (Date) tableModel.getValueAt(selectedRow, 1);
            datePicker.setSelectedDate(date.toLocalDate());
            String description = (String) tableModel.getValueAt(selectedRow, 6);
            txtDescription.setText(description);
            int petId = (int) tableModel.getValueAt(selectedRow, 3);
            int veterinarianId = (int) tableModel.getValueAt(selectedRow, 5);
            for (int i = 0; i < cbVeterinarian.getItemCount(); i++) {
                if (cbVeterinarian.getItemAt(i).getId() == petId) {
                    cbVeterinarian.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cbPet.getItemCount(); i++) {
                if (cbPet.getItemAt(i).getId() == veterinarianId) {
                    cbPet.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveMedicalRecord() {
        Date date = Date.valueOf(datePicker.getSelectedDate());
        String description = txtDescription.getText();
        int petId = cbVeterinarian.getItemAt(cbVeterinarian.getSelectedIndex()).getId();
        int veterinarianId = cbPet.getItemAt(cbPet.getSelectedIndex()).getId();

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.MedicalRecord medicalRecord;
            if (selectedMedicalRecordId == null) {
                medicalRecord = new models.MedicalRecord();
                medicalRecord.setDate(date);
                medicalRecord.setDescription(description);
                medicalRecord.setPet(session.get(models.Pet.class, petId));
                medicalRecord.setVeterinarian(session.get(models.Veterinarian.class, veterinarianId));
                session.persist(medicalRecord);
                tableModel.addRow(new Object[]{
                        medicalRecord.getId(),
                        medicalRecord.getDate(),
                        medicalRecord.getPet().getName(),
                        medicalRecord.getPet().getId(),
                        medicalRecord.getVeterinarian().getFullName(),
                        medicalRecord.getVeterinarian().getId(),
                        medicalRecord.getDescription()
                });
            } else {
                medicalRecord = session.get(models.MedicalRecord.class, selectedMedicalRecordId);
                medicalRecord.setDate(date);
                medicalRecord.setDescription(description);
                medicalRecord.setPet(session.get(models.Pet.class, petId));
                medicalRecord.setVeterinarian(session.get(models.Veterinarian.class, veterinarianId));
                session.merge(medicalRecord);
                int selectedRow = medicalRecordsTable.getSelectedRow();
                tableModel.setValueAt(date, selectedRow, 1);
                tableModel.setValueAt(medicalRecord.getPet().getName(), selectedRow, 2);
                tableModel.setValueAt(medicalRecord.getPet().getId(), selectedRow, 3);
                tableModel.setValueAt(medicalRecord.getVeterinarian().getFullName(), selectedRow, 4);
                tableModel.setValueAt(medicalRecord.getVeterinarian().getId(), selectedRow, 5);
                tableModel.setValueAt(medicalRecord.getDescription(), selectedRow, 6);
                selectedMedicalRecordId = null;
            }
            session.getTransaction().commit();
            clearFields();
        }
    }

    private void deleteMedicalRecord() {
        int selectedRow = medicalRecordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Por favor, seleccioná un registro.");
            return;
        }

        int dialogResult = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que querés eliminar esta registro?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.NO_OPTION) {
            return;
        }

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.MedicalRecord medicalRecord = session.get(models.MedicalRecord.class, selectedMedicalRecordId);
            if (medicalRecord != null) {
                session.remove(medicalRecord);
                session.getTransaction().commit();
                tableModel.removeRow(selectedRow);
            }
            clearFields();
        }
    }

    private void clearFields() {
        datePicker.setSelectedDate(LocalDate.now());
        txtDescription.setText("");
        cbVeterinarian.setSelectedIndex(0);
        cbPet.setSelectedIndex(0);
        selectedMedicalRecordId = null;

        medicalRecordsTable.clearSelection();
    }

    public JPanel getPanel() {
        return MedicalRecordPanel;
    }
}
