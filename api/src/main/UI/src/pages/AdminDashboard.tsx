import { AuthenticatedLayout } from '../components';
import { Container } from 'react-bootstrap';

export function AdminDashboard() {
    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Admin Dashboard</h1>
                <p>Welcome to the admin dashboard. Manage system settings and users here.</p>
            </Container>
        </AuthenticatedLayout>
    );
}
