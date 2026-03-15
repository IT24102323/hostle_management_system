package com.hostel.management.service;

import com.hostel.management.dto.food.FoodPreferenceRequestDto;
import com.hostel.management.dto.food.FoodPreferenceResponseDto;
import com.hostel.management.entity.FoodPreference;
import com.hostel.management.entity.Resident;
import com.hostel.management.repository.FoodPreferenceRepository;
import com.hostel.management.repository.ResidentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FoodPreferenceService {

    private final FoodPreferenceRepository foodPreferenceRepository;
    private final ResidentRepository residentRepository;

    public FoodPreferenceService(FoodPreferenceRepository foodPreferenceRepository, ResidentRepository residentRepository) {
        this.foodPreferenceRepository = foodPreferenceRepository;
        this.residentRepository = residentRepository;
    }

    public FoodPreferenceResponseDto createOrUpdatePreference(FoodPreferenceRequestDto dto) {
        Resident resident = residentRepository.findById(dto.getResidentId())
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        LocalDate date = dto.getDate() != null ? dto.getDate() : LocalDate.now();

        Optional<FoodPreference> existing = foodPreferenceRepository.findByResidentIdAndDate(dto.getResidentId(), date);

        FoodPreference preference;
        if (existing.isPresent()) {
            preference = existing.get();
        } else {
            preference = new FoodPreference();
            preference.setResident(resident);
            preference.setDate(date);
        }

        if (dto.getBreakfast() != null) preference.setBreakfast(dto.getBreakfast());
        if (dto.getLunch() != null) preference.setLunch(dto.getLunch());
        if (dto.getDinner() != null) preference.setDinner(dto.getDinner());
        if (dto.getMealType() != null) preference.setMealType(dto.getMealType());
        if (dto.getSpecialRequirements() != null) preference.setSpecialRequirements(dto.getSpecialRequirements());

        return mapToResponseDto(foodPreferenceRepository.save(preference));
    }

    public void deletePreference(Long id) {
        foodPreferenceRepository.deleteById(id);
    }

    public List<FoodPreferenceResponseDto> getAllPreferences() {
        return foodPreferenceRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<FoodPreferenceResponseDto> getPreferencesByDate(LocalDate date) {
        return foodPreferenceRepository.findByDate(date).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<FoodPreferenceResponseDto> getPreferencesByResident(Long residentId) {
        return foodPreferenceRepository.findByResidentId(residentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public FoodPreferenceResponseDto getTodayPreference(Long residentId) {
        Optional<FoodPreference> preference = foodPreferenceRepository.findByResidentIdAndDate(residentId, LocalDate.now());
        return preference.map(this::mapToResponseDto).orElse(null);
    }

    public Map<String, Object> getFoodStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        List<FoodPreference> todayPreferences = foodPreferenceRepository.findByDate(today);

        long breakfastCount = todayPreferences.stream().filter(p -> Boolean.TRUE.equals(p.getBreakfast())).count();
        long lunchCount = todayPreferences.stream().filter(p -> Boolean.TRUE.equals(p.getLunch())).count();
        long dinnerCount = todayPreferences.stream().filter(p -> Boolean.TRUE.equals(p.getDinner())).count();
        long vegCount = todayPreferences.stream().filter(p -> "VEG".equals(p.getMealType())).count();
        long nonVegCount = todayPreferences.stream().filter(p -> "NON_VEG".equals(p.getMealType())).count();

        stats.put("date", today);
        stats.put("breakfast", breakfastCount);
        stats.put("lunch", lunchCount);
        stats.put("dinner", dinnerCount);
        stats.put("veg", vegCount);
        stats.put("nonVeg", nonVegCount);
        stats.put("total", todayPreferences.size());

        return stats;
    }

    private FoodPreferenceResponseDto mapToResponseDto(FoodPreference preference) {
        FoodPreferenceResponseDto dto = new FoodPreferenceResponseDto();
        dto.setId(preference.getId());
        dto.setResidentId(preference.getResident().getId());
        dto.setResidentName(preference.getResident().getName());
        dto.setRoomNumber(preference.getResident().getRoom() != null ?
                preference.getResident().getRoom().getRoomNumber() : null);
        dto.setDate(preference.getDate());
        dto.setBreakfast(preference.getBreakfast());
        dto.setLunch(preference.getLunch());
        dto.setDinner(preference.getDinner());
        dto.setMealType(preference.getMealType());
        dto.setSpecialRequirements(preference.getSpecialRequirements());
        return dto;
    }
}
