const express = require('express');
const router = express.Router();
const {
    createComplaint,
    getAllComplaints,
    getComplaintById,
    updateComplaint,
    deleteComplaint,
} = require('../controllers/complaintController');

// GET    /api/complaints     — Get all complaints (supports ?status=, ?category=, ?priority=, ?studentId= filters)
router.get('/', getAllComplaints);

// GET    /api/complaints/:id — Get a single complaint by ID
router.get('/:id', getComplaintById);

// POST   /api/complaints     — Submit a new complaint
router.post('/', createComplaint);

// PUT    /api/complaints/:id — Update complaint status/priority/response
router.put('/:id', updateComplaint);

// DELETE /api/complaints/:id — Delete a complaint
router.delete('/:id', deleteComplaint);

module.exports = router;
