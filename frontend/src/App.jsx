import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'

// Placeholder page components (will be built in Phases 4-6)
const Dashboard = () => (
  <div>
    <h2>Dashboard</h2>
    <p>Complaint & Maintenance Management Dashboard — Coming in Phase 6</p>
  </div>
)

const ComplaintList = () => (
  <div>
    <h2>All Complaints</h2>
    <p>Complaint list with status badges — Coming in Phase 4</p>
  </div>
)

const ComplaintForm = () => (
  <div>
    <h2>Submit New Complaint</h2>
    <p>Complaint submission form — Coming in Phase 4</p>
  </div>
)

const ComplaintDetail = () => (
  <div>
    <h2>Complaint Detail</h2>
    <p>View and update complaint status — Coming in Phase 4</p>
  </div>
)

const MaintenanceList = () => (
  <div>
    <h2>Maintenance Requests</h2>
    <p>Maintenance request list with filters — Coming in Phase 5</p>
  </div>
)

const MaintenanceForm = () => (
  <div>
    <h2>Submit Maintenance Request</h2>
    <p>Maintenance request form — Coming in Phase 5</p>
  </div>
)

const MaintenanceDetail = () => (
  <div>
    <h2>Maintenance Request Detail</h2>
    <p>View and update maintenance request — Coming in Phase 5</p>
  </div>
)

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/complaints" element={<ComplaintList />} />
        <Route path="/complaints/new" element={<ComplaintForm />} />
        <Route path="/complaints/:id" element={<ComplaintDetail />} />
        <Route path="/maintenance" element={<MaintenanceList />} />
        <Route path="/maintenance/new" element={<MaintenanceForm />} />
        <Route path="/maintenance/:id" element={<MaintenanceDetail />} />
      </Routes>
    </Layout>
  )
}

export default App
