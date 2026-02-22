import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import ComplaintForm from './components/ComplaintForm'
import ComplaintList from './components/ComplaintList'

// Placeholder page components (will be built later)
const Dashboard = () => (
  <div>
    <h2>Dashboard</h2>
    <p>Complaint & Maintenance Management Dashboard — Coming Soon</p>
  </div>
)

const ComplaintDetail = () => (
  <div>
    <h2>Complaint Detail</h2>
    <p>Update soon</p>
  </div>
)

const MaintenanceList = () => (
  <div>
    <h2>Maintenance Requests</h2>
    <p>update soon</p>
  </div>
)

const MaintenanceForm = () => (
  <div>
    <h2>Submit Maintenance Request</h2>
    <p>Upcoming fixing</p>
  </div>
)

const MaintenanceDetail = () => (
  <div>
    <h2>Maintenance Request Detail</h2>
    <p>update soon</p>
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
