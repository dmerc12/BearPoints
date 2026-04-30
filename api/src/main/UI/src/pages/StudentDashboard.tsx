import { AuthenticatedLayout } from '../components';
import { Container } from 'react-bootstrap';

export function StudentDashboard() {
    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Student Dashboard</h1>
                <p>Welcome to your student dashboard. View your points and rewards here.</p>
                {/* Points / fundraiser bar */}
                {/* My bear brags table (as student) */}
                {/* My related rewards table */}
                {/* Classroom leaderboard table */}
                {/* Class bear brags table (teacher) */}
            </Container>
        </AuthenticatedLayout>
    );
}
