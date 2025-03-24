import { LeaderboardEntry } from '../services/types';
import { Table, Pagination } from 'react-bootstrap';
import { useState, useEffect } from 'react';

interface LeaderboardTableProps {
    entries: LeaderboardEntry[];
    itemsPerPage?: number;
}

export default function LeaderboardTable ({ entries, itemsPerPage = 10 }: LeaderboardTableProps) {
    const [ currentPage, setCurrentPage ] = useState(1);

    const safeEntries = Array.isArray(entries) ? entries : [];
    const rankedEntries = safeEntries.map((entry, index) => ({
        ...entry,
        rank: index + 1,
    }));

    const totalItems = entries.length;
    const totalPages = Math.ceil(totalItems / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedEntries = rankedEntries.slice(startIndex, endIndex);

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
            <Table striped bordered hover responsive>
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
                        <tr key={ entry.studentID }>
                            <td>{ entry.rank }</td>
                            <td>{ entry.points }</td>
                            <td>{ entry.name }</td>
                            <td>{ entry.teacher }</td>
                            <td>{ entry.grade }</td>
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
