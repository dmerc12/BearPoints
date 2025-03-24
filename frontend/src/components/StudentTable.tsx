import { Table, Pagination } from 'react-bootstrap';
import { Student } from '../services/types';
import { QRCodeSVG } from 'qrcode.react'; 
import { useState } from 'react';

interface StudentTableProps {
    students: Student[];
    onQRScan?: (token: string) => void;
    itemsPerPage?: number;
}

export default function StudentTable ({ students, onQRScan, itemsPerPage = 10 }: StudentTableProps) {
    const [ currentPage, setCurrentPage ] = useState(1);

    const totalItems = students.length;
    const totalPages = Math.ceil(totalItems / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedStudents = students.slice(startIndex, endIndex);

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
            <Table striped bordered hover responsive>
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
                            <td>{ student.teacher }</td>
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
            <div className='d-flex justify-content-center mt-3'>
                    <Pagination className='flex-wrap'>
                        <Pagination.Prev onClick={() => setCurrentPage(Math.max(1, currentPage - 1))} disabled={currentPage === 1} />
                        {pageItems}
                        <Pagination.Next onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))} disabled={currentPage === totalPages} />
                    </Pagination>
            </div>
        </div>
    );
}
