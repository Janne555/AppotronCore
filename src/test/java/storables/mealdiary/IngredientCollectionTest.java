/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storables.mealdiary;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import storables.Foodstuff;

/**
 *
 * @author Janne
 */
public class IngredientCollectionTest {

    public IngredientCollectionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test() {
        Foodstuff foodstuff1 = new Foodstuff("", "", "", "", 0, 0, 0, null, null, 100, 10, 20, 30, 2, 3, 5, 7, 11, 13, 17);
        
        float mass1 = 100;
        float mass2 = (float) 33.33;
        float mass3 = (float) 23.57;
        float totalmass = mass1 + mass2 + mass3;
        
        Ingredient ingredient1 = new Ingredient(0, 0, 0, mass1, foodstuff1);
        Ingredient ingredient2 = new Ingredient(0, 0, 0, mass2, foodstuff1);
        Ingredient ingredient3 = new Ingredient(0, 0, 0, mass3, foodstuff1);
        
        float caloriesTotal = mass1 * 100 + mass2 * 100 + mass3 * 100;
        float carbsTotal = mass1 * 10 + mass2 * 10 + mass3 * 10;
        float fatTotal = mass1 * 20 + mass2 * 20 + mass3 * 20;
        float proteinTotal = mass1 * 30 + mass2 * 30 + mass3 * 30;
        float ironTotal = mass1 * 2 + mass2 * 2 + mass3 * 2;
        float sodiumTotal = mass1 * 3 + mass2 * 3 + mass3 * 3;
        float potassiumTotal = mass1 * 5 + mass2 * 5 + mass3 * 5;
        float calciumTotal = mass1 * 7 + mass2 * 7 + mass3 * 7;
        float vitb12Total = mass1 * 11 + mass2 * 11 + mass3 * 11;
        float vitcTotal = mass1 * 13 + mass2 * 13 + mass3 * 13;
        float vitdTotal = mass1 * 17 + mass2 * 17 + mass3 * 17;
        
        float calories = caloriesTotal / totalmass;
        float carbs = carbsTotal / totalmass;
        float fat = fatTotal / totalmass;
        float protein = proteinTotal / totalmass;
        float iron = ironTotal / totalmass;
        float sodium = sodiumTotal / totalmass;
        float potassium = potassiumTotal / totalmass;
        float calcium = calciumTotal / totalmass;
        float vitb12 = vitb12Total / totalmass;
        float vitc = vitcTotal / totalmass;
        float vitd = vitdTotal / totalmass;
        
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient1);
        ingredients.add(ingredient2);
        ingredients.add(ingredient3);
        
        IngredientCollection c = new IngredientCollection(0, "", totalmass, null, null, ingredients);
        
        boolean results = c.getCalories() == calories
                && c.getCarbohydrate() == carbs
                && c.getFat() == fat
                && c.getProtein() == protein
                && c.getIron() == iron
                && c.getSodium() == sodium
                && c.getPotassium() == potassium
                && c.getCalcium() == calcium
                && c.getVitB12() == vitb12
                && c.getVitC() == vitc
                && c.getVitD() == vitd;
        
        assertEquals(true, results);
    }

}
