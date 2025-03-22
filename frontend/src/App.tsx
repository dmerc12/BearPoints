import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SubmitBehaviorPage from './pages/BehaviorPage';
import LeaderboardPage from './pages/LeaderboardPage';
import NavigationBar from './components/NavBar';
import StudentsPage from './pages/StudentsPage';
import AboutPage from './pages/AboutPage';


function App() {

  return (
    <Router>
      <NavigationBar />
      <Routes>
        <Route path="/" element={<AboutPage />} />
        <Route path="/students" element={<StudentsPage />} />
        <Route path="/behavior" element={<SubmitBehaviorPage />} />
        <Route path="/leaderboard" element={<LeaderboardPage />} />
      </Routes>
    </Router>
  )
}

export default App
