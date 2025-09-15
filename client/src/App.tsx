import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { StudentDashboard } from "./pages/StudentDashboard.tsx";
import { TeacherDashboard } from "./pages/TeacherDashboard.tsx";
import DashboardRedirect from "./pages/DashboardRedirect.tsx";
import { AdminDashboard } from "./pages/AdminDashboard.tsx";
import BearBragPage from './pages/BearBragPage.tsx';
import LeaderboardPage from './pages/LeaderboardPage';
import NavigationBar from './components/NavBar';
import StudentsPage from './pages/StudentsPage';
import AboutPage from './pages/AboutPage';
import { Provider } from 'react-redux';
import { store } from './store';
import './App.css';

function App() {

  return (
      <Provider store={store}>
          <Router>
              <NavigationBar />
              <Routes>
                  <Route path="/" element={<AboutPage />} />
                  <Route path="/students" element={<StudentsPage />} />
                  <Route path="/brag" element={<BearBragPage />} />
                  <Route path="/leaderboard" element={<LeaderboardPage />} />
                  <Route path="/dashboard" element={<DashboardRedirect />} />
                  <Route path="/dashboard/teacher" element={<TeacherDashboard />} />
                  <Route path="/dashboard/student" element={<StudentDashboard />} />
                  <Route path="/dashboard/admin" element={<AdminDashboard />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
          </Router>
      </Provider>
    )
}

export default App;
