# Render deploy: H2 by default

On Render, the app uses **H2 in-memory** by default so it **always deploys** (no PostgreSQL connection at startup).

- **Do not set** `USE_POSTGRES` in your Web Service environment (or remove it if it exists).
- The app will start and bind to PORT. Data is stored in memory and **is lost on each deploy/restart**.

## To use PostgreSQL on Render later

1. In Render → Web Service → Environment, set **USE_POSTGRES=true**.
2. Set your PostgreSQL connection (e.g. link the database, or set **EXTERNAL_DATABASE_URL** or **DB_URL** + **DB_USER** + **DB_PASSWORD**).
3. Ensure your PostgreSQL service is **Available** (not Paused) and in the same region as the Web Service if using the internal URL.
4. Redeploy. If the connection still fails (e.g. EOFException), the app will not start; remove **USE_POSTGRES** again to fall back to H2 and get a working deploy.
