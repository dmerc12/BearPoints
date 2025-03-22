import { Container, Form, Spinner, Alert } from 'react-bootstrap';
import LeaderboardTable from '../components/LeaderboardTable';
import { LeaderboardEntry, Timeframe } from '../services/types';
import { getLeaderboard } from '../services/api';
import { useEffect, useState } from 'react';

export default function LeaderboardPage () {
    const [ leaderboard, setLeaderboard ] = useState<LeaderboardEntry[]>([]);
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ filters, setFilters ] = useState({
        teacher: '',
        timeframe: 'week'
    });
    
    useEffect(() => {
        const fetchLeaderboard = async () => {
            try {
                setLoading(true);
                const data = await getLeaderboard({
                    teacher: filters.teacher,
                    timeframe: filters.timeframe
                });
                setLeaderboard(Array.isArray(data) ? data : []);
            } catch (error) {
                setError('Failed to load leaderboard');
                console.error('Failed to load leaderboard:', error);
                setLeaderboard([]);
            } finally {
                setLoading(false);
            }
        };
        fetchLeaderboard();
    }, [ filters ]);

    return (
        <Container className='mt-4'>
            <div className='d-flex gap-3 mb-4'>
                <Form.Select value={ filters.timeframe } onChange={ (e) => setFilters({ ...filters, timeframe: e.target.value as Timeframe }) }>
                    <option value='week'>Last Week</option>
                    <option value='month'>Last Month</option>
                    <option value='semester'>Semester</option>
                    <option value='year'>Year</option>
                </Form.Select>
                <Form.Select value={ filters.teacher } onChange={ (e) => setFilters({ ...filters, teacher: e.target.value }) }>
                    <option value=''>All Teachers</option>
                </Form.Select>
            </div>
            { loading && <Spinner animation='border' /> }
            { error && <Alert variant='danger'>{ error }</Alert> }
            <LeaderboardTable entries={ leaderboard } />
        </Container>
    );
}
