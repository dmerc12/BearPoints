import { LeaderboardTable, AuthenticatedLayout } from '../components';

export default function LeaderboardPage () {
    return (
        <AuthenticatedLayout>
            <LeaderboardTable />
        </AuthenticatedLayout>
    );
}
