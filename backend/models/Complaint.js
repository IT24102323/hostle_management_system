const mongoose = require('mongoose');

const complaintSchema = new mongoose.Schema(
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
            required: [true, 'Complaint category is required'],
            enum: {
                values: ['Noise', 'Cleanliness', 'Facilities', 'Roommate', 'Security', 'Other'],
                message: '{VALUE} is not a valid complaint category',
            },
        },
        subject: {
            type: String,
            required: [true, 'Subject is required'],
            trim: true,
            maxlength: [150, 'Subject cannot exceed 150 characters'],
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
                values: ['Pending', 'In Progress', 'Resolved', 'Dismissed'],
                message: '{VALUE} is not a valid status',
            },
            default: 'Pending',
        },
        response: {
            type: String,
            trim: true,
            maxlength: [2000, 'Response cannot exceed 2000 characters'],
        },
    },
    {
        timestamps: true,
    }
);

module.exports = mongoose.model('Complaint', complaintSchema);
