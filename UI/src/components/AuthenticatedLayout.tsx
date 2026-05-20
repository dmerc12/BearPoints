import { Container, Row, Col, Offcanvas } from 'react-bootstrap';
import React, { useState, useEffect } from 'react';
import { Sidebar, Auth, NavBar } from './index';

interface AuthenticatedLayoutProps {
    children: React.ReactNode;
}

export default function AuthenticatedLayout({ children }: AuthenticatedLayoutProps) {
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [isMobile, setIsMobile] = useState(false);

    useEffect(() => {
        const originalPadding = document.body.style.paddingTop;
        const computedPadding = getComputedStyle(document.body).paddingTop;
        document.body.style.paddingTop = '0';
        return () => {
            document.body.style.paddingTop = originalPadding || computedPadding;
        }
    }, []);

    useEffect(() => {
        const checkMobile = () => setIsMobile(window.innerWidth < 768);
        checkMobile();
        window.addEventListener('resize', checkMobile);
        return () => window.removeEventListener('resize', checkMobile);
    }, []);

    const toggleSidebar = () => setSidebarOpen(prev => !prev);
    const closeSidebar = () => setSidebarOpen(false);

    return (
        <Auth>
            <div className="d-flex flex-column vh-100">
                <NavBar
                    onToggleSidebar={toggleSidebar}
                    isDashboard={true}
                    isMobile={isMobile}
                    sidebarOpen={sidebarOpen}
                />
                <Container fluid className='g-0'>
                    <Row className='g-0'>
                        {!isMobile && (
                            <Col md={sidebarOpen ? 3 : 1}
                                 lg={sidebarOpen ? 2 : 1}
                                 className="bg-light vh-100 overflow-auto transition-sidebar"
                                 style={{
                                     width: sidebarOpen ? 'auto' : 0,
                                     transition: 'width 0.2s ease-in-out',
                                     overflowX: 'hidden',
                                 }}
                            >
                                <Sidebar collapsed={!sidebarOpen} />
                            </Col>
                        )}
                        <Col xs={12}
                             md={sidebarOpen ? 9 : 11}
                             lg={sidebarOpen ? 10 : 11}
                             className='p-4'
                        >
                            {children}
                        </Col>
                    </Row>
                </Container>
            </div>
            <Offcanvas show={isMobile && sidebarOpen} onHide={closeSidebar} placement="start">
                <Offcanvas.Header closeButton>
                    <Offcanvas.Title>Menu</Offcanvas.Title>
                </Offcanvas.Header>
                <Offcanvas.Body>
                    <Sidebar />
                </Offcanvas.Body>
            </Offcanvas>
        </Auth>
    );
}
