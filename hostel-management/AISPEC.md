\# AI SYSTEM SPECIFICATION — HOSTEL MANAGEMENT SYSTEM



You are acting as a \*\*Senior Software Architect and Spring Boot Engineer\*\*.



Your task is to generate a \*\*complete production-ready backend system\*\* for a Hostel / Boarding House Management System.



---



\## TECHNOLOGY STACK



\* Java 21

\* Spring Boot 3+

\* Maven

\* Spring Web (REST APIs)

\* Spring Data JPA (Hibernate)

\* MySQL Database

\* Lombok

\* Jakarta Validation

\* DTO Pattern

\* Layered Architecture

\* RESTful API standards



---



\## ARCHITECTURE REQUIREMENTS



Follow enterprise-level clean architecture:



com.hostel.management

│

├── config

├── controller

├── service

├── service.impl

├── repository

├── entity

├── dto

├── enums

├── exception

├── response

└── util



Rules:



1\. Controllers MUST contain only API logic.

2\. Services contain business logic.

3\. Repositories handle database access.

4\. Use DTOs for requests and responses.

5\. Never expose entities directly to controllers.

6\. Use constructor injection only.

7\. Apply SOLID principles.

8\. Use transactional boundaries in service layer.

9\. Add validation annotations.

10\. Use consistent API response wrapper.



---



\## GLOBAL FEATURES (GENERATE FIRST)



Create:



\### Standard API Response



Fields:



\* success

\* message

\* data

\* timestamp



\### Global Exception Handling



Handle:



\* ResourceNotFoundException

\* BadRequestException

\* Validation errors

\* Generic server errors



\### Enum Types



Create enums where needed:



\* RoomStatus (AVAILABLE, OCCUPIED, MAINTENANCE)

\* RoomType (SINGLE, DOUBLE, SHARED)

\* PaymentStatus (PAID, PENDING, LATE)

\* ComplaintStatus (PENDING, IN\_PROGRESS, RESOLVED)



---



\## DATABASE DESIGN PRINCIPLES



\* Use UUID or Long auto-generated IDs

\* Include createdAt and updatedAt timestamps

\* Use @PrePersist and @PreUpdate

\* Proper relationships (OneToMany, ManyToOne)

\* Lazy loading by default



---



\# IMPLEMENTATION PRIORITY (VERY IMPORTANT)



You MUST implement modules in this order:



\## ✅ MODULE 1 — ROOM MANAGEMENT (FIRST \& FULLY COMPLETE)



Room Entity fields:



\* id

\* roomNumber (unique)

\* roomType

\* pricePerMonth

\* capacity

\* currentOccupancy

\* facilities

\* imageUrl

\* status

\* vacancyDate

\* createdAt

\* updatedAt



Required Business Logic:



\* CRUD operations

\* Prevent over-capacity assignments

\* Automatically update room status

\* Show available rooms only

\* Vacancy tracking

\* Search/filter rooms



Generate:



\* Entity

\* DTOs

\* Repository

\* Service Interface

\* Service Implementation

\* REST Controller



Endpoints:

POST /api/rooms

PUT /api/rooms/{id}

DELETE /api/rooms/{id}

GET /api/rooms

GET /api/rooms/{id}

GET /api/rooms/available



---



\## MODULE 2 — RESIDENT MANAGEMENT



Features:



\* Register resident

\* Update profile

\* Assign room

\* Remove resident

\* View resident history



Relationships:



\* Many residents belong to one room.



---



\## MODULE 3 — BOOKING / ROOM VISIT MANAGEMENT



Features:



\* Book room visit

\* Approve/reject booking

\* Prevent double booking

\* Track visit status



---



\## MODULE 4 — PAYMENT \& FEE MANAGEMENT



Features:



\* Monthly rent calculation

\* Payment tracking

\* Receipt generation metadata

\* Late fee handling



---



\## MODULE 5 — COMPLAINT \& MAINTENANCE MANAGEMENT



Features:



\* Submit complaint

\* Assign maintenance category

\* Track status

\* Update resolution



---



\## MODULE 6 — ATTENDANCE \& FOOD PREFERENCE



Features:



\* Daily attendance tracking

\* Food preference storage

\* Notification-ready structure



---



\## API DESIGN RULES



\* RESTful naming conventions

\* Proper HTTP status codes

\* Pagination support where applicable

\* Validation using annotations

\* Return ApiResponse wrapper always



---



\## SECURITY PREPARATION (STRUCTURE ONLY)



Prepare structure for future authentication:



\* User entity placeholder

\* Role enum (ADMIN, RESIDENT, STAFF)

\* No authentication implementation yet.



---



\## CODE QUALITY REQUIREMENTS



\* Production-ready code.

\* Avoid demo/simple examples.

\* Use meaningful naming.

\* Include JavaDocs for services.

\* Clean readable formatting.



---



\## FINAL OBJECTIVE



Generate a complete backend system ready to connect with an HTML/CSS/JS frontend.



Room Management must be fully functional before generating other modules.



Always reuse existing patterns to maintain architectural consistency.



