import StudentTable from '../components/student/StudentTable.tsx';
import { Container, Row, Col } from 'react-bootstrap';
import Auth from '../components/Auth';

export default function StudentsPage () {
    return (
        <Auth>
            <Container className='mt-3 pt-2 mb-4'>
                <Row className='mb-4 justify-content-center'>
                    <Col md={ 6 } className='text-center'>
                        <h1 className='mb-4'>Students</h1>
                    </Col>
                </Row>
                <StudentTable size='l' itemsPerPage={15} />
            </Container>
        </Auth>
    );
}
