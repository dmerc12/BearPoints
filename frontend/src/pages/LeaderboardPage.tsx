import { getProcessedLeaderboard, getUniqueTeachers, getUniqueGrades } from '../helpers/leaderboard';
import { LeaderboardEntry, Timeframe, BehaviorLog, Student } from '../services/types';
import { Container, Form, Spinner, Alert, Row, Col } from 'react-bootstrap';
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
        grade: string;
    }>({
        teacher: '',
        timeframe: 'week',
        grade: ''
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
            const processed = getProcessedLeaderboard(rawData, filters)
                .map((entry, index) => ({ ...entry, rank: index + 1 }));
            setLeaderboard(processed);
        }
    }, [ filters, rawData ]);

    return (
        <Container fluid className='mt-md-5 mt-3 px-lg-5'>
            <Row className='mb-4 justify-content-center'>
                <Col xs={12} className='text-center'>
                    <h1 className='mb-4'>Leaderboard</h1>
                </Col>
            </Row>
            <Row className='d-flex gap-3 mb-4 justify-content-center'>
                {/* Timeframe Filter */ }
                <Col xs={12} lg={10} xl={8} className='d-flex flex-column align-items-center gap-3'>
                    <Form.Select value={ filters.timeframe } onChange={ (e) => setFilters(prev => ({ ...prev, timeframe: e.target.value as Timeframe })) } style={{ maxWidth: '300px' }}>
                        <option value='week'>Last Week</option>
                        <option value='month'>Last Month</option>
                        <option value='semester'>Semester</option>
                        <option value='year'>Year</option>
                    </Form.Select>
                </Col>
                {/* Teacher Filter */ }
                <Col xs={12} lg={10} xl={8} className='d-flex flex-column align-items-center gap-3'>
                    <Form.Select value={ filters.teacher } onChange={ (e) => setFilters(prev => ({ ...prev, teacher: e.target.value })) } style={{ maxWidth: '300px' }}>
                        <option value=''>All Teachers</option>
                        { getUniqueTeachers(leaderboard).map(teacher => (
                            <option key={ teacher } value={ teacher }>{ teacher }</option>
                        )) }
                    </Form.Select>
                </Col>
                {/* Grade Filter */ }
                <Col xs={12} lg={10} xl={8} className='d-flex flex-column align-items-center gap-3'>
                    <Form.Select value={ filters.grade } onChange={ (e) => setFilters(prev => ({ ...prev, grade: e.target.value })) } style={{ maxWidth: '300px' }}>
                        <option value=''>All Grades</option>
                        { getUniqueGrades(leaderboard).map(grade => (
                            <option key={ grade } value={ grade }>{ grade }</option>
                        )) }
                    </Form.Select>
                </Col>
            </Row>
            {/* Loading and Error States */}
            { loading && (
                <div className='text-center my-4'>
                    <Spinner animation='border' role='status'>
                        <span className='visually-hidden'>Loading...</span>
                    </Spinner>
                    <p>Loading leaderboard...</p>
                </div>
            ) }
            { error && <Alert variant='danger'>{ error }</Alert> }
            {/* Results Section */ }
            { !loading && !error && (
                <>
                    { leaderboard.length === 0 ? (
                        <Alert variant='info' className='mt-4'>No data to calculate leaderboard yet matching the current filters</Alert>
                    ) : (
                        <div className='border rounded-3 overflow-hidden'>
                            <LeaderboardTable entries={ leaderboard } />
                        </div>
                    )}
                </>
            )}
        </Container>
    );
}
