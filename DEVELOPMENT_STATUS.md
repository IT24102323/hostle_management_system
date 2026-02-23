# 📋 Hostel Management System — Development Status Report

**Project:** Complaint & Maintenance Management Module  
**Branch:** `complaint-management`  
**Date:** 2026-02-23  
**Repository:** https://github.com/IT24102323/hostle_management_system.git

---

## 📁 Folder Structure

```
hostel-management/
├── backend/
│   ├── .env                          # Environment variables (MongoDB URI, PORT)
│   ├── package.json                  # Backend dependencies & scripts
│   ├── server.js                     # Express server entry point
│   ├── controllers/
│   │   ├── complaintController.js    # Complaint CRUD logic
│   │   └── maintenanceController.js  # Maintenance CRUD logic
│   ├── models/
│   │   ├── Complaint.js              # Mongoose schema for complaints
│   │   └── MaintenanceRequest.js     # Mongoose schema for maintenance
│   └── routes/
│       ├── complaints.js             # Complaint API routes
│       └── maintenance.js            # Maintenance API routes
│
├── frontend/
│   ├── index.html                    # HTML entry (FontAwesome CDN included)
│   ├── package.json                  # Frontend dependencies & scripts
│   ├── vite.config.js                # Vite config with API proxy
│   └── src/
│       ├── main.jsx                  # React entry point with BrowserRouter
│       ├── App.jsx                   # Route definitions
│       ├── index.css                 # Global styles & design system
│       └── components/
│           ├── Layout.jsx            # Sidebar layout with navigation
│           ├── ComplaintForm.jsx     # ✅ Complaint submission form
│           ├── ComplaintForm.css     # Styles for complaint form
│           ├── ComplaintList.jsx     # ✅ Complaint list with status badges
│           └── ComplaintList.css     # Styles for complaint list
│
└── README.md
```

---

## 🗄️ Database Connection

The backend connects to **MongoDB Atlas** using Mongoose. The connection string is stored in the `.env` file.

### `.env` Configuration

```env
PORT=5000
MONGODB_URI=mongodb+srv://IT24102323:<password>@cluster0.mongodb.net/hostel_management?retryWrites=true&w=majority
```

### Connection Code — `server.js`

```javascript
const mongoose = require('mongoose');
const dotenv = require('dotenv');

dotenv.config();

// MongoDB Connection
mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/hostel_management')
    .then(() => console.log('✅ MongoDB connected successfully'))
    .catch((err) => console.error('❌ MongoDB connection error:', err));
```

**How it works:**
- `dotenv.config()` loads the `.env` file into `process.env`
- `mongoose.connect()` establishes a connection to MongoDB Atlas cloud database
- Falls back to local MongoDB (`localhost:27017`) if no URI is set
- Logs success/failure to the console

---

## 🛠️ Backend — Developed Code

### 1. Express Server Setup — `server.js`

```javascript
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const dotenv = require('dotenv');

dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());          // Allow cross-origin requests from frontend
app.use(express.json());  // Parse JSON request bodies

// Routes
const complaintRoutes = require('./routes/complaints');
const maintenanceRoutes = require('./routes/maintenance');
app.use('/api/complaints', complaintRoutes);
app.use('/api/maintenance', maintenanceRoutes);

// Base route
app.get('/', (req, res) => {
    res.json({
        message: 'Hostel Management - Complaint & Maintenance API',
        status: 'running',
        version: '1.0.0'
    });
});

app.listen(PORT, () => {
    console.log(`🚀 Server running on port ${PORT}`);
});
```

**Explanation:**
- Sets up an Express.js server on port 5000
- `cors()` middleware allows the React frontend (port 5173) to call the API
- `express.json()` parses incoming JSON POST/PUT request bodies
- Mounts complaint routes at `/api/complaints` and maintenance at `/api/maintenance`

---

### 2. Complaint Model — `models/Complaint.js`

```javascript
const mongoose = require('mongoose');

const complaintSchema = new mongoose.Schema(
    {
        studentName:  { type: String, required: [true, 'Student name is required'], trim: true },
        studentId:    { type: String, required: [true, 'Student ID is required'], trim: true },
        roomNumber:   { type: String, required: [true, 'Room number is required'], trim: true },
        category: {
            type: String,
            required: [true, 'Complaint category is required'],
            enum: {
                values: ['Noise', 'Cleanliness', 'Facilities', 'Roommate', 'Security', 'Other'],
                message: '{VALUE} is not a valid complaint category',
            },
        },
        subject:     { type: String, required: true, trim: true, maxlength: 150 },
        description: { type: String, required: true, trim: true, maxlength: 2000 },
        priority: {
            type: String,
            enum: ['Low', 'Medium', 'High', 'Urgent'],
            default: 'Medium',
        },
        status: {
            type: String,
            enum: ['Pending', 'In Progress', 'Resolved', 'Dismissed'],
            default: 'Pending',
        },
        response: { type: String, trim: true, maxlength: 2000 },
    },
    { timestamps: true }
);

module.exports = mongoose.model('Complaint', complaintSchema);
```

