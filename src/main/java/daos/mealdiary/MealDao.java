/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daos.mealdiary;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import database.Database;
import java.time.temporal.ChronoUnit;
import storables.mealdiary.Meal;
import storables.mealdiary.MealComponent;
import storables.User;
import utilities.Container;

public class MealDao {

    private Database db;
    private MealComponentDao mecDao;

    public MealDao(Database db) {
        this.db = db;
        this.mecDao = new MealComponentDao(db);
    }

    public Meal store(Meal meal) throws SQLException {
        int update = db.update("INSERT INTO meal(person_identifier, date, deleted) VALUES(?,?,?)", true,
                meal.getUser().getId(),
                meal.getDate(),
                false);

        meal.setId(update);
        return meal;
    }

    public List<Meal> findAll(User user) throws SQLException {
        return db.queryAndCollect("SELECT * FROM Meal WHERE deleted = 'false' AND person_identifier = ? ORDER BY date DESC", rs -> {
            return new Meal(rs.getInt("id"), user, rs.getTimestamp("date"), mecDao.findByMealId(rs.getInt("id")));
        }, user.getId());
    }

    public List<Meal> findAll(User user, int limit) throws SQLException {
        return db.queryAndCollect("SELECT * FROM Meal WHERE deleted = 'false' AND person_identifier = ? ORDER BY date DESC LIMIT ?", rs -> {
            return new Meal(rs.getInt("id"), user, rs.getTimestamp("date"), mecDao.findByMealId(rs.getInt("id")));
        }, user.getId(), limit);
    }

    public List<Meal> findAll(User user, Timestamp from, Timestamp to) throws SQLException {
        return db.queryAndCollect("SELECT * FROM Meal WHERE deleted = 'false' AND person_identifier = ? AND date >= ? AND date <= ? ORDER BY date DESC", rs -> {
            return new Meal(rs.getInt("id"), user, rs.getTimestamp("date"), mecDao.findByMealId(rs.getInt("id")));
        }, user.getId(), from, to);
    }

    public int count(User user, Timestamp from, Timestamp to) throws SQLException {
        List<Integer> queryAndCollect = db.queryAndCollect("SELECT COUNT(id) AS meals_number FROM Meal WHERE deleted = 'false' AND person_identifier = ? AND date >= ? AND date <= ?", rs -> {
            return rs.getInt("meals_number");
        }, user.getId(), from, to);

        return queryAndCollect.get(0);
    }

    public List<Meal> findAll(User user, Timestamp from, Timestamp to, int offset, int limit) throws SQLException {
        return db.queryAndCollect("SELECT * FROM Meal WHERE deleted = 'false' AND person_identifier = ? AND date >= ? AND date <= ? ORDER BY date DESC OFFSET ? LIMIT ?", rs -> {
            return new Meal(rs.getInt("id"), user, rs.getTimestamp("date"), mecDao.findByMealId(rs.getInt("id")));
        }, user.getId(), from, to, offset, limit);
    }

    public List<Meal> findTodays(User user) throws SQLException {
        return db.queryAndCollect("SELECT * FROM Meal WHERE deleted = 'false' AND person_identifier = ? AND date >= CURRENT_DATE AND date <= CURRENT_DATE + interval '1 day' ORDER BY date DESC", rs -> {
            return new Meal(rs.getInt("id"), user, rs.getTimestamp("date"), mecDao.findByMealId(rs.getInt("id")));
        }, user.getId());
    }

    public Meal findOne(User user, int id) throws SQLException {
        List<Meal> queryAndCollect = db.queryAndCollect("SELECT * FROM Meal WHERE deleted = 'false' AND person_identifier = ? AND id = ?", rs -> {
            return new Meal(rs.getInt("id"), user, rs.getTimestamp("date"), mecDao.findByMealId(rs.getInt("id")));
        }, user.getId(), id);

        if (queryAndCollect.isEmpty()) {
            return null;
        }

        return queryAndCollect.get(0);
    }

    public void delete(User user, int id) throws SQLException {
        mecDao.deleteAllByMealId(id);
        db.update("DELETE FROM meal WHERE id = ? AND person_identifier = ?", false, id, user.getId());
    }

