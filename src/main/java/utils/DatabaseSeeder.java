package utils;

import models.Veterinarian;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class DatabaseSeeder {
    public static void seedAdminUser(SessionFactory factory) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();

            Query<Veterinarian> query = session.createQuery("FROM models.Veterinarian WHERE username = :username", Veterinarian.class);
            query.setParameter("username", "admin");
            Veterinarian admin = query.uniqueResult();

            if (admin == null) {
                admin = new Veterinarian();
                admin.setUsername("admin");
                admin.setPassword("admin");
                session.persist(admin);
            }

            transaction.commit();
        }
    }
}
