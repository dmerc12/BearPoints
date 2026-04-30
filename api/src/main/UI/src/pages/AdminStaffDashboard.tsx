import { AuthenticatedLayout } from '../components';
import { Container } from 'react-bootstrap';

export function AdminStaffDashboard() {
    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Admin Dashboard</h1>
                <p>Welcome to the admin dashboard. Manage system settings and users here.</p>
                {/* Sync button */}
                {/* Entity tables */}
                {/* Leaderboard table */}
                {/* My bear brags table (as submitter) */}
            </Container>
        </AuthenticatedLayout>
    );
}
