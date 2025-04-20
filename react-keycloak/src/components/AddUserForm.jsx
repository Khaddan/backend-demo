import React, { useState } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import './style.css'

const AddUserForm = ({ onUserAdded }) => {
    const { keycloak } = useKeycloak();
    const [username, serUsername] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();

        const newUser = {username, firstName, lastName, email, password };

        try {
            const res = await fetch('http://localhost:8999/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${keycloak.token}`,
                },
                body: JSON.stringify(newUser),
            });

            if (!res.ok) {
                const errorText = await res.text();
                throw new Error(errorText || 'Failed to add user');
            }

            alert('User added successfully');
            serUsername('')
            setFirstName('');
            setLastName('');
            setEmail('');
            setPassword('');
            onUserAdded();
        } catch (err) {
            console.error('Error adding user:', err);
            alert('Error adding user: ' + err.message);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="add-user-form">
            <h3>Add New User</h3>
            <div>
                <label>User Name:&nbsp;</label>
                <input
                    type="text"
                    value={username}
                    onChange={(e) => serUsername(e.target.value)}
                    required
                />
            </div>
            <div>
                <label>First Name:&nbsp;</label>
                <input
                    type="text"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    required
                />
            </div>
            <div>
                <label>Last Name:&nbsp;</label>
                <input
                    type="text"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    required
                />
            </div>
            <div>
                <label>Email:&nbsp;</label>
                <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />
            </div>
            <div>
                <label>Password:&nbsp;</label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
            </div>
            <button type="submit">Add User</button>
        </form>
    );
};

export default AddUserForm;
