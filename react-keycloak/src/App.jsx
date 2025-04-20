
import './App.css'
import {useKeycloak} from "@react-keycloak/web";
import UserList from "./components/UserList.jsx";

function App() {
    const { keycloak, initialized } = useKeycloak();

    if (!initialized) return <div className="loading-text">Initializing Authentication...</div>;

    if (!keycloak.authenticated) {
        return (
            <div className="auth-container">
                <button className="auth-button" onClick={() => keycloak.login()}>
                    Login with Keycloak
                </button>
            </div>
        );
    }
    return (
        <div className="app-container">
            <div className="header-section">
                <h1 className="welcome-title">
                    Welcome, {keycloak.tokenParsed?.preferred_username}
                </h1>
                <button className="auth-button logout-button" onClick={() => keycloak.logout()}>
                    Logout
                </button>
            </div>
            <UserList />
        </div>
    );
}
export default App
