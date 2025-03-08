import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Badge, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import TicketService from '../services/TicketService';
import { useAuth } from '../context/AuthContext';

const Dashboard = () => {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useAuth();

  useEffect(() => {
    const fetchTickets = async () => {
      try {
        const data = await TicketService.getAllTickets();
        setTickets(data);
      } catch (err) {
        console.error('Error fetching tickets:', err);
        setError('Failed to load tickets. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchTickets();
  }, []);

  const getStatusColor = (status) => {
    switch(status?.toLowerCase()) {
      case 'open':
        return 'primary';
      case 'in progress':
        return 'info';
      case 'on hold':
        return 'warning';
      case 'resolved':
        return 'success';
      case 'closed':
        return 'secondary';
      default:
        return 'light';
    }
  };

  const renderRecentTickets = () => {
    if (tickets.length === 0) {
      return <p>No tickets found. Create your first ticket!</p>;
    }

    // Show only the 5 most recent tickets
    const recentTickets = tickets.slice(0, 5);
    
    return recentTickets.map(ticket => (
      <Card key={ticket.id} className="mb-3">
        <Card.Body>
          <div className="d-flex justify-content-between align-items-center mb-2">
            <h5 className="card-title mb-0">
              <Link to={`/tickets/${ticket.id}`} className="text-decoration-none">
                {ticket.subject}
              </Link>
            </h5>
            <Badge bg={getStatusColor(ticket.status)}>{ticket.status}</Badge>
          </div>
          <Card.Text className="text-muted mb-2">
            ID: {ticket.id}
          </Card.Text>
          <Card.Text>
            {ticket.description?.length > 100
              ? `${ticket.description.substring(0, 100)}...`
              : ticket.description}
          </Card.Text>
          <div className="d-flex justify-content-between align-items-center mt-3">
            <small className="text-muted">
              Created: {new Date(ticket.createdTime).toLocaleDateString()}
            </small>
            <Link to={`/tickets/${ticket.id}`} className="btn btn-sm btn-outline-primary">
              View Details
            </Link>
          </div>
        </Card.Body>
      </Card>
    ));
  };

  return (
    <div className="dashboard">
      <Row className="mb-4">
        <Col>
          <h1>Welcome to Your Ticket Dashboard</h1>
          <p className="lead">
            Manage your support tickets and get help from our team.
          </p>
        </Col>
      </Row>

      {error && (
        <Row className="mb-4">
          <Col>
            <div className="alert alert-danger">{error}</div>
          </Col>
        </Row>
      )}

      <Row className="mb-4">
        <Col md={8}>
          <Card className="shadow-sm">
            <Card.Header className="bg-white">
              <div className="d-flex justify-content-between align-items-center">
                <h4 className="mb-0">Recent Tickets</h4>
                <Link to="/tickets" className="btn btn-sm btn-outline-primary">
                  View All
                </Link>
              </div>
            </Card.Header>
            <Card.Body>
              {loading ? (
                <div className="text-center py-4">
                  <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                  <p className="mt-2">Loading tickets...</p>
                </div>
              ) : (
                renderRecentTickets()
              )}
            </Card.Body>
          </Card>
        </Col>
        
        <Col md={4}>
          <Card className="shadow-sm mb-4">
            <Card.Header className="bg-white">
              <h4 className="mb-0">Quick Actions</h4>
            </Card.Header>
            <Card.Body>
              <div className="d-grid gap-2">
                <Link to="/tickets/new" className="btn btn-primary">
                  Create New Ticket
                </Link>
                <Link to="/tickets" className="btn btn-outline-secondary">
                  View All Tickets
                </Link>
              </div>
            </Card.Body>
          </Card>
          
          <Card className="shadow-sm">
            <Card.Header className="bg-white">
              <h4 className="mb-0">Account Info</h4>
            </Card.Header>
            <Card.Body>
            {user && (
                <div>
                  <p><strong>Email:</strong> {user.email}</p>
                  <p><strong>User ID:</strong> {user.id}</p>
                </div>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;