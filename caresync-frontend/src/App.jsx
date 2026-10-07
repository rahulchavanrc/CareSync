import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';

import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import FindDoctors from './pages/patient/FindDoctors';
import DoctorDetail from './pages/patient/DoctorDetail';
import MyAppointments from './pages/patient/MyAppointments';
import MyRecords from './pages/patient/MyRecords';
import Schedule from './pages/doctor/Schedule';
import CreateDoctor from './pages/admin/CreateDoctor';
import AdminDashboard from './pages/admin/AdminDashboard';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route path="/doctors" element={
              <ProtectedRoute roles={['PATIENT']}><FindDoctors /></ProtectedRoute>
            } />
            <Route path="/doctors/:doctorId" element={
              <ProtectedRoute roles={['PATIENT']}><DoctorDetail /></ProtectedRoute>
            } />
            <Route path="/appointments" element={
              <ProtectedRoute roles={['PATIENT']}><MyAppointments /></ProtectedRoute>
            } />
            <Route path="/records" element={
              <ProtectedRoute roles={['PATIENT']}><MyRecords /></ProtectedRoute>
            } />

            <Route path="/schedule" element={
              <ProtectedRoute roles={['DOCTOR']}><Schedule /></ProtectedRoute>
            } />

            <Route path="/admin" element={
              <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
            } />
            <Route path="/admin/doctors" element={
              <ProtectedRoute roles={['ADMIN']}><CreateDoctor /></ProtectedRoute>
            } />

            <Route path="*" element={<Landing />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
