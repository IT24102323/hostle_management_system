# Hostel Management System - Complete Full Stack Application

## 🎯 Project Overview

A complete enterprise-level Hostel Management System with Spring Boot backend and responsive HTML/CSS/JavaScript frontend.

## 🏗️ Architecture

### Backend (Spring Boot 3.4.2 + Java 21)
- **Package**: `com.hostel.management`
- **Architecture**: Layered (Controller → Service → Repository → Entity)
- **Database**: MySQL
- **API**: RESTful with consistent ApiResponse wrapper
- **Features**: 
  - Global exception handling
  - DTO pattern for request/response
  - Enum-based status management
  - Proper entity relationships with JPA

### Frontend (HTML/CSS/JavaScript)
- **Pages**: Login, Register, Admin Dashboard, Resident Dashboard
- **Features**:
  - Responsive design
  - Real-time API integration
  - Session management with localStorage
  - Interactive CRUD operations

## 📦 What's Included

### 🎨 Frontend Pages
1. **login.html** - Login page with role selection (Admin/Resident)
2. **register.html** - New resident registration form
3. **dashboard.html** - Complete admin dashboard with all modules
4. **resident-dashboard.html** - Resident portal for viewing info
5. **dashboard.js** - Full JavaScript logic for API integration
6. **index.html** - Landing page (redirects to login)

### 🚀 Backend Modules

#### 1. Room Management
- CRUD operations
- Capacity tracking
- Automatic status updates (AVAILABLE/OCCUPIED/MAINTENANCE)
- Vacancy tracking
- Filter by type and status

#### 2. Resident Management
- Register/Update/Delete residents
- Room assignment with capacity validation
- Status tracking (ACTIVE/PENDING/INACTIVE)
- Profile management

#### 3. Payment Management
- Create monthly payments
- Auto-calculate totals (rent + food + late fees)
- Mark as paid
- Payment history
- Multiple payment methods

#### 4. Complaint Management
- Submit complaints
- Priority levels (LOW/MEDIUM/HIGH)
- Status workflow (PENDING → IN_PROGRESS → RESOLVED)
- Resolution tracking

#### 5. Visit/Booking Management
- Room visit requests
- Status tracking (NEW → CONTACTED → CLOSED)
- Admin notes

#### 6. Cleaning Schedule
- Task assignment
- Day/time scheduling
- Staff assignment
- Status tracking

## 🛠️ Setup Instructions

### Prerequisites
- Java 21 (JDK)
- MySQL 8.0+
- Maven (included as wrapper)
- Modern web browser

### Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE amber_lodge;
```

2. Update database credentials in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/amber_lodge?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### Backend Setup

1. Navigate to project directory:
```bash
cd D:\Hostel_Management\hostel-management\hostel-management
```

2. Build the project:
```bash
mvnw.cmd clean package
```

3. Run the application:
```bash
mvnw.cmd spring-boot:run
```

Or run the JAR:
```bash
java -jar target\hostel-management-0.0.1-SNAPSHOT.jar
```

The backend will start on `http://localhost:8080`

### Frontend Setup

No additional setup needed! The frontend files are in `src/main/resources/static/`

Access the application at: `http://localhost:8080`

## 🎮 How to Use

### Demo Credentials

**Admin Login:**
- Username: `admin`
- Password: `admin123`

**Resident Login:**
- Username: `resident`
- Password: `resident123`

### First Time Setup

1. Start the backend server
2. The database tables will be created automatically (JPA auto-create)
3. Demo data will be seeded automatically (8 rooms, 4 residents, payments, complaints, visits, cleaning tasks)
4. Open browser and go to `http://localhost:8080`
5. Login with demo credentials

### Admin Dashboard Features

**Navigation Menu:**
- Dashboard - Statistics overview
- Rooms - Add/Edit/Delete rooms
- Residents - Manage resident profiles and room assignments
- Payments - Create payments, mark as paid, view history
- Complaints - View and resolve complaints
- Visits - Manage room visit requests
- Cleaning Schedule - Assign cleaning tasks

**Dashboard Operations:**
- View real-time stats (rooms, residents, payments, complaints)
- Add new records with modal forms
- Edit existing records
- Delete records with confirmation
- Update statuses (mark payment as paid, resolve complaints, etc.)

### Resident Dashboard Features

- View profile information
- See assigned room details
- Check payment history
- View submitted complaints

## 📡 API Endpoints

### Rooms
- `GET /api/rooms` - Get all rooms
- `GET /api/rooms/{id}` - Get room by ID
- `GET /api/rooms/available` - Get available rooms
- `GET /api/rooms/stats` - Get room statistics
- `POST /api/rooms` - Create new room
- `PUT /api/rooms/{id}` - Update room
- `DELETE /api/rooms/{id}` - Delete room

### Residents
- `GET /api/residents` - Get all residents
- `GET /api/residents/{id}` - Get resident by ID
- `GET /api/residents/stats` - Get resident statistics
- `POST /api/residents` - Create new resident
- `PUT /api/residents/{id}` - Update resident
- `PUT /api/residents/{id}/assign-room/{roomId}` - Assign room
- `PUT /api/residents/{id}/remove-from-room` - Remove from room
- `DELETE /api/residents/{id}` - Delete resident

