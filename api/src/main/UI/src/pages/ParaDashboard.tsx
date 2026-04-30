import { AuthenticatedLayout } from '../components';
import { Container } from 'react-bootstrap';

export function ParaDashboard() {
    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Para Dashboard</h1>
                <p>Welcome to your para dashboard.</p>
                {/* Leaderboard table */}
                {/* Students table */}
                {/* My bear brags table (as submitter) */}
            </Container>
        </AuthenticatedLayout>
    );
}