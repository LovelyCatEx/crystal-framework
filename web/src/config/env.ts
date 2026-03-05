export const API_CONFIG = {
    development: {
        baseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    },
    production: {
        baseUrl: import.meta.env.VITE_API_BASE_URL || 'https://your-api-domain.com'
    }
};

const env = import.meta.env.MODE || 'development';
export const currentEnvironment = API_CONFIG[env as keyof typeof API_CONFIG];