import { Container, Row, Col, Card, Button, ListGroup, Badge, Image } from 'react-bootstrap';
import bearMascot from '../assets/bear-mascot.png';
import { useAuthState } from 'react-firebase-hooks/auth';
import { auth, login } from '../Auth';
import { toast } from 'react-toastify';
import { useState } from 'react';

export default function AboutPage () {
    const [user] = useAuthState(auth);
    const [signingIn, setSigningIn] = useState(false);

    const handleLogin = async () => {
        setSigningIn(true);
        try {
            await login();
        } catch (error: unknown) {
            let errorMessage = 'Login failed';
            if (error instanceof Error) {
                errorMessage += `: ${error.message}`;
            }
            toast.error(errorMessage);
        } finally {
            setSigningIn(false);
        }
    }

    return (
        <Container className='mt-5 pt-5'>
            {/* Hero Section */ }
            <Row className='text-center mb-5'>
                <Col>
                    <Image src={ bearMascot }
                        alt='Buchanan Bear Mascot'
                        fluid
                        style={ { maxWidth: '200px' } }
                        className='mb-4'
                    />
                    <h1 className='display-4 text-primary'>BearPoints Behavior System</h1>
                    <p className='lead text-muted'>Recognizing Excellence at Buchanan Elementary</p>
                </Col>
            </Row>
            {/* How It Works Section */ }
            <Row className='mb-5'>
                <Col>
                    <h2 className='text-center mb-4'>
                        How It Works
                        <Badge bg='success' className='ms-2'>New</Badge>
                    </h2>
                    <Row xs={ 1 } md={ 3 } className='g-4'>
                        { [
                            {
                                title: 'Earn Points',
                                content: 'Staff scan QR codes to reward positive behavior',
                                variant: 'primary'
                            },
                            {
                                title: 'Track Progress',
                                content: 'Students view achievements & goals',
                                variant: 'info'
                            },
                            {
                                title: 'Celebrate',
                                content: 'Leaderboards show top performers',
                                variant: 'warning'
                            }
                        ].map((step, index) => (
                            <Col key={ index }>
                                <Card border={ step.variant } className='h-100 shadow-sm'>
                                    <Card.Body className='text-center'>
                                        <Badge pill bg={ step.variant } className='mb-3 fs-4'>
                                            { index + 1 }
                                        </Badge>
                                        <Card.Title>{ step.title }</Card.Title>
                                        <Card.Text>{ step.content }</Card.Text>
                                    </Card.Body>
                                </Card>
                            </Col>
                        )) }
                    </Row>
                </Col>
            </Row>
            {/* QR Code Section */ }
            <Row className='align-items-center mb-5'>
                <Col md={ 6 } className='text-center mb-4 mb-md-0'>
                    {/* <Image src={ qrExample }
                        alt='QR Code Example'
                        fluid thumbnail
                        style={ { maxWidth: '250px' } }
                    /> */}
                </Col>
                <Col md={ 6 }>
                    <h3 className='mb-3'>Positive Behaviors We Recognize</h3>
                    <ListGroup variant='flush'>
                        { [
                            'Brilliant Behavior',
                            'Excelled in Math',
                            'Answered & Participated',
                            'Read & Thought Carefully',
                            'Sensational Writing / Bear Time'
                        ].map((behavior, index) => (
                            <ListGroup.Item key={ index } className='d-flex align-items-center'>
                                <Badge bg='success' className='me-2'>✓</Badge>
                                { behavior }
                            </ListGroup.Item>
                        )) }
                    </ListGroup>
                </Col>
            </Row>
            {/* Call to Action */ }
            <Card className='text-center bg-primary text-white mb-5'>
                <Card.Body>
                    <Card.Title as='h2'>Ready to Participate?</Card.Title>
                    <Card.Text className='fs-5'>
                        Staff members can start recognizing students today!
                    </Card.Text>
                    { user ? (
                        <Button as='a' href='/students' variant='light' size='lg' className='rounded-pill px-4'>
                            Go to Dashboard
                        </Button>
                    ) : (
                        <Button onClick={handleLogin} disabled={signingIn} variant='light' size='lg' className='rounded-pill px-4'>
                            { signingIn ? 'Logging In...' : 'Staff Login' }
                        </Button>
                    )}
                </Card.Body>
            </Card>
            {/* School Info */ }
            <Row className='text-muted small'>
                <Col>
                    <hr />
                    <address className='text-center'>
                        <strong>Buchanan Elementary School</strong><br />
                        4126 NW 18 Oklahoma City, OK 73107<br />
                        Phone: (405) 587-4700
                        Fax: (405) 587-4705
                    </address>
                </Col>
            </Row>
        </Container>
    );
}
