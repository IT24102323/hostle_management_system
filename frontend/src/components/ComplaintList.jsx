import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import './ComplaintList.css'

const statusConfig = {
    'Pending': { color: '#f59e0b', bg: '#fefce8', icon: 'fa-solid fa-clock' },
    'In Progress': { color: '#3b82f6', bg: '#eff6ff', icon: 'fa-solid fa-spinner' },
    'Resolved': { color: '#22c55e', bg: '#f0fdf4', icon: 'fa-solid fa-circle-check' },
    'Dismissed': { color: '#6b7280', bg: '#f3f4f6', icon: 'fa-solid fa-ban' },
}

const priorityConfig = {
    'Low': { color: '#22c55e', icon: 'fa-solid fa-arrow-down' },
    'Medium': { color: '#f59e0b', icon: 'fa-solid fa-minus' },
    'High': { color: '#f97316', icon: 'fa-solid fa-arrow-up' },
    'Urgent': { color: '#ef4444', icon: 'fa-solid fa-circle-exclamation' },
}

const categoryIcons = {
    'Noise': 'fa-solid fa-volume-high',
    'Cleanliness': 'fa-solid fa-broom',
    'Facilities': 'fa-solid fa-building',
    'Roommate': 'fa-solid fa-user-group',
    'Security': 'fa-solid fa-shield-halved',
    'Other': 'fa-solid fa-ellipsis',
}

