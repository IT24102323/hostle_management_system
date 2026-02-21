import { NavLink } from 'react-router-dom'

const navItems = [
    { path: '/', label: 'Dashboard', icon: '📊' },
    { path: '/complaints', label: 'Complaints', icon: '📋' },
    { path: '/complaints/new', label: 'New Complaint', icon: '➕' },
    { path: '/maintenance', label: 'Maintenance', icon: '🔧' },
    { path: '/maintenance/new', label: 'New Request', icon: '➕' },
]

function Layout({ children }) {
    return (
        <div className="app-layout">
            {/* Sidebar */}
            <aside className="sidebar">
                <div className="sidebar-header">
                    <h1>🏠 Hostel Management</h1>
                    <p>Complaint & Maintenance</p>
                </div>
                <nav className="sidebar-nav">
                    {navItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            end={item.path === '/'}
                            className={({ isActive }) =>
                                `nav-link ${isActive ? 'active' : ''}`
                            }
                        >
                            <span className="nav-icon">{item.icon}</span>
                            <span className="nav-label">{item.label}</span>
                        </NavLink>
                    ))}
                </nav>
            </aside>

            {/* Main Content */}
            <main className="main-content">
                <header className="content-header">
                    <h2>Complaint & Maintenance Module</h2>
                </header>
                <div className="content-body">
                    {children}
                </div>
            </main>
        </div>
    )
}

export default Layout
