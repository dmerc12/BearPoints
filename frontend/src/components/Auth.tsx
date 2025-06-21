import { onAuthStateChanged, User } from 'firebase/auth';
import { getCurrentUser } from '../services/api';
import {UserDTO} from '../services/types.ts';
import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { Spinner } from 'react-bootstrap';
import { auth } from '../Auth';
import * as React from 'react';

interface AuthProps {
    children: React.ReactNode;
}

export default function Auth ({ children }: AuthProps) {
    const [ user, setUser ] = useState<User | null>(null);
    const [ userData, setUserData ] = useState<UserDTO | null>(null);
    const [ loading, setLoading ] = useState(true);

    useEffect(() => { 
        const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
            if (currentUser) {
                const token = await currentUser.getIdTokenResult();
                const email = token.claims.email as string || '';
                const isValidEmail = email.endsWith('@okcps.org')
                if (!token.claims.email_verified || !isValidEmail) {
                    await auth.signOut();
                    setLoading(false);
                    return;
                }
                try {
                    const userData = await getCurrentUser();
                    setUserData(userData);
                } catch (error) {
                    console.error('Failed to fetch user data:', error);
                }
            }
            setUser(currentUser);
            setLoading(false);
        });
        return () => unsubscribe();
    }, []);

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

    if (user && userData) {
        if (window.location.pathname === '/dashboard') {
            if (userData.role === 'TEACHER') {
                return <Navigate to={'/dashboard/teacher'} />;
            } else if (userData.role === 'STUDENT') {
                return <Navigate to={'/dashboard/student'} />;
            } else if (userData.role === 'ADMIN') {
                return <Navigate to={'/dashboard/admin'} />;
            }
        }
        return <>{children}</>
    }

    return user ? <>{ children }</> : <Navigate to='/' />;
}