function ComplaintList() {
    const [complaints, setComplaints] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [filterStatus, setFilterStatus] = useState('')
    const [filterCategory, setFilterCategory] = useState('')
    const [searchTerm, setSearchTerm] = useState('')

    useEffect(() => {
        fetchComplaints()
    }, [filterStatus, filterCategory])

    const fetchComplaints = async () => {
        try {
            setLoading(true)
            const params = new URLSearchParams()
            if (filterStatus) params.append('status', filterStatus)
            if (filterCategory) params.append('category', filterCategory)

            const res = await axios.get(`/api/complaints?${params.toString()}`)
            setComplaints(res.data.data)
        } catch (err) {
            setError('Failed to load complaints. Make sure the backend server is running.')
        } finally {
            setLoading(false)
        }
    }

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this complaint?')) return
        try {
            await axios.delete(`/api/complaints/${id}`)
            setComplaints((prev) => prev.filter((c) => c._id !== id))
        } catch {
            alert('Failed to delete complaint.')
        }
    }

    const filteredComplaints = complaints.filter((c) => {
        if (!searchTerm) return true
        const term = searchTerm.toLowerCase()
        return (
            c.subject.toLowerCase().includes(term) ||
            c.studentName.toLowerCase().includes(term) ||
            c.studentId.toLowerCase().includes(term) ||
            c.roomNumber.toLowerCase().includes(term)
        )
    })

    const formatDate = (dateStr) => {
        const d = new Date(dateStr)
        return d.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        })
    }

    return (
        <div className="cl-container">
            {/* Page Header */}
            <div className="cl-page-header">
                <div className="cl-page-header-left">
                    <div className="cl-page-icon">
                        <i className="fa-solid fa-clipboard-list"></i>
                    </div>
                    <div>
                        <h2>All Complaints</h2>
                        <p>{complaints.length} total complaint{complaints.length !== 1 ? 's' : ''}</p>
                    </div>
                </div>
                <Link to="/complaints/new" className="cl-btn-new">
                    <i className="fa-solid fa-plus"></i> New Complaint
                </Link>
            </div>

            {/* Filters Bar */}
            <div className="cl-filters">
                <div className="cl-search-box">
                    <i className="fa-solid fa-magnifying-glass"></i>
                    <input
                        type="text"
                        placeholder="Search by subject, name, ID, or room..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    className="cl-filter-select"
                >
                    <option value="">All Status</option>
                    <option value="Pending">Pending</option>
                    <option value="In Progress">In Progress</option>
                    <option value="Resolved">Resolved</option>
                    <option value="Dismissed">Dismissed</option>
                </select>
                <select
                    value={filterCategory}
                    onChange={(e) => setFilterCategory(e.target.value)}
                    className="cl-filter-select"
                >
                    <option value="">All Categories</option>
                    <option value="Noise">Noise</option>
                    <option value="Cleanliness">Cleanliness</option>
                    <option value="Facilities">Facilities</option>
                    <option value="Roommate">Roommate</option>
                    <option value="Security">Security</option>
                    <option value="Other">Other</option>
                </select>
            </div>

            {/* Error */}
            {error && (
                <div className="cl-alert-error">
                    <i className="fa-solid fa-triangle-exclamation"></i> {error}
                </div>
            )}

            {/* Loading */}
            {loading && (
                <div className="cl-loading">
                    <i className="fa-solid fa-spinner fa-spin"></i>
                    <span>Loading complaints...</span>
                </div>
            )}

            {/* Empty State */}
            {!loading && !error && filteredComplaints.length === 0 && (
                <div className="cl-empty">
                    <i className="fa-solid fa-inbox"></i>
                    <h3>No complaints found</h3>
                    <p>
                        {searchTerm || filterStatus || filterCategory
                            ? 'Try adjusting your filters or search.'
                            : 'No complaints have been submitted yet.'}
                    </p>
                    {!searchTerm && !filterStatus && !filterCategory && (
                        <Link to="/complaints/new" className="cl-btn-new cl-btn-sm">
                            <i className="fa-solid fa-plus"></i> Submit First Complaint
                        </Link>
                    )}
                </div>
            )}

            {/* Complaint Cards */}
            {!loading && filteredComplaints.length > 0 && (
                <div className="cl-list">
                    {filteredComplaints.map((complaint) => {
                        const status = statusConfig[complaint.status] || statusConfig['Pending']
                        const priority = priorityConfig[complaint.priority] || priorityConfig['Medium']
                        const catIcon = categoryIcons[complaint.category] || 'fa-solid fa-ellipsis'

                        return (
                            <div key={complaint._id} className="cl-card">
                                <div className="cl-card-top">
                                    <div className="cl-card-meta">
                                        <span
                                            className="cl-badge cl-badge-status"
                                            style={{ color: status.color, background: status.bg }}
                                        >
                                            <i className={status.icon}></i> {complaint.status}
                                        </span>
                                        <span
                                            className="cl-badge cl-badge-priority"
                                            style={{ color: priority.color }}
                                        >
                                            <i className={priority.icon}></i> {complaint.priority}
                                        </span>
                                        <span className="cl-badge cl-badge-category">
                                            <i className={catIcon}></i> {complaint.category}
                                        </span>
                                    </div>
                                    <div className="cl-card-actions">
                                        <Link
                                            to={`/complaints/${complaint._id}`}
                                            className="cl-action-btn cl-action-view"
                                            title="View Details"
                                        >
                                            <i className="fa-solid fa-eye"></i>
                                        </Link>
                                        <button
                                            className="cl-action-btn cl-action-delete"
                                            title="Delete"
                                            onClick={() => handleDelete(complaint._id)}
                                        >
                                            <i className="fa-solid fa-trash-can"></i>
                                        </button>
                                    </div>
                                </div>

                                <h3 className="cl-card-subject">{complaint.subject}</h3>
                                <p className="cl-card-desc">{complaint.description}</p>

                                <div className="cl-card-footer">
                                    <div className="cl-card-info">
                                        <span><i className="fa-solid fa-user"></i> {complaint.studentName}</span>
                                        <span><i className="fa-solid fa-id-card"></i> {complaint.studentId}</span>
                                        <span><i className="fa-solid fa-door-open"></i> {complaint.roomNumber}</span>
                                    </div>
                                    <span className="cl-card-date">
                                        <i className="fa-regular fa-calendar"></i> {formatDate(complaint.createdAt)}
                                    </span>
                                </div>
                            </div>
                        )
                    })}
                </div>
            )}
        </div>
    )
}

export default ComplaintList
