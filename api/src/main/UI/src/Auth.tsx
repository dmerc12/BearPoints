import { browserPopupRedirectResolver, browserSessionPersistence, GoogleAuthProvider, initializeAuth, signInWithPopup } from 'firebase/auth';
import { initializeApp } from 'firebase/app';

// Firebase configuration
const firebaseConfig = {
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
    storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
    appId: import.meta.env.VITE_FIREBASE_APP_ID
};

// Initialize Firebase
export const app = initializeApp(firebaseConfig);
export const auth = initializeAuth(app, {
    persistence: browserSessionPersistence,
    popupRedirectResolver: browserPopupRedirectResolver
});

export const login = async (): Promise<void> => {
    try {
        const googleProvider = new GoogleAuthProvider();
        googleProvider.setCustomParameters({ hd: 'okcps.org' });
        await signInWithPopup(auth, googleProvider);
    } catch (error) {
        console.error('Login error:', error);
    }
};
