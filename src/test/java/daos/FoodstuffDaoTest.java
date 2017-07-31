/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daos;

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
import storables.Foodstuff;
import storables.User;

/**
 *
 * @author Janne
 */
public class FoodstuffDaoTest {

    static Database database;
    static ZonedDateTime zdt;
    static User user;
    static FoodstuffDao instance;
    static int counter;

    public FoodstuffDaoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Statement stmt = null;
        counter = 0;
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

            System.out.println("create item table");
            database.update("CREATE TABLE item (id serial NOT NULL,globalreference_id bigint NOT NULL,location varchar(255) NOT NULL,date TIMESTAMP NOT NULL,expiration TIMESTAMP,deleted BOOLEAN NOT NULL DEFAULT 'false',CONSTRAINT item_pk PRIMARY KEY (id)) WITH (OIDS=FALSE)", false);

            System.out.println("create person table");
            database.update("CREATE TABLE person (identifier varchar(255) NOT NULL,name varchar(255) NOT NULL,email varchar(255),password varchar(255) NOT NULL,apikey varchar(255),date TIMESTAMP NOT NULL,deleted BOOLEAN NOT NULL,CONSTRAINT person_pk PRIMARY KEY (identifier),CONSTRAINT person_unique UNIQUE(name)) WITH (OIDS=FALSE)", false);

            System.out.println("create permission table");
            database.update("CREATE TABLE permission (id serial NOT NULL,person_identifier varchar(255) NOT NULL,item_id bigint NOT NULL,canedit BOOLEAN NOT NULL,candelete BOOLEAN NOT NULL,CONSTRAINT permission_pk PRIMARY KEY (id),CONSTRAINT permission_unique UNIQUE(person_identifier, item_id)) WITH (OIDS=FALSE)", false);

            System.out.println("add foreign key constraint between foodstuffmeta and globalreference");
            database.update("ALTER TABLE foodstuffmeta ADD CONSTRAINT foodstuffmeta_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id)", false);

            System.out.println("add foreign key constraint between item and globalreference");
            database.update("ALTER TABLE item ADD CONSTRAINT item_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id)", false);

            System.out.println("add foreign key constraint between permission and item");
            database.update("ALTER TABLE permission ADD CONSTRAINT permission_fk0 FOREIGN KEY (person_identifier) REFERENCES person(identifier)", false);

            System.out.println("add foreign key constraint between permission and person");
            database.update("ALTER TABLE permission ADD CONSTRAINT permission_fk1 FOREIGN KEY (item_id) REFERENCES item(id)", false);

