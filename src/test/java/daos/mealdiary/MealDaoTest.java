/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daos.mealdiary;

import daos.FoodstuffDao;
import daos.UserDao;
import database.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
import storables.mealdiary.Meal;
import storables.mealdiary.MealComponent;
import utilities.Container;

/**
 *
 * @author Janne
 */
public class MealDaoTest {

    static Database database;
    static ZonedDateTime zdt;

    public MealDaoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Statement stmt = null;
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

            System.out.println("create meal table");
            database.update("CREATE TABLE meal (id serial NOT NULL,date TIMESTAMP NOT NULL,deleted BOOLEAN NOT NULL,person_identifier varchar NOT NULL,CONSTRAINT meal_pk PRIMARY KEY (id)) WITH (OIDS=FALSE)", false);

            System.out.println("create mealcomponent table");
            database.update("CREATE TABLE mealcomponent (id serial NOT NULL,meal_id bigint NOT NULL,globalreference_id bigint NOT NULL,mass DECIMAL NOT NULL,CONSTRAINT mealcomponent_pk PRIMARY KEY (id)) WITH (OIDS=FALSE)", false);

            System.out.println("create person table");
            database.update("CREATE TABLE person (identifier varchar(255) NOT NULL,name varchar(255) NOT NULL,email varchar(255),password varchar(255) NOT NULL,apikey varchar(255),date TIMESTAMP NOT NULL,deleted BOOLEAN NOT NULL,CONSTRAINT person_pk PRIMARY KEY (identifier),CONSTRAINT person_unique UNIQUE(name)) WITH (OIDS=FALSE)", false);

            System.out.println("create foreign key constraint between meal and person");
            database.update("ALTER TABLE meal ADD CONSTRAINT meal_fk0 FOREIGN KEY (person_identifier) REFERENCES person(identifier)", false);

            System.out.println("create foreign key constraint between mealcomponent and meal");
            database.update("ALTER TABLE mealcomponent ADD CONSTRAINT mealcomponent_fk0 FOREIGN KEY (meal_id) REFERENCES meal(id)", false);

            System.out.println("create foreign key constraint between mealcomponent and globalreference");
            database.update("ALTER TABLE mealcomponent ADD CONSTRAINT mealcomponent_fk1 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id)", false);

            System.out.println("add foreign key constraint between foodstuffmeta and globalreference");
            database.update("ALTER TABLE foodstuffmeta ADD CONSTRAINT foodstuffmeta_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id)", false);

