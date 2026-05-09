import { LayoutDashboard, Users, PencilRuler, GraduationCap, ListChecks, Star, Gift, History, Trophy } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Nav } from 'react-bootstrap';

const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/users', label: 'Users', icon: Users },
    { path: '/teachers', label: 'Teachers', icon: PencilRuler },
    { path: '/students', label: 'Students', icon: GraduationCap },
    { path: '/behaviors', label: 'Behavior Types', icon: ListChecks },
    { path: '/brags', label: 'Bear Brags', icon: Star },
    { path: '/rewards', label: 'Reward Items', icon: Gift },
    { path: '/redemptions', label: 'Redeemed Rewards', icon: History },
    { path: '/leaderboard', label: 'Leaderboard', icon: Trophy },
];

interface SidebarProps {
    collapsed?: boolean;
}

export default function Sidebar({ collapsed = false }: SidebarProps) {
    return (
        <Nav className="flex-column">
            {navItems.map(item => (
                <Nav.Link
                    as={Link}
                    to={item.path}
                    key={item.path}
                    className="d-flex align-items-center gap-2"
                >
                    <item.icon size={20} />
                    {!collapsed && <span>{item.label}</span>}
                </Nav.Link>
            ))}
        </Nav>
    );
}
