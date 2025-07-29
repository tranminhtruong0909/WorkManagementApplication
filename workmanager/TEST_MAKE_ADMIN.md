# 🔧 Test Gán Quyền ADMIN cho User

## 🎯 **Mục tiêu:**
Gán quyền ADMIN cho user ID 9 (admin@example.com) để có toàn quyền trên hệ thống.

## 📋 **Các API Test:**

### **1. Tạo Board và Gán Quyền ADMIN (Khuyến nghị)**

**API:** `POST /api/roles/test-create-board-and-admin`

**Request:**
```json
{
  "userId": 9,
  "boardName": "Admin Test Board"
}
```

**Response:**
```json
{
  "message": "Đã tạo board và gán quyền ADMIN thành công!",
  "userEmail": "admin@example.com",
  "boardName": "Admin Test Board",
  "boardId": 1,
  "role": "ADMIN",
  "userRole": {
    "id": 1,
    "user": {...},
    "board": {...},
    "role": "ADMIN"
  }
}
```

### **2. Gán Quyền ADMIN trên Board Có Sẵn**

**API:** `POST /api/roles/test-make-admin`

**Request:**
```json
{
  "userId": 9,
  "boardId": 1
}
```

**Response:**
```json
{
  "message": "Đã gán quyền ADMIN thành công!",
  "userEmail": "admin@example.com",
  "boardName": "Test Board",
  "role": "ADMIN",
  "userRole": {...}
}
```

## 🧪 **Cách Test với Postman:**

### **Bước 1: Login để lấy token**
```bash
POST /api/auth/login
{
  "email": "admin@example.com",
  "password": "password123"
}
```

### **Bước 2: Tạo Board và Gán Quyền ADMIN**
```bash
POST /api/roles/test-create-board-and-admin
Headers: 
  Authorization: Bearer {token}
  Content-Type: application/json

Body:
{
  "userId": 9,
  "boardName": "Admin Test Board"
}
```

### **Bước 3: Verify Quyền**
```bash
POST /api/auth/login
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Expected Response:**
```json
{
  "user": {...},
  "token": "...",
  "isSystemAdmin": true,
  "permissions": [
    {
      "boardId": 1,
      "boardName": "Admin Test Board",
      "role": "ADMIN",
      "canManageBoard": true,
      "canViewBoard": true
    }
  ],
  "totalBoards": 1,
  "message": "Đăng nhập thành công! Bạn có 1 board."
}
```

## 🔍 **Console Output:**

Khi gán quyền thành công, bạn sẽ thấy:
```
🔧 Creating new ADMIN role for user 9 on board 1
✅ Successfully assigned ADMIN role to user admin@example.com on board Admin Test Board
```

Khi login lại:
```
🔐 USER PERMISSIONS:
User ID: 9
User Email: admin@example.com
Is System Admin: true
Total Boards: 1
📋 Board Permissions:
  - Board: Admin Test Board (ID: 1) | Role: ADMIN | Can Manage: true | Can View: true
🔐 END PERMISSIONS
```

## 📊 **Test Cases:**

### **Case 1: User chưa có quyền**
1. Login → thấy `totalBoards: 0`
2. Gọi API tạo board và gán quyền
3. Login lại → thấy `totalBoards: 1`, `isSystemAdmin: true`

### **Case 2: User đã có quyền khác**
1. User có role MEMBER trên board
2. Gọi API gán quyền ADMIN
3. Role sẽ được cập nhật thành ADMIN

### **Case 3: Tạo nhiều board**
1. Gọi API tạo board nhiều lần
2. User sẽ có quyền ADMIN trên nhiều board
3. `totalBoards` sẽ tăng theo

## 🚨 **Lưu ý quan trọng:**

1. **API này chỉ để test** - không có kiểm tra permission
2. **User ID 9** là user bạn muốn gán quyền
3. **Board sẽ được tạo tự động** nếu dùng API tạo board
4. **Sau khi gán quyền**, login lại để thấy thay đổi

## 🔧 **Troubleshooting:**

### **Lỗi "Không tìm thấy user":**
- Kiểm tra user ID có đúng không
- Đảm bảo user đã tồn tại trong database

### **Lỗi "Không tìm thấy board":**
- Dùng API tạo board thay vì gán quyền trên board có sẵn
- Hoặc kiểm tra board ID có đúng không

### **Quyền không thay đổi:**
- Login lại để refresh token
- Kiểm tra console output có thông báo thành công không

## 📝 **Ví dụ hoàn chỉnh:**

### **1. Login ban đầu:**
```json
{
  "isSystemAdmin": false,
  "permissions": [],
  "totalBoards": 0,
  "message": "Đăng nhập thành công! Bạn có 0 board."
}
```

### **2. Gán quyền ADMIN:**
```bash
POST /api/roles/test-create-board-and-admin
{
  "userId": 9,
  "boardName": "Admin Test Board"
}
```

### **3. Login lại:**
```json
{
  "isSystemAdmin": true,
  "permissions": [
    {
      "boardId": 1,
      "boardName": "Admin Test Board",
      "role": "ADMIN",
      "canManageBoard": true,
      "canViewBoard": true
    }
  ],
  "totalBoards": 1,
  "message": "Đăng nhập thành công! Bạn có 1 board."
}
``` 