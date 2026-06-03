export interface CrystalFrameworkEnv {
    baseUrl: string;
    map: {
        baidu: {
            ak: string;
        }
    }
}

export const API_CONFIG: { [K in "development" | "production"]: CrystalFrameworkEnv } = {
    development: {
        baseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
        map: {
            baidu: {
                ak: import.meta.env.VITE_BAIDU_MAP_AK || '',
            }
        }
    },
    production: {
        baseUrl: import.meta.env.VITE_API_BASE_URL || 'https://your-api-domain.com',
        map: {
            baidu: {
                ak: import.meta.env.VITE_BAIDU_MAP_AK || '',
            }
        }
    }
};

const env = import.meta.env.MODE || 'development';
export const currentEnvironment = API_CONFIG[env as keyof typeof API_CONFIG];