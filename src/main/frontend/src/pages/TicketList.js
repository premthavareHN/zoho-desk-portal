import React, { useState, useEffect } from 'react';
import { Table, Badge, Button, Card, Row, Col, Form } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import TicketService from '../services/TicketService';

const TicketList = () => {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  useEffect(() => {
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    try {
      const data = await TicketService.getAllTickets();
      setTickets(data);
      setError('');
    } catch (err) {
      console.error('Error fetching tickets:', err);
      setError('Failed to load tickets. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this ticket?')) {
      try {
        await TicketService.deleteTicket(id);
        setTickets(tickets.filter(ticket => ticket.id !== id));
      } catch (err) {
        console.error('Error deleting ticket:', err);
        setError('Failed to delete ticket. Please try again.');
      }
    }
  };

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

  const filteredTickets = filterStatus 
    ? tickets.filter(ticket => ticket.status?.toLowerCase() === filterStatus.toLowerCase())
    : tickets;

  return (
    <div className="ticket-list">
      <h1>Your Tickets</h1>
      
      <Row className="mb-3">
        <Col md={6}>
          <Form.Group>
            <Form.Label>Filter by Status</Form.Label>
            <Form.Select 
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="open">Open</option>
              <option value="in progress">In Progress</option>
              <option value="on hold">On Hold</option>
              <option value="resolved">Resolved</option>
              <option value="closed">Closed</option>
            </Form.Select>
          </Form.Group>
        </Col>
        <Col md={6} className="d-flex align-items-end justify-content-end">
          <Link to="/tickets/new" className="btn btn-primary">
            Create New Ticket
          </Link>
        </Col>
      </Row>

      {error && <div className="alert alert-danger">{error}</div>}
      
      <Card>
        {loading ? (
          <div className="text-center p-5">
            <div className="spinner-border text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
            <p className="mt-2">Loading tickets...</p>
          </div>
        ) : (
          <div className="table-responsive">
            <Table striped hover>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Subject</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Created</th>
                  <th>Updated</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredTickets.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="text-center">
                      No tickets found
                    </td>
                  </tr>
                ) : (
                  filteredTickets.map(ticket => (
                    <tr key={ticket.id}>
                      <td>{ticket.id}</td>
                      <td>
                        <Link to={`/tickets/${ticket.id}`}>
                          {ticket.subject}
                        </Link>
                      </td>
                      <td>
                        <Badge bg={getStatusColor(ticket.status)}>
                          {ticket.status}
                        </Badge>
                      </td>
                      <td>{ticket.priority}</td>
                      <td>
                        {ticket.createdTime ? new Date(ticket.createdTime).toLocaleDateString() : ''}
                      </td>
                      <td>
                        {ticket.modifiedTime ? new Date(ticket.modifiedTime).toLocaleDateString() : ''}
                      </td>
                      <td>
                        <div className="btn-group btn-group-sm">
                          <Link 
                            to={`/tickets/${ticket.id}`}
                            className="btn btn-outline-primary"
                          >
                            View
                          </Link>
                          <Link 
                            to={`/tickets/${ticket.id}/edit`}
                            className="btn btn-outline-secondary"
                          >
                            Edit
                          </Link>
                          <Button 
                            variant="outline-danger"
                            onClick={() => handleDelete(ticket.id)}
                          >
                            Delete
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </Table>
          </div>
        )}
      </Card>
    </div>
  );
};

export default TicketList;