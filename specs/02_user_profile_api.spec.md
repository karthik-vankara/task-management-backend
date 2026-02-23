# User Profile API Specification

## Overview
Implement user profile viewing and editing endpoints with validation and authorization checks.

## Objectives
- [ ] Profile retrieval endpoints
- [ ] Profile update endpoint with validation
- [ ] User authorization checks
- [ ] Input sanitization
- [ ] Comprehensive error handling

## Functional Requirements
- Get user profile by ID
- Get current authenticated user profile
- Update own profile (name, bio, avatar)
- Prevent unauthorized profile modifications
- Validate input data
- Return consistent response format

## Data Model

### User Profile Response DTO
```java
@Data
public class UserProfileDTO {
    private Long id;
    private String email;
    private String name;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### User Profile Update Request DTO
```java
@Data
public class UserProfileUpdateRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif)$", 
             message = "Avatar URL must be a valid image URL")
    private String avatarUrl;
}
```

## API Endpoints

### 1. GET /api/users/{id}
**Purpose:** Get public user profile by ID

**Request:**
```http
GET /api/users/123
Authorization: Bearer <token>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "email": "user@example.com",
    "name": "John Doe",
    "avatarUrl": "https://...",
    "bio": "Software developer",
    "createdAt": "2026-02-24T10:30:00Z",
    "updatedAt": "2026-02-24T10:30:00Z"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

**Error (404):**
```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User with ID 123 not found"
  }
}
```

### 2. GET /api/users/me
**Purpose:** Get current authenticated user profile

**Request:**
```http
GET /api/users/me
Authorization: Bearer <token>
```

**Response (200):** Same as above

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

### 3. PUT /api/users/{id}
**Purpose:** Update user profile (only own profile)

**Request:**
```http
PUT /api/users/123
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Jane Doe",
  "bio": "Product manager",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "email": "user@example.com",
    "name": "Jane Doe",
    "avatarUrl": "https://example.com/avatar.jpg",
    "bio": "Product manager",
    "updatedAt": "2026-02-24T11:00:00Z"
  },
  "timestamp": "2026-02-24T11:00:00Z"
}
```

**Error (400) - Validation Error:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "name": "Name is required"
    }
  }
}
```

**Error (403) - Unauthorized:**
```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "You can only edit your own profile"
  }
}
```

## Validation Rules
- Name: Required, 1-100 characters
- Bio: Optional, max 500 characters
- AvatarUrl: Optional, must be valid HTTPS image URL

## Security Considerations
- User can only update own profile
- Email field read-only
- No SQL injection via parameterized queries
- Input validation on all fields
- XSS prevention via JSON escaping

## Performance Requirements
- Get request <200ms
- Update request <300ms
- Database indexes on id and email

## Service Implementation

```java
@Service
public class UserService {
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToDTO(user);
    }
    
    public UserProfileDTO updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setAvatarUrl(request.getAvatarUrl());
        
        User updated = userRepository.save(user);
        return mapToDTO(updated);
    }
}
```

## Repository Methods
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
}
```

## Test Plan
- [ ] Get user profile returns correct data
- [ ] Get own profile with /me endpoint
- [ ] Update profile with valid data
- [ ] Reject update with invalid name length
- [ ] Reject update from different user (403)
- [ ] Reject update without auth (401)
- [ ] Get non-existent user (404)

## Acceptance Criteria
- [ ] All profile endpoints return correct data
- [ ] Validation rules enforced
- [ ] User can only edit own profile
- [ ] Response format matches spec
- [ ] Error codes consistent with API standards

## Notes
- Email field should not be editable (even via direct SQL)
- Use DTOs to avoid exposing unnecessary entities
- Always use @Transactional for update operations
