import client from './client';

// --- Auth ---
export const registerPatient = (payload) => client.post('/api/auth/register', payload);
export const login = (payload) => client.post('/api/auth/login', payload);
export const createDoctor = (payload) => client.post('/api/admin/doctors', payload);
export const getDashboardStats = () => client.get('/api/admin/dashboard');
export const getAllDoctorsAdmin = () => client.get('/api/admin/doctors');
export const getAllPatientsAdmin = () => client.get('/api/admin/patients');

// --- Doctors ---
export const getDoctors = (specialization) =>
  client.get('/api/doctors', { params: specialization ? { specialization } : {} });
export const getDoctor = (doctorId) => client.get(`/api/doctors/${doctorId}`);

// --- Patient ---
export const getMyPatientProfile = () => client.get('/api/patients/me');

// --- Appointments ---
export const bookAppointment = (payload) => client.post('/api/appointments/patient/book', payload);
export const getMyAppointments = () => client.get('/api/appointments/patient/me');
export const getMySchedule = () => client.get('/api/appointments/doctor/schedule');
export const updateAppointmentStatus = (appointmentId, status) =>
  client.patch(`/api/appointments/doctor/${appointmentId}/status`, { status });

// --- Medical Records ---
export const createMedicalRecord = (payload) => client.post('/api/records', payload);
export const getMyRecords = () => client.get('/api/records/me');
export const getRecord = (recordId) => client.get(`/api/records/${recordId}`);
export const downloadPrescriptionPdf = (recordId) =>
  client.get(`/api/records/${recordId}/prescription/pdf`, { responseType: 'blob' });

// --- Reviews ---
export const createReview = (payload) => client.post('/api/reviews', payload);
export const getDoctorReviews = (doctorId) => client.get(`/api/doctors/${doctorId}/reviews`);
