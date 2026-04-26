import { TeacherTable, Auth } from '../components';
import { Container, Row, Col } from 'react-bootstrap';

export default function TeachersPage () {
    return (
        <Auth>
            <Container className='mt-3 pt-2 mb-4'>
                <Row className='mb-4 justify-content-center'>
                    <Col md={ 6 } className='text-center'>
                        <h1 className='mb-4'>Teachers</h1>
                    </Col>
                </Row>
                <TeacherTable size='l' itemsPerPage={15} />
            </Container>
        </Auth>
    );
}
