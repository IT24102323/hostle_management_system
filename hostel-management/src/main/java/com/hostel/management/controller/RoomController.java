package com.hostel.management.controller;

import com.hostel.management.dto.room.RoomRequestDto;
import com.hostel.management.dto.room.RoomResponseDto;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponseDto>> createRoom(
            @Valid @RequestBody RoomRequestDto requestDto) {
        RoomResponseDto room = roomService.createRoom(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully.", room));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequestDto requestDto) {
        RoomResponseDto room = roomService.updateRoom(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully.", room));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDto>> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Room retrieved successfully.", roomService.getRoomById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponseDto>>> getAllRooms(
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) RoomType roomType) {
        List<RoomResponseDto> rooms = roomService.getAllRooms(status, roomType);
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully.", rooms));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomResponseDto>>> getAvailableRooms() {
        List<RoomResponseDto> rooms = roomService.getAvailableRooms();
        return ResponseEntity.ok(ApiResponse.success("Available rooms retrieved successfully.", rooms));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomStats() {
        return ResponseEntity.ok(ApiResponse.success("Room stats retrieved.", roomService.getRoomStats()));
    }
}
