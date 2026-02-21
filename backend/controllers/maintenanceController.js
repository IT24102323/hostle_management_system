const MaintenanceRequest = require('../models/MaintenanceRequest');

// @desc    Create a new maintenance request
// @route   POST /api/maintenance
// @access  Public
const createMaintenanceRequest = async (req, res) => {
    try {
        const {
            studentName,
            studentId,
            roomNumber,
            category,
            issueTitle,
            description,
            priority,
        } = req.body;

        // Validate required fields
        if (!studentName || !studentId || !roomNumber || !category || !issueTitle || !description) {
            return res.status(400).json({
                success: false,
                message: 'Please provide all required fields: studentName, studentId, roomNumber, category, issueTitle, description',
            });
        }

        const request = await MaintenanceRequest.create({
            studentName,
            studentId,
            roomNumber,
            category,
            issueTitle,
            description,
            priority: priority || 'Medium',
        });

        res.status(201).json({
            success: true,
            message: 'Maintenance request submitted successfully',
            data: request,
        });
    } catch (error) {
        if (error.name === 'ValidationError') {
            const messages = Object.values(error.errors).map((err) => err.message);
            return res.status(400).json({
                success: false,
                message: 'Validation failed',
                errors: messages,
            });
        }

        console.error('Error creating maintenance request:', error);
        res.status(500).json({
            success: false,
            message: 'Server error. Could not create maintenance request.',
        });
    }
};

// @desc    Get all maintenance requests
// @route   GET /api/maintenance
// @access  Public
const getAllMaintenanceRequests = async (req, res) => {
    try {
        const filter = {};
        if (req.query.status) filter.status = req.query.status;
        if (req.query.category) filter.category = req.query.category;
        if (req.query.priority) filter.priority = req.query.priority;
        if (req.query.studentId) filter.studentId = req.query.studentId;
        if (req.query.assignedTo) filter.assignedTo = req.query.assignedTo;

        const requests = await MaintenanceRequest.find(filter).sort({ createdAt: -1 });

        res.status(200).json({
            success: true,
            count: requests.length,
            data: requests,
        });
    } catch (error) {
        console.error('Error fetching maintenance requests:', error);
        res.status(500).json({
            success: false,
            message: 'Server error. Could not fetch maintenance requests.',
        });
    }
};

// @desc    Get a single maintenance request by ID
// @route   GET /api/maintenance/:id
// @access  Public
const getMaintenanceRequestById = async (req, res) => {
    try {
        const request = await MaintenanceRequest.findById(req.params.id);

        if (!request) {
            return res.status(404).json({
                success: false,
                message: 'Maintenance request not found',
            });
        }

        res.status(200).json({
            success: true,
            data: request,
        });
    } catch (error) {
        if (error.kind === 'ObjectId' || error.name === 'CastError') {
            return res.status(400).json({
                success: false,
                message: 'Invalid maintenance request ID format',
            });
        }

        console.error('Error fetching maintenance request:', error);
        res.status(500).json({
            success: false,
            message: 'Server error. Could not fetch maintenance request.',
        });
    }
};

// @desc    Update a maintenance request
// @route   PUT /api/maintenance/:id
// @access  Public
const updateMaintenanceRequest = async (req, res) => {
    try {
        const { status, priority, assignedTo, estimatedCompletion, completionNotes } = req.body;

        if (!status && !priority && !assignedTo && !estimatedCompletion && !completionNotes) {
            return res.status(400).json({
                success: false,
                message: 'Please provide at least one field to update: status, priority, assignedTo, estimatedCompletion, or completionNotes',
            });
        }

        const updateFields = {};
        if (status) updateFields.status = status;
        if (priority) updateFields.priority = priority;
        if (assignedTo) updateFields.assignedTo = assignedTo;
        if (estimatedCompletion) updateFields.estimatedCompletion = estimatedCompletion;
        if (completionNotes) updateFields.completionNotes = completionNotes;

        const request = await MaintenanceRequest.findByIdAndUpdate(
            req.params.id,
            updateFields,
            { new: true, runValidators: true }
        );

        if (!request) {
            return res.status(404).json({
                success: false,
                message: 'Maintenance request not found',
            });
        }

        res.status(200).json({
            success: true,
            message: 'Maintenance request updated successfully',
            data: request,
        });
    } catch (error) {
        if (error.kind === 'ObjectId' || error.name === 'CastError') {
            return res.status(400).json({
                success: false,
                message: 'Invalid maintenance request ID format',
            });
        }

        if (error.name === 'ValidationError') {
            const messages = Object.values(error.errors).map((err) => err.message);
            return res.status(400).json({
                success: false,
                message: 'Validation failed',
                errors: messages,
            });
        }

        console.error('Error updating maintenance request:', error);
        res.status(500).json({
            success: false,
            message: 'Server error. Could not update maintenance request.',
        });
    }
};

// @desc    Delete a maintenance request
// @route   DELETE /api/maintenance/:id
// @access  Public
const deleteMaintenanceRequest = async (req, res) => {
    try {
        const request = await MaintenanceRequest.findByIdAndDelete(req.params.id);

        if (!request) {
            return res.status(404).json({
                success: false,
                message: 'Maintenance request not found',
            });
        }

        res.status(200).json({
            success: true,
            message: 'Maintenance request deleted successfully',
            data: request,
        });
    } catch (error) {
        if (error.kind === 'ObjectId' || error.name === 'CastError') {
            return res.status(400).json({
                success: false,
                message: 'Invalid maintenance request ID format',
            });
        }

        console.error('Error deleting maintenance request:', error);
        res.status(500).json({
            success: false,
            message: 'Server error. Could not delete maintenance request.',
        });
    }
};

module.exports = {
    createMaintenanceRequest,
    getAllMaintenanceRequests,
    getMaintenanceRequestById,
    updateMaintenanceRequest,
    deleteMaintenanceRequest,
};
