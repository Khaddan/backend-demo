import React from 'react';
import { Navigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';

const ProtectedRoute = ({ children }) => {
    const { keycloak } = useKeycloak();

    return keycloak.authenticated ? children : <Navigate to="/" />;
};

export default ProtectedRoute;
