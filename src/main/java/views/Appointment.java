package views;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import raven.datetime.component.date.DatePicker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.util.List;
import java.time.LocalDate;

public class Appointment {
    private JPanel AppointmentPanel;
    private JTable appointmentsTable;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JFormattedTextField dateField;
    private JComboBox<ComboBoxItem> cbVeterinarian;
    private JComboBox<ComboBoxItem> cbPet;

    private final DefaultTableModel tableModel;
    private Integer selectedAppointmentId;

    private final SessionFactory factory;
    private final DatePicker datePicker = new DatePicker();

    public Appointment(SessionFactory factory) {
        this.factory = factory;

        tableModel = new DefaultTableModel(new Object[]{"ID", "Fecha", "Mascota", "ID Mascota", "Veterinario", "ID Veterinario"}, 0);
        appointmentsTable.setModel(tableModel);
        loadAppointments();
        loadPets();
        loadVeterinarians();

        appointmentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectAppointment();
            }
        });

        saveButton.addActionListener(_ -> saveAppointment());

        cancelButton.addActionListener(_ -> clearFields());

        deleteButton.addActionListener(_ -> deleteAppointment());

        datePicker.setEditor(dateField);
    }

    private void loadAppointments() {
        try (Session session = factory.openSession()) {
            List<models.Appointment> appointments = session.createQuery("from models.Appointment", models.Appointment.class).list();
            for (models.Appointment appointment : appointments) {
                String petName = appointment.getPet() != null ? appointment.getPet().getName() : "SIN MASCOTA";
                int petId = appointment.getPet() != null ? appointment.getPet().getId() : -1;
                String veterinarianName = appointment.getVeterinarian() != null ? appointment.getVeterinarian().getFullName() : "SIN VETERINARIO";
                int veterinarianId = appointment.getVeterinarian() != null ? appointment.getVeterinarian().getId() : -1;
                tableModel.addRow(new Object[]{
                        appointment.getId(),
                        appointment.getDate(),
                        petName,
                        petId,
                        veterinarianName,
                        veterinarianId
                });
            }
        }
    }

    private void loadPets() {
        try (Session session = factory.openSession()) {
            List<models.Pet> pets = session.createQuery("from models.Pet", models.Pet.class).list();
            for (models.Pet pet : pets) {
                cbPet.addItem(new ComboBoxItem(pet.getId(), pet.getName()));
            }
        }
    }

    private void loadVeterinarians() {
        try (Session session = factory.openSession()) {
            List<models.Veterinarian> veterinarians = session.createQuery("from models.Veterinarian", models.Veterinarian.class).list();
            for (models.Veterinarian veterinarian : veterinarians) {
                cbVeterinarian.addItem(new ComboBoxItem(veterinarian.getId(), veterinarian.getFullName()));
            }
        }
    }

    private void selectAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedAppointmentId = (int) tableModel.getValueAt(selectedRow, 0);
            Date date = (Date) tableModel.getValueAt(selectedRow, 1);
            datePicker.setSelectedDate(date.toLocalDate());
            int petId = (int) tableModel.getValueAt(selectedRow, 3);
            int veterinarianId = (int) tableModel.getValueAt(selectedRow, 5);
            for (int i = 0; i < cbPet.getItemCount(); i++) {
                if (cbPet.getItemAt(i).getId() == petId) {
                    cbPet.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cbVeterinarian.getItemCount(); i++) {
                if (cbVeterinarian.getItemAt(i).getId() == veterinarianId) {
                    cbVeterinarian.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveAppointment() {
        Date date = Date.valueOf(datePicker.getSelectedDate());
        // String details = detailsField.getText();
        int petId = cbPet.getItemAt(cbPet.getSelectedIndex()).getId();
        int veterinarianId = cbVeterinarian.getItemAt(cbVeterinarian.getSelectedIndex()).getId();

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.Appointment appointment;
            if (selectedAppointmentId == null) {
                appointment = new models.Appointment();
                appointment.setDate(date);
                // appointment.setDetails(details);
                appointment.setPet(session.get(models.Pet.class, petId));
                appointment.setVeterinarian(session.get(models.Veterinarian.class, veterinarianId));
                session.persist(appointment);
                tableModel.addRow(new Object[]{
                        appointment.getId(),
                        appointment.getDate(),
                        appointment.getPet().getName(),
                        appointment.getPet().getId(),
                        appointment.getVeterinarian().getFullName(),
                        appointment.getVeterinarian().getId()
                });
            } else {
                appointment = session.get(models.Appointment.class, selectedAppointmentId);
                appointment.setDate(date);
                // appointment.setDetails(details);
                appointment.setPet(session.get(models.Pet.class, petId));
                appointment.setVeterinarian(session.get(models.Veterinarian.class, veterinarianId));
                session.merge(appointment);
                int selectedRow = appointmentsTable.getSelectedRow();
                tableModel.setValueAt(date, selectedRow, 1);
                tableModel.setValueAt(appointment.getPet().getName(), selectedRow, 2);
                tableModel.setValueAt(appointment.getVeterinarian().getFullName(), selectedRow, 3);
                tableModel.setValueAt(appointment.getPet().getId(), selectedRow, 4);
                tableModel.setValueAt(appointment.getVeterinarian().getId(), selectedRow, 5);
                selectedAppointmentId = null;
            }
            session.getTransaction().commit();
            clearFields();
        }
    }

    private void deleteAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Por favor, seleccioná una cita.");
            return;
        }

        int dialogResult = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que querés eliminar esta cita?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.NO_OPTION) {
            return;
        }

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.Appointment appointment = session.get(models.Appointment.class, selectedAppointmentId);
            if (appointment != null) {
                session.remove(appointment);
                session.getTransaction().commit();
                tableModel.removeRow(selectedRow);
            }
            clearFields();
        }
    }

    private void clearFields() {
        datePicker.setSelectedDate(LocalDate.now());
        // detailsField.setText("");
        cbPet.setSelectedIndex(0);
        cbVeterinarian.setSelectedIndex(0);
        selectedAppointmentId = null;

        appointmentsTable.clearSelection();
    }

    public JPanel getPanel() {
        return AppointmentPanel;
    }
}
