/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daos;

import java.sql.SQLException;
import java.util.List;
import database.Database;
import storables.Foodstuff;
import storables.Permission;
import storables.User;

/**
 *
 * @author janne
 */
public class FoodstuffDao {

    private final String columnsMain;
    private final String columnsItem;
    private final String tsvector;

    private final Database db;
    private final PermissionDao perDao;

    public FoodstuffDao(Database db) {
        this.db = db;
        this.perDao = new PermissionDao(db);
        this.columnsMain = "g.name as name, g.identifier as identifier, g.type as type, f.producer as producer, f.calories as calories, f.carbohydrate as carbohydrate, f.fat as fat, f.protein as protein, f.iron as iron, f.sodium as sodium, f.potassium as potassium, f.calcium as calcium, f.vitb12 as vitb12, f.vitc as vitc, f.vitd as vitd, g.id as globalreferenceid, f.id as foodstuffmetaid";
        
        this.columnsItem = "i.date as date, i.expiration as expiration, i.location as location, i.id as id";

        this.tsvector = "to_tsvector(g.identifier) || to_tsvector(g.type) || to_tsvector(f.producer) || to_tsvector(g.name) as document";
    }

    public Foodstuff store(Foodstuff foodstuff, User user) throws SQLException {
        List<Integer> globalReferenceIdList = db.queryAndCollect("SELECT id FROM globalreference WHERE name = ? AND identifier = ?", rs -> {
            return rs.getInt("id");
        }, foodstuff.getName(), foodstuff.getIdentifier());

        if (globalReferenceIdList.isEmpty()) {
            int update = db.update("INSERT INTO globalreference(name, identifier, type) VALUES(?, ?, ?)", true,
                    foodstuff.getName(), foodstuff.getIdentifier(), "foodstuff");
            foodstuff.setGlobalReferenceId(update);
        } else if (globalReferenceIdList.size() > 1) {
            throw new VerifyError("found more than one row with name: " + foodstuff.getName() + " and identifier: " + foodstuff.getIdentifier());
        } else {
            foodstuff.setGlobalReferenceId(globalReferenceIdList.get(0));
        }

        List<Integer> foodstuffMetaIdList = db.queryAndCollect("SELECT id FROM foodstuffmeta WHERE globalreference_id = ?", rs -> {
            return rs.getInt("id");
        }, foodstuff.getGlobalReferenceId());

        if (foodstuffMetaIdList.isEmpty()) {
            int update = db.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", true,
                    foodstuff.getGlobalReferenceId(), foodstuff.getProducer(), foodstuff.getCalories(), foodstuff.getCarbohydrate(), foodstuff.getFat(), foodstuff.getProtein(), foodstuff.getIron(), foodstuff.getSodium(), foodstuff.getPotassium(), foodstuff.getCalcium(), foodstuff.getVitB12(), foodstuff.getVitC(), foodstuff.getVitD());
            foodstuff.setFoodstuffMetaId(update);
        } else if (foodstuffMetaIdList.size() > 1) {
            throw new VerifyError("found more than one row with global reference id: " + foodstuff.getGlobalReferenceId());
        } else {
            foodstuff.setId(foodstuffMetaIdList.get(0));
        }

        int update = db.update("INSERT INTO Item(globalreference_id, location, date, expiration, deleted) VALUES(?,?,?,?,?)", true,
                foodstuff.getGlobalReferenceId(), foodstuff.getLocation(), foodstuff.getDate(), foodstuff.getExpiration(), false);
        foodstuff.setId(update);

        perDao.store(new Permission(0, user.getId(), foodstuff.getId(), true, true));

