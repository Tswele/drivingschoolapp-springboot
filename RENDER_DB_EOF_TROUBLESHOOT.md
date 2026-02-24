# If you see "connection attempt failed" + EOFException on Render

The app is using **EXTERNAL_DATABASE_URL** and the host resolves, but the connection fails during authentication with `java.io.EOFException`. Try the following.

## 1. Check PostgreSQL is running (not paused)

- In **Render Dashboard** → your **PostgreSQL** service.
- Status should be **Available** (green). If it says **Paused** or **Suspended**, click **Resume** and wait until it shows Available.
- Then trigger a new deploy of your Web Service (or wait for it to retry).

## 2. Confirm EXTERNAL_DATABASE_URL is correct

- Open your PostgreSQL service → **Connect** → **External**.
- Copy the **full External Database URL** again.
- In your **Web Service** → **Environment** → set **EXTERNAL_DATABASE_URL** to that exact value (no extra spaces, same password).
- If the password has special characters (`@`, `#`, `%`, `:`), it must be **URL-encoded** in the string (e.g. `@` → `%40`). Render usually provides the URL already encoded.

## 3. Redeploy after DB is available

- After resuming the database or fixing the URL, use **Manual Deploy** → **Deploy latest commit** so the app starts when the DB is ready.

## 4. Optional: use Internal URL from the same region

If your Web Service and PostgreSQL are in the **same region** and the DB is **Available**, you can try the **Internal** URL instead of External (faster, no SSL over internet):

- Remove or leave **EXTERNAL_DATABASE_URL** unset.
- Ensure **DATABASE_URL** is set (e.g. by linking the database to the Web Service).
- Redeploy. Internal hostnames like `dpg-xxx-a` only resolve inside Render’s network when both services are in the same region.
