// src/utils/apiClient.js

const BASE_URL = 'http://localhost:8080/api'; // Ensure this matches your Spring Boot API base URL

function getAuthHeaders() {
    const token = localStorage.getItem('jwt_token');
    if (token) {
        return {
            'Authorization': `Bearer ${token}`,
        };
    }
    return {};
}

async function request(endpoint, method, data = null) {
    const url = `${BASE_URL}${endpoint}`;
    const headers = {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
    };

    const config = {
        method: method,
        headers: headers,
    };

    if (data) {
        config.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, config);

        // First, check if the response was successful (2xx status)
        if (!response.ok) {
            let errorMessage = `HTTP error! Status: ${response.status}`;
            try {
                const errorBody = await response.json();
                // Check if the error response also has a 'message' field in its wrapper
                if (errorBody && errorBody.message) {
                    errorMessage = errorBody.message;
                } else if (errorBody) {
                    errorMessage = JSON.stringify(errorBody); // Fallback to stringify if no specific message field
                }
            } catch (jsonError) {
                console.warn("Could not parse error response as JSON:", jsonError);
            }
            throw new Error(errorMessage);
        }

        // Handle 204 No Content explicitly
        if (response.status === 204) {
            return null;
        }

        // Parse the full JSON response
        const fullResponse = await response.json();

        // *** MODIFICATION START ***
        // Check if the response has a 'data' field and return it
        if (fullResponse && 'data' in fullResponse) {
            return fullResponse.data;
        }
        // *** MODIFICATION END ***

        // If there's no 'data' field, return the full response as is
        // This handles cases where the API might return a simple object directly
        // or a different wrapper structure for specific endpoints.
        return fullResponse;

    } catch (error) {
        console.error(`API Request Error [${method} ${url}]:`, error);
        throw error; // Re-throw for handling in the calling component
    }
}
export async function requestRaw(endpoint, method, data = null, responseType = 'blob') {
    const url = `${BASE_URL}${endpoint}`;
    const headers = {
        ...getAuthHeaders(),
    };

    // Only set Content-Type if sending JSON data
    if (data) {
        headers['Content-Type'] = 'application/json';
    }

    const config = {
        method,
        headers,
    };

    if (data) {
        config.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, config);

        if (!response.ok) {
            let errorMessage = `HTTP error! Status: ${response.status}`;
            try {
                const errorBody = await response.json();
                if (errorBody && errorBody.message) {
                    errorMessage = errorBody.message;
                } else if (errorBody) {
                    errorMessage = JSON.stringify(errorBody);
                }
            } catch (_) {
                // Ignore JSON parse errors here for error body
            }
            throw new Error(errorMessage);
        }

        if (response.status === 204) {
            return null;
        }

        // Return response as requested type
        if (responseType === 'blob') {
            return await response.blob();
        } else if (responseType === 'arraybuffer') {
            return await response.arrayBuffer();
        } else if (responseType === 'text') {
            return await response.text();
        } else {
            throw new Error(`Unsupported responseType: ${responseType}`);
        }

    } catch (error) {
        console.error(`API Raw Request Error [${method} ${url}]:`, error);
        throw error;
    }
}

export const apiClient = {
    get: (endpoint) => request(endpoint, 'GET'),
    post: (endpoint, data) => request(endpoint, 'POST', data),
    put: (endpoint, data) => request(endpoint, 'PUT', data),
    delete: (endpoint) => request(endpoint, 'DELETE'),
};
export const rawApiClient = {
    get: (endpoint) => requestRaw(endpoint, 'GET'),
    post: (endpoint, data) => requestRaw(endpoint, 'POST', data),
    put: (endpoint, data) => requestRaw(endpoint, 'PUT', data),
    delete: (endpoint) => requestRaw(endpoint, 'DELETE'),
};