**Explanation:**
- Defines the data shape for complaints stored in MongoDB
- `enum` restricts values to predefined options (e.g., category can only be Noise, Cleanliness, etc.)
- `required` with custom messages provides validation feedback
- `timestamps: true` automatically creates `createdAt` and `updatedAt` fields
- `default: 'Pending'` means new complaints start with Pending status

---

### 3. Maintenance Request Model — `models/MaintenanceRequest.js`

```javascript
const mongoose = require('mongoose');

const maintenanceRequestSchema = new mongoose.Schema(
    {
        studentName:  { type: String, required: true, trim: true },
        studentId:    { type: String, required: true, trim: true },
        roomNumber:   { type: String, required: true, trim: true },
        category: {
            type: String,
            required: true,
            enum: ['Plumbing', 'Electrical', 'Furniture', 'Appliance', 'Structural', 'Other'],
        },
        issueTitle:   { type: String, required: true, trim: true, maxlength: 150 },
        description:  { type: String, required: true, trim: true, maxlength: 2000 },
        priority:     { type: String, enum: ['Low', 'Medium', 'High', 'Urgent'], default: 'Medium' },
        status:       { type: String, enum: ['Pending', 'Assigned', 'In Progress', 'Completed', 'Cancelled'], default: 'Pending' },
        assignedTo:          { type: String, trim: true },
        estimatedCompletion: { type: Date },
        completionNotes:     { type: String, trim: true, maxlength: 2000 },
    },
    { timestamps: true }
);

module.exports = mongoose.model('MaintenanceRequest', maintenanceRequestSchema);
```

**Explanation:**
- Similar structure to Complaint but with maintenance-specific fields
- `assignedTo` tracks which maintenance team/person is assigned
- `estimatedCompletion` is a Date field for expected fix date
- `completionNotes` stores notes after work is done
- Status includes `Assigned` and `Completed` (different from complaint statuses)

---

### 4. Complaint Controller — `controllers/complaintController.js`

Contains **5 CRUD functions:**

| Function | HTTP Method | Route | Description |
|----------|-------------|-------|-------------|
| `createComplaint` | POST | `/api/complaints` | Create a new complaint |
| `getAllComplaints` | GET | `/api/complaints` | Get all complaints with optional filters |
| `getComplaintById` | GET | `/api/complaints/:id` | Get single complaint by MongoDB ID |
| `updateComplaint` | PUT | `/api/complaints/:id` | Update status, priority, or response |
| `deleteComplaint` | DELETE | `/api/complaints/:id` | Remove a complaint |

**Key code patterns used:**

```javascript
// CREATE — validates fields, then saves to MongoDB
const createComplaint = async (req, res) => {
    try {
        if (!studentName || !studentId || ...) {
            return res.status(400).json({ success: false, message: '...' });
        }
        const complaint = await Complaint.create({ ...fields });
        res.status(201).json({ success: true, data: complaint });
    } catch (error) {
        // Handle Mongoose validation errors
        if (error.name === 'ValidationError') { ... }
    }
};

// READ ALL — with optional query string filters
const getAllComplaints = async (req, res) => {
    const filter = {};
    if (req.query.status) filter.status = req.query.status;
    // ... more filters
    const complaints = await Complaint.find(filter).sort({ createdAt: -1 });
    res.status(200).json({ success: true, count: complaints.length, data: complaints });
};

// UPDATE — only modifies provided fields, runs validators
const updateComplaint = async (req, res) => {
    const complaint = await Complaint.findByIdAndUpdate(
        req.params.id, updateFields,
        { new: true, runValidators: true }
    );
};

// DELETE — removes document, returns the deleted data
const deleteComplaint = async (req, res) => {
    const complaint = await Complaint.findByIdAndDelete(req.params.id);
};
```

---

### 5. Maintenance Controller — `controllers/maintenanceController.js`

Contains **5 CRUD functions** (same pattern as complaint controller):

| Function | HTTP Method | Route | Description |
|----------|-------------|-------|-------------|
| `createMaintenanceRequest` | POST | `/api/maintenance` | Create a new request |
| `getAllMaintenanceRequests` | GET | `/api/maintenance` | Get all with filters (includes `assignedTo` filter) |
| `getMaintenanceRequestById` | GET | `/api/maintenance/:id` | Get single by ID |
| `updateMaintenanceRequest` | PUT | `/api/maintenance/:id` | Update status, priority, assignedTo, etc. |
| `deleteMaintenanceRequest` | DELETE | `/api/maintenance/:id` | Remove a request |

