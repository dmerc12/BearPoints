import { Modal, Button, ButtonGroup, ModalProps } from 'react-bootstrap';
import React from 'react';

interface BaseModalProps extends ModalProps {
    title: string;
    confirmText?: string;
    cancelText?: string;
    onConfirm?: () => void;
    onCancel?: () => void;
    confirmVariant?: string;
    show: boolean;
    children: React.ReactNode;
    isLoading?: boolean;
    disableConfirm?: boolean;
}

export default function BaseModal({ title, onConfirm, onCancel, show, children, 
                                      confirmText = 'Save', cancelText = 'Cancel', confirmVariant = 'primary', 
                                      isLoading = false, disableConfirm = false, ...modalProps
        } : BaseModalProps) {
    return (
        <Modal show={show} onHide={onCancel} {...modalProps}>
            <Modal.Header closeButton>
                <Modal.Title>{title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>{children}</Modal.Body>
            <Modal.Footer>
                <ButtonGroup>
                    <div className="d-flex gap-2 my-auto">
                        <Button variant='secondary'
                                onClick={onCancel}
                                disabled={isLoading}
                        >
                            {cancelText}
                        </Button>
                        { onConfirm && (
                            <Button variant={confirmVariant}
                                    onClick={onConfirm}
                                    disabled={isLoading || disableConfirm}
                            >
                                {isLoading ? 'Loading...' : confirmText}
                            </Button>
                        )}
                    </div>
                </ButtonGroup>
            </Modal.Footer>
        </Modal>
    );
}
        