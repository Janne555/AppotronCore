/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storables.mealdiary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import storables.User;

/**
 *
 * @author Janne
 */
public class IngredientCollection {

    private int id;
    private String name;
    private float totalMass;
    private Timestamp date;
    private List<Ingredient> ingredients;
    private User user;

    public IngredientCollection(int id, String name, float totalMass, Timestamp date, User user, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.totalMass = totalMass;
        this.date = date;
        this.ingredients = ingredients;
        this.user = user;
        if (totalMass <= 0 && ingredients != null) {
            for (Ingredient i : ingredients) {
                this.totalMass += i.getMass();
            }
        }
    }
    
    public IngredientCollection(ResultSet rs, List<Ingredient> ingredients, User user) throws SQLException {
        this.id = rs.getInt("id");
        this.name = rs.getString("name");
        this.totalMass = rs.getFloat("totalmass");
        this.date = rs.getTimestamp("date");
        this.ingredients = ingredients;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public float getTotalMass() {
        return totalMass;
    }

    public void setTotalMass(float totalMass) {
        this.totalMass = totalMass;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
    
    public float getTotalCalories() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getCalories();
        }
        return sum;
    }

    public float getTotalCarbohydrate() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getCarbohydrate();
        }
        return sum;
    }

    public float getTotalFat() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getFat();
        }
        return sum;
    }

    public float getTotalProtein() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getProtein();
        }
        return sum;
    }
    
    public float getTotalIron() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getIron();
        }
        return sum;
    }

    public float getTotalSodium() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getSodium();
        }
        return sum;
    }

    public float getTotalPotassium() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getPotassium();
        }
        return sum;
    }

    public float getTotalCalcium() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getCalcium();
        }
        return sum;
    }

    public float getTotalVitB12() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getVitB12();
        }
        return sum;
    }

    public float getTotalVitC() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getVitC();
        }
        return sum;
    }

    public float getTotalVitD() {
        float sum = 0;
        for (Ingredient i : ingredients) {
            sum += i.getVitD();
        }
        return sum;
    }

    public float getCalories() {
        return getTotalCalories() / getTotalMass();
    }

    public float getCarbohydrate() {
        return getTotalCarbohydrate() / getTotalMass();
    }

    public float getFat() {
        return getTotalFat() / getTotalMass();
    }

    public float getProtein() {
        return getTotalProtein() / getTotalMass();
    }
    
    public float getIron() {
        return getTotalIron()/ getTotalMass();
    }

    public float getSodium() {
        return getTotalSodium()/ getTotalMass();
    }

    public float getPotassium() {
        return getTotalPotassium()/ getTotalMass();
    }

    public float getCalcium() {
        return getTotalCalcium()/ getTotalMass();
    }

    public float getVitB12() {
        return getTotalVitB12()/ getTotalMass();
    }

    public float getVitC() {
        return getTotalVitC()/ getTotalMass();
    }

    public float getVitD() {
        return getTotalVitD()/ getTotalMass();
    }

    public String getIdentifier() {
        String identifier = "ingredientcollection" + id;
        for (Ingredient i : ingredients) {
            identifier += "-" + i.getGlobalReferenceId();
        }
        return identifier;
    }

}
