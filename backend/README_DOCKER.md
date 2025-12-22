# Docker Deployment Guide

## Build Docker Image Locally

```bash
cd backend
docker build -t driving-school-api .
```

## Run Docker Container Locally

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/drivingschool \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  -e FRONTEND_URL=http://localhost:4200 \
  driving-school-api
```

## Using Docker Compose (with MySQL)

```bash
cd backend
docker-compose up -d
```

This will:
- Build the Spring Boot app
- Start MySQL database
- Connect them together
- App available at http://localhost:8080

## Test Locally Before Deploying

1. **Build:**
   ```bash
   docker build -t driving-school-api .
   ```

2. **Run:**
   ```bash
   docker run -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=render \
     -e DATABASE_URL=postgresql://user:pass@host:5432/db \
     -e FRONTEND_URL=https://your-netlify-app.netlify.app \
     driving-school-api
   ```

3. **Test:**
   - Open http://localhost:8080/api/schools
   - Should return JSON data

## Deploy to Render

See `RENDER_DEPLOY.md` for detailed Render deployment instructions.

## Environment Variables for Render

Set these in Render dashboard:
- `SPRING_PROFILES_ACTIVE=render` (or `prod`)
- `PORT=8080` (Render sets this automatically)
- `FRONTEND_URL=https://your-netlify-app.netlify.app`
- `DATABASE_URL` (if using Render PostgreSQL, set automatically)
- Or `DB_URL`, `DB_USER`, `DB_PASSWORD` for MySQL