        return foodstuff;
    }

    public List<Foodstuff> search(User user, Object... searchWords) throws SQLException {
        Object[] terms = new Object[searchWords.length + 1];
        terms[0] = user.getId();
        for (int i = 1; i < terms.length; i++) {
            terms[i] = searchWords[i - 1];
        }

        String sql = "SELECT DISTINCT ON (id) * FROM (SELECT " + this.columnsMain + ", " + this.columnsItem + ", " + this.tsvector + " "
                + "FROM globalreference as g, item as i, foodstuffmeta as f, permission as p "
                + "WHERE g.id = i.globalreference_id "
                + "AND f.globalreference_id = g.id "
                + "AND p.item_id = i.id "
                + "AND i.deleted = 'false' "
                + "AND p.person_identifier = ?) as mainquery "
                + "WHERE mainquery.document @@ to_tsquery(?)";

        for (int i = 0; i < searchWords.length - 1; i++) {
            sql += " AND mainquery.document @@ to_tsquery(?)";
        }

        List<Foodstuff> items = db.queryAndCollect(sql, rs -> {
            return new Foodstuff(rs, true);
        }, terms);

        return items;
    }

    public List<Foodstuff> searchGlobal(Object... searchWords) throws SQLException {
        String sql = "SELECT * FROM (SELECT " + this.columnsMain + ", " + this.tsvector + " "
                + "FROM globalreference as g, foodstuffmeta as f "
                + "WHERE f.globalreference_id = g.id) as mainquery "
                + "WHERE mainquery.document @@ to_tsquery(?)";

        for (int i = 0; i < searchWords.length - 1; i++) {
            sql += " AND mainquery.document @@ to_tsquery(?)";
        }

        List<Foodstuff> items = db.queryAndCollect(sql, rs -> {
            return new Foodstuff(rs, false);
        }, searchWords);

        return items;
    }

    public List<Foodstuff> searchGlobal(boolean sortPopular, Object... searchWords) throws SQLException {
        String sql = "SELECT * FROM (SELECT c.uses as uses, " + this.columnsMain + ", " + this.tsvector + " "
                + "FROM globalreference as g, foodstuffmeta as f LEFT JOIN (SELECT count(c.globalreference_id) as uses, c.globalreference_id as gid FROM mealcomponent c GROUP BY c.globalreference_id) as c ON f.globalreference_id = c.gid WHERE f.globalreference_id = g.id) as mainquery WHERE mainquery.document @@ to_tsquery(?)";

        for (int i = 0; i < searchWords.length - 1; i++) {
            sql += " AND mainquery.document @@ to_tsquery(?)";
        }

        sql += " ORDER BY mainquery.uses DESC NULLS LAST";

        List<Foodstuff> items = db.queryAndCollect(sql, rs -> {
            return new Foodstuff(rs, false);
        }, searchWords);

        return items;
    }

    public List<Foodstuff> findAll(User user) throws SQLException {
        return db.queryAndCollect("SELECT " + this.columnsMain + ", " + this.columnsItem + " "
                + "FROM globalreference as g, item as i, foodstuffmeta as f, permission as p "
                + "WHERE g.id = i.globalreference_id "
                + "AND f.globalreference_id = g.id "
                + "AND p.item_id = i.id "
                + "AND i.deleted = 'false' "
                + "AND p.person_identifier = ? ", rs -> {
                    return new Foodstuff(rs, true);
                }, user.getId());
    }

    public Foodstuff findOne(User user, int id) throws SQLException {
        List<Foodstuff> queryAndCollect = db.queryAndCollect("SELECT " + this.columnsMain + ", " + this.columnsItem + " "
                + "FROM globalreference as g, item as i, foodstuffmeta as f, permission as p "
                + "WHERE g.id = i.globalreference_id "
                + "AND f.globalreference_id = g.id "
                + "AND p.item_id = i.id "
                + "AND i.deleted = 'false'"
                + "AND p.person_identifier = ? AND i.id = ?", rs -> {
                    return new Foodstuff(rs, true);
                }, user.getId(), id);

        if (queryAndCollect.isEmpty()) {
            return null;
        }

        return queryAndCollect.get(0);
    }

    public Foodstuff findOne(int id) throws SQLException {
        List<Foodstuff> queryAndCollect = db.queryAndCollect("SELECT " + this.columnsMain + " "
                + "FROM globalreference as g, foodstuffmeta as f "
                + "WHERE f.globalreference_id = g.id "
                + "AND g.id = ?", rs -> {
                    return new Foodstuff(rs, false);
                }, id);
        if (queryAndCollect.isEmpty()) {
            return null;
        }

        return queryAndCollect.get(0);
    }

    public Foodstuff findOne(String name, String identifier) throws SQLException {
        List<Foodstuff> queryAndCollect = db.queryAndCollect("SELECT " + this.columnsMain + " "
                + "FROM globalreference as g, foodstuffmeta as f WHERE f.globalreference_id = g.id AND g.name = ? AND g.identifier = ?", rs -> {
                    return new Foodstuff(rs, false);
                }, name, identifier);
        if (queryAndCollect.isEmpty()) {
            return null;
        }

        return queryAndCollect.get(0);
    }

    public List<String> getLocations(User user) throws SQLException {
        return db.queryAndCollect("SELECT DISTINCT ON (location) location "
                + "FROM item as i, permission as p "
                + "WHERE p.item_id = i.id "
                + "AND i.deleted = 'false' "
                + "AND p.person_identifier = ? ", rs -> {
                    return rs.getString("location");
                }, user.getId());
    }

    public List<Foodstuff> getExpiring(User user, int limit) throws SQLException {
        return db.queryAndCollect("SELECT " + this.columnsMain + ", " + this.columnsItem + " "
                + "FROM globalreference as g, item as i, foodstuffmeta as f, permission as p "
                + "WHERE g.id = i.globalreference_id "
                + "AND f.globalreference_id = g.id "
                + "AND p.item_id = i.id "
                + "AND i.deleted = 'false' "
                + "AND p.person_identifier = ? "
                + "AND i.expiration > CURRENT_DATE "
                + "ORDER BY i.expiration ASC LIMIT ?", rs -> {
                    return new Foodstuff(rs, true);
                }, user.getId(), limit);
    }

    public void delete(User user, int id) throws SQLException {
        if (db.canDelete(user.getId(), id)) {
            db.update("UPDATE item SET deleted = 'true' WHERE item.id = ?", false, id);
        } else {
            throw new SecurityException("action is not authorized");
        }
    }

    public void editPersonal(User user, Foodstuff foodstuff) throws SQLException {
        if (db.canEdit(user.getId(), foodstuff.getId())) {
            db.update("UPDATE item SET location = ?, expiration = ? WHERE id = ?", false,
                    foodstuff.getLocation(), foodstuff.getExpiration(), foodstuff.getId());
        } else {
            throw new SecurityException("action is not authorized");
        }
    }

    public void editGlobal(User user, Foodstuff foodstuff) throws SQLException {
        if (db.canEdit(user.getId(), foodstuff.getId())) {
            db.update("UPDATE globalreference SET name = ?, identifier = ?, type = ? WHERE item.id = ?", false,
                    foodstuff.getName(), foodstuff.getIdentifier(), "foodstuff", foodstuff.getId());

            db.update("UPDATE foodstuffmeta SET producer = ?, calories = ?, carbohydrate = ?, fat = ?, protein = ?, iron = ?, sodium = ?, potassium = ?, calcium = ?, vitb12 = ?, vitc = ?, vitd = ? WHERE id = ?", false,
                    foodstuff.getProducer(), foodstuff.getCalories(), foodstuff.getCarbohydrate(), foodstuff.getFat(), foodstuff.getProtein(), foodstuff.getFoodstuffMetaId());

            db.update("UPDATE item SET location = ?, expiration = ? WHERE id = ?", false,
                    foodstuff.getLocation(), foodstuff.getExpiration(), foodstuff.getId());
        } else {
            throw new SecurityException("action is not authorized");
        }
    }

    public List<Foodstuff> mostPopular(User user, int limit) throws SQLException {
        String sql = "select count(g.id) as instances, g.id as globalreference_id from mealcomponent m, globalreference g, meal e, person p where p.identifier = e.person_identifier AND e.id = m.meal_id AND m.globalreference_id = g.id AND p.identifier = ? group by g.id order by instances desc limit ?;";

        List<Foodstuff> queryAndCollect = db.queryAndCollect("SELECT * FROM MealComponent WHERE meal_id = ?", rs -> {
            return findOne(rs.getInt("globalreference_id"));
        }, user.getId(), limit);
        return queryAndCollect;
    }

    public List<Foodstuff> globalDump() throws SQLException {
        String sql = "SELECT * FROM globalreference, foodstuffmeta WHERE globalreference.id = foodstuffmeta.id";

        List<Foodstuff> items = db.queryAndCollect(sql, rs -> {
            return new Foodstuff(rs, false);
        });

        return items;
    }

    public void storeGlobal(Foodstuff foodstuff) throws SQLException {
        int update = db.update("INSERT INTO globalreference(name, identifier, type) VALUES(?, ?, ?)", true,
                foodstuff.getName(), foodstuff.getIdentifier(), "foodstuff");
        foodstuff.setGlobalReferenceId(update);
        db.update("INSERT INTO foodstuffmeta(globalreference_id, producer, calories, carbohydrate, fat, protein, iron, sodium, potassium, calcium, vitb12, vitc, vitd) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", true, foodstuff.getGlobalReferenceId(), foodstuff.getProducer(), foodstuff.getCalories(), foodstuff.getCarbohydrate(), foodstuff.getFat(), foodstuff.getProtein(), foodstuff.getIron(), foodstuff.getSodium(), foodstuff.getPotassium(), foodstuff.getCalcium(), foodstuff.getVitB12(), foodstuff.getVitC(), foodstuff.getVitD());
    }
}
