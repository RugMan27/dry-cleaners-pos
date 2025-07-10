import React, { useState } from 'react';
import styles from './Login.module.css';
import { FaUser, FaLock } from "react-icons/fa";


const Login = () => {

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const res = await fetch('http://localhost:8080/api/employees/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (!res.ok) {
                throw new Error('Login failed');
            }

            const data = await res.json();

            localStorage.setItem('jwt_token', data.data.token);
            localStorage.setItem('jwt_expiry', data.data.expires_at);
            localStorage.setItem("eid", data.data.employee_id);

            window.dispatchEvent(new Event("auth-changed"));


        } catch (err) {
            setError('Invalid username or password');
            console.error(err);
        }
    };

    return (
        <div className={styles.pageWrapper}>
            <div className={styles.wrapper}>
                <form onSubmit={handleSubmit}>
                    <h1>Login</h1>
                    <div className={styles['input-box']}>
                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                        <FaUser className={styles.icon} />
                    </div>
                    <div className={styles['input-box']}>
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                        <FaLock className={styles.icon} />
                    </div>
                    <div className={styles.error}>{error}</div>
                    <div className={styles['forgot-password']}>
                        <a href="#">Forgot Password</a>
                    </div>
                    <button type="submit">Login</button>
                </form>
            </div>
        </div>
    );
};

export default Login;
