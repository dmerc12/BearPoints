import { Container, Form, Spinner, Alert, Row, Col } from 'react-bootstrap';
import { formatName, clearNameCaches, formatGrade } from '../utils';
import { getLeaderboard, LeaderboardEntry, Timeframe } from '../services';
import LeaderboardTable from '../components/LeaderboardTable';
import { useEffect, useState, useMemo } from 'react';
import Auth from '../components/Auth';

export default function LeaderboardPage () {
    const [ leaderboard, setLeaderboard ] = useState<LeaderboardEntry[]>([]);
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ filters, setFilters ] = useState({
        teacher: '',
        timeframe: Timeframe.WEEK,
        grade: ''
    });
    
    useEffect(() => {
        const loadData = async () => {
            try {
                setLoading(true);
                setError('');
                clearNameCaches();
                const data = await getLeaderboard(filters.timeframe);
                setLeaderboard(data);
            } catch (error) {
                setError('Failed to load data');
                console.error('Failed to load data:', error);
            } finally {
                setLoading(false);
            }
        };
        loadData().catch(error => {
            console.error('Unhandled fetch error:', error);
            setError('An unexpected error occurred');
        });
    }, [filters.timeframe]);

    const filteredEntries = useMemo(() => {
        return leaderboard.filter(entry => {
            const teacherMatch = filters.teacher === '' ||
                formatName(entry.teacher).includes(filters.teacher);
            const gradeMatch = filters.grade === '' ||
                entry.grade === filters.grade;
            return teacherMatch && gradeMatch;
        });
    }, [leaderboard, filters.teacher, filters.grade]);

    const uniqueTeachers = useMemo(() =>
        [...new Set(leaderboard.map(e => formatName(e.teacher)))], [leaderboard]);

    const uniqueGrades = useMemo(() =>
        [...new Set(leaderboard.map(e => formatGrade(e.grade)))], [leaderboard]);

    return (
        <Auth>
            <Container fluid className='mt-3 pt-2 px-lg-5 mb-4'>
                <Row className='mb-4 justify-content-center'>
                    <Col xs={ 12 } className='text-center'>
                        <h1 className='mb-4'>Leaderboard</h1>
                    </Col>
                </Row>
                <Row className='d-flex gap-3 mb-4 justify-content-center'>
                    {/* Timeframe Filter */ }
                    <Col xs={ 12 } lg={ 10 } xl={ 8 } className='d-flex flex-column align-items-center gap-3'>
                        <Form.Select value={ filters.timeframe }
                                     onChange={ (e) =>
                                         setFilters(prev => ({
                                             ...prev,
                                             timeframe: e.target.value as Timeframe })) }
                                     style={ { maxWidth: '300px' } }
                        >
                            <option value={Timeframe.WEEK}>Last Week</option>
                            <option value={Timeframe.MONTH}>Last Month</option>
                            <option value={Timeframe.SEMESTER}>Semester</option>
                            <option value={Timeframe.YEAR}>Year</option>
                        </Form.Select>
                    </Col>
                    {/* Teacher Filter */ }
                    <Col xs={ 12 } lg={ 10 } xl={ 8 } className='d-flex flex-column align-items-center gap-3'>
                        <Form.Select value={ filters.teacher }
                                     onChange={ (e) =>
                                         setFilters(prev => ({
                                             ...prev,
                                             teacher: e.target.value })) }
                                     style={ { maxWidth: '300px' } }
                        >
                            <option value=''>All Teachers</option>
                            { uniqueTeachers.map(teacher => (
                                <option key={ teacher } value={ teacher }>{ teacher }</option>
                            )) }
                        </Form.Select>
                    </Col>
                    {/* Grade Filter */ }
                    <Col xs={ 12 } lg={ 10 } xl={ 8 } className='d-flex flex-column align-items-center gap-3'>
                        <Form.Select value={ filters.grade }
                                     onChange={ (e) =>
                                         setFilters(prev => ({
                                             ...prev,
                                             grade: e.target.value })) }
                                     style={ { maxWidth: '300px' } }
                        >
                            <option value=''>All Grades</option>
                            { uniqueGrades.map(grade => (
                                <option key={ grade } value={ grade }>{ grade }</option>
                            )) }
                        </Form.Select>
                    </Col>
                </Row>
                {/* Loading and Error States */ }
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
                            <Alert variant='info' className='mt-4'>
                                No data to calculate leaderboard yet matching the current filters
                            </Alert>
                        ) : (
                            <div className='border rounded-3 overflow-hidden'>
                                <LeaderboardTable entries={ filteredEntries } />
                            </div>
                        ) }
                    </>
                ) }
            </Container>
        </Auth>
    );
}
