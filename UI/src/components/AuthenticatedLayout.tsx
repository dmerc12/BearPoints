import { Container, Row, Col } from 'react-bootstrap';
import { Sidebar, Auth, NavBar } from './index';
import React, { useState } from 'react';

interface AuthenticatedLayoutProps {
    children: React.ReactNode;
}

export default function AuthenticatedLayout({ children }: AuthenticatedLayoutProps) {
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const toggleSidebar = () => setSidebarOpen(prev => !prev);

    return (
        <Auth>
            <Container fluid className='g-0'>
                <Row className='g-0'>
                    <Col xs={12}
                         md={sidebarOpen ? 3 : 0}
                         lg={sidebarOpen ? 2 : 0}
                         className="bg-light vh-100 overflow-auto"
                         style={{ transition: 'all 0.2s' }}
                    >
                        <Sidebar />
                    </Col>
                    <Col xs={12}
                         md={sidebarOpen ? 9 : 12}
                         lg={sidebarOpen ? 10 : 12}
                         className='p-4'
                    >
                        <NavBar onToggleSidebar={toggleSidebar} isDashboard={true} />
                        <div className="p-4">
                            {children}
                        </div>
                    </Col>
                </Row>
            </Container>
        </Auth>
    );
}
