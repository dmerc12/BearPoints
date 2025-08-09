import {clearUser, setError, setUser} from '../store/slices/userSlice';
import { Navigate, useLocation } from 'react-router-dom';
import { useCallback, useEffect, useState } from 'react';
import { onAuthStateChanged, User } from 'firebase/auth';
import { useAppDispatch } from '../store/hooks';
import { Spinner } from 'react-bootstrap';
import { auth } from '../Auth';
import * as React from 'react';
import {getCurrentUser} from "../services/api.ts";

interface AuthProps {
    children: React.ReactNode;
}

export default function Auth ({ children }: AuthProps) {
    const [ loading, setLoading ] = useState(true);
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
                    try {
                        const userData = await getCurrentUser();
                        dispatch(setUser(userData));
                    } catch (error) {
                        console.error('Failed to fetch user data', error);
                        dispatch(setError('Failed to fetch user data'));
                    }
                }
            } else {
                console.log('No authenticated user');
                dispatch(clearUser());
            }
        } catch (error) {
            console.error('Auth state change error', error);
            dispatch(setError('Authentication error'));
        } finally {
            setLoading(false);
        }
    }, [dispatch]);

    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, handleAuthStateChanged);
        return () => unsubscribe();
    }, [handleAuthStateChanged]);

    if (loading) {
        return (
            <div className='text-center my-4'>
                <Spinner animation='border' role='status'>
                    <span className='visually-hidden'>Loading...</span>
                </Spinner>
                <p>Loading...</p>
            </div>
        );
    }

    if (!auth.currentUser && location.pathname !== '/') {
        return <Navigate to='/' replace />;
    }

    return <>{ children }</>;
}
