const mongoose = require('mongoose');

const maintenanceRequestSchema = new mongoose.Schema(
    {
        studentName: {
            type: String,
            required: [true, 'Student name is required'],
            trim: true,
        },
        studentId: {
            type: String,
            required: [true, 'Student ID is required'],
            trim: true,
        },
        roomNumber: {
            type: String,
            required: [true, 'Room number is required'],
            trim: true,
        },
        category: {
            type: String,
            required: [true, 'Maintenance category is required'],
            enum: {
                values: ['Plumbing', 'Electrical', 'Furniture', 'Appliance', 'Structural', 'Other'],
                message: '{VALUE} is not a valid maintenance category',
            },
        },
        issueTitle: {
            type: String,
            required: [true, 'Issue title is required'],
            trim: true,
            maxlength: [150, 'Issue title cannot exceed 150 characters'],
        },
        description: {
            type: String,
            required: [true, 'Description is required'],
            trim: true,
            maxlength: [2000, 'Description cannot exceed 2000 characters'],
        },
        priority: {
            type: String,
            enum: {
                values: ['Low', 'Medium', 'High', 'Urgent'],
                message: '{VALUE} is not a valid priority level',
            },
            default: 'Medium',
        },
        status: {
            type: String,
            enum: {
                values: ['Pending', 'Assigned', 'In Progress', 'Completed', 'Cancelled'],
                message: '{VALUE} is not a valid status',
            },
            default: 'Pending',
        },
        assignedTo: {
            type: String,
            trim: true,
        },
        estimatedCompletion: {
            type: Date,
        },
        completionNotes: {
            type: String,
            trim: true,
            maxlength: [2000, 'Completion notes cannot exceed 2000 characters'],
        },
    },
    {
        timestamps: true,
    }
);

module.exports = mongoose.model('MaintenanceRequest', maintenanceRequestSchema);
