import { AuthenticatedLayout, ClassroomLeaderboardTable, ClassroomBragLogsTable, StudentBragLogsTable,
    StudentRewardsTable } from '../components';
import { useAppDispatch, useAppSelector, fetchStudentById} from '../store';
import { Container, Spinner, Alert } from 'react-bootstrap';
import { fullName } from '../utils';
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
    const studentName = fullName(selectedStudent);

    if (!teacherId) {
        return (
            <AuthenticatedLayout>
                <Container className='mt-5'>
                    <h1>Student Dashboard</h1>
                    <p>Welcome to your student dashboard.</p>
                    <Alert variant="info">
                        No teacher is currently assigned to your account. Please contact an administrator.
                    </Alert>
                </Container>
            </AuthenticatedLayout>
        );
    }

    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Student Dashboard</h1>
                <p>Welcome to your student dashboard. View your points and rewards here.</p>
                {/* Points / fundraiser bar */}
                <StudentBragLogsTable
                    studentName={studentName}
                    itemsPerPage={10}
                    size='m'
                />
                {/* My related rewards table */}
                <StudentRewardsTable
                    studentName={studentName}
                    itemsPerPage={10}
                    size='m'
                />
                <ClassroomLeaderboardTable
                    teacherId={teacherId}
                    itemsPerPage={10}
                    size='m'
                />
                <ClassroomBragLogsTable
                    teacherId={teacherId}
                    itemsPerPage={10}
                    size='m'
                />
            </Container>
        </AuthenticatedLayout>
    );
}
