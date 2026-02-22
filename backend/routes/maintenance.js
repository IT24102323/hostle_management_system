const express = require('express');
const router = express.Router();
const {
    createMaintenanceRequest,
    getAllMaintenanceRequests,
    getMaintenanceRequestById,
    updateMaintenanceRequest,
    deleteMaintenanceRequest,
} = require('../controllers/maintenanceController');

// GET    /api/maintenance     — Get all maintenance requests (supports ?status=, ?category=, ?priority=, ?studentId=, ?assignedTo= filters)
router.get('/', getAllMaintenanceRequests);

// GET    /api/maintenance/:id — Get a single maintenance request by ID
router.get('/:id', getMaintenanceRequestById);

// POST   /api/maintenance     — Submit a new maintenance request
router.post('/', createMaintenanceRequest);

// PUT    /api/maintenance/:id — Update maintenance request
router.put('/:id', updateMaintenanceRequest);

// DELETE /api/maintenance/:id — Delete a maintenance request
router.delete('/:id', deleteMaintenanceRequest);

module.exports = router;
