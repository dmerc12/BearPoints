import { LeaderboardEntry } from '../services/types';
import { Table } from 'react-bootstrap';

interface LeaderboardTableProps {
    entries: LeaderboardEntry[];
}

export default function LeaderboardTable ({ entries }: LeaderboardTableProps) {
    const safeEntries = Array.isArray(entries) ? entries : [];
    return (
        <Table striped bordered hover responsive>
            <thead>
                <tr>
                    {/* <th>Rank</th> */ }
                    <th>Student Name</th>
                    <th>Points</th>
                    <th>Teacher</th>
                    <th>Grade</th>
                </tr>
            </thead>
            <tbody>
                { safeEntries.map((entry) => (
                    <tr>
                        {/* <td>{ entry.rank }</td> */ }
                        <td>{ entry.name }</td>
                        <td>{ entry.points }</td>
                        <td>{ entry.teacher }</td>
                        <td>{ entry.grade }</td>
                    </tr>
                )) }
            </tbody>
        </Table>
    );
}
