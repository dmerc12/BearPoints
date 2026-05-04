import { UserTable, AuthenticatedLayout } from '../components';
import { Row, Col } from 'react-bootstrap';

export default function UsersPage () {
    return (
        <AuthenticatedLayout>
            <Row className='mb-4 justify-content-center'>
                <Col md={ 6 } className='text-center'>
                    <h1 className='mb-4'>Users</h1>
                </Col>
            </Row>
            <UserTable size='l' itemsPerPage={15} />
        </AuthenticatedLayout>
    );
}
