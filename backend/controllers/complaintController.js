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

module.exports = {
    createComplaint,
};
