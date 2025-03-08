import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function Dashboard() {
  const [user, setUser] = useState(null);
  const [zohoUrl, setZohoUrl] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    // Fetch user info
    async function fetchUserInfo() {
      try {
        const response = await fetch('/api/auth/user');
        const data = await response.json();
        
        if (data.authenticated) {
          setUser(data);
          
          // If not connected to Zoho, get the auth URL
          if (!data.zohoConnected) {
            fetchZohoAuthUrl();
          }
        } else {
          // Redirect to home if not authenticated
          navigate('/');
        }
      } catch (error) {
        console.error('Error fetching user info:', error);
      }
    }
    
    // Fetch Zoho auth URL
    async function fetchZohoAuthUrl() {
      try {
        const response = await fetch('/api/auth/zoho-url');
        if (response.ok) {
          const data = await response.json();
          setZohoUrl(data.url);
        }
      } catch (error) {
        console.error('Error fetching Zoho auth URL:', error);
      }
    }
    
    fetchUserInfo();
  }, [navigate]);

  return (
    <div className="container mt-4">
      <h2>Dashboard</h2>
      
      {user && (
        <div className="card mt-4">
          <div className="card-body">
            <h5 className="card-title">Welcome, {user.name}</h5>
            <p className="card-text">Your account is successfully authenticated with Okta.</p>
            
            {!user.zohoConnected && zohoUrl && (
              <div className="alert alert-info mt-3">
                <p>To access your Zoho Desk tickets, please connect your Zoho account:</p>
                <a href={zohoUrl} className="btn btn-primary">
                  Connect Zoho Account
                </a>
              </div>
            )}
            
            {user.zohoConnected && (
              <div className="alert alert-success mt-3">
                <p>Your Zoho account is connected! You can now access your tickets.</p>
                <a href="/tickets" className="btn btn-success">
                  View My Tickets
                </a>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;