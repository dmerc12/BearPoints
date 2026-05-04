import { Link } from 'react-router-dom';
import { Nav } from 'react-bootstrap';

const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '🏠' },
    { path: '/users', label: 'Users', icon: '👥' },
    { path: '/teachers', label: 'Teachers', icon: '👩‍🏫' },
    { path: '/students', label: 'Students', icon: '🧑‍🎓' },
    { path: '/behaviors', label: 'Behavior Types', icon: '📋' },
    { path: '/brags', label: 'Bear Brags', icon: '🌟' },
    { path: '/rewards', label: 'Reward Items', icon: '🎁' },
    { path: '/redemptions', label: 'Redeemed Rewards', icon: '🔄' },
    { path: '/leaderboard', label: 'Leaderboard', icon: '🏆' },
];

export default function Sidebar() {
    return (
        <Nav className="flex-column pt-3">
            {navItems.map(item => (
                <Nav.Link
                    as={Link}
                    to={item.path}
                    key={item.path}
                    className="d-flex align-items-center gap-2"
                >
                    <span>{item.icon}</span>
                    {item.label}
                </Nav.Link>
            ))}
        </Nav>
    );
}
