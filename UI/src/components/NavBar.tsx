import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { useAppSelector } from '../store';
import { Link } from 'react-router-dom';
import { useLogin } from '../hooks';

interface NavigationBarProps {
    onToggleSidebar?: () => void;
    isDashboard?: boolean;
}

export default function NavigationBar ({ onToggleSidebar, isDashboard = false }:NavigationBarProps) {
    const currentUser = useAppSelector(state => state.user.data);
    const { signingIn, handleLogin, handleLogout } = useLogin();

    if (isDashboard) {
        return (
            <Navbar bg='primary' variant='dark' expand='lg' className='mb-3' id='mainNav' aria-label='Navigation bar for BearPoints'>
                <Container fluid>
                    <Navbar.Brand as={ Link } to="/dashboard">BearPoints</Navbar.Brand>
                    <Button variant='outline-light'
                            onClick={onToggleSidebar}
                            className='ms-2'
                            aria-controls='basic-navbar-nav'
                            aria-label="Toggle navigation bar"
                    >
                        ☰
                    </Button>
                    <Nav className='ms-auto'>
                        <span className="text-white me-3">Welcome, {currentUser?.firstName}</span>
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
