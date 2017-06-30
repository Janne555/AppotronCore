/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 *
 * @author Janne
 */
public class Container {
    private Timestamp date;
    private float carbohydrate;
    private float fat;
    private float protein;
    private float calories;
    private float iron;
    private float sodium;
    private float potassium;
    private float calcium;
    private float vitB12;
    private float vitC;
    private float vitD;

    public Container(ResultSet rs) throws SQLException {
        this.carbohydrate = rs.getFloat("totalCarbohydrate");
        this.fat = rs.getFloat("totalFat");
        this.protein = rs.getFloat("totalProtein");
        this.calories = rs.getFloat("totalCalories");
        this.iron = rs.getFloat("totalIron");
        this.sodium = rs.getFloat("totalSodium");
        this.potassium = rs.getFloat("totalPotassium");
        this.calcium = rs.getFloat("totalCalcium");
        this.vitB12 = rs.getFloat("totalVitb12");
        this.vitC = rs.getFloat("totalVitc");
        this.vitD = rs.getFloat("totalVitd");
        this.date = rs.getTimestamp("truncdate");
    }

    public Container(Timestamp date, float carbohydrate, float fat, float protein, float calories, float iron, float sodium, float potassium, float calcium, float vitB12, float vitC, float vitD) {
        this.date = date;
        this.carbohydrate = carbohydrate;
        this.fat = fat;
        this.protein = protein;
        this.calories = calories;
        this.iron = iron;
        this.sodium = sodium;
        this.potassium = potassium;
        this.calcium = calcium;
        this.vitB12 = vitB12;
        this.vitC = vitC;
        this.vitD = vitD;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public float getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(float carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public float getProtein() {
        return protein;
    }

    public void setProtein(float protein) {
        this.protein = protein;
    }

    public float getCalories() {
        return calories;
    }

    public Container setCalories(float calories) {
        this.calories = calories;
        return this;
    }

    public float getIron() {
        return iron;
    }

    public void setIron(float iron) {
        this.iron = iron;
    }

    public float getSodium() {
        return sodium;
    }

    public void setSodium(float sodium) {
        this.sodium = sodium;
    }

    public float getPotassium() {
        return potassium;
    }

    public void setPotassium(float potassium) {
        this.potassium = potassium;
    }

    public float getCalcium() {
        return calcium;
    }

    public void setCalcium(float calcium) {
        this.calcium = calcium;
    }

    public float getVitB12() {
        return vitB12;
    }

    public void setVitB12(float vitB12) {
        this.vitB12 = vitB12;
    }

    public float getVitC() {
        return vitC;
    }

    public void setVitC(float vitC) {
        this.vitC = vitC;
    }

    public float getVitD() {
        return vitD;
    }

    public void setVitD(float vitD) {
        this.vitD = vitD;
    }

    public void round() {
        setCalories(Math.round(getCalories()));
        setCarbohydrate(Math.round(getCarbohydrate()));
        setFat(Math.round(getFat()));
        setProtein(Math.round(getProtein()));
        setIron(Math.round(getIron()));
        setSodium(Math.round(getSodium()));
        setPotassium(Math.round(getPotassium()));
        setCalcium(Math.round(getCalcium()));
        setVitB12(Math.round(getVitB12()));
        setVitC(Math.round(getVitC()));
        setVitD(Math.round(getVitD()));
    }
}
