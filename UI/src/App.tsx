import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AdminStaffDashboard } from './pages/AdminStaffDashboard';
import { StudentDashboard } from './pages/StudentDashboard';
import { TeacherDashboard } from './pages/TeacherDashboard';
import DashboardRedirect from './pages/DashboardRedirect';
import StudentRewardsPage from './pages/StudentRewardsPage';
import BehaviorTypesPage from './pages/BehaviorTypePage';
import LeaderboardPage from './pages/LeaderboardPage';
import RewardItemsPage from './pages/RewardItemsPage';
import { ParaDashboard } from './pages/ParaDashboard';
import BearBragPage from './pages/BearBragPage';
import StudentsPage from './pages/StudentsPage';
import TeachersPage from './pages/TeachersPage';
import BragLogsPage from './pages/BragLogsPage';
import UsersPage from './pages/UsersPage';
import AboutPage from './pages/AboutPage';
import { Provider } from 'react-redux';
import { store } from './store';

import 'react-datepicker/dist/react-datepicker.css';
import './App.css';

function App() {

  return (
      <Provider store={store}>
          <Router>
              <Routes>
                  <Route path="/" element={<AboutPage />} />
                  <Route path="/users" element={<UsersPage />} />
                  <Route path="/students" element={<StudentsPage />} />
                  <Route path="/teachers" element={<TeachersPage />} />
                  <Route path="/behaviors" element={<BehaviorTypesPage />} />
                  <Route path="/brags" element={<BragLogsPage />} />
                  <Route path="/rewards" element={<RewardItemsPage />} />
                  <Route path="/redemptions" element={<StudentRewardsPage />} />
                  <Route path="/brag" element={<BearBragPage />} />
                  <Route path="/leaderboard" element={<LeaderboardPage />} />
                  <Route path="/dashboard" element={<DashboardRedirect />} />
                  <Route path="/dashboard/teacher" element={<TeacherDashboard />} />
                  <Route path="/dashboard/student" element={<StudentDashboard />} />
                  <Route path="/dashboard/admin" element={<AdminStaffDashboard />} />
                  <Route path="/dashboard/para" element={<ParaDashboard />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
          </Router>
      </Provider>
    )
}

export default App;
