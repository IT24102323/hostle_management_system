package com.hostel.management.repository;

import com.hostel.management.entity.Resident;
import com.hostel.management.entity.Room;
import com.hostel.management.enums.ResidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    List<Resident> findByNameContainingIgnoreCase(String name);

    Optional<Resident> findByNic(String nic);

    Optional<Resident> findByEmail(String email);

    boolean existsByNic(String nic);

    List<Resident> findByStatus(ResidentStatus status);

    List<Resident> findByRoom(Room room);

    List<Resident> findByRoomId(Long roomId);

    long countByStatus(ResidentStatus status);
}
