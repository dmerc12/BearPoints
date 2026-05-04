import { StudentTable, AuthenticatedLayout } from '../components';
import { Row, Col } from 'react-bootstrap';

export default function StudentsPage () {
    return (
        <AuthenticatedLayout>
            <Row className='mb-4 justify-content-center'>
                <Col md={ 6 } className='text-center'>
                    <h1 className='mb-4'>Students</h1>
                </Col>
            </Row>
            <StudentTable size='l' itemsPerPage={15} />
        </AuthenticatedLayout>
    );
}
