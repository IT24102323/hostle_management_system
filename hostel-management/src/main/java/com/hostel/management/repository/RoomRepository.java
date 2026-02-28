package com.hostel.management.repository;

import com.hostel.management.entity.Room;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByStatus(RoomStatus status);

    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByStatusAndRoomType(RoomStatus status, RoomType roomType);

    @Query("SELECT r FROM Room r WHERE r.currentOccupancy < r.capacity")
    List<Room> findRoomsWithAvailableSpots();

    long countByStatus(RoomStatus status);
}