**PUT updatable fields for maintenance:** `status`, `priority`, `assignedTo`, `estimatedCompletion`, `completionNotes`

---

### 6. Route Files

**`routes/complaints.js`** — Maps HTTP methods to controller functions:

```javascript
const express = require('express');
const router = express.Router();
const { createComplaint, getAllComplaints, getComplaintById,
        updateComplaint, deleteComplaint } = require('../controllers/complaintController');

router.get('/', getAllComplaints);
router.get('/:id', getComplaintById);
router.post('/', createComplaint);
router.put('/:id', updateComplaint);
router.delete('/:id', deleteComplaint);

module.exports = router;
```

**`routes/maintenance.js`** — Same pattern for maintenance requests.

---

## 🎨 Frontend — Developed Code

### 1. Vite Proxy — `vite.config.js`

```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:5000',
        changeOrigin: true,
      },
    },
  },
});
```

**Explanation:** Frontend runs on port 5173, backend on port 5000. The proxy forwards any `/api/*` requests from the frontend dev server to the backend, avoiding CORS issues during development.

---

### 2. App Router — `App.jsx`

```javascript
import ComplaintForm from './components/ComplaintForm'
import ComplaintList from './components/ComplaintList'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/"                element={<Dashboard />} />
        <Route path="/complaints"      element={<ComplaintList />} />
        <Route path="/complaints/new"  element={<ComplaintForm />} />
        <Route path="/complaints/:id"  element={<ComplaintDetail />} />
        <Route path="/maintenance"     element={<MaintenanceList />} />
        <Route path="/maintenance/new" element={<MaintenanceForm />} />
        <Route path="/maintenance/:id" element={<MaintenanceDetail />} />
      </Routes>
    </Layout>
  )
}
```

**Explanation:** Uses React Router v6 for client-side navigation. `ComplaintForm` and `ComplaintList` are fully built components; the rest are placeholders.

---

### 3. Sidebar Layout — `Layout.jsx`

```javascript
const navItems = [
    { path: '/',                label: 'Dashboard',     icon: 'fa-solid fa-chart-pie' },
    { path: '/complaints',      label: 'Complaints',    icon: 'fa-solid fa-clipboard-list' },
    { path: '/complaints/new',  label: 'New Complaint', icon: 'fa-solid fa-circle-plus' },
    { path: '/maintenance',     label: 'Maintenance',   icon: 'fa-solid fa-wrench' },
    { path: '/maintenance/new', label: 'New Request',   icon: 'fa-solid fa-screwdriver-wrench' },
]

// NavLink with `end` prop ensures only exact URL matches are highlighted
<NavLink to={item.path} end className={({isActive}) => `nav-link ${isActive ? 'active' : ''}`}>
```

**Explanation:** The sidebar uses `NavLink` with the `end` prop for exact route matching, preventing multiple links from showing as active simultaneously.

---

### 4. Complaint Submission Form — `ComplaintForm.jsx` ✅

A fully functional form that submits complaints to the backend API.

**Features:**
- Student info section (name, ID, room number)
- Category dropdown (Noise, Cleanliness, Facilities, Roommate, Security, Other)
- Priority radio buttons with color coding (Low=green, Medium=amber, High=orange, Urgent=red)
- Subject & description with live character counters
- Loading spinner during submission
- Success animation with auto-redirect to complaints list
- Full validation and error display
- Responsive design

**Key code:**

```javascript
const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
        await axios.post('/api/complaints', formData);
        setSuccess(true);
        setTimeout(() => navigate('/complaints'), 2000);
    } catch (err) {
        setError(err.response?.data?.message || 'Something went wrong.');
    } finally {
        setLoading(false);
    }
};
```

---

### 5. Complaints List Page — `ComplaintList.jsx` ✅

Displays all complaints as cards with color-coded status badges.

**Features:**
- Color-coded **status badges** (Pending=amber, In Progress=blue, Resolved=green, Dismissed=gray)
- Color-coded **priority badges** (Low=green, Medium=amber, High=orange, Urgent=red)
- **Category badges** with matching FontAwesome icons
- **Search bar** — filter by subject, student name, ID, or room
- **Dropdown filters** — filter by status or category (server-side query)
- **Delete button** with confirmation dialog
- **View detail** button
- **"New Complaint"** button in header
- Empty state and loading spinner
- Responsive card layout

**Key code:**

```javascript
const fetchComplaints = async () => {
    const params = new URLSearchParams();
    if (filterStatus) params.append('status', filterStatus);
    if (filterCategory) params.append('category', filterCategory);
    const res = await axios.get(`/api/complaints?${params.toString()}`);
    setComplaints(res.data.data);
};

// Status badge rendering
<span className="cl-badge cl-badge-status"
      style={{ color: status.color, background: status.bg }}>
    <i className={status.icon}></i> {complaint.status}
</span>
```

