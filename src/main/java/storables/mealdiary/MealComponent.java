/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storables.mealdiary;

import storables.Foodstuff;

/**
 *
 * @author Janne
 */
public class MealComponent {

    private int id;
    private int mealId;
    private float mass;
    private Foodstuff foodstuff;

    public MealComponent(int id, int mealId, float mass, Foodstuff foodstuff) {
        this.id = id;
        this.mealId = mealId;
        this.mass = mass;
        this.foodstuff = foodstuff;
    }

    public MealComponent(int id, int mealId, float mass, int globalReferenceId) {
        this.id = id;
        this.mealId = mealId;
        this.mass = mass;
        this.foodstuff = new Foodstuff(null, null, null, null, 0, 0, 0, 0, globalReferenceId, 0, 0, null, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public Foodstuff getFoodstuff() {
        return foodstuff;
    }

    public void setFoodstuff(Foodstuff foodstuff) {
        this.foodstuff = foodstuff;
    }

    public String getName() {
        return getFoodstuff().getName();
    }

    public int getTotalCaloriesRounded() {
        return Math.round(getMass() * getFoodstuff().getCalories());
    }

    public int getTotalCarbohydrateRounded() {
        return Math.round(getMass() * getFoodstuff().getCarbohydrate());
    }

    public int getTotalFatRounded() {
        return Math.round(getMass() * getFoodstuff().getFat());
    }

    public int getTotalProteinRounded() {
        return Math.round(getMass() * getFoodstuff().getProtein());
    }

    public int getTotalIronRounded() {
        return Math.round(getMass() * getFoodstuff().getIron());
    }

    public int getTotalSodiumRounded() {
        return Math.round(getMass() * getFoodstuff().getSodium());
    }

    public int getTotalPotassiumRounded() {
        return Math.round(getMass() * getFoodstuff().getPotassium());
    }

    public int getTotalCalciumRounded() {
        return Math.round(getMass() * getFoodstuff().getCalcium());
    }

    public int getTotalVitB12Rounded() {
        return Math.round(getMass() * getFoodstuff().getVitB12());
    }

    public int getTotalVitCRounded() {
        return Math.round(getMass() * getFoodstuff().getVitC());
    }

    public int getTotalVitDRounded() {
        return Math.round(getMass() * getFoodstuff().getVitD());
    }

    public float getCalories() {
        return getMass() * getFoodstuff().getCalories();
    }

    public float getCarbohydrate() {
        return getMass() * getFoodstuff().getCarbohydrate();
    }

    public float getFat() {
        return getMass() * getFoodstuff().getFat();
    }

    public float getProtein() {
        return getMass() * getFoodstuff().getProtein();
    }

    public float getIron() {
        return getMass() * getFoodstuff().getIron();
    }

    public float getSodium() {
        return getMass() * getFoodstuff().getSodium();
    }

    public float getPotassium() {
        return getMass() * getFoodstuff().getPotassium();
    }

    public float getCalcium() {
        return getMass() * getFoodstuff().getCalcium();
    }

    public float getVitB12() {
        return getMass() * getFoodstuff().getVitB12();
    }

    public float getVitC() {
        return getMass() * getFoodstuff().getVitC();
    }

    public float getVitD() {
        return getMass() * getFoodstuff().getVitD();
    }

    public int getGlobalReferenceId() {
        return getFoodstuff().getGlobalReferenceId();
    }

    @Override
    public String toString() {
        return "ID:" + getId() + ";"
                + "NAME:" + getFoodstuff().getName() + ";"
                + "MASS:" + getMass();
    }
}
