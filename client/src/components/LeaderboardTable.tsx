import { Alert, Button, ButtonGroup, Container, Pagination, Spinner, Table } from 'react-bootstrap';
import { fetchLeaderboard, setTimeframe } from '../store/slices/leaderboardSlice.ts';
import { useAppDispatch, useAppSelector } from '../store/hooks.ts';
import { formatName, fullName } from '../utils/formatNames';
import { useEffect, useMemo, useState } from 'react';
import { formatGrade } from '../utils/formatGrades';
import { Timeframe } from '../services/types';

interface LeaderboardTableProps {
    itemsPerPage?: number;
}

export default function LeaderboardTable ({ itemsPerPage = 10 }: LeaderboardTableProps) {
    const dispatch = useAppDispatch();
    const { entries, loading, error, currentTimeframe } = useAppSelector(state =>
        state.leaderboard);
    const [ currentPage, setCurrentPage ] = useState(1);

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

    const totalPages = Math.ceil(entries.length / itemsPerPage);

    const paginatedEntries = useMemo(() => {
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        return rankedEntries.slice(startIndex, endIndex);
    }, [rankedEntries, currentPage, itemsPerPage]);

    useEffect(() => {
        setCurrentPage(1);
    }, [ rankedEntries ]);
    
    const handleTimeframeChange = (timeframe: Timeframe) => {
        dispatch(setTimeframe(timeframe));
    };
    
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
            <div className='mb-3 mt-3 text-center'>
                Page {currentPage} of {totalPages} - Showing {paginatedEntries.length} of {rankedEntries.length} entries
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
    );
}
