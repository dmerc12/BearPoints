import { onAuthStateChanged, User } from 'firebase/auth';
import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { auth } from '../Auth';

interface AuthProps {
    children: React.ReactNode;
}

export default function Auth ({ children }: AuthProps) {
    const [ user, setUser ] = useState<User | null>(null);
    const [ loading, setLoading ] = useState(true);

    useEffect(() => { 
        const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
            if (currentUser) {
                const token = await currentUser.getIdTokenResult();
                const email = token.claims.email as string || '';
                const isValidEmail = email.endsWith('@okcps.org')
                if (!token.claims.email_verified || !isValidEmail) {
                    await auth.signOut();
                    return;
                }
            }
            setUser(currentUser);
            setLoading(false);
        });
        return () => unsubscribe();
    }, []);

    if (loading) return <div>Loading</div>

    return user ? <>{ children }</> : <Navigate to='/' />;
}
