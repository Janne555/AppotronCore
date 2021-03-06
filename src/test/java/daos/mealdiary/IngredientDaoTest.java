/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daos.mealdiary;

import static daos.mealdiary.IngredientCollectionDaoTest.database;
import static daos.mealdiary.IngredientCollectionDaoTest.user;
import database.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import storables.User;
import storables.mealdiary.Ingredient;

/**
 *
 * @author Janne
 */
public class IngredientDaoTest {

    static Database database;
    static ZonedDateTime zdt;
    static User user;
    static IngredientDao instance;

    public IngredientDaoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Statement stmt = null;
        user = new User("testitar", "testaaja", null, null, null);
        zdt = ZonedDateTime.of(2017, 6, 24, 12, 0, 0, 0, ZoneId.of("Europe/Helsinki"));

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "salasana");

            System.out.println("Creating database...");

            stmt = conn.createStatement();
            String sql = "CREATE DATABASE tests";
            stmt.executeUpdate(sql);
            System.out.println("Database created succesfully");

            database = new Database("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/tests", "postgres", "salasana");
            System.out.println("Database object created succesfully");

            System.out.println("create globalreference table");
            database.update("CREATE TABLE globalreference (id serial NOT NULL,name varchar(255) NOT NULL,identifier varchar(255) NOT NULL,type varchar(255) NOT NULL,CONSTRAINT globalreference_pk PRIMARY KEY (id),CONSTRAINT globalreference_unique UNIQUE(name, identifier)) WITH (OIDS=FALSE)", false);

            System.out.println("create foodstuffmeta table");
            database.update("CREATE TABLE foodstuffmeta (id serial NOT NULL,globalreference_id bigint NOT NULL,producer varchar(255) NOT NULL,calories DECIMAL NOT NULL,carbohydrate DECIMAL NOT NULL,fat DECIMAL NOT NULL,protein DECIMAL NOT NULL, iron DECIMAL, sodium DECIMAL, potassium DECIMAL, calcium DECIMAL, vitb12 DECIMAL, vitc DECIMAL, vitd DECIMAL, CONSTRAINT foodstuffmeta_pk PRIMARY KEY (id),CONSTRAINT foodstuffmeta_pk_unique UNIQUE(globalreference_id)) WITH (OIDS=FALSE)", false);

            System.out.println("create person table");
            database.update("CREATE TABLE person (identifier varchar(255) NOT NULL,name varchar(255) NOT NULL,email varchar(255),password varchar(255) NOT NULL,apikey varchar(255),date TIMESTAMP NOT NULL,deleted BOOLEAN NOT NULL,CONSTRAINT person_pk PRIMARY KEY (identifier),CONSTRAINT person_unique UNIQUE(name)) WITH (OIDS=FALSE)", false);

            System.out.println("create ingredient table");
            database.update("CREATE TABLE ingredient (id serial NOT NULL,globalreference_id bigint NOT NULL,ingredientcollection_id bigint NOT NULL,mass DECIMAL NOT NULL,CONSTRAINT ingredient_pk PRIMARY KEY (id),CONSTRAINT ingredient_unique UNIQUE(ingredientcollection_id, globalreference_id)) WITH (OIDS=FALSE)", false);

            System.out.println("create ingredientcollection table");
            database.update("CREATE TABLE ingredientcollection (id serial NOT NULL,name varchar(255) NOT NULL,totalmass DECIMAL NOT NULL,date TIMESTAMP NOT NULL,deleted BOOLEAN NOT NULL,person_identifier varchar(255) NOT NULL,CONSTRAINT ingredientcollection_pk PRIMARY KEY (id)) WITH (OIDS=FALSE)", false);

            System.out.println("add foreign key constraint between foodstuffmeta and globalreference");
            database.update("ALTER TABLE foodstuffmeta ADD CONSTRAINT foodstuffmeta_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id)", false);

            System.out.println("add foreign key constraint between ingredientcollection and person");
            database.update("ALTER TABLE ingredientcollection ADD CONSTRAINT ingredientcollection_fk0 FOREIGN KEY (person_identifier) REFERENCES person(identifier)", false);

            System.out.println("add foreign key constraint between ingredient and globalreference");
            database.update("ALTER TABLE ingredient ADD CONSTRAINT ingredient_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id)", false);

            System.out.println("add foreign key constraint between ingredient and ingredientcollection");
            database.update("ALTER TABLE ingredient ADD CONSTRAINT ingredient_fk1 FOREIGN KEY (ingredientcollection_id) REFERENCES ingredientcollection(id)", false);

            System.out.println("create example food items");
            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('peruna', 'p3run4', 'foodstuff')", true);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(1, 'maajussi', 1.35, 0.181, 0.022, 0.054, 0.008, 2.408, 3.987, 0.083, 0.001, 0.05, 0.003)", false);

            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('pulla', 'pull4', 'foodstuff')", false);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(2, 'leipuri', 3.26, 0.456, 0.124, 0.068, 0.006, 1.322, 1.028, 0.161, 0.001, 0.001, 0.007)", false);

            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('kinkku', 'k1nkku', 'foodstuff')", false);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(3, 'teurastaja', 0.82, 0.016, 0.048, 0.081, 0.054, 0.07, 1.21, 3.5, 0, 0.002, 0)", false);
            
            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('tryffeli', '289427', 'foodstuff')", false);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(4, 'possu', 0.82, 0.016, 0.048, 0.081, 0.054, 0.07, 1.21, 3.5, 0, 0.002, 0)", false);
            
            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('raparperi', '341313541235', 'foodstuff')", false);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(5, 'piha', 0.82, 0.016, 0.048, 0.081, 0.054, 0.07, 1.21, 3.5, 0, 0.002, 0)", false);

            System.out.println("create example person");
            database.update("INSERT INTO person(identifier, name, date, deleted, password) VALUES('testitar', 'testaaja', ?, 'false', 'salis')", false, new Timestamp(zdt.toInstant().toEpochMilli()));

            System.out.println("create example ingredientcollection");
            database.update("INSERT INTO ingredientcollection(name, totalmass, person_identifier, date, deleted) VALUES('testi', 100, 'testitar', ?, 'false')", false, new Timestamp(zdt.toInstant().toEpochMilli()));

            instance = new IngredientDao(database);

        } finally {
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }

    @AfterClass
    public static void tearDownClass() {
        Connection conn = null;
        Statement stmt = null;
        try {

            Class.forName("org.postgresql.Driver");
            System.out.println("Connecting to server...");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "salasana");

            System.out.println("Dropping database...");

            stmt = conn.createStatement();
            String sql = "DROP DATABASE tests";
            stmt.executeUpdate(sql);
            System.out.println("Database dropped succesfully");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
            database = null;
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of store method, of class IngredientDao.
     */
    @Test
    public void testStore() throws Exception {
        System.out.println("store");
        Ingredient i = new Ingredient(0, 1, 1, 50, null);
        i = instance.store(i);
        assertEquals(true, i.getId() != 0);
    }

    /**
     * Test of findAllByIngredientCollectionId method, of class IngredientDao.
     */
    @Test
    public void testFindAllByIngredientCollectionId() throws Exception {
        System.out.println("findAllByIngredientCollectionId");
        Ingredient i = new Ingredient(0, 2, 1, 50, null);
        i = instance.store(i);
        List<Ingredient> list = instance.findAllByIngredientCollectionId(1);
        assertEquals(false, list.isEmpty());
    }

    /**
     * Test of delete method, of class IngredientDao.
     */
    @Test
    public void testDelete() throws Exception {
        System.out.println("findAllByIngredientCollectionId");
        Ingredient i = new Ingredient(0, 3, 1, 50, null);
        i = instance.store(i);
        instance.delete(i.getId());
        Ingredient response = instance.findOne(i.getId());
        assertEquals(true, response == null);
    }

    /**
     * Test of update method, of class IngredientDao.
     */
    @Test
    public void testUpdate() throws Exception {
        System.out.println("update");
        Ingredient i = new Ingredient(0, 4, 1, 50, null);
        Ingredient store = instance.store(i);
        store.setGlobalReferenceId(5);
        store.setMass(100);
        instance.update(store);
        Ingredient response = instance.findOne(store.getId());
        boolean result = response.getMass() == 100 && response.getFoodstuff().getGlobalReferenceId() == 5;
        assertEquals(true, result);
    }

}
