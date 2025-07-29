# 🔍 GIẢI THÍCH VỀ PHÂN QUYỀN VÀ USER MỚI ĐĂNG KÝ

## 🤔 **VẤN ĐỀ: Tại sao user mới đăng ký có thể tạo board?**

### **📋 Phân tích logic hiện tại:**

#### **1. Khi user đăng ký (AuthService.register):**
```java
public User register(RegisterRequest request) {
    // ... validation ...
    
    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setAvatarUrl(request.getAvatarUrl());
    user.setRole("USER"); // ⚠️ Chỉ set role "USER" trong User entity
    
    return userRepository.save(user);
}
```

#### **2. Khi user tạo board (BoardController.createBoard):**
```java
@PostMapping
public ResponseEntity<BoardResponse> createBoard(@RequestBody Board board,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
    // ✅ Chỉ kiểm tra authentication, KHÔNG kiểm tra permission
    if (userDetails == null) {
        return ResponseEntity.status(401).build();
    }
    
    User user = userDetails.getUser();
    Board created = boardService.createBoard(board);
    
    // ✅ Tự động gán quyền ADMIN cho user tạo board
    AssignRoleRequest assignRequest = new AssignRoleRequest();
    assignRequest.setUserId(user.getId());
    assignRequest.setRole("ADMIN");
    boardService.assignUserRole(user, created.getId(), assignRequest);
    
    return ResponseEntity.ok(new BoardResponse(created));
}
```

### **🔍 VẤN ĐỀ CHÍNH:**

1. **Không có kiểm tra permission khi tạo board**
   - API chỉ kiểm tra user đã đăng nhập chưa
   - Không kiểm tra user có quyền tạo board không

2. **Logic "Ai tạo board thì được làm ADMIN"**
   - Bất kỳ user nào tạo board → tự động được gán quyền ADMIN
   - Điều này có thể gây ra vấn đề bảo mật

3. **Role "USER" trong User entity không ảnh hưởng đến board permissions**
   - Role trong User entity chỉ là thông tin
   - Quyền thực sự được quản lý qua UserRole entity

---

## 🛠️ **GIẢI PHÁP ĐỀ XUẤT:**

### **1. Thêm kiểm tra permission khi tạo board:**

```java
@PostMapping
public ResponseEntity<BoardResponse> createBoard(@RequestBody Board board,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
        return ResponseEntity.status(401).build();
    }
    
    User user = userDetails.getUser();
    
    // 🔒 Thêm kiểm tra: Chỉ ADMIN hệ thống mới được tạo board
    if (!permissionService.isAdmin(user.getId())) {
        return ResponseEntity.status(403).body("Chỉ ADMIN mới có quyền tạo board");
    }
    
    Board created = boardService.createBoard(board);
    
    // Tự động gán quyền ADMIN cho user tạo board
    AssignRoleRequest assignRequest = new AssignRoleRequest();
    assignRequest.setUserId(user.getId());
    assignRequest.setRole("ADMIN");
    boardService.assignUserRole(user, created.getId(), assignRequest);
    
    return ResponseEntity.ok(new BoardResponse(created));
}
```

### **2. Hoặc tạo role mặc định cho user mới:**

```java
public User register(RegisterRequest request) {
    // ... validation ...
    
    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setAvatarUrl(request.getAvatarUrl());
    user.setRole("MEMBER"); // Set role mặc định là MEMBER
    
    return userRepository.save(user);
}
```

---

## 🔍 **API MỚI ĐỂ XEM PHÂN QUYỀN:**

### **1. Xem phân quyền của user hiện tại:**
```
GET /api/auth/my-permissions
```
**Response:**
```json
{
  "userId": 1,
  "userEmail": "user@example.com",
  "userName": "Test User",
  "isSystemAdmin": true,
  "permissions": [
    {
      "boardId": 1,
      "boardName": "Test Board",
      "role": "ADMIN",
      "canManageBoard": true,
      "canViewBoard": true
    }
  ],
  "totalBoards": 1
}
```

### **2. Xem tất cả user và phân quyền (chỉ ADMIN):**
```
GET /api/auth/all-users-permissions
```
**Response:**
```json
{
  "totalUsers": 3,
  "users": [
    {
      "userId": 1,
      "userEmail": "admin@example.com",
      "userName": "Admin User",
      "isSystemAdmin": true,
      "permissions": [...],
      "totalBoards": 2
    }
  ]
}
```

### **3. Xem phân quyền của user cụ thể (chỉ ADMIN):**
```
GET /api/auth/user-permissions/{userId}
```

---

## 📊 **BẢNG PHÂN QUYỀN HIỆN TẠI:**

| Chức năng | User mới đăng ký | ADMIN | MANAGER | MEMBER | VIEWER |
|-----------|------------------|-------|---------|--------|--------|
| **Tạo board** | ✅ (Vấn đề!) | ✅ | ✅ | ❌ | ❌ |
| **Xem board** | ❌ | ✅ | ✅ | ✅ | ✅ |
| **Sửa board** | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Xóa board** | ❌ | ✅ | ✅ | ❌ | ❌ |

---

## 🚨 **KHUYẾN NGHỊ:**

### **1. Ngắn hạn:**
- Thêm kiểm tra permission khi tạo board
- Chỉ cho phép ADMIN tạo board
- Hoặc tạo role mặc định cho user mới

### **2. Dài hạn:**
- Implement hệ thống role hierarchy
- Thêm audit log cho các thao tác quan trọng
- Implement approval workflow cho việc tạo board

### **3. Test cases cần thêm:**
- Test user không có quyền tạo board
- Test user không có quyền truy cập board
- Test phân quyền cross-board

---

## 🔧 **CÁCH TEST:**

### **1. Test user mới đăng ký:**
```bash
# 1. Đăng ký user mới
POST /api/auth/register
{
  "name": "New User",
  "email": "newuser@example.com",
  "password": "password123"
}

# 2. Đăng nhập
POST /api/auth/login
{
  "email": "newuser@example.com",
  "password": "password123"
}

# 3. Xem phân quyền
GET /api/auth/my-permissions

# 4. Thử tạo board (sẽ thành công hiện tại)
POST /api/boards
{
  "name": "Test Board"
}
```

### **2. Test với ADMIN:**
```bash
# 1. Xem tất cả user
GET /api/auth/all-users-permissions

# 2. Xem user cụ thể
GET /api/auth/user-permissions/1
``` 