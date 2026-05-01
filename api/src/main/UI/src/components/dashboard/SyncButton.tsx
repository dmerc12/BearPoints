import { Button, Spinner, Alert } from 'react-bootstrap';
import { useSync } from '../../hooks';
import { useEffect } from 'react';

interface SyncButtonProps {
    variant?: string;
    size?: 'sm' | 'lg';
    className?: string;
    showStatus?: boolean;
}

export default function SyncButton({
    variant = 'secondary',
    size,
    className = '',
    showStatus = true
}: SyncButtonProps) {
    const { syncing, success, error, handleSync, clearStatus } = useSync();

    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => {
                clearStatus();
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [success, clearStatus]);

    return (
        <div className={className}>
            <Button variant={variant}
                    size={size}
                    onClick={handleSync}
                    disabled={syncing}
            >
                {syncing ? (
                    <>
                        <Spinner size="sm" animation="border" className="me-2" />
                        Syncing...
                    </>
                ) : (
                    'Sync to Sheets'
                )}
            </Button>
            {showStatus && success && (
                <Alert variant="success" className="mt-2 mb-0 py-2">
                    Sync completed successfully!
                </Alert>
            )}
            {showStatus && error && (
                <Alert variant="danger" className="mt-2 mb-0 py-2">
                    Sync failed: {error}
                </Alert>
            )}
        </div>
    );
}
