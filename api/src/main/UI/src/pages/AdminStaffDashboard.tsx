import { AuthenticatedLayout, SyncButton } from '../components';
import { Container } from 'react-bootstrap';

export function AdminStaffDashboard() {
    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <div className="d-flex justify-content-between align-items-center mb-4">
                    <h1>Admin Dashboard</h1>
                    <SyncButton variant="primary" />
                </div>
                <p>Welcome to the admin dashboard. Manage system settings and users here.</p>
                {/* Sync button */}
                {/* Entity tables */}
                {/* Leaderboard table */}
                {/* My bear brags table (as submitter) */}
            </Container>
        </AuthenticatedLayout>
    );
}
