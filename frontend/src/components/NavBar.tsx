import { Navbar, Nav, Container } from 'react-bootstrap';
import { useAuthState } from 'react-firebase-hooks/auth';
import { Link } from 'react-router-dom';
import { auth, login } from '../Auth';
import { useState } from 'react';

export default function NavigationBar () {
    const [ user ] = useAuthState(auth);
    const [ signingIn, setSigningIn ] = useState(false);

    const handleLogin = async () => {
        setSigningIn(true);
        try {
            await login();
        } catch (error) {
            console.error('Login failed:', error);
        } finally {
            setSigningIn(false);
        }
    };

    return (
        <Navbar bg='primary' variant='dark' expand='lg' className='fixed-top' id='mainNav' aria-label='Navigation bar for BearPoints'>
            <Container fluid>
                <Navbar.Brand as={ Link } to="/">BearPoints</Navbar.Brand>
                <Navbar.Toggle aria-controls='basic-navbar-nav' aria-label="Toggle navigation bar" />
                <Navbar.Collapse id='basic-navbar-nav'>
                    <Nav className='ms-auto'>
                        { user ? (
                            <>
                                <Nav.Link as={ Link } to='/students'>Students</Nav.Link>
                                <Nav.Link as={ Link } to='/leaderboard'>Leaderboard</Nav.Link>
                                <Nav.Link onClick={ () => auth.signOut() }>Logout</Nav.Link>
                            </>
                        ) :
                            <Nav.Link onClick={ handleLogin } disabled={ signingIn }>{ signingIn ? 'Logging In...' : 'Login'}</Nav.Link>
                            
                        }
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
