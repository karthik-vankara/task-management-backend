# Task CRUD API Specification

## Overview
Implement core task management operations: create, read, update, delete with pagination, filtering, and user authorization.

## Objectives
- [ ] Task entity and JPA repository
- [ ] CRUD endpoint implementation
- [ ] Pagination and sorting
- [ ] Status-based filtering
- [ ] User authorization checks
- [ ] Input validation

## Functional Requirements
- Create new tasks with validation
- List tasks with pagination (10 per page default)
- Retrieve single task detail
- Update task fields
- Delete tasks
- Filter by status (OPEN, IN_PROGRESS, COMPLETED)
- Sort by creation date, priority, due date

## Data Model

### Task Entity
```java
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### TaskStatus Enum
```java
public enum TaskStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED
}
```

### Request/Response DTOs
```java
@Data
public class CreateTaskRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100)
    private String title;
    
    @Size(max = 1000)
    private String description;
}

@Data
public class UpdateTaskRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100)
    private String title;
    
    @Size(max = 1000)
    private String description;
    
    private TaskStatus status;
}

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## API Endpoints

### 1. POST /api/tasks
**Purpose:** Create a new task

**Request:**
```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Implement login feature",
  "description": "Add Google OAuth2 authentication"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Implement login feature",
    "description": "Add Google OAuth2 authentication",
    "status": "OPEN",
    "userId": 123,
    "createdAt": "2026-02-24T10:30:00Z",
    "updatedAt": "2026-02-24T10:30:00Z"
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

### 2. GET /api/tasks
**Purpose:** List user's tasks with pagination

**Request:**
```http
GET /api/tasks?page=0&size=10&status=OPEN&sort=createdAt,desc
Authorization: Bearer <token>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Task 1",
        "status": "OPEN",
        "createdAt": "2026-02-24T10:30:00Z"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 10,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": "2026-02-24T10:30:00Z"
}
```

### 3. GET /api/tasks/{id}
**Purpose:** Get task detail

**Request:**
```http
GET /api/tasks/1
Authorization: Bearer <token>
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Implement login feature",
    "description": "Full OAuth2 setup",
    "status": "IN_PROGRESS",
    "userId": 123,
    "createdAt": "2026-02-24T10:30:00Z",
    "updatedAt": "2026-02-24T11:00:00Z"
  }
}
```

### 4. PUT /api/tasks/{id}
**Purpose:** Update task

**Request:**
```http
PUT /api/tasks/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Implement OAuth2 login",
  "status": "IN_PROGRESS"
}
```

**Response (200):** Updated task

### 5. DELETE /api/tasks/{id}
**Purpose:** Delete task

**Request:**
```http
DELETE /api/tasks/1
Authorization: Bearer <token>
```

**Response (204):** No content

## Validation Rules
- Title: Required, 1-100 characters
- Description: Optional, max 1000 characters
- Status: Valid enum value

## Security & Authorization
- User can only CRUD own tasks
- Return 403 Forbidden if user tries to modify another's task
- Return 401 Unauthorized if token invalid

## Pagination Details
- Default page: 0 (first page)
- Default size: 10 items
- Maximum size: 100 items
- Support sorting by: createdAt, updatedAt, status

## Performance Requirements
- List query <300ms (with 1000+ tasks)
- Create/update <200ms
- Delete <100ms
- Database indexes on userId, status

## Repository Implementation

```java
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByUserId(Long userId, Pageable pageable);
    Page<Task> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);
}
```

## Test Plan
- [ ] Create task with valid data
- [ ] List tasks with pagination
- [ ] Get task detail
- [ ] Update task with valid data
- [ ] Delete task
- [ ] Reject create with invalid title
- [ ] Prevent user from modifying other's tasks (403)
- [ ] Return 404 for non-existent task
- [ ] Filter by status

## Acceptance Criteria
- [ ] All CRUD operations work correctly
- [ ] Pagination works with configurable page size
- [ ] Filtering by status works
- [ ] Authorization enforced
- [ ] Input validation enforced
- [ ] Response format matches spec

## Notes
- Use Spring Data Specifications for complex queries
- Consider caching frequently accessed tasks (future)
- Implement soft delete if needed (future)
