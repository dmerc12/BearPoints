import { useAppDispatch, useAppSelector, fetchStudentById} from '../store';
import { AuthenticatedLayout, PointsBar } from '../components';
import { Container, Spinner, Alert } from 'react-bootstrap';
import { useEffect } from 'react';

export function StudentDashboard() {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const { selectedStudent, loading, error } = useAppSelector(state => state.students);

    useEffect(() => {
        if (currentUser && currentUser.studentId) {
            dispatch(fetchStudentById(currentUser.studentId));
        }
    }, [dispatch, currentUser]);

    if (loading) {
        return (
            <AuthenticatedLayout>
                <Container className='mt-5 text-center'>
                    <Spinner animation="border" role="status" />
                    <span className="visually-hidden">Loading...</span>
                </Container>
            </AuthenticatedLayout>
        );
    }

    if (error) {
        return (
            <AuthenticatedLayout>
                <Container className='mt-5'>
                    <Alert variant="danger">
                        Error loading student data: {error}
                    </Alert>
                </Container>
            </AuthenticatedLayout>
        );
    }

    if (!selectedStudent) {
        return (
            <AuthenticatedLayout>
                <Container className='mt-5'>
                    <Alert variant="warning">
                        No student data found. Please contact support.
                    </Alert>
                </Container>
            </AuthenticatedLayout>
        );
    }

    const teacherId = selectedStudent.teacher?.id;
    // const studentName = fullName(selectedStudent);
    const points = selectedStudent.points || 0;

    if (!teacherId) {
        return (
            <AuthenticatedLayout>
                <Container className='mt-5'>
                    <h1>Student Dashboard</h1>
                    <Alert variant="info" className="mb-4">
                        <Alert.Heading>Coming Soon</Alert.Heading>
                        <p>Full dashboard features will be available in a future release.</p>
                    </Alert>
                    {/* Tables temporarily disabled for future release after thorough testing */}
                    {/*<p>Welcome to your student dashboard.</p>*/}
                    {/*<Alert variant="info">*/}
                    {/*    No teacher is currently assigned to your account. Please contact an administrator.*/}
                    {/*</Alert>*/}
                </Container>
            </AuthenticatedLayout>
        );
    }

    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Student Dashboard</h1>
                <Alert variant="info" className="mb-4">
                    <Alert.Heading>Coming Soon</Alert.Heading>
                    <p>Points, bear brags, rewards, and classroom views will be re-enabled in a future .</p>
                </Alert>
                <p>Welcome to your student dashboard. View your points and rewards here.</p>
                <PointsBar
                    points={points}
                    label="Your points"
                    size='l'
                    showNextReward={true}
                />
                {/* Tables temporarily disabled for future release after thorough testing */}
                {/*<StudentBragLogsTable*/}
                {/*    studentName={studentName}*/}
                {/*    itemsPerPage={10}*/}
                {/*    size='m'*/}
                {/*/>*/}
                {/*<StudentRewardsTable*/}
                {/*    studentName={studentName}*/}
                {/*    itemsPerPage={10}*/}
                {/*    size='m'*/}
                {/*/>*/}
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
            </Container>
        </AuthenticatedLayout>
    );
}
