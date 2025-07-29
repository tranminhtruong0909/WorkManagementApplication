# 🔐 Test API Login với Thông tin Phân quyền

## 📋 **API Login mới:**

### **POST /api/auth/login**
**Request:**
```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Response mới:**
```json
{
  "user": {
    "id": 1,
    "email": "admin@example.com",
    "name": "Admin User",
    "avatarUrl": null
  },
  "token": "eyJhbGciOiJIUzI1NiJ9...",
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
  "totalBoards": 1,
  "message": "Đăng nhập thành công! Bạn có 1 board."
}
```

## 🔍 **Console Output:**

Khi login, bạn sẽ thấy trong console:

```
🔐 USER PERMISSIONS:
User ID: 1
User Email: admin@example.com
Is System Admin: true
Total Boards: 1
📋 Board Permissions:
  - Board: Test Board (ID: 1) | Role: ADMIN | Can Manage: true | Can View: true
🔐 END PERMISSIONS
```

## 🧪 **Test Cases:**

### **1. Test với Admin:**
```bash
POST /api/auth/login
{
  "email": "admin@example.com",
  "password": "password123"
}
```
**Expected:**
- `isSystemAdmin: true`
- Có permissions trên các board
- Console hiển thị chi tiết quyền

### **2. Test với User mới:**
```bash
POST /api/auth/login
{
  "email": "newuser@example.com",
  "password": "password123"
}
```
**Expected:**
- `isSystemAdmin: false`
- `totalBoards: 0` (ban đầu)
- Console hiển thị "User chưa có quyền trên board nào!"

### **3. Test với User có quyền trên nhiều board:**
```bash
POST /api/auth/login
{
  "email": "manager@example.com",
  "password": "password123"
}
```
**Expected:**
- `totalBoards: > 0`
- Console hiển thị danh sách tất cả board và quyền

## 📊 **Các trường hợp đặc biệt:**

### **User chưa có quyền:**
```json
{
  "user": {...},
  "token": "...",
  "isSystemAdmin": false,
  "permissions": [],
  "totalBoards": 0,
  "message": "Đăng nhập thành công! Bạn có 0 board."
}
```

### **User là System Admin:**
```json
{
  "user": {...},
  "token": "...",
  "isSystemAdmin": true,
  "permissions": [...],
  "totalBoards": 3,
  "message": "Đăng nhập thành công! Bạn có 3 board."
}
```

## 🔧 **Cách test với Postman:**

1. **Tạo request mới:**
   - Method: `POST`
   - URL: `{{baseUrl}}/api/auth/login`
   - Headers: `Content-Type: application/json`

2. **Body:**
   ```json
   {
     "email": "admin@example.com",
     "password": "password123"
   }
   ```

3. **Test script:**
   ```javascript
   pm.test("Status code is 200", function () {
       pm.response.to.have.status(200);
   });

   pm.test("Response has permission info", function () {
       const response = pm.response.json();
       pm.expect(response).to.have.property('isSystemAdmin');
       pm.expect(response).to.have.property('permissions');
       pm.expect(response).to.have.property('totalBoards');
       pm.expect(response).to.have.property('message');
   });

   pm.test("Token is present", function () {
       const response = pm.response.json();
       pm.expect(response).to.have.property('token');
       pm.expect(response.token).to.not.be.empty;
   });
   ```

## 🚨 **Lưu ý:**

1. **Console output** sẽ hiển thị chi tiết quyền của user
2. **Response** bao gồm thông tin phân quyền đầy đủ
3. **Message** cho biết số board user có quyền
4. **isSystemAdmin** cho biết user có phải admin hệ thống không

## 📝 **Ví dụ thực tế:**

### **Login Admin:**
```
🔐 USER PERMISSIONS:
User ID: 1
User Email: admin@example.com
Is System Admin: true
Total Boards: 2
📋 Board Permissions:
  - Board: Project A (ID: 1) | Role: ADMIN | Can Manage: true | Can View: true
  - Board: Project B (ID: 2) | Role: MANAGER | Can Manage: true | Can View: true
🔐 END PERMISSIONS
```

### **Login User mới:**
```
🔐 USER PERMISSIONS:
User ID: 3
User Email: newuser@example.com
Is System Admin: false
Total Boards: 0
⚠️  User chưa có quyền trên board nào!
🔐 END PERMISSIONS
``` 