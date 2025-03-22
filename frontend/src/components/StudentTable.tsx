import { Student } from '../services/types';
import { Table } from 'react-bootstrap';
import { QRCodeSVG } from 'qrcode.react'; 

interface StudentTableProps {
    students: Student[];
    onQRScan?: (studentID: number) => void;
}

export default function StudentTable ({ students, onQRScan }: StudentTableProps) {
    return (
        <Table striped bordered hover responsive>
            <thead>
                <tr>
                    <th>Student ID</th>
                    <th>Name</th>
                    <th>Grade</th>
                    <th>Teacher</th>
                    {/* <th>Points</th> */ }
                    <th>QR Code</th>
                </tr>
            </thead>
            <tbody>
                { students.map((student) => (
                    <tr key={ student.studentID }>
                        <td>{ student.studentID }</td>
                        <td>{ student.name }</td>
                        <td>{ student.grade }</td>
                        <td>{ student.teacher }</td>
                        {/* <td>{ student.points }</td> */ }
                        <td>
                            <QRCodeSVG value={ JSON.stringify({
                                studentID: student.studentID,
                                timestamp: Date.now()
                            }) }
                                size={ 80 }
                                onClick={ () => onQRScan?.(student.studentID) }
                                bgColor='#FFFFFF'
                                fgColor='#000000'
                                level='L'
                            />
                        </td>
                    </tr>
                )) }
            </tbody>
        </Table>
    );
}