            System.out.println("create example food items");
            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('peruna', 'p3run4', 'foodstuff')", true);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(1, 'maajussi', 1.35, 0.181, 0.022, 0.054, 0.008, 2.408, 3.987, 0.083, 0.001, 0.05, 0.003)", false);

            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('pulla', 'pull4', 'foodstuff')", false);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(2, 'leipuri', 3.26, 0.456, 0.124, 0.068, 0.006, 1.322, 1.028, 0.161, 0.001, 0.001, 0.007)", false);

            database.update("INSERT INTO globalreference(name, identifier, type) VALUES('kinkku', 'k1nkku', 'foodstuff')", false);
            database.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein) VALUES(3, 'teurastaja', 2.14, 0.05, 0.21, 0.64)", false);

            System.out.println("create example person");
            database.update("INSERT INTO person(identifier, name, date, deleted, password) VALUES('testitar', 'testaaja', ?, 'false', 'salis')", false, new Timestamp(zdt.toInstant().toEpochMilli()));

            System.out.println("create example meal 1");
            database.update("INSERT INTO meal(date, deleted, person_identifier) VALUES(?, 'false', 'testitar')", false, new Timestamp(zdt.toInstant().toEpochMilli()));
            database.update("INSERT INTO mealcomponent(meal_id, globalreference_id, mass) VALUES(1, 1, 142)", false);
            database.update("INSERT INTO mealcomponent(meal_id, globalreference_id, mass) VALUES(1, 2, 13)", false);
            database.update("INSERT INTO mealcomponent(meal_id, globalreference_id, mass) VALUES(1, 3, 54)", false);

            System.out.println("create example meal 2");
            database.update("INSERT INTO meal(date, deleted, person_identifier) VALUES(?, 'false', 'testitar')", false, new Timestamp(zdt.minusDays(1).toInstant().toEpochMilli()));
            database.update("INSERT INTO mealcomponent(meal_id, globalreference_id, mass) VALUES(2, 1, 43)", false);
            database.update("INSERT INTO mealcomponent(meal_id, globalreference_id, mass) VALUES(2, 2, 65)", false);

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
    public void setUp() throws ClassNotFoundException, SQLException {

    }

    @After
    public void tearDown() throws SQLException {
        database.update("DELETE FROM mealcomponent WHERE meal_id > 2", false);
        database.update("DELETE FROM meal WHERE id > 2", false);
    }

    /**
     * Test of store method, of class MealDao.
     */
    @Test
    public void testStore() throws Exception {
        System.out.println("store");
        User user = new User("testitar", "testaaja", null, null, null);
        Meal meal = new Meal(0, user, new Timestamp(System.currentTimeMillis()), null);

        MealDao instance = new MealDao(database);
        Meal response = instance.store(meal);

        boolean result = response.getId() != 0;

        assertEquals(result, true);
    }

    /**
     * Test of findAll method, of class MealDao.
     */
    @Test
    public void testFindAll_User() throws Exception {
        System.out.println("findAll");
        User user = new User("testitar", "testi", null, null, null);
        MealDao instance = new MealDao(database);
        List<Meal> result = instance.findAll(user);
        assertEquals(2, result.size());
    }

    /**
     * Test of findAll method, of class MealDao.
     */
    @Test
    public void testFindAll_User_int() throws Exception {
        System.out.println("findAll");
        User user = new User("testitar", "testi", null, null, null);
        MealDao instance = new MealDao(database);
        List<Meal> result = instance.findAll(user, 1);
        assertEquals(1, result.size());
    }

    /**
     * Test of findAll method, of class MealDao.
     */
    @Test
    public void testFindAll_3args() throws Exception {
        System.out.println("findAll");
        User user = new User("testitar", "testi", null, null, null);
        Timestamp from = new Timestamp(zdt.withHour(0).toInstant().toEpochMilli());
        Timestamp to = new Timestamp(zdt.withHour(23).withMinute(59).toInstant().toEpochMilli());
        MealDao instance = new MealDao(database);
        List<Meal> findAll = instance.findAll(user, from, to);

        assertEquals(1, findAll.size());
    }

    /**
     * Test of count method, of class MealDao.
     */
    @Test
    public void testCount() throws Exception {
        System.out.println("count");
        User user = new User("testitar", "testi", null, null, null);
        Timestamp from = new Timestamp(zdt.withHour(0).minusDays(1).toInstant().toEpochMilli());
        Timestamp to = new Timestamp(zdt.withHour(23).withMinute(59).toInstant().toEpochMilli());
        MealDao instance = new MealDao(database);
        int count = instance.count(user, from, to);

        assertEquals(2, count);
    }

    /**
     * Test of findAll method, of class MealDao.
     */
    @Test
    public void testFindAll_5args() throws Exception {
        System.out.println("findAll");
        User user = new User("testitar", "testi", null, null, null);
        Timestamp from = new Timestamp(zdt.withHour(0).minusDays(1).toInstant().toEpochMilli());
        Timestamp to = new Timestamp(zdt.withHour(23).withMinute(59).toInstant().toEpochMilli());
        MealDao instance = new MealDao(database);
        List<Meal> findAll = instance.findAll(user, from, to, 1, 5);

        assertEquals(1, findAll.size());
    }

    /**
     * Test of findTodays method, of class MealDao.
     */
    @Test
    public void testFindTodays() throws Exception {
        System.out.println("findTodays");
        User user = new User("testitar", "testi", null, null, null);
        MealDao instance = new MealDao(database);
        instance.store(new Meal(0, user, new Timestamp(System.currentTimeMillis()), null));
        List<Meal> response = instance.findTodays(user);
        boolean result = response.size() > 0;
        assertEquals(true, result);
    }

    /**
     * Test of findOne method, of class MealDao.
     */
    @Test
    public void testFindOne() throws Exception {
        System.out.println("findOne");
        User user = new User("testitar", "testi", null, null, null);
        MealDao instance = new MealDao(database);
        Meal findOne = instance.findOne(user, 1);
        boolean result = findOne != null;
        assertEquals(true, result);
    }

    /**
     * Test of delete method, of class MealDao.
     */
    @Test
    public void testDelete() throws Exception {
        System.out.println("delete");
        User user = new User("testitar", "testi", null, null, null);
        int id = 0;
        MealDao instance = new MealDao(database);
        Meal store = instance.store(new Meal(0, user, new Timestamp(System.currentTimeMillis()), null));

        instance.delete(user, store.getId());

        Meal findOne = instance.findOne(user, store.getId());
        assertEquals(null, findOne);
    }

    /**
     * Test of dailyTotals method, of class MealDao.
     */
    @Test
    public void testDailyTotals() throws Exception {
        System.out.println("dailyTotals");
        MealDao instance = new MealDao(database);
        User user = new User("testitar", "testi", null, null, null);
        Meal meal = new Meal(0, user, new Timestamp(zdt.withDayOfMonth(1).toInstant().toEpochMilli()), null);
        meal = instance.store(meal);
        float mass = (float) 43.53;
        MealComponent mc = new MealComponent(0, meal.getId(), mass, new FoodstuffDao(database).findOne(2));
        MealComponent mc2 = new MealComponent(0, meal.getId(), mass, new FoodstuffDao(database).findOne(1));
        new MealComponentDao(database).store(mc);
        new MealComponentDao(database).store(mc2);

        Container c = instance.dailyTotals(user, new Timestamp(zdt.withDayOfMonth(1).toInstant().toEpochMilli()), new Timestamp(zdt.withDayOfMonth(2).toInstant().toEpochMilli())).get(0);

        assertEquals(c.getCalcium(), ((mass * mc.getFoodstuff().getCalcium()) + (mass * mc2.getFoodstuff().getCalcium())), 0.001);
        assertEquals(c.getCalories(), ((mass * mc.getFoodstuff().getCalories()) + (mass * mc2.getFoodstuff().getCalories())), 0.001);
        assertEquals(c.getCarbohydrate(), ((mass * mc.getFoodstuff().getCarbohydrate()) + (mass * mc2.getFoodstuff().getCarbohydrate())), 0.001);
        assertEquals(c.getFat(), ((mass * mc.getFoodstuff().getFat()) + (mass * mc2.getFoodstuff().getFat())), 0.001);
        assertEquals(c.getIron(), ((mass * mc.getFoodstuff().getIron()) + (mass * mc2.getFoodstuff().getIron())), 0.001);
        assertEquals(c.getPotassium(), ((mass * mc.getFoodstuff().getPotassium()) + (mass * mc2.getFoodstuff().getPotassium())), 0.001);
        assertEquals(c.getProtein(), ((mass * mc.getFoodstuff().getProtein()) + (mass * mc2.getFoodstuff().getProtein())), 0.001);
        assertEquals(c.getSodium(), ((mass * mc.getFoodstuff().getSodium()) + (mass * mc2.getFoodstuff().getSodium())), 0.001);
        assertEquals(c.getVitB12(), ((mass * mc.getFoodstuff().getVitB12()) + (mass * mc2.getFoodstuff().getVitB12())), 0.001);
        assertEquals(c.getVitC(), ((mass * mc.getFoodstuff().getVitC()) + (mass * mc2.getFoodstuff().getVitC())), 0.001);
        assertEquals(c.getVitD(), ((mass * mc.getFoodstuff().getVitD()) + (mass * mc2.getFoodstuff().getVitD())), 0.001);
    }

    /**
     * Test of findLatest method, of class MealDao.
     */
    @Test
    public void testFindLatest() throws Exception {
        System.out.println("findLatest");
        User user = new User("testitar", "testi", null, null, null);
        MealDao instance = new MealDao(database);
        Meal store = instance.store(new Meal(0, user, new Timestamp(System.currentTimeMillis()), null));
        Meal result = instance.findLatest(user);
        assertEquals(store.getId(), result.getId());
    }

    /**
     * Test of update method, of class MealDao.
     */
    @Test
    public void testUpdate() throws Exception {
//        System.out.println("update");
//        User user = null;
//        Meal newMeal = null;
//        MealDao instance = null;
//        instance.update(user, newMeal);
//        fail("The test case is a prototype.");
    }

    @Test
    public void testAverages() throws Exception {
        System.out.println("dailyTotals");
        MealDao instance = new MealDao(database);
        User user = new User("testitar", "testi", null, null, null);

        Meal meal1 = new Meal(0, user, new Timestamp(zdt.withDayOfMonth(1).toInstant().toEpochMilli()), null);
        meal1 = instance.store(meal1);
        float mass1 = (float) 43.53;
        MealComponent mc1 = new MealComponent(0, meal1.getId(), mass1, new FoodstuffDao(database).findOne(2));
        MealComponent mc2 = new MealComponent(0, meal1.getId(), mass1, new FoodstuffDao(database).findOne(1));
        new MealComponentDao(database).store(mc1);
        new MealComponentDao(database).store(mc2);

        Meal meal2 = new Meal(0, user, new Timestamp(zdt.withDayOfMonth(2).toInstant().toEpochMilli()), null);
        meal2 = instance.store(meal2);
        float mass2 = (float) 54.68;
        MealComponent mc3 = new MealComponent(0, meal2.getId(), mass2, new FoodstuffDao(database).findOne(2));
        MealComponent mc4 = new MealComponent(0, meal2.getId(), mass2, new FoodstuffDao(database).findOne(1));
        new MealComponentDao(database).store(mc3);
        new MealComponentDao(database).store(mc4);

        Container c = instance.averages(user, new Timestamp(zdt.withDayOfMonth(1).toInstant().toEpochMilli()), new Timestamp(zdt.withDayOfMonth(2).toInstant().toEpochMilli()));

        float calories = (mc1.getFoodstuff().getCalories() * mass1 + mc2.getFoodstuff().getCalories() * mass1 + mc3.getFoodstuff().getCalories() * mass2 + mc4.getFoodstuff().getCalories() * mass2) / 2;
        float carbohydrate = (mc1.getFoodstuff().getCarbohydrate() * mass1 + mc2.getFoodstuff().getCarbohydrate() * mass1 + mc3.getFoodstuff().getCarbohydrate() * mass2 + mc4.getFoodstuff().getCarbohydrate() * mass2) / 2;
        float fat = (mc1.getFoodstuff().getFat() * mass1 + mc2.getFoodstuff().getFat() * mass1 + mc3.getFoodstuff().getFat() * mass2 + mc4.getFoodstuff().getFat() * mass2) / 2;
        float protein = (mc1.getFoodstuff().getProtein() * mass1 + mc2.getFoodstuff().getProtein() * mass1 + mc3.getFoodstuff().getProtein() * mass2 + mc4.getFoodstuff().getProtein() * mass2) / 2;
        float iron = (mc1.getFoodstuff().getIron() * mass1 + mc2.getFoodstuff().getIron() * mass1 + mc3.getFoodstuff().getIron() * mass2 + mc4.getFoodstuff().getIron() * mass2) / 2;
        float sodium = (mc1.getFoodstuff().getSodium() * mass1 + mc2.getFoodstuff().getSodium() * mass1 + mc3.getFoodstuff().getSodium() * mass2 + mc4.getFoodstuff().getSodium() * mass2) / 2;
        float potassium = (mc1.getFoodstuff().getPotassium() * mass1 + mc2.getFoodstuff().getPotassium() * mass1 + mc3.getFoodstuff().getPotassium() * mass2 + mc4.getFoodstuff().getPotassium() * mass2) / 2;
        float calcium = (mc1.getFoodstuff().getCalcium() * mass1 + mc2.getFoodstuff().getCalcium() * mass1 + mc3.getFoodstuff().getCalcium() * mass2 + mc4.getFoodstuff().getCalcium() * mass2) / 2;
        float vitb12 = (mc1.getFoodstuff().getVitB12() * mass1 + mc2.getFoodstuff().getVitB12() * mass1 + mc3.getFoodstuff().getVitB12() * mass2 + mc4.getFoodstuff().getVitB12() * mass2) / 2;
        float vitc = (mc1.getFoodstuff().getVitC() * mass1 + mc2.getFoodstuff().getVitC() * mass1 + mc3.getFoodstuff().getVitC() * mass2 + mc4.getFoodstuff().getVitC() * mass2) / 2;
        float vitd = (mc1.getFoodstuff().getVitD() * mass1 + mc2.getFoodstuff().getVitD() * mass1 + mc3.getFoodstuff().getVitD() * mass2 + mc4.getFoodstuff().getVitD() * mass2) / 2;

        assertEquals(calories, c.getCalories(), 0.001);
        assertEquals(carbohydrate, c.getCarbohydrate(), 0.001);
        assertEquals(fat, c.getFat(), 0.001);
        assertEquals(protein, c.getProtein(), 0.001);
        assertEquals(iron, c.getIron(), 0.001);
        assertEquals(sodium, c.getSodium(), 0.001);
        assertEquals(potassium, c.getPotassium(), 0.001);
        assertEquals(calcium, c.getCalcium(), 0.001);
        assertEquals(vitb12, c.getVitB12(), 0.001);
        assertEquals(vitc, c.getVitC(), 0.001);
        assertEquals(vitd, c.getVitD(), 0.001);
    }

}
