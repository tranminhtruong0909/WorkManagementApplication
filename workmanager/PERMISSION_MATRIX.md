# 🔐 BẢNG PHÂN QUYỀN HỆ THỐNG WORKMANAGER

## 📊 TỔNG QUAN HỆ THỐNG PHÂN QUYỀN

### 🎭 **Các Role trong hệ thống:**
1. **ADMIN** - Quản trị viên (Toàn quyền)
2. **MANAGER** - Quản lý (Quyền quản lý)
3. **MEMBER** - Thành viên (Quyền tham gia)
4. **VIEWER** - Người xem (Chỉ xem)

---

## 📋 **BẢNG PHÂN QUYỀN CHI TIẾT**

### 🔐 **AUTHENTICATION & USER MANAGEMENT**

| Chức năng | ADMIN | MANAGER | MEMBER | VIEWER | Không có role |
|-----------|-------|---------|--------|--------|---------------|
| **Đăng nhập** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Đăng ký** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Xem thông tin cá nhân** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Xem thông tin user khác** | ✅ | ❌ | ❌ | ❌ | ❌ |

---

### 📋 **BOARD MANAGEMENT**

| Chức năng | ADMIN | MANAGER | MEMBER | VIEWER | Không có role |
|-----------|-------|---------|--------|--------|---------------|
| **Xem tất cả boards** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Xem board cụ thể** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Tạo board mới** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Cập nhật board** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Xóa board** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Phân quyền user trên board** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Xem role của user khác** | ✅ | ✅ | ❌ | ❌ | ❌ |

---

### 📁 **GROUP MANAGEMENT**

| Chức năng | ADMIN | MANAGER | MEMBER | VIEWER | Không có role |
|-----------|-------|---------|--------|--------|---------------|
| **Xem tất cả groups** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Xem group cụ thể** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Tạo group mới** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Cập nhật group** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Xóa group** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Xem tasks trong group** | ✅ | ✅ | ✅ | ✅ | ❌ |

---

### ✅ **TASK MANAGEMENT**

| Chức năng | ADMIN | MANAGER | MEMBER | VIEWER | Không có role |
|-----------|-------|---------|--------|--------|---------------|
| **Xem tất cả tasks** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Xem task cụ thể** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Tạo task mới** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Cập nhật task (ADMIN/MANAGER)** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Cập nhật task (MEMBER - task được gán)** | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Xóa task** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Gán task cho user** | ✅ | ✅ | ❌ | ❌ | ❌ |

---

### 🔑 **ROLE MANAGEMENT**

| Chức năng | ADMIN | MANAGER | MEMBER | VIEWER | Không có role |
|-----------|-------|---------|--------|--------|---------------|
| **Xem role của user khác** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Xem tất cả user trong board** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Xem boards của user khác** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Gán role cho user** | ✅ | ✅ | ❌ | ❌ | ❌ |

---

## 🔍 **CHI TIẾT LOGIC PHÂN QUYỀN**

### **1. canViewBoard(userId, boardId)**
```java
// Có bất kỳ vai trò nào trên board đều xem được
Role role = getRoleOnBoard(userId, boardId);
return role != null;
```

### **2. canManageBoard(userId, boardId)**
```java
// Chỉ ADMIN và MANAGER mới quản lý được
Role role = getRoleOnBoard(userId, boardId);
return role == Role.ADMIN || role == Role.MANAGER;
```

### **3. canEditTask(userId, boardId, task)**
```java
// ADMIN/MANAGER: sửa được tất cả task
// MEMBER: chỉ sửa được task được gán cho mình
Role role = getRoleOnBoard(userId, boardId);
if (role == Role.ADMIN || role == Role.MANAGER) return true;
if (role == Role.MEMBER && task.getAssignee() != null && 
    task.getAssignee().getId().equals(userId)) return true;
return false;
```

---

## 🚨 **CÁC TRƯỜNG HỢP ĐẶC BIỆT**

### **1. User tạo board**
- Tự động được gán quyền **ADMIN** trên board đó
- Có toàn quyền quản lý board

### **2. Task Assignment**
- **ADMIN/MANAGER**: Có thể gán task cho bất kỳ ai
- **MEMBER**: Chỉ có thể sửa task được gán cho mình
- **VIEWER**: Chỉ xem, không sửa được

### **3. Cross-board Permissions**
- User có thể có role khác nhau trên các board khác nhau
- Quyền được tính theo từng board riêng biệt

---

## 📝 **VÍ DỤ THỰC TẾ**

### **Scenario 1: User A (ADMIN) trên Board X**
- ✅ Tạo/sửa/xóa board
- ✅ Tạo/sửa/xóa groups
- ✅ Tạo/sửa/xóa tasks
- ✅ Phân quyền cho user khác
- ✅ Xem tất cả thông tin

### **Scenario 2: User B (MANAGER) trên Board X**
- ✅ Tạo/sửa/xóa board
- ✅ Tạo/sửa/xóa groups  
- ✅ Tạo/sửa/xóa tasks
- ✅ Phân quyền cho user khác
- ✅ Xem tất cả thông tin

### **Scenario 3: User C (MEMBER) trên Board X**
- ❌ Không thể tạo/sửa/xóa board
- ❌ Không thể tạo/sửa/xóa groups
- ✅ Chỉ sửa được task được gán cho mình
- ❌ Không thể phân quyền
- ✅ Xem được board và tasks

### **Scenario 4: User D (VIEWER) trên Board X**
- ❌ Không thể tạo/sửa/xóa board
- ❌ Không thể tạo/sửa/xóa groups
- ❌ Không thể tạo/sửa/xóa tasks
- ❌ Không thể phân quyền
- ✅ Chỉ xem được board và tasks

### **Scenario 5: User E (Không có role)**
- ❌ Không thể truy cập board
- ❌ Không thể xem groups/tasks
- ✅ Vẫn có thể đăng nhập/đăng ký

---

## 🔧 **IMPLEMENTATION NOTES**

### **Permission Checks trong Controllers:**

```java
// Board Controller
if (!permissionService.canViewBoard(user.getId(), id)) {
    return ResponseEntity.status(403).build();
}

if (!permissionService.canManageBoard(user.getId(), id)) {
    return ResponseEntity.status(403).build();
}

// Task Controller  
if (!permissionService.canEditTask(user.getId(), boardId, task)) {
    return ResponseEntity.status(403).body("Bạn không có quyền sửa task này");
}

// Group Controller
if (!permissionService.canManageBoard(user.getId(), boardId)) {
    return ResponseEntity.status(403).body("Bạn không có quyền tạo group trên board này");
}
```

### **Filtering trong Services:**
```java
// Chỉ trả về data mà user có quyền xem
var filtered = allTasks.stream()
    .filter(task -> permissionService.canViewBoard(user.getId(), task.getGroup().getBoard().getId()))
    .map(TaskResponse::new)
    .toList();
```

---

## 📊 **SUMMARY**

| Role | Quyền quản lý | Quyền tham gia | Quyền xem | Mô tả |
|------|---------------|----------------|-----------|-------|
| **ADMIN** | ✅ Toàn bộ | ✅ Toàn bộ | ✅ Toàn bộ | Quản trị viên |
| **MANAGER** | ✅ Toàn bộ | ✅ Toàn bộ | ✅ Toàn bộ | Quản lý |
| **MEMBER** | ❌ | ✅ Giới hạn | ✅ Toàn bộ | Thành viên |
| **VIEWER** | ❌ | ❌ | ✅ Toàn bộ | Người xem |
| **No Role** | ❌ | ❌ | ❌ | Không có quyền | 