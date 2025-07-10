const API_BASE = 'http://localhost:8080';

/**
 * Checks if the locally stored token is present and valid.
 * Returns the token if valid, or null if expired/invalid.
 */
export async function validateLocalToken() {
    const token = localStorage.getItem('jwt_token');
    const expiry = Number(localStorage.getItem('jwt_expiry') || 0);

    const now = Date.now();

    if (!token || now >= expiry) {
        console.warn('❌ Token missing or expired.');
        //clearToken();
        return null;
    }

    // Ping the backend to make sure the token is still valid
    try {
        const res = await fetch(`${API_BASE}/api/employees/me`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (res.ok) {
            console.log('✅ Token is valid on server.');
            return token;
        } else {
            console.warn('⚠️ Token rejected by server.');
            //clearToken();
            return null;
        }
    } catch (err) {
        console.error('❌ Token validation failed:', err);
        //clearToken();
        return null;
    }
}
export async function logout() {
    const token = localStorage.getItem('jwt_token');


    try {
        await fetch('http://localhost:8080/api/employees/logout', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,   // if backend requires auth header for logout
                'Content-Type': 'application/json',
            }
        });
    } catch (error) {
        console.error('Logout failed:', error);
    }

    // Clear local token anyway
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('jwt_expiry');

    window.dispatchEvent(new Event("auth-changed"));


}


export function clearToken() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('jwt_expiry');
}
