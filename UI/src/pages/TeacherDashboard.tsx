import { AuthenticatedLayout } from '../components';
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
                <Alert variant="info" className="mb-4">
                    <Alert.Heading>Coming Soon</Alert.Heading>
                    <p>
                        Classroom leaderboard, bear brags, student management, and submitter brags will be re-enabled
                        in a future release.
                    </p>
                </Alert>
                {/* Tables temporarily disabled for future release after thorough testing */}
                {/*<p>Welcome to your teacher dashboard. Here you can manage students, view reports, and more.</p>*/}
                {/*<ClassroomLeaderboardTable*/}
                {/*    teacherId={teacherId}*/}
                {/*    itemsPerPage={10}*/}
                {/*    size='m'*/}
                {/*/>*/}
                {/*<ClassroomBragLogsTable*/}
                {/*    teacherId={teacherId}*/}
                {/*    itemsPerPage={10}*/}
                {/*    size='m'*/}
                {/*/>*/}
                {/*<ClassroomStudentsTable*/}
                {/*    teacherId={teacherId}*/}
                {/*    showActions={true}*/}
                {/*    itemsPerPage={10}*/}
                {/*    size='m'*/}
                {/*/>*/}
                {/*<SubmitterBragLogsTable*/}
                {/*    itemsPerPage={10}*/}
                {/*    size='m'*/}
                {/*/>*/}
            </Container>
        </AuthenticatedLayout>
    );
}
