import { Navbar, Nav, Container } from 'react-bootstrap';
import { Link } from 'react-router-dom';

export default function NavigationBar () {
    return (
        <Navbar bg="primary" variant="dark" expanded={ true }>
            <Container>
                <Navbar.Brand as={ Link } to="/">BearPoints</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav.Link as={ Link } to='/students'>Students</Nav.Link>
                    <Nav.Link as={ Link } to='/leaderboard'>Leaderboard</Nav.Link>
                    <Nav.Link as={ Link } to='/behavior'>Submit Behavior</Nav.Link>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
