import React, { useState, useEffect } from 'react';
import { Card, Badge, Button, Row, Col } from 'react-bootstrap';
import { Link, useParams, useNavigate } from 'react-router-dom';
import TicketService from '../services/TicketService';

const TicketDetail = () => {
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { id } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchTicket = async () => {
      try {
        const data = await TicketService.getTicketById(id);
        setTicket(data);
        setError('');
      } catch (err) {
        console.error('Error fetching ticket:', err);
        setError('Failed to load ticket details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchTicket();
  }, [id]);

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this ticket?')) {
      try {
        await TicketService.deleteTicket(id);
        navigate('/tickets');
      } catch (err) {
        console.error('Error deleting ticket:', err);
        setError('Failed to delete ticket. Please try again.');
      }
    }
  };

  const getStatusColor = (status) => {
    if (!status) return 'secondary';
    
    switch(status.toLowerCase()) {
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

  if (loading) {
    return (
      <div className="text-center p-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-2">Loading ticket details...</p>
      </div>
    );
  }

  if (error) {
    return <div className="alert alert-danger">{error}</div>;
  }

  if (!ticket) {
    return <div className="alert alert-warning">Ticket not found</div>;
  }

  return (
    <div className="ticket-detail">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Ticket Details</h1>
        <div>
          <Link to="/tickets" className="btn btn-outline-secondary me-2">
            Back to Tickets
          </Link>
          <Link to={`/tickets/${id}/edit`} className="btn btn-primary">
            Edit Ticket
          </Link>
        </div>
      </div>

      <Card className="mb-4">
        <Card.Header className="bg-white">
          <div className="d-flex justify-content-between align-items-center">
            <h3 className="mb-0">{ticket.subject}</h3>
            <Badge bg={getStatusColor(ticket.status)} className="fs-6">
              {ticket.status}
            </Badge>
          </div>
        </Card.Header>
        <Card.Body>
          <Row className="mb-4">
            <Col md={6}>
              <dl>
                <dt>Ticket ID</dt>
                <dd>{ticket.id}</dd>

                <dt>Priority</dt>
                <dd>{ticket.priority || 'Not assigned'}</dd>

                <dt>Category</dt>
                <dd>{ticket.category || 'Not assigned'}</dd>
              </dl>
            </Col>
            <Col md={6}>
              <dl>
                <dt>Created</dt>
                <dd>
                  {ticket.createdTime ? new Date(ticket.createdTime).toLocaleString() : 'Unknown'}
                </dd>

                <dt>Last Updated</dt>
                <dd>
                  {ticket.modifiedTime ? new Date(ticket.modifiedTime).toLocaleString() : 'Unknown'}
                </dd>

                <dt>Department</dt>
                <dd>{ticket.departmentId || 'Not assigned'}</dd>
              </dl>
            </Col>
          </Row>

          <div>
            <h4>Description</h4>
            <p style={{ whiteSpace: 'pre-wrap' }}>{ticket.description}</p>
          </div>
        </Card.Body>
        <Card.Footer className="bg-white">
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <Button variant="outline-danger" onClick={handleDelete}>
                Delete Ticket
              </Button>
            </div>
          </div>
        </Card.Footer>
      </Card>
    </div>
  );
};

export default TicketDetail;