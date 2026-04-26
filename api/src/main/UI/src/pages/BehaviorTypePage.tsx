import { BehaviorTypeTable, AuthenticatedLayout } from '../components';
import { Row, Col } from 'react-bootstrap';

export default function BehaviorTypesPage () {
    return (
        <AuthenticatedLayout>
            <Row className='mb-4 justify-content-center'>
                <Col md={ 6 } className='text-center'>
                    <h1 className='mb-4'>Behavior Types</h1>
                </Col>
            </Row>
            <BehaviorTypeTable size='l' itemsPerPage={15} />
        </AuthenticatedLayout>
    );
}
