import { Button, Spinner } from 'react-bootstrap';
import { useAppSelector } from '../store/hooks';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import Auth from "../components/Auth.tsx";

export default function DashboardRedirect() {
    const { data: userData, loading, error } = useAppSelector((state) => state.user);
    const navigate = useNavigate();

    useEffect(() => {
        if (!loading && !error) {
            if (!userData) {
                console.log('No user data, redirecting to home');
                navigate('/', { replace: true });
            } else {
                switch (userData.role) {
                    case 'TEACHER':
                        navigate('/dashboard/teacher', { replace: true });
                        break;
                    case 'STUDENT':
                        navigate('/dashboard/student', { replace: true });
                        break;
                    case 'ADMIN':
                        navigate('/dashboard/admin', { replace: true });
                        break;
                    default:
                        navigate('/', { replace: true });
                }
            }
        }
    }, [userData, loading, error, navigate]);

    if (error) {
        console.error('User data error:', error);
        return (
            <div className='text-center my-4 text-danger'>
                <h4>Error Loading Dashboard</h4>
                <p>{error}</p>
                <Button onClick={() => window.location.reload()}>Retry</Button>
            </div>
        );
    }

    if (loading) {
        return (
            <Auth>
                <div className='text-center my-4'>
                    <div className='text-center my-4'>
                        <Spinner animation='border' role='status'>
                            <span className='visually-hidden'>Loading...</span>
                        </Spinner>
                        <p>Loading Your Dashboard...</p>
                    </div>
                    <p>You should be redirected automatically.</p>
                    <Button variant='primary' onClick={() => window.location.reload()}>
                        Refresh Page
                    </Button>
                </div>
            </Auth>
        );
    }

    return null;
}
