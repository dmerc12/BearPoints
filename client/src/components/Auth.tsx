import { Spinner, Alert, Button, Container, Row, Col } from 'react-bootstrap';
import { useAppDispatch, useAppSelector } from '../store/hooks';
import { fetchCurrentUser } from '../store/slices/userSlice';
import { Navigate, useLocation } from 'react-router-dom';
import { onAuthStateChanged, User } from 'firebase/auth';
import { clearUser } from '../store/slices/userSlice';
import { useCallback, useEffect } from 'react';
import { auth } from '../Auth';
import * as React from 'react';

interface AuthProps {
    children: React.ReactNode;
}

export default function Auth ({ children }: AuthProps) {
    const { data: user, loading, error } = useAppSelector(state => state.user);
    const dispatch = useAppDispatch();
    const location = useLocation();

    const handleAuthStateChanged = useCallback(async (currentUser: User | null) => {
        try {
            if (currentUser) {
                const token = await currentUser.getIdTokenResult();
                const email = token.claims.email as string || '';
                const isValidEmail = email.endsWith('@okcps.org');
                if (!token.claims.email_verified && !isValidEmail) {
                    console.warn('Email not verified or is invalid:', email);
                    await auth.signOut();
                    dispatch(clearUser());
                } else {
                    dispatch(fetchCurrentUser({ force: true }));
                }
            } else {
                console.log('No authenticated user');
                dispatch(clearUser());
            }
        } catch (error) {
            console.error('Auth state change error', error);
        }
    }, [dispatch]);

    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, handleAuthStateChanged);
        return () => unsubscribe();
    }, [handleAuthStateChanged]);

    if (loading) {
        return (
            <Container className='d-flex justify-content-center align-items-center min-vh-100'>
                <Row>
                    <Col className='text-center'>
                        <Spinner animation='border' variant='primary'>
                            <span className='visually-hidden'>Loading...</span>
                        </Spinner>
                        <p className='mt-3'>Authenticating...</p>
                    </Col>
                </Row>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className='mt-5'>
                <Row className='justify-content-center'>
                    <Col md={6}>
                        <Alert variant='danger' className='text-center'>
                            <Alert.Heading>Authentication Error</Alert.Heading>
                            <p>{error}</p>
                            <hr />
                            <div className='d-flex justify-content-center'>
                                <Button
                                    variant='outline-danger'
                                    onClick={() =>
                                        dispatch(fetchCurrentUser({ force: true }))}
                                >
                                    Retry Authentication
                                </Button>
                            </div>
                        </Alert>
                    </Col>
                </Row>
            </Container>
        );
    }

    if (!user && location.pathname !== '/') {
        return <Navigate to='/' replace />;
    }

    return <>{ children }</>;
}
