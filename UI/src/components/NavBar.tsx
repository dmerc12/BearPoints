import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Menu, ChevronLeft, ChevronRight } from 'lucide-react';
import { useAppSelector } from '../store';
import { Link } from 'react-router-dom';
import { useLogin } from '../hooks';

interface NavigationBarProps {
    onToggleSidebar?: () => void;
    isDashboard?: boolean;
    isMobile?: boolean;
    sidebarOpen?: boolean;
}

export default function NavigationBar ({ onToggleSidebar, isDashboard = false, isMobile = false, sidebarOpen = true }
                                       :NavigationBarProps) {
    const currentUser = useAppSelector(state => state.user.data);
    const { signingIn, handleLogin, handleLogout } = useLogin();

    if (isDashboard) {
        return (
            <Navbar bg='primary' variant='dark' expand='lg' id='mainNav' aria-label='Navigation bar for BearPoints'>
                <Container fluid>
                    <Navbar.Brand as={ Link } to="/dashboard">BearPoints</Navbar.Brand>
                    <Button variant='outline-light'
                            onClick={onToggleSidebar}
                            aria-controls='basic-navbar-nav'
                            aria-label="Toggle sidebar"
                    >
                        {isMobile ? <Menu size={20} />
                            : (sidebarOpen ? <ChevronLeft size={20} />
                                : <ChevronRight size={20} />)}
                    </Button>
                    <Nav className='ms-auto align-items-center'>
                        <span className="text-white me-3">
                            Welcome, {currentUser?.firstName}
                        </span>
                        <Nav.Link onClick={ handleLogout }>Logout</Nav.Link>
                    </Nav>
                </Container>
            </Navbar>
        );
    }

    return (
        <Navbar bg='primary' variant='dark' expand='lg' className='fixed-top' id='mainNav' aria-label='Navigation bar for BearPoints'>
            <Container fluid>
                <Navbar.Brand as={ Link } to={currentUser ? '/dashboard' : '/'}>BearPoints</Navbar.Brand>
                <Navbar.Toggle aria-controls='basic-navbar-nav' aria-label="Toggle navigation bar" />
                <Navbar.Collapse id='basic-navbar-nav'>
                    <Nav className='ms-auto'>
                        { currentUser ? (
                            <>
                                <Nav.Link onClick={ handleLogout }>Logout</Nav.Link>
                            </>
                        ) :
                            <Nav.Link onClick={ handleLogin } disabled={ signingIn }>
                                { signingIn ? 'Logging In...' : 'Login'}
                            </Nav.Link>
                        }
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
