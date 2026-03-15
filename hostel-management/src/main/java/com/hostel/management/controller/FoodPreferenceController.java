package com.hostel.management.controller;

import com.hostel.management.dto.food.FoodPreferenceRequestDto;
import com.hostel.management.dto.food.FoodPreferenceResponseDto;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.FoodPreferenceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food")
public class FoodPreferenceController {

    private final FoodPreferenceService foodPreferenceService;

    public FoodPreferenceController(FoodPreferenceService foodPreferenceService) {
        this.foodPreferenceService = foodPreferenceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FoodPreferenceResponseDto>> createOrUpdatePreference(@RequestBody FoodPreferenceRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Food preference saved.", foodPreferenceService.createOrUpdatePreference(dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePreference(@PathVariable Long id) {
        foodPreferenceService.deletePreference(id);
        return ResponseEntity.ok(ApiResponse.success("Preference deleted."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodPreferenceResponseDto>>> getAllPreferences() {
        return ResponseEntity.ok(ApiResponse.success("Preferences retrieved.", foodPreferenceService.getAllPreferences()));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<FoodPreferenceResponseDto>>> getPreferencesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success("Preferences retrieved.", foodPreferenceService.getPreferencesByDate(date)));
    }

    @GetMapping("/resident/{residentId}")
    public ResponseEntity<ApiResponse<List<FoodPreferenceResponseDto>>> getPreferencesByResident(@PathVariable Long residentId) {
        return ResponseEntity.ok(ApiResponse.success("Preferences retrieved.", foodPreferenceService.getPreferencesByResident(residentId)));
    }

    @GetMapping("/resident/{residentId}/today")
    public ResponseEntity<ApiResponse<FoodPreferenceResponseDto>> getTodayPreference(@PathVariable Long residentId) {
        FoodPreferenceResponseDto preference = foodPreferenceService.getTodayPreference(residentId);
        if (preference != null) {
            return ResponseEntity.ok(ApiResponse.success("Preference retrieved.", preference));
        }
        return ResponseEntity.ok(ApiResponse.success("No preference set for today.", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFoodStats() {
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved.", foodPreferenceService.getFoodStats()));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<FoodPreferenceResponseDto>>> getTodayPreferences() {
        return ResponseEntity.ok(ApiResponse.success("Today's preferences.", foodPreferenceService.getPreferencesByDate(LocalDate.now())));
    }
}
