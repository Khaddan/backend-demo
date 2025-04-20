/*
import React from 'react';
import { useKeycloak } from '@react-keycloak/web';

const Home = () => {
    const { keycloak } = useKeycloak();
    const handleLogout = () => {
        localStorage.removeItem('kc_token');
        localStorage.removeItem('kc_refreshToken');
        keycloak.logout({ redirectUri: window.location.origin });
    };
    return (
        <div>
            <h1>Bienvenue {keycloak.tokenParsed?.preferred_username}</h1>
            <button onClick={handleLogout}>Déconnexion</button>
        </div>
    );
};

export default Home;
*/
