// API Base URL
const API_BASE = 'http://localhost:8080/api';

// Auth: include JWT in all API requests
function apiFetch(url, options = {}) {
    const token = localStorage.getItem('token');
    const headers = { ...(options.headers || {}) };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return fetch(url, { ...options, headers }).then(res => {
        if (res.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = 'login.html';
        }
        return res;
    });
}

// Check authentication
const token = localStorage.getItem('token');
const user = JSON.parse(localStorage.getItem('user'));
if (!token || !user || !user.loggedIn || user.role !== 'admin') {
    window.location.href = 'login.html';
}

// State management
let allRooms = [];
let allResidents = [];
let allPayments = [];
let allComplaints = [];
let allVisits = [];
let allCleaningTasks = [];
let editingRoomId = null;
let editingResidentId = null;
let editingPaymentId = null;

// Auto-refresh interval (30 seconds)
let autoRefreshInterval = null;

// Logout function
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}

// Toast notification
function showToast(message, type = 'success') {
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.style.cssText = `
            position: fixed;
            bottom: 30px;
            right: 30px;
            padding: 16px 24px;
            border-radius: 12px;
            color: white;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 10px;
            transform: translateY(100px);
            opacity: 0;
            transition: all 0.3s ease;
            z-index: 9999;
            box-shadow: 0 10px 40px rgba(0,0,0,0.3);
        `;
        document.body.appendChild(toast);
    }

    toast.style.background = type === 'success' ? '#22c55e' : type === 'error' ? '#ef4444' : '#f59e0b';
    toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'times-circle' : 'exclamation-circle'}"></i> ${message}`;

    setTimeout(() => {
        toast.style.transform = 'translateY(0)';
        toast.style.opacity = '1';
    }, 10);

    setTimeout(() => {
        toast.style.transform = 'translateY(100px)';
        toast.style.opacity = '0';
    }, 3000);
}

// Update last refresh time
function updateLastRefresh() {
    const now = new Date();
    const timeStr = now.toLocaleTimeString();
    const indicator = document.getElementById('lastRefresh');
    if (indicator) {
        indicator.textContent = `Last updated: ${timeStr}`;
    }
}

// Navigation
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', function (e) {
        e.preventDefault();

        document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
        this.classList.add('active');

        const section = this.dataset.section;
        document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
        document.getElementById(section + '-section').classList.add('active');

        const titles = {
            'dashboard': 'Dashboard Overview',
            'rooms': 'Room Management',
            'residents': 'Resident Management',
            'payments': 'Payment Management',
            'complaints': 'Complaint Management',
            'visits': 'Visit Management',
            'attendance': 'Daily Attendance',
            'food': 'Food Preferences',
            'cleaning': 'Cleaning Schedule'
        };
        document.getElementById('pageTitle').textContent = titles[section];

        loadSectionData(section);
    });
});

// Load section data
function loadSectionData(section) {
    switch (section) {
        case 'dashboard': loadDashboard(); break;
        case 'rooms': loadRooms(); break;
        case 'residents': loadResidents(); break;
        case 'payments': loadPayments(); break;
        case 'complaints': loadComplaints(); break;
        case 'visits': loadVisits(); break;
        case 'attendance': loadAttendance(); break;
        case 'food': loadFoodPreferences(); break;
        case 'cleaning': loadCleaning(); break;
    }
}

// Refresh all data
async function refreshAllData() {
    const activeSection = document.querySelector('.nav-link.active')?.dataset.section || 'dashboard';
    await loadSectionData(activeSection);
    updateLastRefresh();
    showToast('Data refreshed successfully!');
}

// Load Dashboard
async function loadDashboard() {
    try {
        document.getElementById('dashboardLoading').style.display = 'block';

        const [roomsRes, residentsRes, paymentsRes, complaintsRes, visitsRes] = await Promise.all([
            apiFetch(`${API_BASE}/rooms`).then(r => r.json()).catch(() => ({ data: [] })),
            apiFetch(`${API_BASE}/residents`).then(r => r.json()).catch(() => ({ data: [] })),
            apiFetch(`${API_BASE}/payments`).then(r => r.json()).catch(() => ({ data: [] })),
            apiFetch(`${API_BASE}/complaints`).then(r => r.json()).catch(() => ({ data: [] })),
            apiFetch(`${API_BASE}/visits`).then(r => r.json()).catch(() => ({ data: [] }))
        ]);

        allRooms = roomsRes.data || [];
        allResidents = residentsRes.data || [];
        allPayments = paymentsRes.data || [];
        allComplaints = complaintsRes.data || [];
        allVisits = visitsRes.data || [];

        const totalRooms = allRooms.length;
        const availableRooms = allRooms.filter(r => r.status === 'AVAILABLE').length;
        const totalResidents = allResidents.length;
        const activeResidents = allResidents.filter(r => r.status === 'ACTIVE').length;
        const pendingPayments = allPayments.filter(p => p.status === 'PENDING').length;
        const pendingAmount = allPayments.filter(p => p.status === 'PENDING').reduce((sum, p) => sum + (p.total || p.amount || 0), 0);
        const openComplaints = allComplaints.filter(c => c.status !== 'RESOLVED').length;
        const newVisits = allVisits.filter(v => v.status === 'NEW').length;

        document.getElementById('totalRooms').textContent = totalRooms;
        document.getElementById('availableRooms').textContent = availableRooms;
        document.getElementById('totalResidents').textContent = totalResidents;
        document.getElementById('activeResidents').textContent = activeResidents;
        document.getElementById('pendingPayments').textContent = pendingPayments;
        document.getElementById('pendingAmount').textContent = `Rs. ${pendingAmount.toLocaleString()}`;
        document.getElementById('openComplaints').textContent = openComplaints;
        document.getElementById('newVisits').textContent = newVisits;

        document.getElementById('dashboardLoading').style.display = 'none';

        loadRecentActivities();
        loadPendingPaymentsOverview();
        updateLastRefresh();
    } catch (error) {
        console.error('Error loading dashboard:', error);
        document.getElementById('dashboardLoading').innerHTML = `<p style="color: #ef4444;"><i class="fas fa-exclamation-triangle"></i> Error loading data</p>`;
    }
}

function loadRecentActivities() {
    const activities = [];

    allComplaints.slice(0, 3).forEach(c => {
        activities.push({
            icon: 'exclamation-circle', color: 'orange',
            text: `Complaint: "${c.title}" from ${c.residentName || 'Resident'}`,
            date: c.complaintDate
        });
    });

    allPayments.filter(p => p.status === 'PAID').slice(0, 3).forEach(p => {
        activities.push({
            icon: 'check-circle', color: 'green',
            text: `Payment: Rs. ${(p.total || p.amount || 0).toLocaleString()} from ${p.residentName || 'Resident'}`,
            date: p.paidDate || p.paymentDate
        });
    });

    const container = document.getElementById('recentActivities');
    if (activities.length > 0) {
        container.innerHTML = activities.map(a => `
            <div style="display: flex; align-items: center; gap: 15px; padding: 15px; border-bottom: 1px solid #f0f0f0;">
                <div style="width: 40px; height: 40px; border-radius: 10px; background: ${a.color === 'green' ? 'rgba(34,197,94,0.1)' : 'rgba(245,158,11,0.1)'}; display: flex; align-items: center; justify-content: center;">
                    <i class="fas fa-${a.icon}" style="color: ${a.color === 'green' ? '#22c55e' : '#f59e0b'};"></i>
                </div>
                <div style="flex: 1;">
                    <div style="font-weight: 500; color: #333;">${a.text}</div>
                    <div style="font-size: 12px; color: #666;">${a.date || 'Recently'}</div>
                </div>
            </div>
        `).join('');
    } else {
        container.innerHTML = `<div class="empty-state"><i class="fas fa-check-circle"></i><p>No recent activities</p></div>`;
    }
}

