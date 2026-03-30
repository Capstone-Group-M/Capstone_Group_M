# Frontend – NOTAM Analyzer

This is the React frontend for the NOTAM Analyzer project. It was initialized using React and Vite.

## Requirements
Node.js (LTS recommended)

Check that Node and npm are installed:

node -v  
npm -v

## Setup

Navigate to the frontend directory from the project root:

cd frontend

Install dependencies:

npm install

Copy the environment variable template and fill in your Firebase project values:

cp .env.local.example .env.local

Ask a team member with Firebase console access for the actual values, or find them in the Firebase project settings under "Your apps".

## Run the Development Server

Start the React development server:

npm run dev

The application will run locally at:

http://localhost:5173

## Notes

The frontend uses Vite for fast development builds and will serve as the foundation for upcoming features such as the airport input form, NOTAM display components, and Firebase integration.