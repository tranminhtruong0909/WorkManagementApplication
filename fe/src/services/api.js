import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_URL,
});

// Auth
export const login = async (email, password) => {
  return await api.post('/auth/login', { email, password });
};

export const register = async (data) => {
  return await api.post('/auth/register', data);
};

// Board
export const getBoards = async () => api.get('/boards');
export const createBoard = async (data) => api.post('/boards', data);
export const updateBoard = async (id, data) => api.put(`/boards/${id}`, data);
export const deleteBoard = async (id) => api.delete(`/boards/${id}`);

// Group
export const getGroupsByBoard = async (boardId) => api.get(`/boards/${boardId}/groups`);
export const createGroup = async (data) => api.post('/groups', data);
export const updateGroup = async (id, data) => api.put(`/groups/${id}`, data);
export const deleteGroup = async (id) => api.delete(`/groups/${id}`);

// Task
export const getTasksByGroup = async (groupId) => api.get(`/groups/${groupId}/tasks`);
export const createTask = async (data) => api.post('/tasks', data);
export const updateTask = async (id, data) => api.put(`/tasks/${id}`, data);
export const deleteTask = async (id) => api.delete(`/tasks/${id}`);