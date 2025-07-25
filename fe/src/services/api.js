import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // Replace with your backend URL
});

export const login = (credentials) => api.post('/auth/login', credentials);
export const register = (userData) => api.post('/auth/register', userData);
export const getBoards = () => api.get('/boards');
export const getBoard = (id) => api.get(`/boards/${id}`);
export const createBoard = (board) => api.post('/boards', board);
export const updateBoard = (id, board) => api.put(`/boards/${id}`, board);
export const deleteBoard = (id) => api.delete(`/boards/${id}`);
export const getGroups = (boardId) => api.get(`/boards/${boardId}/groups`);
export const createGroup = (group) => api.post('/groups', group);
export const updateGroup = (id, group) => api.put(`/groups/${id}`, group);
export const deleteGroup = (id) => api.delete(`/groups/${id}`);
export const getTasks = (groupId) => api.get(`/groups/${groupId}/tasks`);
export const createTask = (task) => api.post('/tasks', task);
export const updateTask = (id, task) => api.put(`/tasks/${id}`, task);
export const deleteTask = (id) => api.delete(`/tasks/${id}`);
export const getTask = (id) => api.get(`/tasks/${id}`);
export const getUser = (id) => api.get(`/users/${id}`);