            System.out.println("create example food items");
            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('peruna', 'p3run4', 'foodstuff')", true);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(1, 'maajussi', 1.35, 0.181, 0.022, 0.054, 0.008, 2.408, 3.987, 0.083, 0.001, 0.05, 0.003)", false);

            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('pulla', 'pull4', 'foodstuff')", false);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(2, 'leipuri', 3.26, 0.456, 0.124, 0.068, 0.006, 1.322, 1.028, 0.161, 0.001, 0.001, 0.007)", false);

            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('kinkku', 'k1nkku', 'foodstuff')", false);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(3, 'teurastaja', 0.82, 0.016, 0.048, 0.081, 0.054, 0.07, 1.21, 3.5, 0, 0.002, 0)", false);

            System.out.println("create example person");
            database.update("INSERT INTO person(identifier, name, date, deleted, password) VALUES('testitar', 'testaaja', ?, 'false', 'salis')", false, new Timestamp(zdt.toInstant().toEpochMilli()));
            instance = new FoodstuffDao(database);

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
     * Test of store method, of class FoodstuffDao.
     */
    @Test
    public void testStore() throws Exception {
        System.out.println("store");
        Foodstuff foodstuff = new Foodstuff("soylent green", "235711131719", "madefromppl", "undisclosed", 0, 0, 0, new Timestamp(zdt.toInstant().toEpochMilli()), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);

        Foodstuff response = instance.store(foodstuff, user);
        counter++;
        boolean result = response.getId() != 0 && response.getGlobalReferenceId() != 0 && response.getFoodstuffMetaId() != 0;
        assertEquals(true, result);
    }

    /**
     * Test of search method, of class FoodstuffDao.
     */
    @Test
    public void testSearch() throws Exception {
        System.out.println("search");
        Foodstuff foodstuff = new Foodstuff("sunnuntainen darrapizza", "23423423", "uguu", "pizzeriia", 0, 0, 0, new Timestamp(zdt.toInstant().toEpochMilli()), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);

        instance.store(foodstuff, user);
        counter++;
        Object[] searchWords = {"sunnuntainen", "darrapizza"};
        List<Foodstuff> result = instance.search(user, searchWords);
        assertEquals("sunnuntainen darrapizza", result.get(0).getName());
    }

    /**
     * Test of searchGlobal method, of class FoodstuffDao.
     */
    @Test
    public void testSearchGlobal_ObjectArr() throws Exception {
        System.out.println("searchGlobal");
        Object[] searchWords = {"peruna"};
        List<Foodstuff> result = instance.searchGlobal(searchWords);
        assertEquals("peruna", result.get(0).getName());
    }

    /**
     * Test of searchGlobal method, of class FoodstuffDao.
     */
    @Test
    public void testSearchGlobal_boolean_ObjectArr() throws Exception {
        System.out.println("searchGlobal not yet implemented");
//        boolean sortPopular = false;
//        Object[] searchWords = null;
//        FoodstuffDao instance = null;
//        List<Foodstuff> expResult = null;
//        List<Foodstuff> result = instance.searchGlobal(sortPopular, searchWords);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
    }

    /**
     * Test of findAll method, of class FoodstuffDao.
     */
    @Test
    public void testFindAll() throws Exception {
        System.out.println("findAll");
        Foodstuff foodstuff = new Foodstuff("roiskelappa", "4324524", "saarioinen", "jääkaappi", 0, 0, 0, new Timestamp(zdt.toInstant().toEpochMilli()), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);
        instance.store(foodstuff, user);
        counter++;
        List<Foodstuff> list = instance.findAll(user);
        boolean result = false;
        for (Foodstuff f : list) {
            if (f.getName().equals("roiskelappa")) {
                result = true;
            }
        }
        assertEquals(true, result);
    }

    /**
     * Test of findOne method, of class FoodstuffDao.
     */
    @Test
    public void testFindOne_User_int() throws Exception {
        System.out.println("findOne");
        Foodstuff foodstuff = new Foodstuff("mansikka", "12311234345", "pelto", "jääkaappi", 0, 0, 0, new Timestamp(zdt.toInstant().toEpochMilli()), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);
        Foodstuff store = instance.store(foodstuff, user);
        counter++;

        Foodstuff findOne = instance.findOne(user, store.getId());
        assertEquals(store.getName(), findOne.getName());
    }

    /**
     * Test of findOne method, of class FoodstuffDao.
     */
    @Test
    public void testFindOne_int() throws Exception {
        System.out.println("findOne");
        int id = 1;
        Foodstuff result = instance.findOne(id);
        assertEquals("peruna", result.getName());
    }

    /**
     * Test of findOne method, of class FoodstuffDao.
     */
    @Test
    public void testFindOne_String_String() throws Exception {
        System.out.println("findOne");
        String name = "peruna";
        String identifier = "p3run4";
        Foodstuff result = instance.findOne(name, identifier);
        assertEquals(result.getName(), name);
    }

    /**
     * Test of getLocations method, of class FoodstuffDao.
     */
    @Test
    public void testGetLocations() throws Exception {
        System.out.println("getLocations");
        Foodstuff foodstuff = new Foodstuff("persikka", "23412341235532", "puu", "kaappi", 0, 0, 0, new Timestamp(zdt.toInstant().toEpochMilli()), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);

        instance.store(foodstuff, user);
        counter++;
        List<String> list = instance.getLocations(user);
        boolean result = false;
        for (String s : list) {
            if (s.equals("kaappi")) {
                result = true;
            }
        }

        assertEquals(true, result);
    }

    /**
     * Test of getExpiring method, of class FoodstuffDao.
     */
    @Test
    public void testGetExpiring() throws Exception {
        System.out.println("getExpiring");
        Foodstuff foodstuff1 = new Foodstuff("mustikka", "6455325624576", "metsä", "pakastin", 0, 0, 0, new Timestamp(System.currentTimeMillis() + 43200000), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);
        Foodstuff foodstuff2 = new Foodstuff("puolukka", "342334234111", "metsä", "pakastin", 0, 0, 0, new Timestamp(System.currentTimeMillis() + 86400000), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);
        instance.store(foodstuff1, user);
        counter++;
        instance.store(foodstuff2, user);
        counter++;

        boolean result = true;

        List<Foodstuff> list = instance.getExpiring(user, 10);

        if (!list.get(0).getName().equals("mustikka")) {
            result = false;
        }
        if (!list.get(1).getName().equals("puolukka")) {
            result = false;
        }
        assertEquals(true, result);
    }

    /**
     * Test of delete method, of class FoodstuffDao.
     */
    @Test
    public void testDelete() throws Exception {
        System.out.println("delete");
        Foodstuff foodstuff = new Foodstuff("vispipuuro", "vntg80q4", "kokki", "pakastin", 0, 0, 0, new Timestamp(zdt.toInstant().toEpochMilli()), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);
        Foodstuff store = instance.store(foodstuff, user);
        counter++;
        List<Foodstuff> before = instance.findAll(user);
        instance.delete(user, store.getId());
        List<Foodstuff> after = instance.findAll(user);
        
        assertEquals(true, before.size() > after.size());
    }

    /**
     * Test of editPersonal method, of class FoodstuffDao.
     */
    @Test
    public void testEditPersonal() throws Exception {
        System.out.println("editPersonal");
        Foodstuff foodstuff = new Foodstuff("omena", "13rfrg2354", "puu", "pakastin", 0, 0, 0, new Timestamp(zdt.toInstant().toEpochMilli()), new Timestamp(zdt.toInstant().toEpochMilli()), 420, 69, 11, 4, 2, 3, 5, 7, 11, 13, 17);
        foodstuff = instance.store(foodstuff, user);
        counter++;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        foodstuff.setLocation("mikrokaappi");
        foodstuff.setExpiration(timestamp);
        instance.editPersonal(user, foodstuff);
        Foodstuff response = instance.findOne(user, foodstuff.getId());
        boolean result = response.getLocation().equals("mikrokaappi") && response.getExpiration().equals(timestamp);
        assertEquals(true, result);
    }

    /**
     * Test of mostPopular method, of class FoodstuffDao.
     */
    @Test
    public void testMostPopular() throws Exception {
        System.out.println("mostPopular not yet implemented");
//        User user = null;
//        int limit = 0;
//        FoodstuffDao instance = null;
//        List<Foodstuff> expResult = null;
//        List<Foodstuff> result = instance.mostPopular(user, limit);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
    }

    /**
     * Test of globalDump method, of class FoodstuffDao.
     */
    @Test
    public void testGlobalDump() throws Exception {
        System.out.println("globalDump not yet implemented");
//        FoodstuffDao instance = null;
//        List<Foodstuff> expResult = null;
//        List<Foodstuff> result = instance.globalDump();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
    }

    /**
     * Test of storeGlobal method, of class FoodstuffDao.
     */
    @Test
    public void testStoreGlobal() throws Exception {
        System.out.println("storeGlobal not yet implemented");
//        Foodstuff foodstuff = null;
//        FoodstuffDao instance = null;
//        instance.storeGlobal(foodstuff);
//        fail("The test case is a prototype.");
    }
    
    @Test
    public void testFindProducers() throws Exception {
        System.out.println("findProducers");
        List<String> list = instance.findProducers();
        assertEquals(true, list.contains("maajussi"));
    }

}
