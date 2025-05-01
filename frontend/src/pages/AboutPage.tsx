import { Container, Row, Col, Card, Button, ListGroup, Badge, Image } from 'react-bootstrap';
import { useAuthState } from 'react-firebase-hooks/auth';
import bearMascot from '../assets/bear-mascot.png';
import { toast } from 'react-toastify';
import { auth, login } from '../Auth';
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

    const steps = [
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
    ];

    const behaviors = [
        'Brilliant Behavior',
        'Excelled in Math',
        'Answered & Participated',
        'Read & Thought Carefully',
        'Sensational Writing / Bear Time'
    ];

    return (
        <Container className='mt-3 pt-3 mb-4'>
            {/* Hero Section */ }
            <Row className='text-center mb-5'>
                <Col>
                    <Image src={ bearMascot }
                        alt='Buchanan Bear Mascot'
                        fluid
                        style={ { maxWidth: '200px' } }
                        className='mb-4'
                    />
                    <h1 className='display-6 display-md-5 display-lg-3 text-primary'>BearPoints Behavior System</h1>
                    <p className='fs-6 fs-md-5 text-muted'>Recognizing Excellence at Buchanan Elementary</p>
                </Col>
            </Row>
            {/* How It Works Section */ }
            <Row className='mb-5 justify-content-center align-items-center'>
                <Col lg={10} xl={8} className='text-center'>
                    <h2 className='fs-5 fs-md-4 mb-4'>
                        How It Works
                        <Badge bg='success' className='ms-2'>New</Badge>
                    </h2>
                    <Row xs={ 1 } md={ 3 } className='g-4 justify-content-center'>
                        { steps.map((step, index) => (
                            <Col key={ index } className='d-flex justify-content-center'>
                                <Card border={ step.variant } className='h-100 shadow-sm w-100' style={{ maxWidth: '300px' }}>
                                    <Card.Body className='text-center'>
                                        <Badge pill bg={ step.variant } className='mb-3 fs-6 fs-md-5'>
                                            { index + 1 }
                                        </Badge>
                                        <Card.Title className='fs-6 fs-md-5'>{ step.title }</Card.Title>
                                        <Card.Text className='fs-9 fs-md-8'>{ step.content }</Card.Text>
                                    </Card.Body>
                                </Card>
                            </Col>
                        )) }
                    </Row>
                </Col>
            </Row>
            {/* QR Code Section */ }
            <Row className='align-items-center mb-5 justify-content-center'>
                <Col lg={10} xl={8}>
                    {/* <Image src={ qrExample }
                        alt='QR Code Example'
                        fluid thumbnail
                        style={ { maxWidth: '250px' } }
                    /> */}
                </Col>
                <Col lg={10} xl={8} className='text-center'>
                    <h3 className='fs-6 fs-md-5 mb-3'>Positive Behaviors We Recognize</h3>
                    <ListGroup variant='flush' className='mx-auto' style={{ maxWidth: '300px' }}>
                        { behaviors.map((behavior, index) => (
                            <ListGroup.Item key={ index } className='d-flex justify-content-center align-items-center fs-8 fs-md-7'>
                                <Badge bg='success' className='me-2'>✓</Badge>
                                { behavior }
                            </ListGroup.Item>
                        )) }
                    </ListGroup>
                </Col>
            </Row>
            {/* Call to Action */ }
            <Row className='justify-content-center'>
                <Col md={10} lg={8} xl={6}>
                    <Card className='text-center bg-primary text-white mb-5'>
                        <Card.Body>
                            <Card.Title as='h2' className='fs-5 fs-md-4'>Ready to Participate?</Card.Title>
                            <Card.Text className='fs-6 fs-md-5'>
                                Staff members can start recognizing students today!
                            </Card.Text>
                            { user ? (
                                <Button as='a' href='/students' variant='light' className='rounded-pill fs-6 fs-md-5 px-3 px-md-4 py-2'>
                                    Go to Dashboard
                                </Button>
                            ) : (
                                <Button onClick={handleLogin} disabled={signingIn} variant='light' className='rounded-pill fs-6 fs-md-5 px-3 px-md-4 py-2'>
                                    { signingIn ? 'Logging In...' : 'Staff Login' }
                                </Button>
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
            {/* School Info */ }
            <Row className='text-muted small mb-4'>
                <Col>
                    <hr />
                    <address className='text-center fs-6 fs-md-5'>
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
