import { useAuthState } from 'react-firebase-hooks/auth';
import { useNavigate } from 'react-router-dom';
import { auth, login } from '../Auth';
import { useState } from 'react';

export default function useLogin() {
    const [signingIn, setSigningIn] = useState(false);
    const navigate = useNavigate();
    const [user] = useAuthState(auth);

    const handleLogin = async () => {
        setSigningIn(true);
        try {
            await login();
            navigate('/dashboard');
        } catch (error) {
            console.error('Login failed:', error);
        } finally {
            setSigningIn(false);
        }
    };

    const handleLogout = async () => {
        try {
            await auth.signOut();
            navigate('/');
        } catch (error) {
            console.error('Logout failed:', error);
        }
    }

    return { user, signingIn, handleLogin, handleLogout };
}