---

## 📊 Complete API Reference

### Complaint Endpoints

| Method | Endpoint | Body / Query | Description |
|--------|----------|-------------|-------------|
| `GET` | `/api/complaints` | `?status=`, `?category=`, `?priority=`, `?studentId=` | List all complaints |
| `GET` | `/api/complaints/:id` | — | Get single complaint |
| `POST` | `/api/complaints` | `{ studentName, studentId, roomNumber, category, subject, description, priority }` | Create complaint |
| `PUT` | `/api/complaints/:id` | `{ status, priority, response }` | Update complaint |
| `DELETE` | `/api/complaints/:id` | — | Delete complaint |

### Maintenance Endpoints

| Method | Endpoint | Body / Query | Description |
|--------|----------|-------------|-------------|
| `GET` | `/api/maintenance` | `?status=`, `?category=`, `?priority=`, `?studentId=`, `?assignedTo=` | List all requests |
| `GET` | `/api/maintenance/:id` | — | Get single request |
| `POST` | `/api/maintenance` | `{ studentName, studentId, roomNumber, category, issueTitle, description, priority }` | Create request |
| `PUT` | `/api/maintenance/:id` | `{ status, priority, assignedTo, estimatedCompletion, completionNotes }` | Update request |
| `DELETE` | `/api/maintenance/:id` | — | Delete request |

---

## 📦 Dependencies

### Backend (`backend/package.json`)

| Package | Version | Purpose |
|---------|---------|---------|
| express | ^4.x | Web framework for REST API |
| mongoose | ^8.x | MongoDB ODM for schema & queries |
| cors | ^2.x | Enable cross-origin requests |
| dotenv | ^16.x | Load `.env` variables |
| nodemon | ^3.x (dev) | Auto-restart server on file changes |

### Frontend (`frontend/package.json`)

| Package | Version | Purpose |
|---------|---------|---------|
| react | ^19.x | UI library |
| react-dom | ^19.x | React DOM rendering |
| react-router-dom | ^7.x | Client-side routing |
| axios | ^1.x | HTTP client for API calls |
| @vitejs/plugin-react | — | Vite React plugin |

---

## 🔄 Git Commit History

```
72e631e  fix nav active state and update placeholder text
f51125f  add complaints list page with status badges
c26f8c1  add complaint submission form component
d1eea45  add maintenance request model and CRUD endpoints
8467cc8  add PUT status update and DELETE for complaints
5dce84a  add GET all and GET by id for complaints
cc77e5d  add complaint model and POST /api/complaints endpoint
a775f6e  add complaint & maintenance module schemas
19bb073  first commit
d512fd1  init: project setup
```

---

## ✅ Development Progress Summary

| Module | Feature | Status |
|--------|---------|--------|
| **Backend — Complaints** | Mongoose model | ✅ Complete |
| | POST create | ✅ Complete & Tested |
| | GET all (with filters) | ✅ Complete & Tested |
| | GET by ID | ✅ Complete & Tested |
| | PUT update | ✅ Complete & Tested |
| | DELETE | ✅ Complete & Tested |
| **Backend — Maintenance** | Mongoose model | ✅ Complete |
| | POST create | ✅ Complete & Tested |
| | GET all (with filters) | ✅ Complete |
| | GET by ID | ✅ Complete |
| | PUT update | ✅ Complete |
| | DELETE | ✅ Complete |
| **Frontend — Complaints** | Submission form | ✅ Complete |
| | List page with badges | ✅ Complete |
| | Detail/update page | 🔲 Placeholder |
| **Frontend — Maintenance** | Submission form | 🔲 Placeholder |
| | List page | 🔲 Placeholder |
| | Detail/update page | 🔲 Placeholder |
| **Frontend — General** | Sidebar navigation | ✅ Complete |
| | Dashboard | 🔲 Placeholder |
| | Responsive layout | ✅ Complete |

---

## 🚀 How to Run

### Backend
```bash
cd backend
npm install
npm run dev    # Starts on http://localhost:5000
```

### Frontend
```bash
cd frontend
npm install
npm run dev    # Starts on http://localhost:5173
```

### Access Points
- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:5000
- **Complaints List:** http://localhost:5173/complaints
- **New Complaint Form:** http://localhost:5173/complaints/new

---

## 🔜 Upcoming Development

1. **Complaint Detail Page** — View full complaint details, update status
2. **Maintenance Form** — Submit new maintenance requests from the frontend
3. **Maintenance List Page** — Display maintenance requests with status badges
4. **Maintenance Detail Page** — View and update maintenance request status
5. **Dashboard** — Statistics overview with charts (total complaints, pending, resolved, etc.)
6. **Authentication** — JWT-based login for students and admin
