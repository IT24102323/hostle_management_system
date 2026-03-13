package com.hostel.management.dto.food;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class FoodPreferenceRequestDto {
    private Long residentId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Boolean breakfast;
    private Boolean lunch;
    private Boolean dinner;
    private String mealType;
    private String specialRequirements;

    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }

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
