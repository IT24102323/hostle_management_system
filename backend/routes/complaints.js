const express = require('express');
const router = express.Router();
const {
    createComplaint,
    getAllComplaints,
    getComplaintById,
} = require('../controllers/complaintController');

// GET  /api/complaints     — Get all complaints (supports ?status=, ?category=, ?priority=, ?studentId= filters)
router.get('/', getAllComplaints);

// GET  /api/complaints/:id — Get a single complaint by ID
router.get('/:id', getComplaintById);

// POST /api/complaints     — Submit a new complaint
router.post('/', createComplaint);

module.exports = router;
