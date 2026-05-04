import { TeacherTable, AuthenticatedLayout } from '../components';
import { Row, Col } from 'react-bootstrap';

export default function TeachersPage () {
    return (
        <AuthenticatedLayout>
            <Row className='mb-4 justify-content-center'>
                <Col md={ 6 } className='text-center'>
                    <h1 className='mb-4'>Teachers</h1>
                </Col>
            </Row>
            <TeacherTable size='l' itemsPerPage={15} />
        </AuthenticatedLayout>
    );
}
