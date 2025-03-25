import { Container, Row, Col, Card } from 'react-bootstrap';
import { Student } from '../services/types';
import { QRCodeSVG } from 'qrcode.react';
import { forwardRef } from 'react';

interface QRCodesPrintProps {
    students: Student[];
}

const QRCodesPrint = forwardRef<HTMLDivElement, QRCodesPrintProps>(({ students }, ref) => {
    return (
        <div className='d-none'>
            <Container ref={ref} className='mt-4'>
                <h2 className='text-center mb-3 mt-3'>Student QR Codes</h2>
                <Row className='row-cols-3 row-cols-md-3 row-cols-lg-4 g-5'>
                    { students.map((student) => (
                        <Col key={ student.token } className='mb-3'>
                            <Card className='h-100 text-center p-3'>
                                <QRCodeSVG
                                    value={ `${import.meta.env.VITE_APP_URL}/behavior?token=${student.token}` }
                                    size={ 100 }
                                    bgColor='#FFFFFF'
                                    fgColor='#000000'
                                    level='L'
                                    className='mx-auto'
                                />
                                <Card.Body className='p-2'>
                                    <Card.Text className='mb-0'>{ student.name }</Card.Text>
                                    <small className='text-muted'>{ student.grade } - { student.teacher }</small>
                                </Card.Body>
                            </Card>
                        </Col>
                    )) }
                </Row>
            </Container>
        </div>
    );
}); 

export default QRCodesPrint;