function loadPendingPaymentsOverview() {
    const pending = allPayments.filter(p => p.status === 'PENDING');
    const container = document.getElementById('pendingPaymentsList');

    if (pending.length > 0) {
        container.innerHTML = `
            <table style="width: 100%;">
                <thead><tr><th>Resident</th><th>Month</th><th>Amount</th><th>Action</th></tr></thead>
                <tbody>
                    ${pending.slice(0, 5).map(p => `
                        <tr>
                            <td><strong>${p.residentName || 'N/A'}</strong></td>
                            <td>${p.month || 'N/A'}</td>
                            <td>Rs. ${(p.total || p.amount || 0).toLocaleString()}</td>
                            <td><button class="btn btn-sm btn-success" onclick="showPaymentCollectModal(${p.id})"><i class="fas fa-check"></i> Collect</button></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
            ${pending.length > 5 ? `<p style="text-align: center; margin-top: 15px;"><a href="#" onclick="document.querySelector('[data-section=payments]').click();" style="color: #6366f1;">View all ${pending.length} pending payments</a></p>` : ''}
        `;
    } else {
        container.innerHTML = `<div class="empty-state"><i class="fas fa-check-circle" style="color: #22c55e;"></i><p>All payments up to date!</p></div>`;
    }
}

// Load Rooms
async function loadRooms() {
    try {
        const response = await apiFetch(`${API_BASE}/rooms`);
        const result = await response.json();
        allRooms = result.data || [];
        const tbody = document.getElementById('roomsTableBody');

        if (allRooms.length > 0) {
            tbody.innerHTML = allRooms.map(room => `
                <tr>
                    <td><strong>${room.roomNumber}</strong></td>
                    <td><span class="badge badge-info">${room.roomType}</span></td>
                    <td>Rs. ${(room.pricePerMonth || 0).toLocaleString()}</td>
                    <td>${room.capacity || 1}</td>
                    <td>${room.currentOccupancy || 0}/${room.capacity || 1}</td>
                    <td><span class="badge badge-${getStatusColor(room.status)}">${room.status}</span></td>
                    <td>${room.facilities || 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="editRoom(${room.id})"><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-danger" onclick="deleteRoom(${room.id})"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="8" class="empty-state"><i class="fas fa-door-open"></i><p>No rooms found</p></td></tr>';
        }
    } catch (error) {
        console.error('Error loading rooms:', error);
    }
}

