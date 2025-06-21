import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SubmitBehaviorPage from './pages/BehaviorPage';
import LeaderboardPage from './pages/LeaderboardPage';
import NavigationBar from './components/NavBar';
import StudentsPage from './pages/StudentsPage';
import AboutPage from './pages/AboutPage';
import './App.css';
import {StudentDashboard} from "./pages/StudentDashboard.tsx";
import {TeacherDashboard} from "./pages/TeacherDashboard.tsx";
import {AdminDashboard} from "./pages/AdminDashboard.tsx";

function App() {

  return (
        <Router>
            <NavigationBar />
            <Routes>
                <Route path="/" element={<AboutPage />} />
                <Route path="/students" element={<StudentsPage />} />
                <Route path="/brag" element={<SubmitBehaviorPage />} />
                <Route path="/leaderboard" element={<LeaderboardPage />} />
                <Route path="/dashboard/teacher" element={<TeacherDashboard />} />
                <Route path="/dashboard/student" element={<StudentDashboard />} />
                <Route path="/dashboard/admin" element={<AdminDashboard />} />
            </Routes>
        </Router>
    )
}

export default App
