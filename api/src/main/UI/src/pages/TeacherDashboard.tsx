import { AuthenticatedLayout, ClassroomStudentsTable, ClassroomLeaderboardTable } from '../components';
import { Container, Alert } from 'react-bootstrap';
import { useAppSelector } from '../store';

export function TeacherDashboard() {
    const currentUser = useAppSelector(state => state.user.data);
    const teacherId = currentUser?.teacherId;

    if (!teacherId) {
        return (
            <AuthenticatedLayout>
                <Container className='mt-5'>
                    <Alert variant='info'>
                        Loading your classroom data...
                    </Alert>
                </Container>
            </AuthenticatedLayout>
        );
    }

    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Teacher Dashboard</h1>
                <p>Welcome to your teacher dashboard. Here you can manage students, view reports, and more.</p>
                {/* Classroom leaderboard table */}
                <ClassroomLeaderboardTable
                    teacherId={teacherId}
                    itemsPerPage={10}
                    size='m'
                />
                {/* Class bear brags table (teacher) */}
                {/* Class rewards table */}
                {/* Students table */}
                <ClassroomStudentsTable
                    teacherId={teacherId}
                    showActions={true}
                    itemsPerPage={10}
                    size='m'
                />
                {/* My bear brags table (as submitter) */}
            </Container>
        </AuthenticatedLayout>
    );
}
