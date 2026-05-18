import React, {useEffect, useState} from "react";
import {RSAUtils} from "@/utils/rsa-utils.ts";
import {RSA_PRIVATE_KEY_STORAGE_KEY, RSA_PUBLIC_KEY_STORAGE_KEY} from "@/utils/global-constants.ts";

export function ProtectedApp(props: { children: React.ReactNode }) {
    const [isKeyPairReady, setIsKeyPairReady] = useState(false);

    // Initialize RSA key pair on app start
    useEffect(() => {
        const initializeRSAKeys = async () => {
            try {
                // Check if private key already exists in sessionStorage
                const privateKey = sessionStorage.getItem(RSA_PRIVATE_KEY_STORAGE_KEY);
                const publicKey = sessionStorage.getItem(RSA_PUBLIC_KEY_STORAGE_KEY);

                if (!privateKey || !publicKey) {
                    console.log('No RSA key pair found, generating new one...');

                    // Generate new key pair
                    const keyPair = await RSAUtils.generateKeyPair(2048);

                    // Store in sessionStorage (cleared when tab/browser closes)
                    sessionStorage.setItem(RSA_PRIVATE_KEY_STORAGE_KEY, keyPair.privateKey);
                    sessionStorage.setItem(RSA_PUBLIC_KEY_STORAGE_KEY, keyPair.publicKey);

                    console.log('RSA key pair generated and stored in sessionStorage');
                } else {
                    console.log('Existing RSA key pair found in sessionStorage');
                }

                setIsKeyPairReady(true);
            } catch (error) {
                console.error('Failed to initialize RSA key pair:', error);
                // Still set to true to avoid blocking the app
                setIsKeyPairReady(true);
            }
        };

        void initializeRSAKeys();
    }, []);

    if (!isKeyPairReady) {
        return (
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '100vh'
            }}>
                Initializing security...
            </div>
        );
    }


    return (
        <>
            {props.children}
        </>
    );
}