    public List<Container> dailyTotals(User user, Timestamp from, Timestamp to) throws SQLException {
        String sql = "SELECT SUM(calories_) as calories, SUM(carbohydrate_) as carbohydrate, SUM(fat_) as fat, SUM(protein_) as protein, DATE_TRUNC('day', meal.date) as truncdate, SUM(iron_) as iron, SUM(sodium_) as sodium, SUM(potassium_) as potassium, SUM(calcium_) as calcium, SUM(vitb12_) as vitb12, SUM(vitc_) as vitc, SUM(vitd_) as vitd FROM (SELECT *, (mc.mass * fm.calories) as calories_, (mc.mass * fm.carbohydrate) as carbohydrate_, (mc.mass * fm.protein) as protein_, (mc.mass * fm.fat) as fat_, (mc.mass * fm.iron) as iron_, (mc.mass * fm.sodium) as sodium_, (mc.mass * fm.potassium) as potassium_, (mc.mass * fm.calcium) as calcium_, (mc.mass * fm.vitb12) as vitb12_, (mc.mass * fm.vitc) as vitc_, (mc.mass * fm.vitd) as vitd_ FROM mealcomponent mc, foodstuffmeta fm WHERE mc.globalreference_id = fm.globalreference_id) as foo, person, meal WHERE meal.id = meal_id AND meal.person_identifier = person.identifier AND person.identifier = ? AND meal.date >= ? AND meal.date <= ? GROUP BY truncdate ORDER BY truncdate ASC";

        return db.queryAndCollect(sql, rs -> {
            return new Container(rs);
        }, user.getId(), from, to);
    }

    public Container averages(User user, Timestamp from, Timestamp to) throws SQLException {
        String sql = "SELECT current_date as truncdate, (q.calories / q.days) as calories, (q.carbohydrate / q.days) as carbohydrate, (q.fat / q.days) as fat, (q.protein / q.days) as protein, (q.iron / q.days) as iron, (q.sodium / q.days) as sodium, (q.potassium / q.days) as potassium, (q.calcium / q.days) as calcium, (q.vitb12 / q.days) as vitb12, (q.vitc / q.days) as vitc, (q.vitd / q.days) as vitd FROM (SELECT SUM(f.calories * mc.mass) as calories, SUM(f.carbohydrate * mc.mass) as carbohydrate, SUM(f.fat * mc.mass) as fat, SUM(f.protein * mc.mass) as protein, SUM(f.iron * mc.mass) iron, SUM(f.Sodium * mc.mass) as sodium, SUM(potassium * mc.mass) as potassium, SUM(calcium * mc.mass) as calcium, SUM(vitb12 * mc.mass) as vitb12, SUM(vitc * mc.mass) as vitc, SUM(vitd * mc.mass) as vitd, (?::date - ?::date + 1) as days FROM foodstuffmeta as f, meal as m, mealcomponent as mc, person as p WHERE f.globalreference_id = mc.globalreference_id AND mc.meal_id = m.id AND m.person_identifier = p.identifier AND m.date >= ? AND m.date <= ?) as q";

        List<Container> queryAndCollect = db.queryAndCollect(sql, rs -> {
            return new Container(rs);
        }, to, from, from, to);

        if (queryAndCollect.isEmpty()) {
            return null;
        }
        
        return queryAndCollect.get(0);
    }

    public Meal findLatest(User user) throws SQLException {
        List<Meal> queryAndCollect = db.queryAndCollect("SELECT * FROM Meal WHERE deleted = 'false' AND person_identifier = ? ORDER BY date DESC LIMIT 1", rs -> {
            return new Meal(rs.getInt("id"), user, rs.getTimestamp("date"), mecDao.findByMealId(rs.getInt("id")));
        }, user.getId());

        if (queryAndCollect.isEmpty()) {
            return null;
        }

        return queryAndCollect.get(0);
    }

    public void update(User user, Meal newMeal) throws SQLException {
        if (db.canEditMeal(user.getId(), newMeal.getId())) {
            db.update("UPDATE meal SET date = ? WHERE id = ?", false, newMeal.getDate(), newMeal.getId());
            findOne(user, newMeal.getId()).getComponents();
            for (MealComponent c : newMeal.getComponents()) {
                if (c.getId() != 0) {
                    System.out.println(c);
                    mecDao.update(user, c);
                }
            }
        }
    }
}
