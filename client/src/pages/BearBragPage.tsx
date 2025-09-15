import { Container, Row, Col } from 'react-bootstrap';
import { useSearchParams } from 'react-router-dom';
import { PublicBragLogForm } from '../components';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

export default function BearBragPage () {
    const [ searchParams ] = useSearchParams();
    const [ studentToken, setStudentToken] = useState<string>('');

    const token = searchParams.get('token');

    useEffect(() => {
        if (!token || !/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(token)) {
            toast.error('Invalid QR code format');
            setStudentToken('');
            return;
        }
        setStudentToken(token);
    }, [ token ]);

    return (
        <Container className='mt-3 pt-2 mb-4'>
            <Row className='mb-4 justify-content-center'>
                <Col className='text-center'>
                    <h1>Bear Brag</h1>
                </Col>
            </Row>
            <PublicBragLogForm studentToken={studentToken} />
        </Container>
    );
}
