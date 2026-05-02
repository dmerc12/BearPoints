import { AuthenticatedLayout, LeaderboardTable, UserTable, TeacherTable, StudentTable, BehaviorTypeTable,
    RewardItemTable, BragLogTable, StudentRewardTable, SubmitterBragLogsTable, SyncButton } from '../components';
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
                <UserTable
                    itemsPerPage={10}
                    size='m'
                />
                <TeacherTable
                    itemsPerPage={10}
                    size='m'
                />
                <StudentTable
                    itemsPerPage={10}
                    size='m'
                />
                <BehaviorTypeTable
                    itemsPerPage={10}
                    size='m'
                />
                <RewardItemTable
                    itemsPerPage={10}
                    size='m'
                />
                <BragLogTable
                    itemsPerPage={10}
                    size='m'
                />
                <StudentRewardTable
                    itemsPerPage={10}
                    size='m'
                />
                <LeaderboardTable
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
