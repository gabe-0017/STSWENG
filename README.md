# SeniorChain PH — Frontend Prototype

A responsive React + Vite prototype for the SeniorChain PH academic project.

## Requirements
- Node.js 18 or newer
- npm

## Run locally
1. Extract this project folder.
2. Open a terminal in the `seniorchain-ph` folder.
3. Install dependencies:

   ```bash
   npm install
   ```

4. Start the development server:

   ```bash
   npm run dev
   ```

5. Open the local URL printed in the terminal (usually `http://localhost:5173`).

## Current prototype features
- Landing page and project overview
- Role selection and demo login
- Senior citizen registration form (local demo interaction only)
- Senior citizen dashboard and sample digital ID
- LGU / OSCA application review dashboard
- Establishment dashboard with simulated ID verification and discount records
- Government oversight dashboard
- Responsive desktop and mobile layouts

## Important limitations
- This is a frontend-only prototype. It does not have a backend, database, real authentication, real QR scanning, or blockchain integration yet.
- All displayed people, IDs, counts, and transactions are fictional sample data.
- Do not enter real personal information.
- The sample ID is not a valid government-issued identification document.

## Suggested next development stages
1. Add an Express backend and MySQL database.
2. Implement secure authentication and role-based authorization.
3. Add application submission and LGU review APIs.
4. Generate and scan QR codes using a registry lookup endpoint.
5. Add a smart contract on a local/test blockchain for non-sensitive verification records.
6. Add automated tests and GitHub Actions CI.
