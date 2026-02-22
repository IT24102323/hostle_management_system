import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import './ComplaintForm.css'

const categories = [
    { value: '', label: 'Select Category', icon: 'fa-solid fa-list' },
    { value: 'Noise', label: 'Noise', icon: 'fa-solid fa-volume-high' },
    { value: 'Cleanliness', label: 'Cleanliness', icon: 'fa-solid fa-broom' },
    { value: 'Facilities', label: 'Facilities', icon: 'fa-solid fa-building' },
    { value: 'Roommate', label: 'Roommate', icon: 'fa-solid fa-user-group' },
    { value: 'Security', label: 'Security', icon: 'fa-solid fa-shield-halved' },
    { value: 'Other', label: 'Other', icon: 'fa-solid fa-ellipsis' },
]

const priorities = [
    { value: 'Low', label: 'Low', color: '#22c55e' },
    { value: 'Medium', label: 'Medium', color: '#f59e0b' },
    { value: 'High', label: 'High', color: '#f97316' },
    { value: 'Urgent', label: 'Urgent', color: '#ef4444' },
]

function ComplaintForm() {
    const navigate = useNavigate()

    const [formData, setFormData] = useState({
        studentName: '',
        studentId: '',
        roomNumber: '',
        category: '',
        subject: '',
        description: '',
        priority: 'Medium',
    })

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState(false)

    const handleChange = (e) => {
        const { name, value } = e.target
        setFormData((prev) => ({ ...prev, [name]: value }))
        if (error) setError('')
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        setError('')

        try {
            await axios.post('/api/complaints', formData)
            setSuccess(true)
            setTimeout(() => {
                navigate('/complaints')
            }, 2000)
        } catch (err) {
            const msg =
                err.response?.data?.message ||
                err.response?.data?.errors?.join(', ') ||
                'Something went wrong. Please try again.'
            setError(msg)
        } finally {
            setLoading(false)
        }
    }

    if (success) {
        return (
            <div className="cf-success-container">
                <div className="cf-success-card">
                    <div className="cf-success-icon">
                        <i className="fa-solid fa-circle-check"></i>
                    </div>
                    <h2>Complaint Submitted!</h2>
                    <p>Your complaint has been recorded and will be reviewed shortly.</p>
                    <p className="cf-redirect-text">
                        <i className="fa-solid fa-spinner fa-spin"></i> Redirecting to complaints list...
                    </p>
                </div>
            </div>
        )
    }

    return (
        <div className="cf-container">
            <div className="cf-header">
                <div className="cf-header-icon">
                    <i className="fa-solid fa-file-circle-plus"></i>
                </div>
                <div>
                    <h2>Submit a Complaint</h2>
                    <p>Fill in the details below to submit your complaint to the hostel management.</p>
                </div>
            </div>

            {error && (
                <div className="cf-alert cf-alert-error">
                    <i className="fa-solid fa-triangle-exclamation"></i>
                    <span>{error}</span>
                </div>
            )}

            <form onSubmit={handleSubmit} className="cf-form">
                {/* Student Information Section */}
                <div className="cf-section">
                    <h3 className="cf-section-title">
                        <i className="fa-solid fa-user-graduate"></i> Student Information
                    </h3>
                    <div className="cf-grid cf-grid-3">
                        <div className="cf-field">
                            <label htmlFor="studentName">
                                <i className="fa-solid fa-user"></i> Full Name
                            </label>
                            <input
                                type="text"
                                id="studentName"
                                name="studentName"
                                value={formData.studentName}
                                onChange={handleChange}
                                placeholder="e.g. John Doe"
                                required
                            />
                        </div>
                        <div className="cf-field">
                            <label htmlFor="studentId">
                                <i className="fa-solid fa-id-card"></i> Student ID
                            </label>
                            <input
                                type="text"
                                id="studentId"
                                name="studentId"
                                value={formData.studentId}
                                onChange={handleChange}
                                placeholder="e.g. STU001"
                                required
                            />
                        </div>
                        <div className="cf-field">
                            <label htmlFor="roomNumber">
                                <i className="fa-solid fa-door-open"></i> Room Number
                            </label>
                            <input
                                type="text"
                                id="roomNumber"
                                name="roomNumber"
                                value={formData.roomNumber}
                                onChange={handleChange}
                                placeholder="e.g. A-101"
                                required
                            />
                        </div>
                    </div>
                </div>

                {/* Complaint Details Section */}
                <div className="cf-section">
                    <h3 className="cf-section-title">
                        <i className="fa-solid fa-clipboard-list"></i> Complaint Details
                    </h3>

                    <div className="cf-grid cf-grid-2">
                        <div className="cf-field">
                            <label htmlFor="category">
                                <i className="fa-solid fa-tag"></i> Category
                            </label>
                            <select
                                id="category"
                                name="category"
                                value={formData.category}
                                onChange={handleChange}
                                required
                            >
                                {categories.map((cat) => (
                                    <option key={cat.value} value={cat.value} disabled={cat.value === ''}>
                                        {cat.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="cf-field">
                            <label>
                                <i className="fa-solid fa-flag"></i> Priority
                            </label>
                            <div className="cf-priority-group">
                                {priorities.map((p) => (
                                    <label
                                        key={p.value}
                                        className={`cf-priority-btn ${formData.priority === p.value ? 'active' : ''}`}
                                        style={{
                                            '--priority-color': p.color,
                                        }}
                                    >
                                        <input
                                            type="radio"
                                            name="priority"
                                            value={p.value}
                                            checked={formData.priority === p.value}
                                            onChange={handleChange}
                                        />
                                        {p.label}
                                    </label>
                                ))}
                            </div>
                        </div>
                    </div>

                    <div className="cf-field">
                        <label htmlFor="subject">
                            <i className="fa-solid fa-heading"></i> Subject
                        </label>
                        <input
                            type="text"
                            id="subject"
                            name="subject"
                            value={formData.subject}
                            onChange={handleChange}
                            placeholder="Brief subject of your complaint"
                            maxLength={150}
                            required
                        />
                        <span className="cf-char-count">{formData.subject.length}/150</span>
                    </div>

                    <div className="cf-field">
                        <label htmlFor="description">
                            <i className="fa-solid fa-align-left"></i> Description
                        </label>
                        <textarea
                            id="description"
                            name="description"
                            value={formData.description}
                            onChange={handleChange}
                            placeholder="Provide a detailed description of your complaint..."
                            rows={5}
                            maxLength={2000}
                            required
                        ></textarea>
                        <span className="cf-char-count">{formData.description.length}/2000</span>
                    </div>
                </div>

                {/* Action Buttons */}
                <div className="cf-actions">
                    <button
                        type="button"
                        className="cf-btn cf-btn-cancel"
                        onClick={() => navigate('/complaints')}
                    >
                        <i className="fa-solid fa-xmark"></i> Cancel
                    </button>
                    <button
                        type="submit"
                        className="cf-btn cf-btn-submit"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <i className="fa-solid fa-spinner fa-spin"></i> Submitting...
                            </>
                        ) : (
                            <>
                                <i className="fa-solid fa-paper-plane"></i> Submit Complaint
                            </>
                        )}
                    </button>
                </div>
            </form>
        </div>
    )
}

export default ComplaintForm
