/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daos.mealdiary;

import java.sql.SQLException;
import java.util.List;
import database.Database;
import storables.User;
import storables.mealdiary.Ingredient;
import storables.mealdiary.IngredientCollection;

/**
 *
 * @author Janne
 */
public class IngredientCollectionDao {

    private Database db;
    private IngredientDao ingDao;

    public IngredientCollectionDao(Database db) {
        this.db = db;
        this.ingDao = new IngredientDao(db);
    }

    public IngredientCollection store(IngredientCollection ingredientCollection) throws SQLException {
        if (ingredientCollection.getIngredients() != null) {
            for (Ingredient ingredient : ingredientCollection.getIngredients()) {
                ingDao.store(ingredient);
            }
        }
        int update = db.update("INSERT INTO ingredientcollection(name, totalmass, date, person_identifier, deleted) VALUES(?,?,?,?,?)", true,
                ingredientCollection.getName(), ingredientCollection.getTotalMass(), ingredientCollection.getDate(), ingredientCollection.getUser().getId(), false);
        ingredientCollection.setId(update);
        return ingredientCollection;
    }

    public IngredientCollection findOne(int id, User user) throws SQLException {
        List<IngredientCollection> queryAndCollect = db.queryAndCollect("SELECT * FROM ingredientcollection WHERE id = ? AND person_identifier = ?", rs -> {
            return new IngredientCollection(rs.getInt("id"),
                    rs.getString("name"),
                    rs.getFloat("totalmass"),
                    rs.getTimestamp("date"),
                    user,
                    ingDao.findAllByIngredientCollectionId(rs.getInt("id")));
        }, id, user.getId());

        if (queryAndCollect.isEmpty()) {
            return null;
        }
        return queryAndCollect.get(0);
    }

    public List<IngredientCollection> findAll(User user) throws SQLException {
        return db.queryAndCollect("SELECT * FROM ingredientcollection WHERE person_identifier = ?", rs -> {
            return new IngredientCollection(rs.getInt("id"),
                    rs.getString("name"),
                    rs.getFloat("totalmass"),
                    rs.getTimestamp("date"),
                    user,
                    ingDao.findAllByIngredientCollectionId(rs.getInt("id")));
        }, user.getId());
    }
}
