import { AuthenticatedLayout } from '../components';
import { Container, Alert } from 'react-bootstrap';

export function ParaDashboard() {
    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Para Dashboard</h1>
                <Alert variant="info" className="mb-5">
                    <Alert.Heading>Coming Soon</Alert.Heading>
                    <p>
                        Leaderboard, student management, and bear brags brags will be re-enabled in a future release.
                    </p>
                </Alert>
                {/* Tables temporarily disabled for future release after thorough testing */}
                {/*<p>Welcome to your para dashboard.</p>*/}
                {/*<LeaderboardTable*/}
                {/*    itemsPerPage={10}*/}
                {/*    size='m'*/}
                {/*/>*/}
                {/*<StudentTable*/}
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
