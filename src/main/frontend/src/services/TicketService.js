import axios from 'axios';

const API_URL = '/api/tickets';

class TicketService {
  getAllTickets() {
    return axios.get(API_URL, { withCredentials: true })
      .then(response => response.data);
  }

  getTicketById(id) {
    return axios.get(`${API_URL}/${id}`, { withCredentials: true })
      .then(response => response.data);
  }

  createTicket(ticket) {
    return axios.post(API_URL, ticket, { withCredentials: true })
      .then(response => response.data);
  }

  updateTicket(id, ticket) {
    return axios.put(`${API_URL}/${id}`, ticket, { withCredentials: true })
      .then(response => response.data);
  }

  deleteTicket(id) {
    return axios.delete(`${API_URL}/${id}`, { withCredentials: true })
      .then(response => response.data);
  }
}

export default new TicketService();