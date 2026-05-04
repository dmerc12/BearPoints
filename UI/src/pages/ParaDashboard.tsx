import { AuthenticatedLayout, LeaderboardTable, StudentTable, SubmitterBragLogsTable } from '../components';
import { Container } from 'react-bootstrap';

export function ParaDashboard() {
    return (
        <AuthenticatedLayout>
            <Container className='mt-5'>
                <h1>Para Dashboard</h1>
                <p>Welcome to your para dashboard.</p>
                <LeaderboardTable
                    itemsPerPage={10}
                    size='m'
                />
                <StudentTable
                    itemsPerPage={10}
                    size='m'
                />
                <SubmitterBragLogsTable
                    itemsPerPage={10}
                    size='m'
                />
            </Container>
        </AuthenticatedLayout>
    );
}
