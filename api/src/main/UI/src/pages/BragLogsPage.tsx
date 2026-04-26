import { BragLogTable, AuthenticatedLayout } from '../components';
import { Row, Col } from 'react-bootstrap';

export default function BragLogsPage () {
    return (
        <AuthenticatedLayout>
            <Row className='mb-4 justify-content-center'>
                <Col md={ 6 } className='text-center'>
                    <h1 className='mb-4'>Bear Brags</h1>
                </Col>
            </Row>
            <BragLogTable size='l' itemsPerPage={15} />
        </AuthenticatedLayout>
    );
}
