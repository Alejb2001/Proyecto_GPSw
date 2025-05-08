package mx.tecnm.chih2.proyecto_gpsw;

import java.io.Serializable;
import java.util.Date;

public class FoodEntry implements Serializable {
    private int id;
    private String foodName;
    private int calories;
    private Date date;

    public FoodEntry(String foodName, int calories) {
        this.foodName = foodName;
        this.calories = calories;
        this.date = new Date();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}