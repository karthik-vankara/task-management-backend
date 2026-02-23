# OAuth2 Authentication API Specification

## Overview
Implement complete Google OAuth2 login flow with JWT token generation, user creation/updates, and secure token management for the Task Management System backend.

## Objectives
- [ ] User JPA entity with Google OAuth fields
- [ ] Spring Security OAuth2 configuration
- [ ] JWT token generation and validation
- [ ] Auth endpoints implementation
- [ ] Error handling and security headers
- [ ] Comprehensive endpoint testing

## Functional Requirements
- Redirect to Google OAuth login
- Handle OAuth callback with authorization code
- Create new user or update existing
- Generate JWT access token (24-hour expiration)
- Secure logout mechanism
- Current user profile retrieval
- Invalid token rejection with 401

## Technical Architecture

### OAuth2 Flow
1. Frontend calls `POST /api/auth/login` (empty body)
2. Backend generates random state and stores in session
3. Backend returns Google OAuth URL to frontend
4. Frontend redirects browser to Google URL
5. User authenticates with Google
6. Google redirects to `http://localhost:8080/api/auth/callback?code=...&state=...`
7. Backend validates state, exchanges code for token
8. Backend calls Google UserInfo endpoint
9. Backend creates/updates User in database
10. Backend generates JWT and returns to frontend

### JWT Strategy
- Algorithm: HS256 (HMAC with SHA-256)
- Expiration: 24 hours
- Payload: userId, email, iat, exp
- Secret: Environment variable (min 256 bits)

### Spring Security Configuration
```java
// Use Spring Boot auto-configuration for OAuth2
// Custom SecurityFilterChain for resource server (JWT validation)
// CORS configuration for frontend domain
```

## Data Model / Schema

### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String googleId;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    private String avatarUrl;
    private String bio;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### JWT Payload
```json
{
  "sub": "123",
  "email": "user@example.com",
  "iat": 1708782600,
  "exp": 1708869000
}
```

## API Endpoints & Contracts

### 1. POST /api/auth/login
**Purpose:** Initiate Google OAuth login flow

**Request:**
```http
POST /api/auth/login
Content-Type: application/json

// Empty body
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "redirectUrl": "https://accounts.google.com/o/oauth2/v2/auth?..."
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

### 2. GET /api/auth/callback
**Purpose:** Handle Google OAuth callback

**Request:**
```http
GET /api/auth/callback?code=...&state=...
```

**Response (200):** Redirect to frontend with token
```
Location: http://localhost:3000/dashboard?token=eyJhbGc...
```

**Error (400):**
```json
{
  "success": false,
  "error": {
    "code": "OAUTH_CALLBACK_ERROR",
    "message": "Failed to exchange authorization code"
  }
}
```

### 3. POST /api/auth/logout
**Purpose:** Logout current user

**Request:**
```http
POST /api/auth/logout
Authorization: Bearer eyJhbGc...
```

**Response (204):** No content

### 4. GET /api/auth/profile
**Purpose:** Get current authenticated user profile

**Request:**
```http
GET /api/auth/profile
Authorization: Bearer eyJhbGc...
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "avatarUrl": "https://...",
    "bio": "",
    "createdAt": "2026-02-24T10:30:00Z"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Error (401):**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "JWT token is invalid or expired"
  }
}
```

## Validation Rules
- OAuth state must match between request and callback
- Google authorization code must not be expired
- JWT token signature must be valid
- JWT token must not be expired

## Security Considerations

### Must Implement
- CORS: Only allow frontend domain
- Security Headers:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `X-XSS-Protection: 1; mode=block`
- HTTPS only in production
- No sensitive data in JWT payload
- No sensitive data in logs

### OAuth2 Config
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            redirect-uri: http://localhost:8080/api/auth/callback
            scope: openid,email,profile
        provider:
          google:
            token-uri: https://www.googleapis.com/oauth2/v4/token
            user-info-uri: https://www.googleapis.com/oauth2/v1/userinfo
            user-name-attribute: email
```

## Error Codes
- `INVALID_TOKEN` - JWT validation failed
- `EXPIRED_TOKEN` - JWT token expired
- `OAUTH_ERROR` - Google OAuth failure
- `USER_CREATION_FAILED` - Error creating user
- `INVALID_STATE` - CSRF state mismatch

## Performance Requirements
- OAuth callback processed <500ms
- JWT validation <100ms
- User lookup from database <50ms

## Dependencies
- spring-security-oauth2-client
- spring-security-oauth2-resource-server
- jjwt (JWT library)
- jackson (JSON processing)

## Test Plan
- [ ] OAuth flow end-to-end manual test
- [ ] JWT token generation and validation
- [ ] Invalid token rejection
- [ ] User creation on first login
- [ ] User update on subsequent login
- [ ] CORS headers present

## Acceptance Criteria
- [ ] Google OAuth login works end-to-end
- [ ] JWT tokens generated and validated correctly
- [ ] Profile endpoint returns correct user
- [ ] Logout endpoint works
- [ ] Invalid tokens rejected with 401
- [ ] Security headers configured

## Notes
- Use Spring Boot's built-in OAuth2 support
- JWT library: io.jsonwebtoken:jjwt
- Store JWT secret in environment variable
