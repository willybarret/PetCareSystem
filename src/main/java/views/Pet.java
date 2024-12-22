package views;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class Pet {
    private JPanel PetPanel;
    private JTextField txtName;
    private JTextField txtBreed;
    private JTextField txtAge;
    private JComboBox<ComboBoxItem> cbOwner;
    private JTable petsTable;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;

    private final DefaultTableModel tableModel;
    private Integer selectedPetId;

    private final SessionFactory factory;

    public Pet(SessionFactory factory) {
        this.factory = factory;

        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Raza", "Edad", "Dueño", "ID Dueño"}, 0);
        petsTable.setModel(tableModel);
        loadPets();
        loadOwners();

        petsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectPet();
            }
        });

        saveButton.addActionListener(_ -> savePet());

        cancelButton.addActionListener(_ -> clearFields());

        deleteButton.addActionListener(_ -> deletePet());
    }

    private void loadPets() {
        try (Session session = factory.openSession()) {
            List<models.Pet> pets = session.createQuery("from models.Pet", models.Pet.class).list();
            for (models.Pet pet : pets) {
                String ownerName = pet.getOwner() != null ? pet.getOwner().getFullName() : "SIN DUEÑO";
                int ownerId = pet.getOwner() != null ? pet.getOwner().getId() : -1;
                tableModel.addRow(new Object[]{pet.getId(), pet.getName(), pet.getBreed(), pet.getAge(), ownerName, ownerId});
            }
        }
    }

    private void loadOwners() {
        try (Session session = factory.openSession()) {
            List<models.Person> owners = session.createQuery("from models.Person", models.Person.class).list();
            for (models.Person owner : owners) {
                cbOwner.addItem(new ComboBoxItem(owner.getId(), owner.getFullName()));
            }
        }
    }

    private void selectPet() {
        int selectedRow = petsTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedPetId = (int) tableModel.getValueAt(selectedRow, 0);
            txtName.setText((String) tableModel.getValueAt(selectedRow, 1));
            txtBreed.setText((String) tableModel.getValueAt(selectedRow, 2));
            txtAge.setText(tableModel.getValueAt(selectedRow, 3).toString());
            int ownerId = (int) tableModel.getValueAt(selectedRow, 5);
            for (int i = 0; i < cbOwner.getItemCount(); i++) {
                if (cbOwner.getItemAt(i).getId() == ownerId) {
                    cbOwner.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void savePet() {
        String name = txtName.getText();
        String breed = txtBreed.getText();
        int age;
        try {
            age = Integer.parseInt(txtAge.getText());
            if (age < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Por favor, ingresá una edad válida.");
            return;
        }
        int ownerId = cbOwner.getItemAt(cbOwner.getSelectedIndex()).getId();
        models.Person owner = null;
        if (ownerId != 0) {
            try (Session session = factory.openSession()) {
                owner = session.get(models.Person.class, ownerId);
            }
        }

        if (name.isEmpty() || breed.isEmpty() || owner == null) {
            JOptionPane.showMessageDialog(null, "Por favor, completá todos los campos.");
            return;
        }

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.Pet pet;
            if (selectedPetId == null) {
                pet = new models.Pet();
                pet.setName(name);
                pet.setBreed(breed);
                pet.setAge(age);
                pet.setOwner(owner);
                session.persist(pet);
                tableModel.addRow(new Object[]{pet.getId(), pet.getName(), pet.getBreed(), pet.getAge(), pet.getOwner().getFullName(), pet.getOwner().getId()});
            } else {
                pet = session.get(models.Pet.class, selectedPetId);
                pet.setName(name);
                pet.setBreed(breed);
                pet.setAge(age);
                pet.setOwner(owner);
                session.merge(pet);
                int selectedRow = petsTable.getSelectedRow();
                tableModel.setValueAt(name, selectedRow, 1);
                tableModel.setValueAt(breed, selectedRow, 2);
                tableModel.setValueAt(age, selectedRow, 3);
                tableModel.setValueAt(owner.getFullName(), selectedRow, 4);
                tableModel.setValueAt(owner.getId(), selectedRow, 5);
                selectedPetId = null;
            }
            session.getTransaction().commit();
            clearFields();
        }
    }

    private void deletePet() {
        int selectedRow = petsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Por favor, seleccioná una mascota.");
            return;
        }

        int dialogResult = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que querés eliminar esta mascota?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.NO_OPTION) {
            return;
        }

        try (Session session = factory.openSession()) {
            session.beginTransaction();
            models.Pet pet = session.get(models.Pet.class, selectedPetId);
            if (pet != null) {
                session.remove(pet);
                session.getTransaction().commit();
                tableModel.removeRow(selectedRow);
            }
            clearFields();
        }
    }

    private void clearFields() {
        txtName.setText("");
        txtBreed.setText("");
        txtAge.setText("");
        cbOwner.setSelectedIndex(0);
        selectedPetId = null;

        petsTable.clearSelection();
    }

    public JPanel getPanel() {
        return PetPanel;
    }
}
