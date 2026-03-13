package com.hostel.management.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "food_preference")
public class FoodPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resident_id")
    private Resident resident;

    private LocalDate date;

    private Boolean breakfast; // true = opted in, false = opted out

    private Boolean lunch;

    private Boolean dinner;

    private String mealType; // VEG, NON_VEG, VEGAN

    private String specialRequirements;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resident getResident() { return resident; }
    public void setResident(Resident resident) { this.resident = resident; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Boolean getBreakfast() { return breakfast; }
    public void setBreakfast(Boolean breakfast) { this.breakfast = breakfast; }

    public Boolean getLunch() { return lunch; }
    public void setLunch(Boolean lunch) { this.lunch = lunch; }

    public Boolean getDinner() { return dinner; }
    public void setDinner(Boolean dinner) { this.dinner = dinner; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }
}
