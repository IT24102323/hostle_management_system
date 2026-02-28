package com.hostel.management.service;

import com.hostel.management.dto.room.RoomRequestDto;
import com.hostel.management.dto.room.RoomResponseDto;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Room management operations.
 */
public interface RoomService {

    /** Create a new room. */
    RoomResponseDto createRoom(RoomRequestDto requestDto);

    /** Update an existing room by ID. */
    RoomResponseDto updateRoom(Long id, RoomRequestDto requestDto);

    /** Delete a room by ID. */
    void deleteRoom(Long id);

    /** Get a room by ID. */
    RoomResponseDto getRoomById(Long id);

    /** Get all rooms with optional status/type filters. */
    List<RoomResponseDto> getAllRooms(RoomStatus status, RoomType roomType);

    /** Get all rooms with available capacity. */
    List<RoomResponseDto> getAvailableRooms();

    /** Get dashboard stats (total, available, occupied, maintenance). */
    Map<String, Object> getRoomStats();
}
