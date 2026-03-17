package com.hostel.management.config;

import com.hostel.management.entity.*;
import com.hostel.management.enums.*;
import com.hostel.management.repository.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds default demo data into the database on first startup.
 * Skipped automatically if rooms table already has data.
 */
@Configuration
public class DataSeeder {

    @Bean
    ApplicationRunner seedDatabase(
            RoomRepository rooms,
            ResidentRepository residents,
            PaymentRepository payments,
            CleaningTaskRepository cleaning,
            VisitRepository visits,
            ComplaintRepository complaints,
            UserRepository users,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (rooms.count() > 0 || users.count() > 0) return;

            System.out.println("🌱 Seeding default data...");

            // ── Rooms ─────────────────────────────────────────────────────────
            String[][] roomData = {
                { "101", "SINGLE",  "45000", "2",  "AC,WiFi,Attached Bath,TV",
                  "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=500" },
                { "102", "DOUBLE",  "25000", "2",  "WiFi,Balcony,Study Table",
                  "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=500" },
                { "103", "SHARED",  "15000", "6",  "WiFi,Common Area,Lockers",
                  "https://images.unsplash.com/photo-1522771753062-58877370a271?w=500" },
                { "104", "SINGLE",  "50000", "1",  "AC,Private Garden,WiFi",
                  "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?w=500" },
                { "105", "DOUBLE",  "35000", "2",  "Kitchenette,WiFi,AC",
                  "https://images.unsplash.com/photo-1505693314120-0d443867891c?w=500" },
                { "106", "SHARED",  "12000", "4",  "WiFi,Fan,Study Table",
                  "https://images.unsplash.com/photo-1522771753062-58877370a271?w=500" },
                { "107", "SINGLE",  "48000", "1",  "AC,TV,WiFi,Attached Bath",
                  "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=500" },
                { "108", "DOUBLE",  "28000", "2",  "WiFi,Study Table,Fan",
                  "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=500" },
            };

            RoomStatus[] statuses = {
                RoomStatus.AVAILABLE, RoomStatus.AVAILABLE, RoomStatus.MAINTENANCE,
                RoomStatus.AVAILABLE, RoomStatus.AVAILABLE, RoomStatus.AVAILABLE,
                RoomStatus.AVAILABLE, RoomStatus.AVAILABLE
            };

            Room[] savedRooms = new Room[roomData.length];
            for (int i = 0; i < roomData.length; i++) {
                String[] d = roomData[i];
                Room room = Room.builder()
                        .roomNumber(d[0])
                        .roomType(RoomType.valueOf(d[1]))
                        .pricePerMonth(Double.parseDouble(d[2]))
                        .capacity(Integer.parseInt(d[3]))
                        .currentOccupancy(0)
                        .facilities(d[4])
                        .imageUrl(d[5])
                        .status(statuses[i])
                        .build();
                savedRooms[i] = rooms.save(room);
            }

            // ── Residents ─────────────────────────────────────────────────────
            Resident r1 = residents.save(Resident.builder()
                    .name("Alice Walker").nic("987654321V")
                    .contact("0771234567").email("alice@example.com").course("Computing")
                    .rating(5).joinDate(LocalDate.of(2025, 1, 15))
                    .status(ResidentStatus.ACTIVE).room(savedRooms[0]).build());
            savedRooms[0].setCurrentOccupancy(1);
            savedRooms[0].setStatus(RoomStatus.AVAILABLE);
            rooms.save(savedRooms[0]);

            Resident r2 = residents.save(Resident.builder()
                    .name("Bob Martin").nic("123456789V")
                    .contact("0719876543").email("bob@example.com").course("Engineering")
                    .rating(4).joinDate(LocalDate.of(2025, 2, 1))
                    .status(ResidentStatus.ACTIVE).room(savedRooms[1]).build());
            savedRooms[1].setCurrentOccupancy(1);
            savedRooms[1].setStatus(RoomStatus.AVAILABLE);
            rooms.save(savedRooms[1]);

            residents.save(Resident.builder()
                    .name("Charlie Davis").nic("456789123V")
                    .contact("0751122334").email("charlie@example.com").course("Business")
                    .rating(3).joinDate(LocalDate.of(2025, 2, 10))
                    .status(ResidentStatus.ACTIVE).room(savedRooms[4]).build());
            savedRooms[4].setCurrentOccupancy(1);
            rooms.save(savedRooms[4]);

            residents.save(Resident.builder()
                    .name("Diana Prince").nic("789123456V")
                    .contact("0705566778").email("diana@example.com").course("Arts")
                    .rating(5).joinDate(LocalDate.of(2025, 1, 20))
                    .status(ResidentStatus.PENDING).build());

            // ── Payments ──────────────────────────────────────────────────────
            payments.save(Payment.builder()
                    .resident(r1).month("January 2025")
                    .amount(45000.0).foodCharge(5000.0).lateFee(0.0).total(50000.0)
                    .status(PaymentStatus.PAID).method(PaymentMethod.BANK_TRANSFER)
                    .paymentDate(LocalDate.of(2025, 1, 5))
                    .paidDate(LocalDate.of(2025, 1, 5)).build());

            payments.save(Payment.builder()
                    .resident(r2).month("February 2025")
                    .amount(25000.0).foodCharge(4000.0).lateFee(0.0).total(29000.0)
                    .status(PaymentStatus.PENDING).method(PaymentMethod.CASH)
                    .paymentDate(LocalDate.of(2025, 2, 1)).build());

            payments.save(Payment.builder()
                    .resident(r1).month("February 2025")
                    .amount(45000.0).foodCharge(5000.0).lateFee(2000.0).total(52000.0)
                    .status(PaymentStatus.LATE).method(PaymentMethod.CASH)
                    .paymentDate(LocalDate.of(2025, 2, 1)).build());

            // ── Complaints ────────────────────────────────────────────────────
            complaints.save(Complaint.builder()
                    .resident(r1).title("AC not working")
                    .description("The air conditioner in room 101 stopped working.")
                    .category("Electrical").priority(ComplaintPriority.HIGH)
                    .status(ComplaintStatus.IN_PROGRESS)
                    .complaintDate(LocalDate.now().minusDays(3)).build());

            complaints.save(Complaint.builder()
                    .resident(r2).title("Leaking tap")
                    .description("Bathroom tap is leaking continuously.")
                    .category("Plumbing").priority(ComplaintPriority.MEDIUM)
                    .status(ComplaintStatus.PENDING)
                    .complaintDate(LocalDate.now().minusDays(1)).build());

            // ── Visits ────────────────────────────────────────────────────────
            visits.save(Visit.builder()
                    .visitorName("John Doe")
                    .visitorContact("0771112222")
                    .visitorEmail("john@example.com")
                    .preferredRoomType("SINGLE")
                    .message("Interested in a single room for 6 months.")
                    .visitDate(LocalDate.now().plusDays(2))
                    .visitTime("10:00 AM")
                    .status(VisitStatus.NEW).build());

            visits.save(Visit.builder()
                    .visitorName("Sarah Lee")
                    .visitorContact("0773334444")
                    .visitorEmail("sarah@example.com")
                    .preferredRoomType("DOUBLE")
                    .message("Looking for a double room.")
                    .visitDate(LocalDate.now().plusDays(5))
                    .visitTime("02:00 PM")
                    .status(VisitStatus.CONTACTED).build());

            // ── Cleaning Tasks ────────────────────────────────────────────────
            List<String[]> tasks = List.of(
                new String[]{"Common Area", "Monday",    "08:00 AM", "Kamal"},
                new String[]{"Kitchen",     "Monday",    "09:00 AM", "Nimal"},
                new String[]{"Corridors",   "Wednesday", "08:00 AM", "Kamal"},
                new String[]{"Bathrooms",   "Wednesday", "10:00 AM", "Saman"},
                new String[]{"Common Area", "Friday",    "08:00 AM", "Nimal"},
                new String[]{"Roof",        "Saturday",  "07:00 AM", "Saman"}
            );
            tasks.forEach(t -> cleaning.save(CleaningTask.builder()
                    .area(t[0]).dayOfWeek(t[1]).timeSlot(t[2])
                    .assignedStaff(t[3]).completionStatus("Pending").build()));

            // ── Users (auth with JWT) ─────────────────────────────────────────
            String defaultPassword = passwordEncoder.encode("resident123");
            String adminPassword = passwordEncoder.encode("admin123");

            users.save(User.builder()
                    .email("admin@hostelhub.com")
                    .passwordHash(adminPassword)
                    .role(Role.ADMIN)
                    .name("Administrator")
                    .verified(true)
                    .build());

            users.save(User.builder()
                    .email("alice@example.com")
                    .passwordHash(defaultPassword)
                    .role(Role.RESIDENT)
                    .name("Alice Walker")
                    .resident(r1)
                    .verified(true)
                    .build());

            users.save(User.builder()
                    .email("bob@example.com")
                    .passwordHash(defaultPassword)
                    .role(Role.RESIDENT)
                    .name("Bob Martin")
                    .resident(r2)
                    .verified(true)
                    .build());

            residents.findByEmail("charlie@example.com").ifPresent(r3 ->
                users.save(User.builder()
                        .email("charlie@example.com")
                        .passwordHash(defaultPassword)
                        .role(Role.RESIDENT)
                        .name("Charlie Davis")
                        .resident(r3)
                        .verified(true)
                        .build()));

            residents.findByEmail("diana@example.com").ifPresent(r4 ->
                users.save(User.builder()
                        .email("diana@example.com")
                        .passwordHash(defaultPassword)
                        .role(Role.RESIDENT)
                        .name("Diana Prince")
                        .resident(r4)
                        .verified(true)
                        .build()));

            System.out.println("✅ Seeding complete.");
        };
    }

    /** Ensures admin user exists and is verified (for existing DBs). */
    @Bean
    ApplicationRunner ensureAdminUser(UserRepository users, PasswordEncoder passwordEncoder) {
        return args -> {
            users.findByEmailIgnoreCase("admin@hostelhub.com").ifPresentOrElse(
                admin -> {
                    if (admin.getVerified() == null || !admin.getVerified()) {
                        admin.setVerified(true);
                        users.save(admin);
                        System.out.println("✅ Admin user marked as verified.");
                    }
                },
                () -> {
                    users.save(User.builder()
                            .email("admin@hostelhub.com")
                            .passwordHash(passwordEncoder.encode("admin123"))
                            .role(Role.ADMIN)
                            .name("Administrator")
                            .verified(true)
                            .build());
                    System.out.println("✅ Admin user created for JWT auth.");
                }
            );
        };
    }
}
