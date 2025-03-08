import React, { useState, useEffect } from 'react';
import { Card, Form, Button, Alert } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import TicketService from '../services/TicketService';

const TicketForm = () => {
  const [ticket, setTicket] = useState({
    subject: '',
    description: '',
    status: 'Open',
    priority: 'Medium',
    category: '',
    subCategory: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isEditMode, setIsEditMode] = useState(false);
  const { id } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      setIsEditMode(true);
      fetchTicket(id);
    }
  }, [id]);

  const fetchTicket = async (ticketId) => {
    try {
      setLoading(true);
      const data = await TicketService.getTicketById(ticketId);
      setTicket(data);
      setError('');
    } catch (err) {
      console.error('Error fetching ticket:', err);
      setError('Failed to load ticket details. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setTicket(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!ticket.subject || !ticket.description) {
      setError('Subject and description are required.');
      return;
    }
    
    try {
      setLoading(true);
      
      if (isEditMode) {
        await TicketService.updateTicket(id, ticket);
        navigate(`/tickets/${id}`);
      } else {
        const newTicket = await TicketService.createTicket(ticket);
        navigate(`/tickets/${newTicket.id}`);
      }
    } catch (err) {
      console.error('Error saving ticket:', err);
      setError('Failed to save ticket. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ticket-form">
      <h1>{isEditMode ? 'Edit Ticket' : 'Create New Ticket'}</h1>
      
      <Card>
        <Card.Body>
          {error && <Alert variant="danger">{error}</Alert>}
          
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Subject</Form.Label>
              <Form.Control
                type="text"
                name="subject"
                value={ticket.subject}
                onChange={handleChange}
                placeholder="Enter ticket subject"
                required
              />
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label>Description</Form.Label>
              <Form.Control
                as="textarea"
                name="description"
                value={ticket.description}
                onChange={handleChange}
                placeholder="Enter ticket description"
                rows={5}
                required
              />
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label>Status</Form.Label>
              <Form.Select
                name="status"
                value={ticket.status}
                onChange={handleChange}
              >
                <option value="Open">Open</option>
                <option value="In Progress">In Progress</option>
                <option value="On Hold">On Hold</option>
                <option value="Resolved">Resolved</option>
                <option value="Closed">Closed</option>
              </Form.Select>
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label>Priority</Form.Label>
              <Form.Select
                name="priority"
                value={ticket.priority}
                onChange={handleChange}
              >
                <option value="Low">Low</option>
                <option value="Medium">Medium</option>
                <option value="High">High</option>
                <option value="Urgent">Urgent</option>
              </Form.Select>
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label>Category</Form.Label>
              <Form.Control
                type="text"
                name="category"
                value={ticket.category}
                onChange={handleChange}
                placeholder="Enter category (optional)"
              />
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label>Sub-category</Form.Label>
              <Form.Control
                type="text"
                name="subCategory"
                value={ticket.subCategory}
                onChange={handleChange}
                placeholder="Enter sub-category (optional)"
              />
            </Form.Group>
            
            <div className="d-flex justify-content-between">
              <Button 
                variant="secondary" 
                onClick={() => navigate('/tickets')}
              >
                Cancel
              </Button>
              <Button 
                variant="primary" 
                type="submit"
                disabled={loading}
              >
                {loading ? 'Saving...' : isEditMode ? 'Update Ticket' : 'Create Ticket'}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </div>
  );
};

export default TicketForm;