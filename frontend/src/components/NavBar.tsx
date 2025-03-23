import { Navbar, Nav, Container } from 'react-bootstrap';
import { Link } from 'react-router-dom';

export default function NavigationBar () {
    return (
        <Navbar bg='primary' variant='dark' expand='lg' className='fixed-top' id='mainNav' aria-label='Navigation bar for BearPoints'>
            <Container>
                <Navbar.Brand as={ Link } to="/">BearPoints</Navbar.Brand>
                <Navbar.Toggle aria-controls='basic-navbar-nav' aria-label="Toggle navigation bar" />
                <Navbar.Collapse id='basic-navbar-nav'>
                    <Nav className='ms-auto'>
                        <Nav.Link as={ Link } to='/students'>Students</Nav.Link>
                        <Nav.Link as={ Link } to='/leaderboard'>Leaderboard</Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
