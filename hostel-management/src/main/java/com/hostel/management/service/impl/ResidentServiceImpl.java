package com.hostel.management.service.impl;

import com.hostel.management.dto.resident.ResidentRequestDto;
import com.hostel.management.dto.resident.ResidentResponseDto;
import com.hostel.management.entity.Resident;
import com.hostel.management.entity.Room;
import com.hostel.management.enums.ResidentStatus;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.ResidentRepository;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.service.ResidentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ResidentService} containing all resident business logic.
 */
@Service
@Transactional
public class ResidentServiceImpl implements ResidentService {

    private final ResidentRepository residentRepository;
    private final RoomRepository roomRepository;
    private final RoomServiceImpl roomServiceImpl;

    public ResidentServiceImpl(ResidentRepository residentRepository,
                               RoomRepository roomRepository,
                               RoomServiceImpl roomServiceImpl) {
        this.residentRepository = residentRepository;
        this.roomRepository = roomRepository;
        this.roomServiceImpl = roomServiceImpl;
    }

    @Override
    public ResidentResponseDto createResident(ResidentRequestDto dto) {
        if (dto.getNic() != null && residentRepository.existsByNic(dto.getNic())) {
            throw new BadRequestException("A resident with NIC '" + dto.getNic() + "' already exists.");
        }

        Resident resident = Resident.builder()
                .name(dto.getName())
                .nic(dto.getNic())
                .contact(dto.getContact())
                .email(dto.getEmail())
                .course(dto.getCourse())
                .rating(dto.getRating() != null ? dto.getRating() : 3)
                .joinDate(dto.getJoinDate() != null ? dto.getJoinDate() : LocalDate.now())
                .status(dto.getStatus() != null ? dto.getStatus() : ResidentStatus.ACTIVE)
                .build();

        if (dto.getRoomId() != null) {
            Room room = findRoomById(dto.getRoomId());
            validateRoomCapacity(room);
            resident.setRoom(room);
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
            roomServiceImpl.refreshRoomStatus(room);
            roomRepository.save(room);
        }

        return toDto(residentRepository.save(resident));
    }

    @Override
    public ResidentResponseDto updateResident(Long id, ResidentRequestDto dto) {
        Resident resident = findById(id);

        if (dto.getNic() != null && !dto.getNic().equals(resident.getNic())
                && residentRepository.existsByNic(dto.getNic())) {
            throw new BadRequestException("A resident with NIC '" + dto.getNic() + "' already exists.");
        }

        if (dto.getName() != null) resident.setName(dto.getName());
        if (dto.getNic() != null) resident.setNic(dto.getNic());
        if (dto.getContact() != null) resident.setContact(dto.getContact());
        if (dto.getEmail() != null) resident.setEmail(dto.getEmail());
        if (dto.getCourse() != null) resident.setCourse(dto.getCourse());
        if (dto.getRating() != null) resident.setRating(dto.getRating());
        if (dto.getJoinDate() != null) resident.setJoinDate(dto.getJoinDate());
        if (dto.getStatus() != null) resident.setStatus(dto.getStatus());

        // Handle room re-assignment
        if (dto.getRoomId() != null) {
            Long currentRoomId = resident.getRoom() != null ? resident.getRoom().getId() : null;
            if (!dto.getRoomId().equals(currentRoomId)) {
                // Leave old room
                if (currentRoomId != null) {
                    decrementRoomOccupancy(resident.getRoom());
                }
                // Join new room
                Room newRoom = findRoomById(dto.getRoomId());
                validateRoomCapacity(newRoom);
                resident.setRoom(newRoom);
                newRoom.setCurrentOccupancy(newRoom.getCurrentOccupancy() + 1);
                roomServiceImpl.refreshRoomStatus(newRoom);
                roomRepository.save(newRoom);
            }
        }

        return toDto(residentRepository.save(resident));
    }

    @Override
    public void deleteResident(Long id) {
        Resident resident = findById(id);
        if (resident.getRoom() != null) {
            decrementRoomOccupancy(resident.getRoom());
        }
        residentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ResidentResponseDto getResidentById(Long id) {
        return toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentResponseDto> getAllResidents(String name, ResidentStatus status) {
        List<Resident> residents;
        if (name != null && !name.isBlank()) {
            residents = residentRepository.findByNameContainingIgnoreCase(name);
        } else if (status != null) {
            residents = residentRepository.findByStatus(status);
        } else {
            residents = residentRepository.findAll();
        }
        return residents.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ResidentResponseDto assignRoom(Long residentId, Long roomId) {
        Resident resident = findById(residentId);
        Room newRoom = findRoomById(roomId);

        // Leave current room
        if (resident.getRoom() != null) {
            decrementRoomOccupancy(resident.getRoom());
        }

        validateRoomCapacity(newRoom);
        resident.setRoom(newRoom);
        newRoom.setCurrentOccupancy(newRoom.getCurrentOccupancy() + 1);
        roomServiceImpl.refreshRoomStatus(newRoom);
        roomRepository.save(newRoom);
        return toDto(residentRepository.save(resident));
    }

    @Override
    public ResidentResponseDto removeFromRoom(Long residentId) {
        Resident resident = findById(residentId);
        if (resident.getRoom() == null) {
            throw new BadRequestException("Resident is not assigned to any room.");
        }
        decrementRoomOccupancy(resident.getRoom());
        resident.setRoom(null);
        return toDto(residentRepository.save(resident));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getResidentStats() {
        return Map.of(
                "total", residentRepository.count(),
                "active", residentRepository.countByStatus(ResidentStatus.ACTIVE),
                "pending", residentRepository.countByStatus(ResidentStatus.PENDING),
                "inactive", residentRepository.countByStatus(ResidentStatus.INACTIVE)
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Resident findById(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", id));
    }

    private Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
    }

    private void validateRoomCapacity(Room room) {
        if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new BadRequestException("Room '" + room.getRoomNumber() + "' is under maintenance.");
        }
        if (room.getCurrentOccupancy() >= room.getCapacity()) {
            throw new BadRequestException("Room '" + room.getRoomNumber() + "' is at full capacity.");
        }
    }

    private void decrementRoomOccupancy(Room room) {
        int updated = Math.max(0, room.getCurrentOccupancy() - 1);
        room.setCurrentOccupancy(updated);
        roomServiceImpl.refreshRoomStatus(room);
        roomRepository.save(room);
    }

    private ResidentResponseDto toDto(Resident r) {
        return ResidentResponseDto.builder()
                .id(r.getId())
                .name(r.getName())
                .nic(r.getNic())
                .contact(r.getContact())
                .email(r.getEmail())
                .course(r.getCourse())
                .rating(r.getRating())
                .joinDate(r.getJoinDate())
                .leaveDate(r.getLeaveDate())
                .status(r.getStatus())
                .roomId(r.getRoom() != null ? r.getRoom().getId() : null)
                .roomNumber(r.getRoom() != null ? r.getRoom().getRoomNumber() : null)
                .roomType(r.getRoom() != null ? r.getRoom().getRoomType().name() : null)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
