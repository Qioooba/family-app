# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Family App is a WeChat mini-program (and H5) for family management featuring task management, anniversaries, weather queries, and family organization. The project has both backend (Java/Spring Boot) and frontend (UniApp/Vue 3) components.

## Common Commands

### Backend (Java/Spring Boot)

```bash
# Start backend service
cd backend/family-service
mvn spring-boot:run

# Build backend JAR
cd backend
mvn clean package -DskipTests

# Run with environment variables (requires .env file)
export WEIXIN_APPID=$(grep WEIXIN_APPID ../../.env | cut -d= -f2)
export WEIXIN_APPSECRET=$(grep WEIXIN_APPSECRET ../../.env | cut -d= -f2)
export MYSQL_PASSWORD=$(grep MYSQL_PASSWORD ../../.env | cut -d= -f2)
mvn spring-boot:run
```

### Frontend (UniApp/Vue 3)

```bash
# Install dependencies
cd frontend && npm install

# Development servers
npm run dev:h5      # H5 web version
npm run dev:mp-weixin  # WeChat mini-program

# Build for production
npm run build:h5        # Build H5
npm run build:mp-weixin # Build WeChat mini-program
```

### Database

```bash
# SQL scripts are in backend/sql/ directory
# Database initialization: database/ folder contains schema files
```

## Architecture

### Backend Structure

```
backend/
├── family-common/common-core/     # Shared utilities
├── family-service/                # Main Spring Boot application
│   └── src/main/java/com/family/
│       ├── family/                # Family management module
│       ├── user/                  # User authentication module
│       ├── task/                  # Task management module
│       ├── calendar/              # Anniversary/calendar module
│       └── weather/               # Weather API module
└── pom.xml                        # Maven configuration
```

### Frontend Structure

```
frontend/
├── src/
│   ├── api/                       # API service definitions
│   ├── components/                # Reusable Vue components
│   ├── pages/                     # Page components (by feature)
│   │   ├── task/                  # Task-related pages
│   │   ├── family/               # Family pages
│   │   ├── anniversary/          # Calendar pages
│   │   └── weather/              # Weather pages
│   ├── stores/                   # Pinia state management
│   └── utils/                    # Utility functions
└── package.json
```

### API Design

- **Base URL**: `/api/{module}/{resource}`
- **Authentication**: Sa-Token (header: `satoken: {token}`)
- **Response Format**: JSON `{ code, message, data }`
- **Pagination**: `pageNum`, `pageSize` parameters
- **API Docs**: Available at `http://localhost:8080/swagger-ui.html`

### Key Modules

| Module | Path | Description |
|--------|------|-------------|
| User | `/api/user` | Login, registration, profile |
| Family | `/api/family` | Family CRUD, members, invite codes |
| Task | `/api/task` | Tasks, subtasks, scheduling |
| Calendar | `/api/calendar/anniversary` | Anniversaries, lunar calendar |
| Weather | `/api/weather` | Weather forecasts |

## Important Configuration

### Environment Variables

Create `.env` file from `.env.example`:
- `WEIXIN_APPID` / `WEIXIN_APPSECRET` - WeChat mini-program credentials
- `TENCENT_MAP_KEY` - Tencent Maps API key
- `MYSQL_PASSWORD` - Database password
- `SSL_KEY_STORE_PASSWORD` - SSL certificate password

### Tech Stack

- **Backend**: Java 17, Spring Boot 3.1.5, MyBatis Plus, MySQL 8.0, Redis 7.0, Sa-Token
- **Frontend**: Vue 3.4, TypeScript 5.4, UniApp 3.0, Pinia, uView Plus, ECharts

## Commit Convention

Use Conventional Commits format:
- `feat:` new feature
- `fix:` bug fix
- `docs:` documentation
- `style:` formatting
- `refactor:` code restructuring
- `perf:` performance
- `test:` testing
- `chore:` build/tooling changes
