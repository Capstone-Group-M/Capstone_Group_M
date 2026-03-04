import { initializeApp } from "firebase/app";

const firebaseConfig = {
  apiKey: "AIzaSyBTtUe5s4UIbvib63wmP8Qdj7bJJBOEkRY",
  authDomain: "notam-analyzer.firebaseapp.com",
  projectId: "notam-analyzer",
  storageBucket: "notam-analyzer.firebasestorage.app",
  messagingSenderId: "520849181355",
  appId: "1:520849181355:web:ed76b35364674b941f45ee",
  measurementId: "G-ZRYT763YSE"
};

const app = initializeApp(firebaseConfig);


console.log("Firebase initialized");

export default app;
