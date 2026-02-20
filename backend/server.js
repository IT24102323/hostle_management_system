const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const dotenv = require('dotenv');

// Load environment variables
dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

// MongoDB Connection
mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/hostel_management')
    .then(() => console.log('MongoDB connected successfully'))
    .catch((err) => console.error('MongoDB connection error:', err));

// Routes
const complaintRoutes = require('./routes/complaints');
app.use('/api/complaints', complaintRoutes);

// Base route
app.get('/', (req, res) => {
    res.json({
        message: 'Hostel Management - Complaint & Maintenance API',
        status: 'running',
        version: '1.0.0'
    });
});

// Start server
app.listen(PORT, () => {
    console.log(Server running on port );
});
