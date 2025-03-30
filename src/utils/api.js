/**
 * Utility functions for API requests with authentication
 */

const API_URL = process.env.REACT_APP_API_URL;

/**
 * Get the authentication token from localStorage
 * @returns {string|null} The authentication token or null if not found
 */
export const getAuthToken = () => {
  return localStorage.getItem('token');
};

/**
 * Check if the given URL is an authentication endpoint that doesn't need a token
 * @param {string} url - The URL to check
 * @returns {boolean} True if the URL is an auth endpoint
 */
const isAuthEndpoint = (url) => {
  return url.includes('/auth/') || 
         url.includes('/reset/') || 
         url.includes('/forgot-password');
};

/**
 * Fetch with authentication token added to headers for non-auth endpoints
 * @param {string} url - The URL to fetch
 * @param {Object} options - Fetch options
 * @returns {Promise} The fetch promise
 */
export const fetchWithAuth = async (url, options = {}) => {
  // Don't add auth headers to auth endpoints
  if (isAuthEndpoint(url)) {
    return fetch(url, options);
  }

  const token = getAuthToken();
  
  // Prepare headers with auth token
  const headers = {
    ...options.headers || {},
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Return fetch with auth headers
  return fetch(url, {
    ...options,
    headers,
  });
};