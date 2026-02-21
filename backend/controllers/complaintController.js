const Complaint = require('../models/Complaint');

// @desc    Create a new complaint
// @route   POST /api/complaints
// @access  Public
const createComplaint = async (req, res) => {
    try {
        const {
            studentName,
            studentId,
            roomNumber,
            category,
            subject,
            description,
            priority,
        } = req.body;

        // Validate required fields
        if (!studentName || !studentId || !roomNumber || !category || !subject || !description) {
            return res.status(400).json({
                success: false,
                message: 'Please provide all required fields: studentName, studentId, roomNumber, category, subject, description',
            });
        }

        // Create complaint
        const complaint = await Complaint.create({
            studentName,
            studentId,
            roomNumber,
            category,
            subject,
            description,
            priority: priority || 'Medium',
        });

        res.status(201).json({
            success: true,
            message: 'Complaint submitted successfully',
            data: complaint,
        });
    } catch (error) {
        // Handle Mongoose validation errors
        if (error.name === 'ValidationError') {
            const messages = Object.values(error.errors).map((err) => err.message);
            return res.status(400).json({
                success: false,
                message: 'Validation failed',
                errors: messages,
            });
        }

        console.error('Error creating complaint:', error);
        res.status(500).json({
            success: false,
            message: 'Server error. Could not create complaint.',
        });
    }
};

// @desc    Get all complaints
// @route   GET /api/complaints
// @access  Public
const getAllComplaints = async (req, res) => {
    try {
        // Optional query filters
        const filter = {};
        if (req.query.status) filter.status = req.query.status;
        if (req.query.category) filter.category = req.query.category;
        if (req.query.priority) filter.priority = req.query.priority;
        if (req.query.studentId) filter.studentId = req.query.studentId;

        const complaints = await Complaint.find(filter).sort({ createdAt: -1 });

        res.status(200).json({
            success: true,
            count: complaints.length,
            data: complaints,
        });
    } catch (error) {
        console.error('Error fetching complaints:', error);
        res.status(500).json({
            success: false,
            message: 'Server error. Could not fetch complaints.',
        });
    }
};

// @desc    Get a single complaint by ID
// @route   GET /api/complaints/:id
// @access  Public
const getComplaintById = async (req, res) => {
    try {
        const complaint = await Complaint.findById(req.params.id);

        if (!complaint) {
            return res.status(404).json({
                success: false,
                message: 'Complaint not found',
            });
        }

        res.status(200).json({
            success: true,
            data: complaint,
        });
    } catch (error) {
        // Handle invalid ObjectId format
        if (error.kind === 'ObjectId' || error.name === 'CastError') {
            return res.status(400).json({
                success: false,
                message: 'Invalid complaint ID format',
            });
        }

        console.error('Error fetching complaint:', error);
        res.status(500).json({
            success: false,
            message: 'Server error. Could not fetch complaint.',
        });
    }
};

module.exports = {
    createComplaint,
    getAllComplaints,
    getComplaintById,
};