// Load Residents
async function loadResidents() {
    try {
        const response = await apiFetch(`${API_BASE}/residents`);
        const result = await response.json();
        allResidents = result.data || [];
        const tbody = document.getElementById('residentsTableBody');

        if (allResidents.length > 0) {
            tbody.innerHTML = allResidents.map(resident => `
                <tr>
                    <td><strong>${resident.name}</strong><br><span style="font-size:11px;color:#666;">${resident.email || ''}</span></td>
                    <td>${resident.nic || 'N/A'}</td>
                    <td>${resident.contact || 'N/A'}</td>
                    <td>${resident.roomNumber ? `<span class="badge badge-info">${resident.roomNumber}</span>` : 'Not Assigned'}</td>
                    <td>${resident.course || 'N/A'}</td>
                    <td><span class="badge badge-${getStatusColor(resident.status)}">${resident.status}</span></td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="editResident(${resident.id})"><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-warning" onclick="assignRoomToResident(${resident.id})"><i class="fas fa-door-open"></i></button>
                        <button class="btn btn-sm btn-danger" onclick="deleteResident(${resident.id})"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state"><i class="fas fa-users"></i><p>No residents found</p></td></tr>';
        }
    } catch (error) {
        console.error('Error loading residents:', error);
    }
}

// Load Payments
async function loadPayments() {
    try {
        const response = await apiFetch(`${API_BASE}/payments`);
        const result = await response.json();
        allPayments = result.data || [];
        const tbody = document.getElementById('paymentsTableBody');

        const totalPending = allPayments.filter(p => p.status === 'PENDING').reduce((sum, p) => sum + (p.total || p.amount || 0), 0);
        const totalCollected = allPayments.filter(p => p.status === 'PAID').reduce((sum, p) => sum + (p.total || p.amount || 0), 0);

        document.getElementById('paymentStatsContainer').innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-bottom: 20px;">
                <div style="background: linear-gradient(135deg, rgba(245,158,11,0.1), rgba(245,158,11,0.05)); padding: 20px; border-radius: 12px; border-left: 4px solid #f59e0b;">
                    <div style="font-size: 24px; font-weight: 700; color: #f59e0b;">Rs. ${totalPending.toLocaleString()}</div>
                    <div style="color: #666; font-size: 13px;">Total Pending</div>
                </div>
                <div style="background: linear-gradient(135deg, rgba(34,197,94,0.1), rgba(34,197,94,0.05)); padding: 20px; border-radius: 12px; border-left: 4px solid #22c55e;">
                    <div style="font-size: 24px; font-weight: 700; color: #22c55e;">Rs. ${totalCollected.toLocaleString()}</div>
                    <div style="color: #666; font-size: 13px;">Total Collected</div>
                </div>
                <div style="background: linear-gradient(135deg, rgba(99,102,241,0.1), rgba(99,102,241,0.05)); padding: 20px; border-radius: 12px; border-left: 4px solid #6366f1;">
                    <div style="font-size: 24px; font-weight: 700; color: #6366f1;">${allPayments.length}</div>
                    <div style="color: #666; font-size: 13px;">Total Records</div>
                </div>
            </div>
        `;

        if (allPayments.length > 0) {
            tbody.innerHTML = allPayments.map(payment => `
                <tr style="${payment.status === 'PENDING' ? 'background: rgba(245,158,11,0.05);' : ''}">
                    <td><strong>${payment.residentName || 'N/A'}</strong></td>
                    <td><span class="badge badge-info">${payment.month || 'N/A'}</span></td>
                    <td>Rs. ${(payment.amount || 0).toLocaleString()}</td>
                    <td>Rs. ${(payment.foodCharge || 0).toLocaleString()}</td>
                    <td>Rs. ${(payment.lateFee || 0).toLocaleString()}</td>
                    <td><strong>Rs. ${(payment.total || payment.amount || 0).toLocaleString()}</strong></td>
                    <td><span class="badge badge-${getPaymentStatusColor(payment.status)}">${payment.status}</span></td>
                    <td>${payment.method || 'N/A'}</td>
                    <td>
                        ${payment.status === 'PENDING' ? `<button class="btn btn-sm btn-success" onclick="showPaymentCollectModal(${payment.id})"><i class="fas fa-hand-holding-usd"></i> Collect</button>` : `<span style="color: #22c55e;"><i class="fas fa-check"></i></span>`}
                        <button class="btn btn-sm btn-primary" onclick="editPayment(${payment.id})"><i class="fas fa-edit"></i></button>
                        <button class="btn btn-sm btn-danger" onclick="deletePayment(${payment.id})"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="9" class="empty-state"><i class="fas fa-receipt"></i><p>No payment records found</p></td></tr>';
        }
    } catch (error) {
        console.error('Error loading payments:', error);
    }
}

// Load Complaints
async function loadComplaints() {
    try {
        const response = await apiFetch(`${API_BASE}/complaints`);
        const result = await response.json();
        allComplaints = result.data || [];
        renderComplaints(allComplaints);
    } catch (error) {
        console.error('Error loading complaints:', error);
    }
}

function filterComplaints() {
    const statusFilter = document.getElementById('complaintFilterStatus')?.value || '';
    const priorityFilter = document.getElementById('complaintFilterPriority')?.value || '';
    const searchText = (document.getElementById('complaintSearch')?.value || '').toLowerCase();

    let filtered = allComplaints.filter(c => {
        const matchStatus = !statusFilter || c.status === statusFilter;
        const matchPriority = !priorityFilter || c.priority === priorityFilter;
        const matchSearch = !searchText ||
            (c.residentName || '').toLowerCase().includes(searchText) ||
            (c.title || '').toLowerCase().includes(searchText) ||
            (c.category || '').toLowerCase().includes(searchText);
        return matchStatus && matchPriority && matchSearch;
    });
    renderComplaints(filtered);
}

function renderComplaints(complaints) {
    const tbody = document.getElementById('complaintsTableBody');
    if (!tbody) return;

    if (complaints.length > 0) {
        tbody.innerHTML = complaints.map(c => `
            <tr style="${c.status === 'PENDING' && c.priority === 'HIGH' ? 'background: rgba(239,68,68,0.05);' : ''}">
                <td>
                    <strong>${c.residentName || 'N/A'}</strong>
                    ${c.roomNumber ? `<br><span style="font-size:11px;color:#666;">Room ${c.roomNumber}</span>` : ''}
                </td>
                <td>
                    <strong style="display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${c.title}</strong>
                    <div style="font-size:11px;color:#666;margin-top:3px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${(c.description || '').substring(0, 60)}${(c.description || '').length > 60 ? '...' : ''}</div>
                </td>
                <td><span class="badge badge-info" style="white-space:nowrap;">${c.category || 'General'}</span></td>
                <td><span class="badge badge-${getPriorityColor(c.priority)}" style="white-space:nowrap;">${c.priority}</span></td>
                <td><span class="badge badge-${getComplaintStatusColor(c.status)}" style="white-space:nowrap;display:inline-block;">${getStatusLabel(c.status)}</span></td>
                <td style="text-align:center;white-space:nowrap;">
                    ${(c.reminderCount || 0) > 0
                        ? `<span style="background:#fef3c7;color:#d97706;border-radius:20px;padding:3px 8px;font-size:12px;font-weight:600;white-space:nowrap;">
                              <i class="fas fa-bell"></i> ${c.reminderCount}
                           </span>`
                        : `<span style="color:#94a3b8;font-size:12px;">—</span>`
                    }
                </td>
                <td style="font-size:12px;white-space:nowrap;">${c.complaintDate || 'N/A'}</td>
                <td style="white-space:nowrap;">
                    <div style="display:flex;gap:4px;align-items:center;">
                        <button class="btn btn-sm btn-primary" onclick="openComplaintStatusModal(${c.id})" title="Update Status" style="padding:6px 10px;min-width:32px;min-height:32px;display:inline-flex;align-items:center;justify-content:center;">
                            <i class="fas fa-edit" style="font-size:13px;"></i>
                        </button>
                        <button class="btn btn-sm btn-info" onclick="viewComplaintDetails(${c.id})" title="View Details" style="padding:6px 10px;min-width:32px;min-height:32px;display:inline-flex;align-items:center;justify-content:center;">
                            <i class="fas fa-eye" style="font-size:13px;"></i>
                        </button>
                        <button class="btn btn-sm btn-danger" onclick="deleteComplaint(${c.id})" title="Delete" style="padding:6px 10px;min-width:32px;min-height:32px;display:inline-flex;align-items:center;justify-content:center;">
                            <i class="fas fa-trash" style="font-size:13px;"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    } else {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state"><i class="fas fa-clipboard-check"></i><p>No complaints found</p></td></tr>';
    }
}

function getStatusLabel(status) {
    const labels = {
        'PENDING': 'Pending',
        'IN_PROGRESS': 'In Progress',
        'WILL_TAKE_ACTION': 'Will Take Action',
        'RESOLVED': 'Resolved',
        'REJECTED': 'Rejected'
    };
    return labels[status] || status;
}

function openComplaintStatusModal(complaintId) {
    const complaint = allComplaints.find(c => c.id === complaintId);
    if (!complaint) return;

    document.getElementById('complaintStatusId').value = complaintId;
    document.getElementById('complaintStatusSelect').value = complaint.status;
    document.getElementById('complaintResolutionNote').value = complaint.resolution || '';
    document.getElementById('complaintAdminNote').value = complaint.adminNote || '';

    document.getElementById('complaintStatusInfo').innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:10px;">
            <div>
                <div style="font-weight:700;font-size:15px;margin-bottom:4px;">${complaint.title}</div>
                <div style="font-size:13px;color:#64748b;">${complaint.residentName} 
                    ${complaint.roomNumber ? '• Room ' + complaint.roomNumber : ''}
                </div>
                <div style="font-size:12px;color:#94a3b8;margin-top:4px;">${complaint.category || 'General'} • ${complaint.complaintDate || ''}</div>
            </div>
            <div style="display:flex;gap:6px;flex-shrink:0;">
                <span class="badge badge-${getPriorityColor(complaint.priority)}">${complaint.priority}</span>
                <span class="badge badge-${getComplaintStatusColor(complaint.status)}">${getStatusLabel(complaint.status)}</span>
            </div>
        </div>
        ${complaint.description ? `<div style="margin-top:10px;font-size:13px;color:#475569;border-top:1px solid #e2e8f0;padding-top:10px;">${complaint.description}</div>` : ''}
        ${(complaint.reminderCount || 0) > 0 ? `<div style="margin-top:8px;font-size:12px;color:#d97706;"><i class="fas fa-bell"></i> ${complaint.reminderCount} reminder(s) sent by student</div>` : ''}
    `;

    document.getElementById('complaintStatusModal').classList.add('active');
}

function viewComplaintDetails(complaintId) {
    const c = allComplaints.find(x => x.id === complaintId);
    if (!c) return;

    document.getElementById('complaintDetailsContent').innerHTML = `
        <div style="padding:10px 0;">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:16px;">
                <div><span style="font-size:12px;color:#94a3b8;">RESIDENT</span><br><strong>${c.residentName || 'N/A'}</strong></div>
                <div><span style="font-size:12px;color:#94a3b8;">ROOM</span><br><strong>${c.roomNumber || 'N/A'}</strong></div>
                <div><span style="font-size:12px;color:#94a3b8;">STATUS</span><br><span class="badge badge-${getComplaintStatusColor(c.status)}">${getStatusLabel(c.status)}</span></div>
                <div><span style="font-size:12px;color:#94a3b8;">PRIORITY</span><br><span class="badge badge-${getPriorityColor(c.priority)}">${c.priority}</span></div>
                <div><span style="font-size:12px;color:#94a3b8;">CATEGORY</span><br>${c.category || 'General'}</div>
                <div><span style="font-size:12px;color:#94a3b8;">DATE</span><br>${c.complaintDate || 'N/A'}</div>
            </div>
            <div style="margin-bottom:12px;">
                <div style="font-size:12px;color:#94a3b8;margin-bottom:4px;">TITLE</div>
                <div style="font-weight:600;font-size:15px;">${c.title}</div>
            </div>
            <div style="margin-bottom:12px;">
                <div style="font-size:12px;color:#94a3b8;margin-bottom:4px;">DESCRIPTION</div>
                <div style="background:#f8fafc;padding:12px;border-radius:8px;font-size:13px;color:#475569;">${c.description || '—'}</div>
            </div>
            ${c.resolution ? `<div style="margin-bottom:12px;"><div style="font-size:12px;color:#94a3b8;margin-bottom:4px;">RESOLUTION NOTE</div><div style="background:#f0fdf4;padding:12px;border-radius:8px;font-size:13px;color:#166534;">${c.resolution}</div></div>` : ''}
            ${c.adminNote ? `<div style="margin-bottom:12px;"><div style="font-size:12px;color:#94a3b8;margin-bottom:4px;">ADMIN NOTE</div><div style="background:#fef9c3;padding:12px;border-radius:8px;font-size:13px;color:#713f12;">${c.adminNote}</div></div>` : ''}
            ${(c.reminderCount || 0) > 0 ? `<div style="background:#fff7ed;padding:10px 14px;border-radius:8px;font-size:13px;color:#c2410c;margin-bottom:12px;"><i class="fas fa-bell"></i> <strong>${c.reminderCount}</strong> reminder(s) sent</div>` : ''}
            <div style="display:flex;justify-content:flex-end;margin-top:16px;">
                <button class="btn btn-primary" onclick="closeModal('complaintDetailsModal');openComplaintStatusModal(${c.id})">
                    <i class="fas fa-edit"></i> Update Status
                </button>
            </div>
        </div>
    `;
    document.getElementById('complaintDetailsModal').classList.add('active');
}

document.getElementById('complaintStatusForm')?.addEventListener('submit', async function (e) {
    e.preventDefault();
    const id = document.getElementById('complaintStatusId').value;
    const data = {
        status: document.getElementById('complaintStatusSelect').value,
        resolution: document.getElementById('complaintResolutionNote').value || null,
        adminNote: document.getElementById('complaintAdminNote').value || null
    };

    try {
        const response = await apiFetch(`${API_BASE}/complaints/${id}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await response.json();
        if (result.success) {
            closeModal('complaintStatusModal');
            showToast('Complaint status updated. Resident notified!', 'success');
            await loadComplaints();
        } else {
            showToast(result.message || 'Failed to update status', 'error');
        }
    } catch (err) {
        console.error(err);
        showToast('Connection error', 'error');
    }
});

async function deleteComplaint(id) {
    if (!confirm('Delete this complaint? This cannot be undone.\n(Note: only PENDING complaints can be deleted by the system)')) return;
    try {
        const response = await apiFetch(`${API_BASE}/complaints/${id}`, { method: 'DELETE' });
        const result = await response.json();
        if (result.success) {
            showToast('Complaint deleted.', 'success');
            await loadComplaints();
        } else {
            showToast(result.message || 'Cannot delete this complaint (not PENDING)', 'error');
        }
    } catch (err) {
        showToast('Error deleting complaint', 'error');
    }
}

// Load Visits
async function loadVisits() {
    try {
        const response = await apiFetch(`${API_BASE}/visits`);
        const result = await response.json();
        allVisits = result.data || [];
        const tbody = document.getElementById('visitsTableBody');

        if (allVisits.length > 0) {
            tbody.innerHTML = allVisits.map(v => `
                <tr>
                    <td><strong>${v.visitorName}</strong></td>
                    <td>${v.visitorContact || 'N/A'}</td>
                    <td>${v.preferredRoomType || 'Any'}</td>
                    <td>${v.visitDate || 'N/A'}</td>
                    <td><span class="badge badge-${getVisitStatusColor(v.status)}">${v.status}</span></td>
                    <td>
                        ${v.status === 'NEW' ? `<button class="btn btn-sm btn-success" onclick="updateVisitStatus(${v.id}, 'CONTACTED')"><i class="fas fa-phone"></i></button>` : ''}
                        ${v.status !== 'CLOSED' ? `<button class="btn btn-sm btn-warning" onclick="updateVisitStatus(${v.id}, 'CLOSED')"><i class="fas fa-times"></i></button>` : ''}
                        <button class="btn btn-sm btn-danger" onclick="deleteVisit(${v.id})"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="empty-state"><i class="fas fa-calendar-times"></i><p>No visit requests found</p></td></tr>';
        }
    } catch (error) {
        console.error('Error loading visits:', error);
    }
}

// Load Cleaning
async function loadCleaning() {
    try {
        const response = await apiFetch(`${API_BASE}/cleaning`);
        const result = await response.json();
        allCleaningTasks = result.data || [];
        const tbody = document.getElementById('cleaningTableBody');

        if (allCleaningTasks.length > 0) {
            tbody.innerHTML = allCleaningTasks.map(t => `
                <tr>
                    <td><strong>${t.area}</strong></td>
                    <td><span class="badge badge-info">${t.dayOfWeek}</span></td>
                    <td>${t.timeSlot}</td>
                    <td>${t.assignedStaff || '-'}</td>
                    <td>${t.assignedTo || '-'}</td>
                    <td>${t.scheduledDate || '-'}</td>
                    <td><span class="badge badge-${t.completionStatus === 'Completed' ? 'success' : 'warning'}">${t.completionStatus || 'Pending'}</span></td>
                    <td>${t.completedBy || '-'}</td>
                    <td>
                        ${t.completionStatus !== 'Completed' ? `<button class="btn btn-sm btn-success" onclick="markCleaningComplete(${t.id})" title="Mark Complete"><i class="fas fa-check"></i></button>` : ''}
                        <button class="btn btn-sm btn-danger" onclick="deleteCleaning(${t.id})" title="Delete"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="9" class="empty-state"><i class="fas fa-broom"></i><p>No cleaning tasks found</p></td></tr>';
        }
    } catch (error) {
        console.error('Error loading cleaning schedule:', error);
    }
}

// Modal functions
function showAddRoomModal() {
    editingRoomId = null;
    document.getElementById('roomForm').reset();
    document.querySelector('#roomModal .modal-title').textContent = 'Add New Room';
    document.getElementById('roomModal').classList.add('active');
}

async function showAddResidentModal() {
    editingResidentId = null;
    document.getElementById('residentForm').reset();
    document.querySelector('#residentModal .modal-title').textContent = 'Add New Resident';
    await loadAvailableRoomsForSelect('residentRoom');
    document.getElementById('residentModal').classList.add('active');
}

async function showAddPaymentModal() {
    editingPaymentId = null;
    document.getElementById('paymentForm').reset();
    document.querySelector('#paymentModal .modal-title').textContent = 'Create Payment';
    const now = new Date();
    const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    document.getElementById('paymentMonth').value = `${months[now.getMonth()]} ${now.getFullYear()}`;
    await loadResidentsForSelect('paymentResident');
    document.getElementById('paymentModal').classList.add('active');
}

async function showAddCleaningModal() {
    document.getElementById('cleaningForm').reset();
    document.getElementById('cleaningAreaCustom').style.display = 'none';
    document.getElementById('cleaningRoomGroup').style.display = 'none';
    document.getElementById('cleaningRoomResidentsGroup').style.display = 'none';
    document.getElementById('cleaningAssignedToGroup').style.display = 'block';

    // Pre-load rooms + residents
    try {
        const [roomsRes, residentsRes] = await Promise.all([
            apiFetch(`${API_BASE}/rooms`),
            apiFetch(`${API_BASE}/residents`)
        ]);
        const roomsData = await roomsRes.json();
        const residentsData = await residentsRes.json();
        window._cleaningRooms = roomsData.data || [];
        window._cleaningResidents = residentsData.data || [];

        const roomSelect = document.getElementById('cleaningRoomSelect');
        roomSelect.innerHTML = '<option value="">Select Room</option>';
        window._cleaningRooms.forEach(r => {
            roomSelect.innerHTML += `<option value="${r.id}" data-number="${r.roomNumber}">${r.roomNumber} (${r.roomType})</option>`;
        });
    } catch (e) { console.error('Error preloading rooms/residents:', e); }

    // Area change handler
    const areaSelect = document.getElementById('cleaningArea');
    areaSelect.onchange = function() {
        const val = this.value;
        document.getElementById('cleaningAreaCustom').style.display = val === 'Other' ? 'block' : 'none';
        document.getElementById('cleaningRoomGroup').style.display = val === 'Room Clean' ? 'block' : 'none';
        if (val !== 'Room Clean') {
            document.getElementById('cleaningRoomResidentsGroup').style.display = 'none';
            document.getElementById('cleaningAssignedToGroup').style.display = 'block';
            document.getElementById('cleaningRoomSelect').value = '';
        } else {
            document.getElementById('cleaningAssignedToGroup').style.display = 'none';
        }
    };

    // Room change handler — show residents in that room
    document.getElementById('cleaningRoomSelect').onchange = function() {
        const roomId = parseInt(this.value);
        const resGroup = document.getElementById('cleaningRoomResidentsGroup');
        const resContainer = document.getElementById('cleaningRoomResidents');
        if (!roomId) { resGroup.style.display = 'none'; return; }

        const selectedOption = this.options[this.selectedIndex];
        const roomNumber = selectedOption.getAttribute('data-number');
        document.getElementById('cleaningRoomLabel').textContent = '(' + roomNumber + ')';

        const residentsInRoom = (window._cleaningResidents || []).filter(r => r.roomId === roomId || r.roomNumber === roomNumber);
        if (residentsInRoom.length > 0) {
            resContainer.innerHTML = residentsInRoom.map(r =>
                `<label style="display:flex;align-items:center;gap:8px;cursor:pointer;padding:6px 8px;border-radius:6px;transition:background 0.2s;">
                    <input type="checkbox" class="cleaning-resident-cb" value="${r.name}" checked style="width:18px;height:18px;accent-color:#6366f1;">
                    <span style="font-weight:500;">${r.name}</span>
                    <span style="color:#94a3b8;font-size:12px;">${r.nic || ''}</span>
                </label>`
            ).join('');
            resGroup.style.display = 'block';
        } else {
            resContainer.innerHTML = '<p style="color:#94a3b8;text-align:center;margin:0;">No residents in this room</p>';
            resGroup.style.display = 'block';
        }
    };

    document.getElementById('cleaningModal').classList.add('active');
}

async function showPaymentCollectModal(paymentId) {
    const payment = allPayments.find(p => p.id === paymentId);
    if (!payment) return;

    let modal = document.getElementById('collectPaymentModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'collectPaymentModal';
        modal.className = 'modal';
        document.body.appendChild(modal);
    }

    modal.innerHTML = `
        <div class="modal-content" style="max-width: 450px;">
            <div class="modal-header">
                <h3 class="modal-title">Collect Payment</h3>
                <button class="close-btn" onclick="closeModal('collectPaymentModal')">&times;</button>
            </div>
            <div style="background: #f8f9fa; padding: 20px; border-radius: 12px; margin-bottom: 20px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 10px;"><span>Resident:</span><strong>${payment.residentName || 'N/A'}</strong></div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 10px;"><span>Month:</span><strong>${payment.month}</strong></div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 10px;"><span>Room Rent:</span><span>Rs. ${(payment.amount || 0).toLocaleString()}</span></div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 10px;"><span>Food:</span><span>Rs. ${(payment.foodCharge || 0).toLocaleString()}</span></div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 10px;"><span>Late Fee:</span><span>Rs. ${(payment.lateFee || 0).toLocaleString()}</span></div>
                <hr style="margin: 15px 0;"><div style="display: flex; justify-content: space-between;"><strong style="font-size: 18px;">Total:</strong><strong style="font-size: 18px; color: #22c55e;">Rs. ${(payment.total || payment.amount || 0).toLocaleString()}</strong></div>
            </div>
            <div class="form-group"><label>Payment Method *</label><select id="collectPaymentMethod"><option value="CASH">Cash</option><option value="BANK_TRANSFER">Bank Transfer</option><option value="VISA">Visa Card</option><option value="CREDIT_CARD">Credit Card</option></select></div>
            <button onclick="collectPayment(${payment.id})" class="btn btn-success" style="width: 100%;"><i class="fas fa-check-circle"></i> Confirm Payment</button>
        </div>
    `;
    modal.classList.add('active');
}

async function collectPayment(paymentId) {
    const method = document.getElementById('collectPaymentMethod').value;
    try {
        const response = await apiFetch(`${API_BASE}/payments/${paymentId}/pay?method=${method}`, { method: 'PUT' });
        const result = await response.json();
        if (result.success) {
            showToast('Payment collected successfully!');
            closeModal('collectPaymentModal');
            loadPayments();
            loadDashboard();
        } else {
            showToast(result.message || 'Failed to collect payment', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Failed to collect payment', 'error');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
        modal.style.display = '';  // clear any inline display style
    }
    if (modalId === 'roomModal') editingRoomId = null;
    if (modalId === 'residentModal') editingResidentId = null;
    if (modalId === 'paymentModal') editingPaymentId = null;
}

async function loadAvailableRoomsForSelect(selectId) {
    try {
        const response = await apiFetch(`${API_BASE}/rooms/available`);
        const result = await response.json();
        const select = document.getElementById(selectId);
        select.innerHTML = '<option value="">No Room</option>' + (result.data || []).map(room => `<option value="${room.id}">${room.roomNumber} - ${room.roomType}</option>`).join('');
    } catch (error) { console.error('Error:', error); }
}

async function loadResidentsForSelect(selectId) {
    try {
        const response = await apiFetch(`${API_BASE}/residents`);
        const result = await response.json();
        const select = document.getElementById(selectId);
        select.innerHTML = '<option value="">Select Resident</option>' + (result.data || []).map(r => `<option value="${r.id}">${r.name} ${r.roomNumber ? '- Room ' + r.roomNumber : ''}</option>`).join('');
    } catch (error) { console.error('Error:', error); }
}

// Form submissions
document.getElementById('roomForm').addEventListener('submit', async function (e) {
    e.preventDefault();
    const data = {
        roomNumber: document.getElementById('roomNumber').value,
        roomType: document.getElementById('roomType').value,
        pricePerMonth: parseFloat(document.getElementById('pricePerMonth').value),
        capacity: parseInt(document.getElementById('capacity').value),
        facilities: document.getElementById('facilities').value,
        imageUrl: document.getElementById('imageUrl').value,
        status: document.getElementById('roomStatus').value
    };
    try {
        const url = editingRoomId ? `${API_BASE}/rooms/${editingRoomId}` : `${API_BASE}/rooms`;
        const response = await apiFetch(url, { method: editingRoomId ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
        const result = await response.json();
        if (result.success) { showToast('Room saved!'); closeModal('roomModal'); loadRooms(); }
        else { showToast(result.message || 'Failed', 'error'); }
    } catch (error) { console.error('Error:', error); showToast('Failed', 'error'); }
});

document.getElementById('residentForm').addEventListener('submit', async function (e) {
    e.preventDefault();
    const roomId = document.getElementById('residentRoom').value;
    const data = {
        name: document.getElementById('residentName').value,
        nic: document.getElementById('residentNic').value,
        contact: document.getElementById('residentContact').value,
        email: document.getElementById('residentEmail').value,
        course: document.getElementById('residentCourse').value,
        status: document.getElementById('residentStatus').value,
        rating: 3,
        joinDate: new Date().toISOString().split('T')[0]
    };
    try {
        const url = editingResidentId ? `${API_BASE}/residents/${editingResidentId}` : `${API_BASE}/residents`;
        const response = await apiFetch(url, { method: editingResidentId ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
        const result = await response.json();
        if (result.success) {
            if (roomId && result.data?.id) { await apiFetch(`${API_BASE}/residents/${result.data.id}/assign-room/${roomId}`, { method: 'PUT' }); }
            showToast('Resident saved!'); closeModal('residentModal'); loadResidents();
        } else { showToast(result.message || 'Failed', 'error'); }
    } catch (error) { console.error('Error:', error); showToast('Failed', 'error'); }
});

document.getElementById('paymentForm').addEventListener('submit', async function (e) {
    e.preventDefault();
    const amount = parseFloat(document.getElementById('paymentAmount').value) || 0;
    const food = parseFloat(document.getElementById('paymentFood').value) || 0;
    const late = parseFloat(document.getElementById('paymentLate').value) || 0;
    const data = {
        residentId: parseInt(document.getElementById('paymentResident').value),
        month: document.getElementById('paymentMonth').value,
        amount: amount, foodCharge: food, lateFee: late, total: amount + food + late,
        method: document.getElementById('paymentMethod').value,
        status: 'PENDING',
        paymentDate: new Date().toISOString().split('T')[0]
    };
    try {
        const url = editingPaymentId ? `${API_BASE}/payments/${editingPaymentId}` : `${API_BASE}/payments`;
        const response = await apiFetch(url, { method: editingPaymentId ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
        const result = await response.json();
        if (result.success) { showToast('Payment saved!'); closeModal('paymentModal'); loadPayments(); loadDashboard(); }
        else { showToast(result.message || 'Failed', 'error'); }
    } catch (error) { console.error('Error:', error); showToast('Failed', 'error'); }
});

document.getElementById('cleaningForm').addEventListener('submit', async function (e) {
    e.preventDefault();
    let area = document.getElementById('cleaningArea').value;
    let assignedTo = null;

    if (area === 'Other') {
        area = document.getElementById('cleaningAreaCustom').value.trim();
        if (!area) { showToast('Please enter a custom area name', 'error'); return; }
        assignedTo = document.getElementById('cleaningAssignedTo').value || null;
    } else if (area === 'Room Clean') {
        const roomSelect = document.getElementById('cleaningRoomSelect');
        const roomNumber = roomSelect.options[roomSelect.selectedIndex]?.getAttribute('data-number');
        if (!roomSelect.value) { showToast('Please select a room', 'error'); return; }
        area = 'Room ' + roomNumber;
        // Get checked residents
        const checked = document.querySelectorAll('.cleaning-resident-cb:checked');
        const names = Array.from(checked).map(cb => cb.value);
        assignedTo = names.length > 0 ? names.join(', ') : null;
    } else {
        assignedTo = document.getElementById('cleaningAssignedTo').value || null;
    }

    const data = {
        area: area,
        dayOfWeek: document.getElementById('cleaningDay').value,
        timeSlot: document.getElementById('cleaningTime').value,
        assignedStaff: document.getElementById('cleaningStaff').value,
        assignedTo: assignedTo,
        scheduledDate: document.getElementById('cleaningScheduledDate').value || null,
        notes: document.getElementById('cleaningNotes').value || null,
        completionStatus: 'Pending'
    };
    try {
        const response = await apiFetch(`${API_BASE}/cleaning`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
        const result = await response.json();
        if (result.success) { showToast('Task added!'); closeModal('cleaningModal'); loadCleaning(); }
        else { showToast(result.message || 'Failed', 'error'); }
    } catch (error) { console.error('Error:', error); showToast('Failed', 'error'); }
});

// Edit functions
async function editRoom(id) {
    try {
        const response = await apiFetch(`${API_BASE}/rooms/${id}`);
        const result = await response.json();
        if (result.success && result.data) {
            const room = result.data;
            editingRoomId = id;
            document.getElementById('roomNumber').value = room.roomNumber || '';
            document.getElementById('roomType').value = room.roomType || '';
            document.getElementById('pricePerMonth').value = room.pricePerMonth || '';
            document.getElementById('capacity').value = room.capacity || '';
            document.getElementById('facilities').value = room.facilities || '';
            document.getElementById('imageUrl').value = room.imageUrl || '';
            document.getElementById('roomStatus').value = room.status || 'AVAILABLE';
            document.querySelector('#roomModal .modal-title').textContent = 'Edit Room';
            document.getElementById('roomModal').classList.add('active');
        }
    } catch (error) { console.error('Error:', error); showToast('Failed to load', 'error'); }
}

async function editResident(id) {
    try {
        const response = await apiFetch(`${API_BASE}/residents/${id}`);
        const result = await response.json();
        if (result.success && result.data) {
            const resident = result.data;
            editingResidentId = id;
            document.getElementById('residentName').value = resident.name || '';
            document.getElementById('residentNic').value = resident.nic || '';
            document.getElementById('residentContact').value = resident.contact || '';
            document.getElementById('residentEmail').value = resident.email || '';
            document.getElementById('residentCourse').value = resident.course || '';
            document.getElementById('residentStatus').value = resident.status || 'ACTIVE';
            await loadAvailableRoomsForSelect('residentRoom');
            document.querySelector('#residentModal .modal-title').textContent = 'Edit Resident';
            document.getElementById('residentModal').classList.add('active');
        }
    } catch (error) { console.error('Error:', error); showToast('Failed to load', 'error'); }
}

async function editPayment(id) {
    const payment = allPayments.find(p => p.id === id);
    if (!payment) return;
    editingPaymentId = id;
    await loadResidentsForSelect('paymentResident');
    document.getElementById('paymentResident').value = payment.residentId || '';
    document.getElementById('paymentMonth').value = payment.month || '';
    document.getElementById('paymentAmount').value = payment.amount || '';
    document.getElementById('paymentFood').value = payment.foodCharge || 0;
    document.getElementById('paymentLate').value = payment.lateFee || 0;
    document.getElementById('paymentMethod').value = payment.method || 'CASH';
    document.querySelector('#paymentModal .modal-title').textContent = 'Edit Payment';
    document.getElementById('paymentModal').classList.add('active');
}

async function assignRoomToResident(residentId) {
    const resident = allResidents.find(r => r.id === residentId);
    if (!resident) return;
    try {
        const roomsRes = await apiFetch(`${API_BASE}/rooms/available`);
        const roomsData = await roomsRes.json();
        const rooms = roomsData.data || [];
        if (rooms.length === 0) { showToast('No available rooms!', 'warning'); return; }
        let modal = document.getElementById('assignRoomModal');
        if (!modal) { modal = document.createElement('div'); modal.id = 'assignRoomModal'; modal.className = 'modal'; document.body.appendChild(modal); }
        modal.innerHTML = `
            <div class="modal-content" style="max-width: 400px;">
                <div class="modal-header"><h3 class="modal-title">Assign Room to ${resident.name}</h3><button class="close-btn" onclick="closeModal('assignRoomModal')">&times;</button></div>
                <div class="form-group"><label>Select Room *</label><select id="assignRoomSelect">${rooms.map(r => `<option value="${r.id}">${r.roomNumber} - ${r.roomType}</option>`).join('')}</select></div>
                <button onclick="confirmAssignRoom(${residentId})" class="btn btn-primary" style="width: 100%;"><i class="fas fa-check"></i> Assign</button>
            </div>
        `;
        modal.classList.add('active');
    } catch (error) { console.error('Error:', error); showToast('Failed', 'error'); }
}

async function confirmAssignRoom(residentId) {
    const roomId = document.getElementById('assignRoomSelect').value;
    try {
        const response = await apiFetch(`${API_BASE}/residents/${residentId}/assign-room/${roomId}`, { method: 'PUT' });
        const result = await response.json();
        if (result.success) { showToast('Room assigned!'); closeModal('assignRoomModal'); loadResidents(); loadRooms(); }
        else { showToast(result.message || 'Failed', 'error'); }
    } catch (error) { console.error('Error:', error); showToast('Failed', 'error'); }
}

// Delete functions
async function deleteRoom(id) { if (!confirm('Delete this room?')) return; try { const r = await apiFetch(`${API_BASE}/rooms/${id}`, { method: 'DELETE' }); const res = await r.json(); if (res.success) { showToast('Deleted!'); loadRooms(); } else showToast(res.message || 'Failed', 'error'); } catch (e) { showToast('Failed', 'error'); } }
async function deleteResident(id) { if (!confirm('Delete this resident?')) return; try { const r = await apiFetch(`${API_BASE}/residents/${id}`, { method: 'DELETE' }); const res = await r.json(); if (res.success) { showToast('Deleted!'); loadResidents(); } else showToast(res.message || 'Failed', 'error'); } catch (e) { showToast('Failed', 'error'); } }
async function deletePayment(id) { if (!confirm('Delete this payment?')) return; try { const r = await apiFetch(`${API_BASE}/payments/${id}`, { method: 'DELETE' }); const res = await r.json(); if (res.success) { showToast('Deleted!'); loadPayments(); loadDashboard(); } else showToast(res.message || 'Failed', 'error'); } catch (e) { showToast('Failed', 'error'); } }
// deleteComplaint is defined in the complaints section above
async function deleteVisit(id) { if (!confirm('Delete this visit?')) return; try { const r = await apiFetch(`${API_BASE}/visits/${id}`, { method: 'DELETE' }); const res = await r.json(); if (res.success) { showToast('Deleted!'); loadVisits(); } else showToast(res.message || 'Failed', 'error'); } catch (e) { showToast('Failed', 'error'); } }
async function deleteCleaning(id) { if (!confirm('Delete this task?')) return; try { const r = await apiFetch(`${API_BASE}/cleaning/${id}`, { method: 'DELETE' }); const res = await r.json(); if (res.success) { showToast('Deleted!'); loadCleaning(); } else showToast(res.message || 'Failed', 'error'); } catch (e) { showToast('Failed', 'error'); } }

async function updateVisitStatus(id, status) {
    try { const r = await apiFetch(`${API_BASE}/visits/${id}/status?status=${status}`, { method: 'PUT' }); const res = await r.json(); if (res.success) { showToast('Updated!'); loadVisits(); } else showToast(res.message || 'Failed', 'error'); } catch (e) { showToast('Failed', 'error'); }
}

async function markCleaningComplete(id) {
    try { const r = await apiFetch(`${API_BASE}/cleaning/${id}/complete?completedBy=Admin`, { method: 'PUT' }); const res = await r.json(); if (res.success) { showToast('Completed!'); loadCleaning(); } else showToast(res.message || 'Failed', 'error'); } catch (e) { showToast('Failed', 'error'); }
}

// ============ ATTENDANCE FUNCTIONS ============
let allAttendance = [];

async function loadAttendance() {
    try {
        // Get all attendance records
        const response = await apiFetch(`${API_BASE}/attendance`);
        const result = await response.json();
        allAttendance = result.data || [];

        // Get all residents to show unmarked ones too
        const residentsRes = await apiFetch(`${API_BASE}/residents`);
        const residentsData = await residentsRes.json();
        const allResidentsList = residentsData.data || [];

        // Set default date to today
        const dateInput = document.getElementById('attendanceDate');
        if (dateInput && !dateInput.value) {
            dateInput.value = new Date().toISOString().split('T')[0];
        }

        // Load stats
        const statsRes = await apiFetch(`${API_BASE}/attendance/stats`);
        const statsData = await statsRes.json();
        const stats = statsData.data || {};

        document.getElementById('attendanceStatsContainer').innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 20px;">
                <div style="background: rgba(34,197,94,0.1); padding: 20px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 28px; font-weight: 700; color: #22c55e;">${stats.present || 0}</div>
                    <div style="color: #666; font-size: 13px;">Present Today</div>
                </div>
                <div style="background: rgba(239,68,68,0.1); padding: 20px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 28px; font-weight: 700; color: #ef4444;">${stats.absent || 0}</div>
                    <div style="color: #666; font-size: 13px;">Absent Today</div>
                </div>
                <div style="background: rgba(245,158,11,0.1); padding: 20px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 28px; font-weight: 700; color: #f59e0b;">${stats.leave || 0}</div>
                    <div style="color: #666; font-size: 13px;">On Leave</div>
                </div>
                <div style="background: rgba(99,102,241,0.1); padding: 20px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 28px; font-weight: 700; color: #6366f1;">${allResidentsList.filter(r => r.status === 'ACTIVE').length}</div>
                    <div style="color: #666; font-size: 13px;">Total Residents</div>
                </div>
            </div>
        `;

        // Get today's date
        const today = new Date().toISOString().split('T')[0];

        // Filter attendance for today
        const todayAttendance = allAttendance.filter(a => a.date === today);

        // Create a map of resident IDs who have attendance marked today
        const markedResidentIds = new Set(todayAttendance.map(a => a.residentId));

        // Get unmarked active residents
        const unmarkedResidents = allResidentsList.filter(r =>
            r.status === 'ACTIVE' && !markedResidentIds.has(r.id)
        );

        const tbody = document.getElementById('attendanceTableBody');

        let tableContent = '';

        // Show marked attendance first
        if (todayAttendance.length > 0) {
            tableContent += todayAttendance.map(a => `
                <tr>
                    <td><strong>${a.residentName || 'N/A'}</strong></td>
                    <td>${a.roomNumber || 'N/A'}</td>
                    <td>${a.date || 'N/A'}</td>
                    <td><span class="badge badge-${getAttendanceStatusColor(a.status)}">${a.status}</span></td>
                    <td>${a.checkInTime || '-'}</td>
                    <td>${a.checkOutTime || '-'}</td>
                    <td>${a.remarks || '-'}</td>
                    <td>
                        <button class="btn btn-sm btn-success" onclick="quickMarkAttendance(${a.residentId}, 'PRESENT')" title="Mark Present"><i class="fas fa-check"></i></button>
                        <button class="btn btn-sm btn-warning" onclick="quickMarkAttendance(${a.residentId}, 'LEAVE')" title="Mark Leave"><i class="fas fa-plane"></i></button>
                        <button class="btn btn-sm btn-danger" onclick="quickMarkAttendance(${a.residentId}, 'ABSENT')" title="Mark Absent"><i class="fas fa-times"></i></button>
                    </td>
                </tr>
            `).join('');
        }

        // Show unmarked residents
        if (unmarkedResidents.length > 0) {
            tableContent += unmarkedResidents.map(r => `
                <tr style="background: rgba(245,158,11,0.05);">
                    <td><strong>${r.name || 'N/A'}</strong></td>
                    <td>${r.roomNumber || 'N/A'}</td>
                    <td>${today}</td>
                    <td><span class="badge badge-info">NOT MARKED</span></td>
                    <td>-</td>
                    <td>-</td>
                    <td>-</td>
                    <td>
                        <button class="btn btn-sm btn-success" onclick="quickMarkAttendance(${r.id}, 'PRESENT')" title="Mark Present"><i class="fas fa-check"></i></button>
                        <button class="btn btn-sm btn-warning" onclick="quickMarkAttendance(${r.id}, 'LEAVE')" title="Mark Leave"><i class="fas fa-plane"></i></button>
                        <button class="btn btn-sm btn-danger" onclick="quickMarkAttendance(${r.id}, 'ABSENT')" title="Mark Absent"><i class="fas fa-times"></i></button>
                    </td>
                </tr>
            `).join('');
        }

        if (tableContent) {
            tbody.innerHTML = tableContent;
        } else {
            tbody.innerHTML = '<tr><td colspan="8" class="empty-state"><i class="fas fa-clipboard-check"></i><p>No residents found</p></td></tr>';
        }
    } catch (error) {
        console.error('Error loading attendance:', error);
        document.getElementById('attendanceTableBody').innerHTML = '<tr><td colspan="8" class="empty-state"><i class="fas fa-exclamation-triangle"></i><p>Error loading attendance</p></td></tr>';
    }
}

async function showMarkAttendanceModal() {
    document.getElementById('attendanceForm').reset();
    document.getElementById('attendanceFormDate').value = new Date().toISOString().split('T')[0];
    await loadResidentsForSelect('attendanceResident');
    document.getElementById('attendanceModal').classList.add('active');
}

// Attendance form submission
const attendanceFormEl = document.getElementById('attendanceForm');
if (attendanceFormEl) {
    attendanceFormEl.addEventListener('submit', async function (e) {
        e.preventDefault();
        const data = {
            residentId: parseInt(document.getElementById('attendanceResident').value),
            date: document.getElementById('attendanceFormDate').value,
            status: document.getElementById('attendanceStatus').value,
            checkInTime: document.getElementById('attendanceCheckIn').value || null,
            checkOutTime: document.getElementById('attendanceCheckOut').value || null,
            remarks: document.getElementById('attendanceRemarks').value || null
        };
        try {
            const response = await apiFetch(`${API_BASE}/attendance`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data)
            });
            const result = await response.json();
            if (result.success) { showToast('Attendance marked!'); closeModal('attendanceModal'); loadAttendance(); }
            else { showToast(result.message || 'Failed', 'error'); }
        } catch (error) {
            console.error('Error marking attendance:', error);
            showToast('Failed to mark attendance', 'error');
        }
    });
}

async function quickMarkAttendance(residentId, status) {
    try {
        const response = await apiFetch(`${API_BASE}/attendance/mark/${residentId}?status=${status}`, { method: 'POST' });
        const result = await response.json();
        if (result.success) { showToast(`Marked as ${status}!`); loadAttendance(); }
        else { showToast(result.message || 'Failed', 'error'); }
    } catch (error) { showToast('Failed', 'error'); }
}

async function markAllPresent() {
    if (!confirm('Mark all active residents as PRESENT for today?')) return;

    try {
        // Get all active residents
        const residentsRes = await apiFetch(`${API_BASE}/residents`);
        const residentsData = await residentsRes.json();
        const activeResidents = (residentsData.data || []).filter(r => r.status === 'ACTIVE');

        showToast('Marking attendance...', 'warning');

        // Mark each resident as present
        let successCount = 0;
        for (const resident of activeResidents) {
            try {
                const response = await apiFetch(`${API_BASE}/attendance/mark/${resident.id}?status=PRESENT`, { method: 'POST' });
                const result = await response.json();
                if (result.success) successCount++;
            } catch (e) { console.error('Error marking', resident.id, e); }
        }

        showToast(`Marked ${successCount} residents as present!`);
        loadAttendance();
    } catch (error) {
        console.error('Error:', error);
        showToast('Failed to mark all present', 'error');
    }
}

async function deleteAttendance(id) {
    if (!confirm('Delete this attendance record?')) return;
    try {
        const r = await apiFetch(`${API_BASE}/attendance/${id}`, { method: 'DELETE' });
        const res = await r.json();
        if (res.success) { showToast('Deleted!'); loadAttendance(); }
        else { showToast(res.message || 'Failed', 'error'); }
    } catch (e) { showToast('Failed', 'error'); }
}

function getAttendanceStatusColor(status) {
    return { 'PRESENT': 'success', 'ABSENT': 'danger', 'LEAVE': 'warning' }[status] || 'info';
}

// ============ FOOD PREFERENCE FUNCTIONS ============
let allFoodPreferences = [];

async function loadFoodPreferences() {
    try {
        const response = await apiFetch(`${API_BASE}/food`);
        const result = await response.json();
        allFoodPreferences = result.data || [];

        // Set default date to today
        const dateInput = document.getElementById('foodDate');
        if (dateInput && !dateInput.value) {
            dateInput.value = new Date().toISOString().split('T')[0];
        }

        // Load stats
        const statsRes = await apiFetch(`${API_BASE}/food/stats`);
        const statsData = await statsRes.json();
        const stats = statsData.data || {};

        document.getElementById('foodStatsContainer').innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(5, 1fr); gap: 15px; margin-bottom: 20px;">
                <div style="background: rgba(245,158,11,0.1); padding: 15px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 24px; font-weight: 700; color: #f59e0b;">${stats.breakfast || 0}</div>
                    <div style="color: #666; font-size: 12px;">Breakfast</div>
                </div>
                <div style="background: rgba(34,197,94,0.1); padding: 15px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 24px; font-weight: 700; color: #22c55e;">${stats.lunch || 0}</div>
                    <div style="color: #666; font-size: 12px;">Lunch</div>
                </div>
                <div style="background: rgba(99,102,241,0.1); padding: 15px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 24px; font-weight: 700; color: #6366f1;">${stats.dinner || 0}</div>
                    <div style="color: #666; font-size: 12px;">Dinner</div>
                </div>
                <div style="background: rgba(34,197,94,0.1); padding: 15px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 24px; font-weight: 700; color: #22c55e;">${stats.veg || 0}</div>
                    <div style="color: #666; font-size: 12px;">Veg</div>
                </div>
                <div style="background: rgba(239,68,68,0.1); padding: 15px; border-radius: 12px; text-align: center;">
                    <div style="font-size: 24px; font-weight: 700; color: #ef4444;">${stats.nonVeg || 0}</div>
                    <div style="color: #666; font-size: 12px;">Non-Veg</div>
                </div>
            </div>
        `;

        const tbody = document.getElementById('foodTableBody');
        if (allFoodPreferences.length > 0) {
            tbody.innerHTML = allFoodPreferences.map(f => `
                <tr>
                    <td><strong>${f.residentName || 'N/A'}</strong></td>
                    <td>${f.roomNumber || 'N/A'}</td>
                    <td>${f.date || 'N/A'}</td>
                    <td>${f.breakfast ? '<i class="fas fa-check-circle" style="color:#22c55e;"></i>' : '<i class="fas fa-times-circle" style="color:#ef4444;"></i>'}</td>
                    <td>${f.lunch ? '<i class="fas fa-check-circle" style="color:#22c55e;"></i>' : '<i class="fas fa-times-circle" style="color:#ef4444;"></i>'}</td>
                    <td>${f.dinner ? '<i class="fas fa-check-circle" style="color:#22c55e;"></i>' : '<i class="fas fa-times-circle" style="color:#ef4444;"></i>'}</td>
                    <td><span class="badge badge-${f.mealType === 'VEG' ? 'success' : f.mealType === 'NON_VEG' ? 'danger' : 'info'}">${f.mealType || 'N/A'}</span></td>
                    <td>${f.specialRequirements || '-'}</td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="8" class="empty-state"><i class="fas fa-utensils"></i><p>No food preferences found</p></td></tr>';
        }
    } catch (error) {
        console.error('Error loading food preferences:', error);
    }
}

async function loadFoodByDate() {
    const date = document.getElementById('foodDate').value;
    if (!date) { showToast('Please select a date', 'warning'); return; }
    try {
        const response = await apiFetch(`${API_BASE}/food/date/${date}`);
        const result = await response.json();
        allFoodPreferences = result.data || [];

        const tbody = document.getElementById('foodTableBody');
        if (allFoodPreferences.length > 0) {
            tbody.innerHTML = allFoodPreferences.map(f => `
                <tr>
                    <td><strong>${f.residentName || 'N/A'}</strong></td>
                    <td>${f.roomNumber || 'N/A'}</td>
                    <td>${f.date || 'N/A'}</td>
                    <td>${f.breakfast ? '<i class="fas fa-check-circle" style="color:#22c55e;"></i>' : '<i class="fas fa-times-circle" style="color:#ef4444;"></i>'}</td>
                    <td>${f.lunch ? '<i class="fas fa-check-circle" style="color:#22c55e;"></i>' : '<i class="fas fa-times-circle" style="color:#ef4444;"></i>'}</td>
                    <td>${f.dinner ? '<i class="fas fa-check-circle" style="color:#22c55e;"></i>' : '<i class="fas fa-times-circle" style="color:#ef4444;"></i>'}</td>
                    <td><span class="badge badge-${f.mealType === 'VEG' ? 'success' : f.mealType === 'NON_VEG' ? 'danger' : 'info'}">${f.mealType || 'N/A'}</span></td>
                    <td>${f.specialRequirements || '-'}</td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `<tr><td colspan="8" class="empty-state"><i class="fas fa-utensils"></i><p>No preferences for ${date}</p></td></tr>`;
        }
        showToast(`Loaded preferences for ${date}`);
    } catch (error) {
        console.error('Error:', error);
        showToast('Failed to load', 'error');
    }
}

// Badge colors
function getStatusColor(status) { return { 'AVAILABLE': 'success', 'OCCUPIED': 'warning', 'MAINTENANCE': 'danger', 'ACTIVE': 'success', 'PENDING': 'warning', 'INACTIVE': 'danger' }[status] || 'info'; }
function getPaymentStatusColor(status) { return { 'PAID': 'success', 'PENDING': 'warning', 'LATE': 'danger' }[status] || 'info'; }
function getComplaintStatusColor(status) { return { 'PENDING': 'warning', 'IN_PROGRESS': 'info', 'WILL_TAKE_ACTION': 'primary', 'RESOLVED': 'success', 'REJECTED': 'danger' }[status] || 'info'; }
function getPriorityColor(priority) { return { 'LOW': 'info', 'MEDIUM': 'warning', 'HIGH': 'danger' }[priority] || 'info'; }
function getVisitStatusColor(status) { return { 'NEW': 'info', 'CONTACTED': 'warning', 'CLOSED': 'success' }[status] || 'info'; }

// Auto refresh
function startAutoRefresh() {
    if (autoRefreshInterval) clearInterval(autoRefreshInterval);
    autoRefreshInterval = setInterval(() => {
        const activeSection = document.querySelector('.nav-link.active')?.dataset.section || 'dashboard';
        loadSectionData(activeSection);
        updateLastRefresh();
    }, 30000);
}

// Initialize
loadDashboard();
startAutoRefresh();
