import { RewardItemTable, AuthenticatedLayout } from '../components';
import { Row, Col } from 'react-bootstrap';

export default function RewardItemsPage () {
    return (
        <AuthenticatedLayout>
            <Row className='mb-4 justify-content-center'>
                <Col md={ 6 } className='text-center'>
                    <h1 className='mb-4'>Reward Items</h1>
                </Col>
            </Row>
            <RewardItemTable size='l' itemsPerPage={15} />
        </AuthenticatedLayout>
    );
}
