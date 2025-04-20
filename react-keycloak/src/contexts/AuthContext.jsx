/*
import { createContext, useEffect, useState } from 'react';
import keycloak from '../services/keycloak';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [isAdmin, setIsAdmin] = useState(false);

    useEffect(() => {
        const init = async () => {
            try {
                const authenticated = await keycloak.init({
                    onLoad: 'login-required',
                    pkceMethod: 'S256'
                });

                if (authenticated) {
                    const userProfile = await keycloak.loadUserInfo();
                    setUser({
                        ...userProfile,
                        roles: keycloak.tokenParsed?.realm_access?.roles || []
                    });
                    setIsAdmin(keycloak.hasRealmRole('admin'));
                }
            } catch (error) {
                console.error('Authentication failed:', error);
            }
        };

        init();
    }, []);

    const value = {
        user,
        isAdmin,
        keycloak,
        login: () => keycloak.login(),
        logout: () => keycloak.logout()
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};*/
