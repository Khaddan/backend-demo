import React from 'react';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import keycloak from './keycloak';

const KeycloakProviderWrapper = ({ children }) => {
    const handleTokens = (tokens) => {
        if (tokens.token) {
            localStorage.setItem('kc_token', tokens.token);
            localStorage.setItem('kc_refreshToken', tokens.refreshToken);
        }
    };

    return (
        <ReactKeycloakProvider
            authClient={keycloak}
            initOptions={{ onLoad: 'login-required' }}
            onTokens={handleTokens}
        >
            {children}
        </ReactKeycloakProvider>
    );
};

export default KeycloakProviderWrapper;
