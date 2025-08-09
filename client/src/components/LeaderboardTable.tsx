import { formatName, fullName } from '../utils/formatNames';
import { useState, useEffect, useMemo } from 'react';
import { LeaderboardEntry } from '../services/types';
import { Table, Pagination } from 'react-bootstrap';
import { formatGrade } from '../utils/formatGrades';

interface LeaderboardTableProps {
    entries: LeaderboardEntry[];
    itemsPerPage?: number;
}

export default function LeaderboardTable ({ entries, itemsPerPage = 10 }: LeaderboardTableProps) {
    const [ currentPage, setCurrentPage ] = useState(1);

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
    }, [ entries ]);

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
            <div className='mb-3 mt-3 text-center'>
                Page {currentPage} of {totalPages} - Showing {paginatedEntries.length} of {entries.length} entries
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
