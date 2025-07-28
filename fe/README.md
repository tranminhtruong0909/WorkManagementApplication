# Work Management System

Hệ thống quản lý công việc với phân quyền người dùng và authentication.

## Tính năng

### Authentication & Authorization
- ✅ Đăng nhập/Đăng ký tài khoản
- ✅ Đăng xuất
- ✅ Cập nhật thông tin cá nhân
- ✅ Đổi mật khẩu
- ✅ Xóa tài khoản
- ✅ Phân quyền theo role (ADMIN, MANAGER, MEMBER, VIEWER)

### Role-based Access Control
- **ADMIN & MANAGER**: Có toàn quyền tạo, sửa, xóa boards, groups, tasks
- **MEMBER & VIEWER**: Chỉ có quyền xem boards, groups, tasks

### Work Management
- ✅ Quản lý Boards
- ✅ Quản lý Groups
- ✅ Quản lý Tasks với status tracking
- ✅ Timeline và due date cho tasks

## Cài đặt

```bash
npm install
```

## Chạy ứng dụng

```bash
npm run dev
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/register` - Đăng ký
- `GET /api/auth/me` - Lấy thông tin user hiện tại
- `PUT /api/auth/profile` - Cập nhật thông tin cá nhân
- `POST /api/auth/change-password` - Đổi mật khẩu
- `POST /api/auth/logout` - Đăng xuất
- `DELETE /api/auth/account` - Xóa tài khoản

### Work Management
- `GET /api/boards` - Lấy danh sách boards
- `POST /api/boards` - Tạo board mới
- `PUT /api/boards/:id` - Cập nhật board
- `DELETE /api/boards/:id` - Xóa board

- `GET /api/boards/:boardId/groups` - Lấy groups theo board
- `POST /api/groups` - Tạo group mới
- `PUT /api/groups/:id` - Cập nhật group
- `DELETE /api/groups/:id` - Xóa group

- `GET /api/groups/:groupId/tasks` - Lấy tasks theo group
- `POST /api/tasks` - Tạo task mới
- `PUT /api/tasks/:id` - Cập nhật task
- `DELETE /api/tasks/:id` - Xóa task

## Cấu trúc dự án

```
src/
├── components/
│   ├── AccessDenied.jsx
│   └── UserRoleInfo.jsx
├── context/
│   └── AuthContext.jsx
├── pages/
│   ├── HomePage.jsx
│   ├── Login.jsx
│   ├── Profile.jsx
│   └── Register.jsx
├── services/
│   └── api.js
└── router/
    └── index.jsx
```

## Sử dụng

1. **Đăng ký tài khoản mới** tại `/register`
2. **Đăng nhập** tại `/login`
3. **Quản lý hồ sơ** tại `/profile`
4. **Quản lý công việc** tại `/home`

## Phân quyền

- **ADMIN**: Toàn quyền quản lý hệ thống
- **MANAGER**: Quyền tạo, sửa, xóa boards, groups, tasks
- **MEMBER**: Chỉ xem boards, groups, tasks
- **VIEWER**: Chỉ xem boards, groups, tasks

## Công nghệ sử dụng

- React 19
- React Router DOM
- Axios
- Tailwind CSS
- Context API cho state management
