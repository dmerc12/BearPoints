import { Navbar, Nav, Container } from 'react-bootstrap';
import { useAuthState } from 'react-firebase-hooks/auth';
import { Link, useNavigate } from 'react-router-dom';
import { auth, login } from '../Auth';
import { useState } from 'react';

export default function NavigationBar () {
    const [ user ] = useAuthState(auth);
    const [ signingIn, setSigningIn ] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async () => {
        setSigningIn(true);
        try {
            await login();
            console.log('Login successful, navigating to dashboard');
            navigate('/dashboard');
        } catch (error) {
            console.error('Login failed:', error);
        } finally {
            setSigningIn(false);
        }
    };

    const handleLogout = async () => {
        try {
            await auth.signOut();
            navigate('/');
        } catch (error) {
            console.error('Logout failed:', error);
        }
    }

    return (
        <Navbar bg='primary' variant='dark' expand='lg' className='fixed-top' id='mainNav' aria-label='Navigation bar for BearPoints'>
            <Container fluid>
                <Navbar.Brand as={ Link } to="/">BearPoints</Navbar.Brand>
                <Navbar.Toggle aria-controls='basic-navbar-nav' aria-label="Toggle navigation bar" />
                <Navbar.Collapse id='basic-navbar-nav'>
                    <Nav className='ms-auto'>
                        { user ? (
                            <>
                                <Nav.Link as={ Link } to='/dashboard'>Dashboard</Nav.Link>
                                <Nav.Link as={ Link } to='/students'>Students</Nav.Link>
                                <Nav.Link as={ Link } to='/leaderboard'>Leaderboard</Nav.Link>
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
