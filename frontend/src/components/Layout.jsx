import { NavLink } from 'react-router-dom'

const navItems = [
    { path: '/', label: 'Dashboard', icon: 'fa-solid fa-chart-pie' },
    { path: '/complaints', label: 'Complaints', icon: 'fa-solid fa-clipboard-list' },
    { path: '/complaints/new', label: 'New Complaint', icon: 'fa-solid fa-circle-plus' },
    { path: '/maintenance', label: 'Maintenance', icon: 'fa-solid fa-wrench' },
    { path: '/maintenance/new', label: 'New Request', icon: 'fa-solid fa-screwdriver-wrench' },
]

function Layout({ children }) {
    return (
        <div className="app-layout">
            {/* Sidebar */}
            <aside className="sidebar">
                <div className="sidebar-header">
                    <h1><i className="fa-solid fa-building"></i> Hostel Mgmt</h1>
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
                            <span className="nav-icon"><i className={item.icon}></i></span>
                            <span className="nav-label">{item.label}</span>
                        </NavLink>
                    ))}
                </nav>
            </aside>

            {/* Main Content */}
            <main className="main-content">
                <header className="content-header">
                    <h2><i className="fa-solid fa-tools" style={{ marginRight: '10px', opacity: 0.7 }}></i>Complaint & Maintenance Module</h2>
                </header>
                <div className="content-body">
                    {children}
                </div>
            </main>
        </div>
    )
}

export default Layout
