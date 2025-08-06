import { useState, useMemo, useCallback } from 'react';
import { Table, Pagination } from 'react-bootstrap';
import { Student } from '../services/types';
import { QRCodeSVG } from 'qrcode.react';

interface StudentTableProps {
    students: Student[];
    onQRScan?: (token: string) => void;
    itemsPerPage?: number;
}

export default function StudentTable ({ students, onQRScan, itemsPerPage = 10 }: StudentTableProps) {
    const [ currentPage, setCurrentPage ] = useState(1);

    const totalPages = Math.ceil(students.length / itemsPerPage);

    const paginatedStudents = useMemo(() => {
        if (students.length === 0) return [];
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = Math.min(startIndex + itemsPerPage, students.length);
        return students.slice(startIndex, endIndex);
    }, [students, currentPage, itemsPerPage]);

    const renderPaginationItems = () => {
        const items = [];
        const maxVisiblePages = 7;
        let startPage = 1;
        let endPage = totalPages;
        if (totalPages > maxVisiblePages) {
            const half = Math.floor(maxVisiblePages / 2);
            startPage = Math.max(1, currentPage - half);
            endPage = Math.min(totalPages, currentPage + half);
            if (currentPage - half < 1) {
                endPage = maxVisiblePages;
            }
            if (currentPage + half > totalPages) {
                startPage = totalPages - maxVisiblePages + 1;
            }
        }
        if (startPage > 1) {
            items.push(
                <Pagination.Item key={1} active={1 === currentPage} onClick={() => setCurrentPage(1)}>
                    1
                </Pagination.Item>
            );
            if (startPage > 2) {
                items.push(<Pagination.Ellipsis key='start-ellipsis' disabled />);
            }
        }
        for (let number = startPage; number <= endPage; number++) {
            items.push(
                <Pagination.Item
                    key={number}
                    active={number === currentPage}
                    onClick={() => setCurrentPage(number)}
                >
                    {number}
                </Pagination.Item>
            );
        }
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                items.push(<Pagination.Ellipsis key='end-ellipsis' disabled />);
            }
            items.push(
                <Pagination.Item
                    key={totalPages}
                    active={totalPages === currentPage}
                    onClick={() => setCurrentPage(totalPages)}
                >
                    {totalPages}
                </Pagination.Item>
            );
        }
        return items;
    }

    const handlePrev = useCallback(() => {
        setCurrentPage(prev => Math.max(1, prev - 1));
    }, []);

    const handleNext = useCallback(() => {
        setCurrentPage(prev => Math.min(totalPages, prev + 1));
    }, [totalPages]);

    return (
        <div className='table-responsive'>
            <Table striped bordered hover responsive className='text-center align-middle'>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Grade</th>
                        <th>Teacher</th>
                        <th>Points</th>
                        <th>QR Code</th>
                    </tr>
                </thead>
                <tbody>
                    { paginatedStudents.map((student) => (
                        <tr key={ student.token }>
                            <td>{ student.name }</td>
                            <td>{ student.grade }</td>
                            <td>
                                { student.teacher.name.split(' ').pop() ||
                                    student.teacher.name }
                            </td>
                            <td>{ student.points }</td>
                            <td>
                                <QRCodeSVG
                                    value={`${import.meta.env.VITE_APP_URL}/behavior?token=${student.token}`}
                                    size={ window.innerWidth < 768 ? 64 : 80 }
                                    onClick={ () => onQRScan?.(student.token) }
                                    bgColor='#FFFFFF'
                                    fgColor='#000000'
                                    level='L'
                                />
                            </td>
                        </tr>
                    )) }
                </tbody>
            </Table>
            { totalPages > 1 && (
                <div className='d-flex justify-content-center mt-3'>
                    <Pagination className='flex-wrap'>
                        <Pagination.Prev onClick={handlePrev} disabled={currentPage === 1} />
                        {renderPaginationItems()}
                        <Pagination.Next onClick={handleNext} disabled={currentPage === totalPages} />
                    </Pagination>
                </div>
            )}
        </div>
    );
}
