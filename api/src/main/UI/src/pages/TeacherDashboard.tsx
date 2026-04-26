import { AuthenticatedLayout } from '../components';
import { Container } from 'react-bootstrap';

export function TeacherDashboard() {
    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Teacher Dashboard</h1>
                <p>Welcome to your teacher dashboard. Here you can manage students, view reports, and more.</p>
            </Container>
        </AuthenticatedLayout>
    );
}
