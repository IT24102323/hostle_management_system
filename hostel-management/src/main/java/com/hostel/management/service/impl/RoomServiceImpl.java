package com.hostel.management.service.impl;

import com.hostel.management.dto.room.RoomRequestDto;
import com.hostel.management.dto.room.RoomResponseDto;
import com.hostel.management.entity.Room;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RoomService} containing all room business logic.
 */
@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomResponseDto createRoom(RoomRequestDto dto) {
        if (roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new BadRequestException("Room with number '" + dto.getRoomNumber() + "' already exists.");
        }
        Room room = Room.builder()
                .roomNumber(dto.getRoomNumber())
                .roomType(dto.getRoomType())
                .pricePerMonth(dto.getPricePerMonth())
                .capacity(dto.getCapacity())
                .currentOccupancy(0)
                .facilities(dto.getFacilities())
                .imageUrl(dto.getImageUrl())
                .status(dto.getStatus() != null ? dto.getStatus() : RoomStatus.AVAILABLE)
                .vacancyDate(dto.getVacancyDate())
                .build();
        return toDto(roomRepository.save(room));
    }

    @Override
    public RoomResponseDto updateRoom(Long id, RoomRequestDto dto) {
        Room room = findById(id);

        // Check duplicate room number only if it changed
        if (!room.getRoomNumber().equals(dto.getRoomNumber())
                && roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            throw new BadRequestException("Room number '" + dto.getRoomNumber() + "' is already taken.");
        }

        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(dto.getRoomType());
        room.setPricePerMonth(dto.getPricePerMonth());
        room.setCapacity(dto.getCapacity());
        if (dto.getFacilities() != null) room.setFacilities(dto.getFacilities());
        if (dto.getImageUrl() != null) room.setImageUrl(dto.getImageUrl());
        if (dto.getStatus() != null) room.setStatus(dto.getStatus());
        if (dto.getVacancyDate() != null) room.setVacancyDate(dto.getVacancyDate());

        // Recalculate status based on occupancy
        refreshRoomStatus(room);
        return toDto(roomRepository.save(room));
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = findById(id);
        if (room.getCurrentOccupancy() > 0) {
            throw new BadRequestException("Cannot delete room '" + room.getRoomNumber()
                    + "' — it still has " + room.getCurrentOccupancy() + " resident(s).");
        }
        roomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(Long id) {
        return toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDto> getAllRooms(RoomStatus status, RoomType roomType) {
        List<Room> rooms;
        if (status != null && roomType != null) {
            rooms = roomRepository.findByStatusAndRoomType(status, roomType);
        } else if (status != null) {
            rooms = roomRepository.findByStatus(status);
        } else if (roomType != null) {
            rooms = roomRepository.findByRoomType(roomType);
        } else {
            rooms = roomRepository.findAll();
        }
        return rooms.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDto> getAvailableRooms() {
        return roomRepository.findRoomsWithAvailableSpots()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRoomStats() {
        return Map.of(
                "total", roomRepository.count(),
                "available", roomRepository.countByStatus(RoomStatus.AVAILABLE),
                "occupied", roomRepository.countByStatus(RoomStatus.OCCUPIED),
                "maintenance", roomRepository.countByStatus(RoomStatus.MAINTENANCE)
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    /**
     * Synchronises the room status with its current occupancy:
     * If occupancy hits capacity → OCCUPIED; if it drops to 0 → AVAILABLE.
     * MAINTENANCE is never auto-overwritten.
     */
    public void refreshRoomStatus(Room room) {
        if (room.getStatus() == RoomStatus.MAINTENANCE) return;
        if (room.getCurrentOccupancy() >= room.getCapacity()) {
            room.setStatus(RoomStatus.OCCUPIED);
        } else {
            room.setStatus(RoomStatus.AVAILABLE);
        }
    }

    private RoomResponseDto toDto(Room room) {
        List<String> facilitiesList = (room.getFacilities() != null && !room.getFacilities().isBlank())
                ? Arrays.asList(room.getFacilities().split(","))
                : List.of();

        return RoomResponseDto.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .pricePerMonth(room.getPricePerMonth())
                .capacity(room.getCapacity())
                .currentOccupancy(room.getCurrentOccupancy())
                .availableSpots(room.getCapacity() - room.getCurrentOccupancy())
                .facilities(facilitiesList)
                .imageUrl(room.getImageUrl())
                .status(room.getStatus())
                .vacancyDate(room.getVacancyDate())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
