/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daos.mealdiary;

import daos.FoodstuffDao;
import java.sql.SQLException;
import java.util.List;
import database.Database;
import storables.mealdiary.Ingredient;

/**
 *
 * @author Janne
 */
public class IngredientDao {

    private Database db;
    private FoodstuffDao foodDao;

    public IngredientDao(Database db) {
        this.db = db;
        this.foodDao = new FoodstuffDao(db);
    }

    public Ingredient store(Ingredient ingredient) throws SQLException {
        int update = db.update("INSERT INTO ingredient(globalreference_id, ingredientcollection_id, mass) VALUES(?,?,?)", true,
                ingredient.getGlobalReferenceId(), ingredient.getIngredientCollectionId(), ingredient.getMass());
        ingredient.setId(update);
        return ingredient;
    }

    public Ingredient findOne(int id) throws SQLException {
        List<Ingredient> list = db.queryAndCollect("SELECT * FROM ingredient WHERE id = ?", rs -> {
            return new Ingredient(rs, foodDao.findOne(rs.getInt("globalreference_id")));
        }, id);
        
        if (list.isEmpty()) {
            return null;
        }
        
        return list.get(0);
    }

    public List<Ingredient> findAllByIngredientCollectionId(int ingredientCollectionId) throws SQLException {
        return db.queryAndCollect("SELECT * FROM ingredient WHERE ingredientcollection_id = ?", rs -> {
            return new Ingredient(rs, foodDao.findOne(rs.getInt("globalreference_id")));
        }, ingredientCollectionId);
    }

    public void delete(int id) throws SQLException {
        db.update("DELETE FROM ingredient WHERE id = ?", false, id);
    }

    public void update(Ingredient ingredient) throws SQLException {
        db.update("UPDATE ingredient SET globalreference_id = ?, mass = ? WHERE id = ?", false, ingredient.getGlobalReferenceId(), ingredient.getMass(), ingredient.getId());
    }
}
