/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storables.mealdiary;

import java.sql.ResultSet;
import java.sql.SQLException;
import storables.Foodstuff;

/**
 *
 * @author Janne
 */
public class Ingredient {

    private int id;
    //TODO have globalreferenceid removed from here and only have it in foodstuff
    private int globalReferenceId;
    private int ingredientCollectionId;
    private float mass;
    private Foodstuff foodstuff;

    public Ingredient(int id, int globalReferenceId, int ingredientCollectionId, float mass, Foodstuff foodstuff) {
        this.id = id;
        this.globalReferenceId = globalReferenceId;
        this.ingredientCollectionId = ingredientCollectionId;
        this.mass = mass;
        this.foodstuff = foodstuff;
    }

    public Ingredient(ResultSet rs, Foodstuff foodstuff) throws SQLException {
        this.id = rs.getInt("id");
        this.globalReferenceId = rs.getInt("globalreference_id");
        this.ingredientCollectionId = rs.getInt("ingredientcollection_id");
        this.mass = rs.getFloat("mass");
        this.foodstuff = foodstuff;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGlobalReferenceId() {
        return globalReferenceId;
    }

    public void setGlobalReferenceId(int globalReferenceId) {
        this.globalReferenceId = globalReferenceId;
    }

    public int getIngredientCollectionId() {
        return ingredientCollectionId;
    }

    public void setIngredientCollectionId(int ingredientCollectionId) {
        this.ingredientCollectionId = ingredientCollectionId;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getCalories() {
        return foodstuff.getCalories() * mass;
    }

    public float getCarbohydrate() {
        return foodstuff.getCarbohydrate()* mass;
    }

    public float getFat() {
        return foodstuff.getFat()* mass;
    }

    public float getProtein() {
        return foodstuff.getProtein()* mass;
    }

    public float getIron() {
        return foodstuff.getIron()* mass;
    }

    public float getSodium() {
        return foodstuff.getSodium()* mass;
    }

    public float getPotassium() {
        return foodstuff.getPotassium()* mass;
    }

    public float getCalcium() {
        return foodstuff.getCalcium()* mass;
    }

    public float getVitB12() {
        return foodstuff.getVitB12()* mass;
    }

    public float getVitC() {
        return foodstuff.getVitC()* mass;
    }

    public float getVitD() {
        return foodstuff.getVitD()* mass;
    }

    public Foodstuff getFoodstuff() {
        return foodstuff;
    }

    public void setFoodstuff(Foodstuff foodstuff) {
        this.foodstuff = foodstuff;
    }

}
