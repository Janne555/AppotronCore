/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package daos;

import java.sql.SQLException;
import database.Database;
import storables.Feedback;

/**
 *
 * @author Janne
 */
public class FeedbackDao {
    private Database db;

    public FeedbackDao(Database db) {
        this.db = db;
    }
    
    public void create(Feedback feedback) throws SQLException {
        db.update("INSERT INTO feedback(subject, description, person_identifier, date) VALUES(?, ?, ?, ?)", false, feedback.getObjs());
    }
}
