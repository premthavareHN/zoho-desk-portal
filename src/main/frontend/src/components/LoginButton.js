// Frontend component for login button
import React from 'react';

function LoginButton() {
  return (
    <a href="/oauth2/authorization/okta" className="btn btn-primary">
      Sign in with Okta
    </a>
  );
}

export default LoginButton;