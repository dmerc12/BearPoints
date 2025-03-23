import { LeaderboardEntry, Timeframe, BehaviorLog, Student } from '../services/types';
import { getProcessedLeaderboard, getUniqueTeachers } from '../helpers/leaderboard';
import { Container, Form, Spinner, Alert } from 'react-bootstrap';
import LeaderboardTable from '../components/LeaderboardTable';
import { getLeaderboard } from '../services/api';
import { useEffect, useState } from 'react';

export default function LeaderboardPage () {
    const [ leaderboard, setLeaderboard ] = useState<LeaderboardEntry[]>([]);
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ rawData, setRawData ] = useState<{
        behaviorLogs: BehaviorLog[];
        students: Student[];
    }>({ behaviorLogs: [], students: [] });
    const [ filters, setFilters ] = useState<{
        teacher: string;
        timeframe: Timeframe;
    }>({
        teacher: '',
        timeframe: 'week'
    });
    
    useEffect(() => {
        const loadData = async () => {
            try {
                setLoading(true);
                const data = await getLeaderboard();
                setRawData(data);
            } catch (error) {
                setError('Failed to load data');
                console.error('Failed to load data:', error);
            } finally {
                setLoading(false);
            }
        };
        loadData();
    }, []);

    useEffect(() => {
        if (rawData.behaviorLogs.length && rawData.students.length) {
            const processed = getProcessedLeaderboard(rawData, filters);
            setLeaderboard(processed);
        }
    }, [ filters, rawData ]);

    return (
        <Container className='mt-4'>
            <div className='d-flex gap-3 mb-4'>
                <Form.Select value={ filters.timeframe } onChange={ (e) => setFilters(prev => ({ ...prev, timeframe: e.target.value as Timeframe })) }>
                    <option value='week'>Last Week</option>
                    <option value='month'>Last Month</option>
                    <option value='semester'>Semester</option>
                    <option value='year'>Year</option>
                </Form.Select>
                <Form.Select value={ filters.teacher } onChange={ (e) => setFilters(prev => ({ ...prev, teacher: e.target.value })) }>
                    <option value=''>All Teachers</option>
                    {getUniqueTeachers(leaderboard).map(teacher => (
                        <option key={ teacher } value={ teacher }>{ teacher }</option>
                    ))}
                </Form.Select>
            </div>
            { loading && <Spinner animation='border' /> }
            { error && <Alert variant='danger'>{ error }</Alert> }
            <LeaderboardTable entries={ leaderboard } />
        </Container>
    );
}
