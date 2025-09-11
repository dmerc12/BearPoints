import { Alert, Button, ButtonGroup, Container, Pagination, Spinner, Table, Row, Col } from 'react-bootstrap';
import { fetchLeaderboard, setTimeframe, useAppDispatch, useAppSelector } from '../store';
import { formatName, fullName, formatGrade } from '../utils';
import { useEffect, useMemo, useState } from 'react';
import { Timeframe } from '../services';
import { SelectFilter } from './index';

interface LeaderboardTableProps {
    itemsPerPage?: number;
}

export default function LeaderboardTable ({ itemsPerPage = 10 }: LeaderboardTableProps) {
    const dispatch = useAppDispatch();
    const { entries, loading, error, currentTimeframe } = useAppSelector(state =>
        state.leaderboard);
    const [ currentPage, setCurrentPage ] = useState(1);
    const [filters, setFilters] = useState({
        teacherFilter: '',
        gradeFilter: ''
    });

    useEffect(() => {
        dispatch(fetchLeaderboard({ timeframe: currentTimeframe, force: false }));
    }, [dispatch, currentTimeframe]);

    const rankedEntries = useMemo(() => {
        const safeEntries = Array.isArray(entries) ? entries : [];
        return safeEntries.map((entry, index) => ({
            ...entry,
            rank: index + 1,
        }));
    }, [entries]);

    const filteredEntries = useMemo(() => {
        return rankedEntries.filter(entry => {
            const teacherFilter = filters.teacherFilter;
            const gradeFilter = filters.gradeFilter;
            const teacherMatch = teacherFilter === '' || formatName(entry.teacher).includes(teacherFilter);
            const gradeMatch = gradeFilter === '' || formatGrade(entry.grade) === gradeFilter;
            return teacherMatch && gradeMatch;
        });
    }, [rankedEntries, filters]);

    const handleFilterChange = (filterName: string, value: string) => {
        setFilters(prev => ({
            ...prev,
            [filterName]: value
        }));
    };

    const totalPages = Math.ceil(entries.length / itemsPerPage);

    const paginatedEntries = useMemo(() => {
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        return filteredEntries.slice(startIndex, endIndex);
    }, [filteredEntries, currentPage, itemsPerPage]);

    useEffect(() => {
        setCurrentPage(1);
    }, [ rankedEntries ]);
    
    const handleTimeframeChange = (timeframe: Timeframe) => {
        dispatch(setTimeframe(timeframe));
    };

    const uniqueTeachers = useMemo(() => [
        ...new Set(rankedEntries.map(e => formatName(e.teacher)))
    ], [rankedEntries]);

    const uniqueGrades = useMemo(() => [
        ...new Set(rankedEntries.map(e => formatGrade(e.grade)))
    ], [rankedEntries]);

    const teacherOptions = useMemo(() =>
        uniqueTeachers.map(teacher => ({ value: teacher, label: teacher })),
        [uniqueTeachers]
    );

    const gradeOptions = useMemo(() =>
        uniqueGrades.map(grade => ({ value: grade, label: grade })),
        [uniqueGrades]
    );
    
    if (loading) {
        return (
            <Container className='d-flex justify-content-center align-items-center' style={{ minHeight: '200px' }}>
                <Spinner animation='border' variant='primary'>
                    <span className='visually-hidden'>Loading...</span>
                </Spinner>
                <span className='ms-2'>Loading leaderboard...</span>
            </Container>
        );
    }
    
    if (error) {
        return (
            <Alert variant='danger' className='text-center'>
                <Alert.Heading>Error Loading Leaderboard</Alert.Heading>
                <p>{error}</p>
                <Button variant='outline-danger' onClick={() => 
                    dispatch(fetchLeaderboard({ timeframe: currentTimeframe, force: true }))}
                >
                    Retry
                </Button>
            </Alert>
        );
    }

    const pageItems = [];
    for (let number = 1; number <= totalPages; number++) {
        pageItems.push(
            <Pagination.Item key={ number } active={ number === currentPage } onClick={ () => setCurrentPage(number) }>
                { number }
            </Pagination.Item>
        );
    }

    return (
        <Container fluid className='mt-3 pt-2 px-lg-5 mb-4'>
            <div className='text-center mb-4'>
                <h1>Leaderboard</h1>
            </div>
            <div className='table-responsive'>
                <div className='mb-3 text-center'>
                    <ButtonGroup className='mb-3'>
                        <Button variant={currentTimeframe === Timeframe.WEEK ? 'primary' : 'outline-primary'}
                                onClick={ () => handleTimeframeChange(Timeframe.WEEK) }
                        >
                            Week
                        </Button>
                        <Button variant={currentTimeframe === Timeframe.MONTH ? 'primary' : 'outline-primary'}
                                onClick={ () => handleTimeframeChange(Timeframe.MONTH) }
                        >
                            Month
                        </Button>
                        <Button variant={currentTimeframe === Timeframe.SEMESTER ? 'primary' : 'outline-primary'}
                                onClick={ () => handleTimeframeChange(Timeframe.SEMESTER) }
                        >
                            Semester
                        </Button>
                        <Button variant={currentTimeframe === Timeframe.YEAR ? 'primary' : 'outline-primary'}
                                onClick={ () => handleTimeframeChange(Timeframe.YEAR) }
                        >
                            Year
                        </Button>
                    </ButtonGroup>
                </div>
                <Row className='mb-3 justify-content-center'>
                    <Col md={4} className='mb-2'>
                        <SelectFilter
                            value={filters.teacherFilter}
                            label='Teacher'
                            onChange={(value) => handleFilterChange('teacherFilter', value)}
                            options={teacherOptions}
                        />
                        <SelectFilter
                            value={filters.gradeFilter}
                            label='Grade'
                            onChange={(value) => handleFilterChange('gradeFilter', value)}
                            options={gradeOptions}
                        />
                    </Col>
                </Row>
                <div className='mb-3 mt-3 text-center'>
                    Page {currentPage} of {totalPages} - Showing {paginatedEntries.length} of {filteredEntries.length} entries
                </div>
                <Table striped bordered hover responsive className='text-center align-center'>
                    <thead>
                    <tr>
                        <th>Rank</th>
                        <th>Points</th>
                        <th>Student Name</th>
                        <th>Teacher</th>
                        <th>Grade</th>
                    </tr>
                    </thead>
                    <tbody>
                    { paginatedEntries.map((entry) => (
                        <tr key={ entry.student.id }>
                            <td>{ entry.rank }</td>
                            <td>{ entry.points }</td>
                            <td>{ fullName(entry.student) }</td>
                            <td>{ formatName(entry.teacher) }</td>
                            <td>{ formatGrade(entry.grade) }</td>
                        </tr>
                    )) }
                    </tbody>
                </Table>
                { totalPages > 1 && (
                    <div className='d-flex justify-content-center mt-3'>
                        <Pagination className='flex-wrap'>
                            <Pagination.Prev onClick={ () => setCurrentPage(Math.max(1, currentPage - 1)) } disabled={ currentPage === 1 } />
                            { pageItems }
                            <Pagination.Next onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))} disabled={currentPage === totalPages} />
                        </Pagination>
                    </div>
                )}
            </div>
        </Container>
    );
}
