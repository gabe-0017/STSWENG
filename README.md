# CivicCareID PH — Frontend Prototype

A responsive React + Vite prototype for the CivicCareID PH academic project.

## Requirements
- Node.js 24
- npm

## Run locally
1. Extract this project folder.
2. Open a terminal in the `civiccareid-ph` folder.
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
- The **login, registration, and LGU review** screens connect to the CivicCareID PH backend (Spring Boot API + Supabase Postgres) with real authentication and role-based access.
- The **QR verification, discounts, tax-relief claims, and government dashboard** screens are still simulated. They show sample data and do not read from the backend yet.
- There is no blockchain integration yet. Registry records are stored in the database only, with a record queued for a future ledger.
- This is an academic project running on a staging environment. All displayed people, IDs, counts, and transactions are fictional sample data.
- Do not enter real personal information or real government ID numbers.
- The sample ID is not a valid government-issued identification document.
- The backend is hosted on a free tier, so the first request after a period of inactivity may take up to a minute while the server wakes up.

## Suggested next development stages
1. Add a registry lookup endpoint and QR generation/scanning for establishment verification (disclosing only age eligibility and registry status).
2. Add discount recording and tax-relief claim APIs for establishments.
3. Build the government dashboard from real registry and claim data.
4. Support PWD records (record type and disability classification) alongside Senior Citizen records.
5. Add a smart contract on a local/test blockchain for non-sensitive verification records.
6. Add login rate limiting, automated tests, and GitHub Actions CI.
