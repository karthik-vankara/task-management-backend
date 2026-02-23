# System Architecture Specification

## Overview
Define the overall system design, data models, and technology decisions for the Task Management System backend.

## Objectives
- [ ] Document system overview and data flow
- [ ] Define user workflows and architecture
- [ ] Justify Java/Spring Boot technology stack
- [ ] Outline REST API structure
- [ ] Design database schema
- [ ] Establish security architecture
- [ ] Define deployment architecture

## Functional Requirements
- User authentication via Google OAuth2 with JWT
- Task CRUD operations with ownership
- User profile management
- Advanced task features (search, filters, priorities)
- Collaboration and notifications
- Dashboard analytics and statistics
- Comprehensive security hardening

## Technical Architecture

### Layered Architecture
```
Controller Layer (Spring REST Controllers)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access via JPA)
    ↓
PostgreSQL Database
```

### Key Components
- **Spring Security:** OAuth2 Configuration + JWT token handling
- **JPA/Hibernate:** ORM for data persistence
- **Spring Data:** Repository pattern for queries
- **Jackson:** JSON serialization/deserialization
- **Validation:** JSR-303 Bean Validation

## Data Model / Schema

### User Entity
```
id (Long, PK)
googleId (String, UNIQUE)
email (String, UNIQUE, NOT NULL)
name (String)
avatarUrl (String)
bio (String)
createdAt (LocalDateTime)
updatedAt (LocalDateTime)
```

### Task Entity
```
id (Long, PK)
title (String, NOT NULL, max 100)
description (String, max 1000)
status (TaskStatus ENUM: OPEN, IN_PROGRESS, COMPLETED)
priority (TaskPriority ENUM: LOW, MEDIUM, HIGH, URGENT)
category (String, max 50)
userId (Long, FK to User)
assignedTo (Long, FK to User, nullable)
createdAt (LocalDateTime)
updatedAt (LocalDateTime)
```

### TaskActivity Entity
```
id (Long, PK)
taskId (Long, FK to Task)
userId (Long, FK to User)
action (String: CREATED, UPDATED, COMPLETED, ASSIGNED)
createdAt (LocalDateTime)
```

### Notification Entity
```
id (Long, PK)
userId (Long, FK to User)
message (String)
read (boolean, default false)
createdAt (LocalDateTime)
```

## API Endpoints / Components

### Authentication Endpoints
- `POST /api/auth/login` - Initiate Google OAuth
- `GET /api/auth/callback` - Handle OAuth callback
- `POST /api/auth/logout` - Logout user
- `GET /api/auth/profile` - Get current user profile

### User Endpoints
- `GET /api/users/{id}` - Get user profile
- `GET /api/users/me` - Get current user
- `PUT /api/users/{id}` - Update profile

### Task Endpoints
- `POST /api/tasks` - Create task
- `GET /api/tasks` - List tasks (paginated)
- `GET /api/tasks/{id}` - Get task detail
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task
- `GET /api/tasks/search` - Search tasks
- `GET /api/tasks/{id}/activity` - Get task activity log

### Notification Endpoints
- `GET /api/notifications` - Get user notifications
- `PUT /api/notifications/{id}` - Mark notification as read
- `DELETE /api/notifications/{id}` - Delete notification

### Analytics Endpoints
- `GET /api/analytics/tasks-summary` - Task count summary
- `GET /api/analytics/tasks-by-status` - Tasks grouped by status
- `GET /api/analytics/tasks-by-priority` - Tasks grouped by priority

## User Flows / Sequences

### Authentication Flow
1. User clicks "Sign in with Google"
2. Frontend calls `POST /api/auth/login`
3. Backend redirects to Google OAuth
4. User authenticates with Google
5. Google redirects to `GET /api/auth/callback?code=...`
6. Backend exchanges code for Google tokens
7. Backend creates/updates User in database
8. Backend generates JWT token
9. JWT token returned to frontend
10. Frontend stores token in localStorage
11. Frontend redirects to dashboard

### Task CRUD Flow
1. User creates task form
2. Frontend validates input
3. Frontend posts to `POST /api/tasks`
4. Backend validates and creates Task
5. Backend logs TaskActivity
6. Backend returns created Task with 201
7. Frontend updates local task list
8. Frontend shows success notification

## Error Handling Strategy

### HTTP Status Codes
- `200 OK` - Successful GET/PUT/PATCH
- `201 Created` - Successful POST creating resource
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Invalid/missing JWT token
- `403 Forbidden` - User lacks permission
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unhandled exception

### Error Response Format
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Task title is required",
    "details": {
      "field": "title"
    }
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

## Validation Rules

### Task Validation
- Title: Required, 1-100 characters
- Description: Optional, max 1000 characters
- Status: Must be valid enum value
- Priority: Must be valid enum value
- AssignedTo: If provided, user must exist

### User Validation
- Email: Required, valid email format
- Name: 1-100 characters
- Bio: Optional, max 500 characters

## Security Architecture

### Authentication Layer
- JWT tokens for API authentication
- 24-hour token expiration
- Refresh token strategy (TBD in phase)

### Authorization Layer
- User can only access/modify own tasks
- User can only read own profile
- Admin endpoints (TBD future phase)

### Data Security
- All inputs validated on backend
- SQL injection prevention via parameterized queries
- No sensitive data in logs
- Environment variables for secrets

### Transport Security
- HTTPS only (enforced in production)
- CORS configured for frontend domain
- Security headers: X-Content-Type-Options, X-Frame-Options

## Performance Requirements
- API response time: <500ms (p95)
- Support 1000+ concurrent users
- Database queries optimized with indexes
- Connection pooling for database
- Response compression (gzip)

## Dependencies
- Spring Boot 3.3.0
- Spring Security 6
- Spring Data JPA
- PostgreSQL Driver
- JWT Library (jjwt)
- Lombok
- Jackson

## Test Plan
- [ ] Unit tests for services
- [ ] Integration tests for ORM layer
- [ ] API endpoint tests
- [ ] Security tests (auth, authorization)
- [ ] Load tests for API endpoints

## Acceptance Criteria
- [ ] All data models defined in spec
- [ ] API endpoints designed according to spec
- [ ] Error handling strategy documented
- [ ] Security architecture approved
- [ ] Tech stack justified in rationale

## Notes
This specification serves as the foundation for all backend development. All subsequent phases reference this architecture.
