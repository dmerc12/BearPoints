import { Container, Row, Col, Card } from 'react-bootstrap';
import { Student } from '../services/types';
import { QRCodeSVG } from 'qrcode.react';
import { forwardRef } from 'react';
import '../print.css';

interface QRCodesPrintProps {
    students: Student[];
}

const QRCodesPrint = forwardRef<HTMLDivElement, QRCodesPrintProps>(
    ({ students }, ref) => {
        const chunkSize = 12;
        const studentGroups = [];
        for (let i = 0; i < students.length; i += chunkSize) {
            studentGroups.push(students.slice(i, i + chunkSize));
        }

    return (
        <div className='d-none'>
            <Container ref={ref} className='print-container'>
                {studentGroups.map((group, groupIndex) => (
                    <div key={groupIndex} className='print-page'>
                        <h2 className='print-header pb-4'>BearPoints QR Codes</h2>
                        <Row className='row-cols-3 g-3'>
                            { group.map((student) => (
                                <Col key={ student.token }>
                                    <Card className='h-100 text-center p-2'>
                                        <QRCodeSVG
                                            value={ `${import.meta.env.VITE_APP_URL}/brag?token=${student.token}` }
                                            size={ 100 }
                                            bgColor='#FFFFFF'
                                            fgColor='#000000'
                                            level='L'
                                            className='mx-auto'
                                        />
                                        <Card.Body className='p-1'>
                                            <Card.Text className='mb-0'>{ student.name }</Card.Text>
                                            <small className='text-muted'>
                                                { student.teacher.name.split(' ').pop()
                                                    || student.teacher.name }
                                            </small><br />
                                            <small className='text-muted'>{student.grade}</small>
                                        </Card.Body>
                                    </Card>
                                </Col>
                            )) }
                        </Row>
                        {groupIndex < studentGroups.length - 1 && (
                            <div className='page-break' />
                        )}
                    </div>
                ))}
            </Container>
        </div>
    );
}); 

export default QRCodesPrint;