### Payments
- `GET /api/payments` - Get all payments
- `GET /api/payments/{id}` - Get payment by ID
- `GET /api/payments?residentId={id}` - Get payments by resident
- `GET /api/payments/stats` - Get payment statistics
- `POST /api/payments` - Create new payment
- `PUT /api/payments/{id}` - Update payment
- `PUT /api/payments/{id}/pay` - Mark as paid
- `POST /api/payments/pay-all` - Pay all pending for a resident
- `DELETE /api/payments/{id}` - Delete payment

### Complaints
- `GET /api/complaints` - Get all complaints
- `GET /api/complaints/{id}` - Get complaint by ID
- `GET /api/complaints?residentId={id}` - Get complaints by resident
- `GET /api/complaints/stats` - Get complaint statistics
- `POST /api/complaints` - Create new complaint
- `PUT /api/complaints/{id}` - Update complaint
- `PUT /api/complaints/{id}/status` - Update status
- `DELETE /api/complaints/{id}` - Delete complaint

### Visits
- `GET /api/visits` - Get all visits
- `GET /api/visits/{id}` - Get visit by ID
- `GET /api/visits/stats` - Get visit statistics
- `POST /api/visits` - Create new visit
- `PUT /api/visits/{id}` - Update visit
- `PUT /api/visits/{id}/status` - Update status
- `DELETE /api/visits/{id}` - Delete visit

### Cleaning
- `GET /api/cleaning` - Get all cleaning tasks
- `GET /api/cleaning/{id}` - Get task by ID
- `POST /api/cleaning` - Create new task
- `PUT /api/cleaning/{id}` - Update task
- `DELETE /api/cleaning/{id}` - Delete task

## 🎨 Frontend Features

### Responsive Design
- Works on desktop, tablet, and mobile
- Modern gradient UI
- Smooth animations
- Interactive modals

### User Experience
- Real-time data loading
- Loading states with spinners
- Success/error alerts
- Confirmation dialogs for deletions
- Form validation

### Session Management
- LocalStorage-based authentication
- Role-based access control
- Automatic redirect if not logged in
- Logout functionality

## 🔒 Security Notes

⚠️ **Important**: This is a demo application without real authentication/authorization!

For production use, implement:
- Spring Security
- JWT tokens
- Password hashing (BCrypt)
- CSRF protection
- Input validation
- SQL injection prevention
- XSS protection

## 📁 Project Structure

```
hostel-management/
├── src/
│   ├── main/
│   │   ├── java/com/hostel/management/
│   │   │   ├── HostelManagementApplication.java
│   │   │   ├── config/
│   │   │   │   ├── WebConfig.java
│   │   │   │   └── DataSeeder.java
│   │   │   ├── controller/
│   │   │   │   ├── RoomController.java
│   │   │   │   ├── ResidentController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── ComplaintController.java
│   │   │   │   ├── VisitController.java
│   │   │   │   └── CleaningController.java
│   │   │   ├── service/
│   │   │   │   └── (6 service interfaces)
│   │   │   ├── service/impl/
│   │   │   │   └── (6 service implementations)
│   │   │   ├── entity/
│   │   │   │   └── (6 entity classes)
│   │   │   ├── repository/
│   │   │   │   └── (6 JPA repositories)
│   │   │   ├── dto/
│   │   │   │   └── (12 DTO classes - Request/Response per module)
│   │   │   ├── enums/
│   │   │   │   └── (9 enum classes)
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BadRequestException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── response/
│   │   │   │   └── ApiResponse.java
│   │   │   └── util/
│   │   │       └── DateUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── dashboard.html
│   │           ├── dashboard.js
│   │           └── resident-dashboard.html
│   └── test/
├── pom.xml
└── README.md
```

## 🐛 Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```properties
# Change port in application.properties
server.port=8081
```

**Database connection error:**
- Verify MySQL is running
- Check database credentials
- Ensure database exists

**Build errors:**
- Ensure Java 21 is installed: `java -version`
- Clean and rebuild: `mvnw.cmd clean install`

### Frontend Issues

**API calls failing:**
- Check backend is running on port 8080
- Open browser console (F12) to see errors
- Verify CORS is enabled (already configured in WebConfig.java)

**Login not working:**
- Use exact demo credentials (case-sensitive)
- Check browser console for JavaScript errors

## 🚀 Production Deployment

### Backend
1. Build production JAR: `mvnw.cmd clean package -DskipTests`
2. Deploy JAR to server
3. Configure production database
4. Set environment variables
5. Use reverse proxy (nginx/Apache)
6. Enable HTTPS

### Frontend
- Already bundled with Spring Boot
- Served from `/src/main/resources/static/`
- No separate deployment needed

## 📝 Future Enhancements

- [ ] Real authentication with Spring Security
- [ ] Role-based permissions
- [ ] Email notifications
- [ ] PDF receipt generation
- [ ] Advanced search and filters
- [ ] Dashboard charts and analytics
- [ ] File upload for profile pictures
- [ ] Messaging system
- [ ] Mobile app (React Native/Flutter)

## 📄 License

This is a demo project for educational purposes.

## 👨‍💻 Developer

Built with ❤️ using Spring Boot 3 and modern web technologies.

---

**Happy Coding! 🎉**
