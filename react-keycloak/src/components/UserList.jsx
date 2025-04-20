import React, { useEffect, useState } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import AddUserForm from './AddUserForm';
import './style.css'

const UserList = () => {
    const { keycloak } = useKeycloak();
    const [users, setUsers] = useState([]);

    const fetchUsers = async () => {
        try {
            const res = await fetch('http://localhost:8999/auth/all', {
                headers: { Authorization: `Bearer ${keycloak.token}` },
            });
            if (!res.ok) {
                throw new Error('Failed to fetch users');
            }
            const data = await res.json();
            setUsers(data);
        } catch (err) {
            console.error('Error fetching users:', err);
        }
    };

    useEffect(() => {
        if (keycloak.authenticated) {
            fetchUsers();
        }
    }, [keycloak.authenticated]);

    return (
        <div className="user-list-container">
            <h2>User List</h2>
            {users && users.length > 0 ? (
                <ul className="user-list">
                    {users.map((user) => (
                        <li key={user.id}>
                            <div>
                                <strong>{user.name}</strong>
                                <span style={{margin: "0 10px"}}>-</span>
                                <span>{user.email}</span>
                            </div>
                            <small>ID: {user.id}</small>
                        </li>
                    ))}
                </ul>
            ) : (
                <p className="no-users">No users found.</p>
            )}
            <AddUserForm onUserAdded={fetchUsers} />
        </div>
    );
};

export default UserList;
