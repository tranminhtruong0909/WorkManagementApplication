import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Toaster, toast } from 'react-hot-toast';
import {
  getBoards, createBoard, updateBoard, deleteBoard,
  getGroupsByBoard, createGroup, updateGroup, deleteGroup,
  getTasksByGroup, createTask, updateTask, deleteTask
} from '../services/api';

const STATUS_OPTIONS = [
  { value: 'Working on it', color: 'bg-orange-400', text: 'text-white' },
  { value: 'Done', color: 'bg-green-500', text: 'text-white' },
  { value: 'Todo', color: 'bg-gray-400', text: 'text-white' },
  { value: 'Expired', color: 'bg-red-500', text: 'text-white' },
];

const HomePage = () => {
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false); // Trạng thái sidebar cho mobile

  // Board state
  const [boards, setBoards] = useState([]);
  const [selectedBoard, setSelectedBoard] = useState(null);
  const [showBoardModal, setShowBoardModal] = useState(false);
  const [boardForm, setBoardForm] = useState({ name: '' });
  const [boardFormError, setBoardFormError] = useState('');
  const [editBoardId, setEditBoardId] = useState(null);
  const [loadingBoards, setLoadingBoards] = useState(false);

  // Group state
  const [groups, setGroups] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState(null);
  const [showGroupModal, setShowGroupModal] = useState(false);
  const [groupForm, setGroupForm] = useState({ name: '' });
  const [groupFormError, setGroupFormError] = useState('');
  const [editGroupId, setEditGroupId] = useState(null);
  const [loadingGroups, setLoadingGroups] = useState(false);

  // Task state
  const [tasks, setTasks] = useState([]);
  const [showTaskModal, setShowTaskModal] = useState(false);
  const [newTask, setNewTask] = useState({
    name: '',
    status: STATUS_OPTIONS[0].value,
    dueDate: '',
    timelineStart: '',
    timelineEnd: '',
    notes: '',
    groupId: null,
  });
  const [taskFormError, setTaskFormError] = useState('');
  const [editTaskId, setEditTaskId] = useState(null);
  const [editTask, setEditTask] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [loadingTasks, setLoadingTasks] = useState(false);

  // Load boards on mount
  useEffect(() => {
    setLoadingBoards(true);
    getBoards()
      .then(res => {
        setBoards(res.data);
        if (res.data.length > 0) setSelectedBoard(res.data[0]);
        setLoadingBoards(false);
      })
      .catch(() => toast.error('Lỗi tải danh sách board'));
  }, []);

  // Load groups when board changes
  useEffect(() => {
    if (selectedBoard) {
      setLoadingGroups(true);
      getGroupsByBoard(selectedBoard.id)
        .then(res => {
          setGroups(res.data);
          if (res.data.length > 0) setSelectedGroup(res.data[0]);
          else setSelectedGroup(null);
          setLoadingGroups(false);
        })
        .catch(() => toast.error('Lỗi tải danh sách group'));
    } else {
      setGroups([]);
      setSelectedGroup(null);
      setLoadingGroups(false);
    }
  }, [selectedBoard]);

  // Load tasks when group changes
  useEffect(() => {
    if (selectedGroup) {
      setLoadingTasks(true);
      getTasksByGroup(selectedGroup.id)
        .then(res => {
          setTasks(res.data);
          setLoadingTasks(false);
        })
        .catch(() => toast.error('Lỗi tải danh sách task'));
    } else {
      setTasks([]);
      setLoadingTasks(false);
    }
  }, [selectedGroup]);

  // Search filter
  const filteredTasks = tasks.filter(t =>
    t.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    t.status.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Board handlers
  const handleAddBoard = async (e) => {
    e.preventDefault();
    if (!boardForm.name) {
      setBoardFormError('Vui lòng nhập tên board');
      return;
    }
    try {
      const res = await createBoard({ name: boardForm.name });
      setBoards([...boards, res.data]);
      setBoardForm({ name: '' });
      setShowBoardModal(false);
      toast.success('Thêm board thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi thêm board');
    }
  };

  const handleEditBoard = (board) => {
    setEditBoardId(board.id);
    setBoardForm({ name: board.name });
    setShowBoardModal(true);
  };

  const handleUpdateBoard = async (e) => {
    e.preventDefault();
    if (!boardForm.name) {
      setBoardFormError('Vui lòng nhập tên board');
      return;
    }
    try {
      const res = await updateBoard(editBoardId, { name: boardForm.name });
      setBoards(boards.map(b => b.id === editBoardId ? res.data : b));
      setEditBoardId(null);
      setBoardForm({ name: '' });
      setShowBoardModal(false);
      toast.success('Cập nhật board thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi cập nhật board');
    }
  };

  const handleDeleteBoard = async (id) => {
    try {
      await deleteBoard(id);
      setBoards(boards.filter(b => b.id !== id));
      if (selectedBoard && selectedBoard.id === id) setSelectedBoard(boards[0] || null);
      toast.success('Xóa board thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi xóa board');
    }
  };

  // Group handlers
  const handleAddGroup = async (e) => {
    e.preventDefault();
    if (!groupForm.name || !selectedBoard) {
      setGroupFormError('Vui lòng nhập tên group');
      return;
    }
    try {
      const res = await createGroup({ name: groupForm.name, boardId: selectedBoard.id });
      setGroups([...groups, res.data]);
      setGroupForm({ name: '' });
      setShowGroupModal(false);
      toast.success('Thêm group thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi thêm group');
    }
  };

  const handleEditGroup = (group) => {
    setEditGroupId(group.id);
    setGroupForm({ name: group.name });
    setShowGroupModal(true);
  };

  const handleUpdateGroup = async (e) => {
    e.preventDefault();
    if (!groupForm.name) {
      setGroupFormError('Vui lòng nhập tên group');
      return;
    }
    try {
      const res = await updateGroup(editGroupId, { name: groupForm.name });
      setGroups(groups.map(g => g.id === editGroupId ? res.data : g));
      setEditGroupId(null);
      setGroupForm({ name: '' });
      setShowGroupModal(false);
      toast.success('Cập nhật group thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi cập nhật group');
    }
  };

  const handleDeleteGroup = async (id) => {
    try {
      await deleteGroup(id);
      setGroups(groups.filter(g => g.id !== id));
      if (selectedGroup && selectedGroup.id === id) setSelectedGroup(groups[0] || null);
      toast.success('Xóa group thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi xóa group');
    }
  };

  // Task handlers
  const handleAddTask = async (e) => {
    e.preventDefault();
    if (!newTask.name || !selectedGroup) {
      setTaskFormError('Vui lòng nhập tên task');
      return;
    }
    try {
      const res = await createTask({
        name: newTask.name,
        status: newTask.status,
        dueDate: newTask.dueDate,
        timelineStart: newTask.timelineStart,
        timelineEnd: newTask.timelineEnd,
        notes: newTask.notes,
        groupId: selectedGroup.id,
      });
      setTasks([...tasks, res.data]);
      setNewTask({
        name: '',
        status: STATUS_OPTIONS[0].value,
        dueDate: '',
        timelineStart: '',
        timelineEnd: '',
        notes: '',
        groupId: null,
      });
      setShowTaskModal(false);
      toast.success('Thêm task thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi thêm task');
    }
  };

  const handleEditTask = (task) => {
    setEditTaskId(task.id);
    setEditTask({ ...task });
    setShowTaskModal(true);
  };

  const handleUpdateTask = async (e) => {
    e.preventDefault();
    if (!editTask.name) {
      setTaskFormError('Vui lòng nhập tên task');
      return;
    }
    try {
      const res = await updateTask(editTaskId, editTask);
      setTasks(tasks.map(t => t.id === editTaskId ? res.data : t));
      setEditTaskId(null);
      setEditTask(null);
      setShowTaskModal(false);
      toast.success('Cập nhật task thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi cập nhật task');
    }
  };

  const handleDeleteTask = async (id) => {
    try {
      await deleteTask(id);
      setTasks(tasks.filter(t => t.id !== id));
      toast.success('Xóa task thành công');
    } catch (error) {
      toast.error(error?.response?.data || 'Lỗi xóa task');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-100 to-gray-200 flex relative">
      {/* Toaster for notifications */}
      <Toaster position="top-right" reverseOrder={false} />

      {/* Hamburger button for mobile */}
      <button
        className="md:hidden fixed top-4 left-4 z-50 p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition duration-200"
        onClick={() => setIsSidebarOpen(!isSidebarOpen)}
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      {/* Sidebar - Board */}
      <aside
        className={`fixed md:static top-0 left-0 h-full w-64 bg-white border-r shadow-lg transform ${
          isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
        } md:translate-x-0 transition-transform duration-300 ease-in-out z-40 flex flex-col`}
      >
        <div className="px-6 py-4 border-b bg-blue-50">
          <span className="text-xl font-bold text-blue-800">Work Management</span>
        </div>
        <div className="px-4 py-4 flex-1 overflow-y-auto">
          <div className="mb-6">
            <select
              className="w-full border rounded-lg p-3 bg-gray-50 text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-600 transition duration-200"
              value={selectedBoard?.id || ''}
              onChange={e => {
                const board = boards.find(b => b.id === Number(e.target.value));
                setSelectedBoard(board);
                setIsSidebarOpen(false); // Đóng sidebar khi chọn board trên mobile
              }}
            >
              <option value="">Chọn Board</option>
              {boards.map(b => (
                <option key={b.id} value={b.id}>{b.name}</option>
              ))}
            </select>
            <button
              className="w-full flex items-center gap-2 bg-blue-600 text-white rounded-lg px-3 py-2 mt-2 hover:bg-blue-700 transition duration-200"
              onClick={() => {
                setShowBoardModal(true);
                setEditBoardId(null);
                setBoardForm({ name: '' });
                setBoardFormError('');
              }}
            >
              <span className="text-lg font-bold">+</span> <span>Thêm Board</span>
            </button>
            <div className="mt-4">
              {loadingBoards ? (
                <div className="animate-pulse space-y-3">
                  <div className="h-8 bg-gray-200 rounded-lg"></div>
                  <div className="h-8 bg-gray-200 rounded-lg"></div>
                  <div className="h-8 bg-gray-200 rounded-lg"></div>
                </div>
              ) : (
                boards.map(b => (
                  <div
                    key={b.id}
                    className={`flex items-center justify-between px-3 py-2 rounded-lg ${
                      selectedBoard?.id === b.id ? 'bg-blue-100 text-blue-800' : 'hover:bg-blue-50'
                    } transition duration-200 cursor-pointer`}
                  >
                    <span onClick={() => { setSelectedBoard(b); setIsSidebarOpen(false); }}>{b.name}</span>
                    <div className="flex gap-2">
                      <button className="text-xs text-blue-600 hover:underline" onClick={() => handleEditBoard(b)}>Sửa</button>
                      <button className="text-xs text-red-500 hover:underline" onClick={() => handleDeleteBoard(b.id)}>Xoá</button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
          <div className="mt-6">
            <div className="flex items-center justify-between mb-3">
              <span className="font-semibold text-gray-700">Groups</span>
              <button
                className="text-blue-600 text-sm hover:underline"
                onClick={() => {
                  setShowGroupModal(true);
                  setEditGroupId(null);
                  setGroupForm({ name: '' });
                  setGroupFormError('');
                }}
              >
                + Thêm
              </button>
            </div>
            <div>
              {loadingGroups ? (
                <div className="animate-pulse space-y-3">
                  <div className="h-8 bg-gray-200 rounded-lg"></div>
                  <div className="h-8 bg-gray-200 rounded-lg"></div>
                </div>
              ) : (
                groups.map(g => (
                  <div
                    key={g.id}
                    className={`flex items-center justify-between px-3 py-2 rounded-lg ${
                      selectedGroup?.id === g.id ? 'bg-green-100 text-green-800' : 'hover:bg-green-50'
                    } transition duration-200 cursor-pointer`}
                  >
                    <span onClick={() => { setSelectedGroup(g); setIsSidebarOpen(false); }}>{g.name}</span>
                    <div className="flex gap-2">
                      <button className="text-xs text-blue-600 hover:underline" onClick={() => handleEditGroup(g)}>Sửa</button>
                      <button className="text-xs text-red-500 hover:underline" onClick={() => handleDeleteGroup(g.id)}>Xoá</button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </aside>

      {/* Main content - Task */}
      <main className="flex-1 p-4 md:p-8 overflow-auto">
        <div className="max-w-6xl mx-auto bg-white rounded-2xl shadow-lg p-6">
          <h2 className="text-2xl md:text-3xl font-bold mb-6 text-gray-800">
            {selectedGroup ? selectedGroup.name : 'Chọn group để xem task'}
          </h2>
          {/* Search bar */}
          <div className="mb-6">
            <input
              className="w-full border rounded-lg p-3 bg-gray-50 text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-600 transition duration-200"
              placeholder="Tìm kiếm task theo tên hoặc trạng thái..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
            />
          </div>
          {/* Task Table */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-4">
              <span className="font-semibold text-blue-600 text-lg">Tasks</span>
              <button
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition duration-200"
                onClick={() => {
                  setShowTaskModal(true);
                  setNewTask({
                    name: '',
                    status: STATUS_OPTIONS[0].value,
                    dueDate: '',
                    timelineStart: '',
                    timelineEnd: '',
                    notes: '',
                    groupId: selectedGroup?.id,
                  });
                  setTaskFormError('');
                }}
              >
                + Thêm task
              </button>
            </div>
            <div className="overflow-x-auto rounded-lg shadow-sm">
              <table className="min-w-full bg-white">
                <thead>
                  <tr className="bg-gray-100 text-gray-700 text-sm">
                    <th className="p-4 text-left font-medium">Task</th>
                    <th className="p-4 text-left font-medium">Trạng thái</th>
                    <th className="p-4 text-left font-medium">Hạn chót</th>
                    <th className="p-4 text-left font-medium">Timeline</th>
                    <th className="p-4 text-left font-medium">Ghi chú</th>
                    <th className="p-4 text-left font-medium">Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {loadingTasks ? (
                    <tr>
                      <td colSpan={6} className="p-4">
                        <div className="animate-pulse space-y-3">
                          <div className="h-8 bg-gray-200 rounded-lg"></div>
                          <div className="h-8 bg-gray-200 rounded-lg"></div>
                          <div className="h-8 bg-gray-200 rounded-lg"></div>
                        </div>
                      </td>
                    </tr>
                  ) : filteredTasks.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="p-4 text-center text-gray-500">Không tìm thấy task nào</td>
                    </tr>
                  ) : (
                    filteredTasks.map((t, idx) => (
                      <tr key={t.id} className="border-b hover:bg-gray-50 transition duration-200">
                        <td className="p-4">{t.name}</td>
                        <td className="p-4">
                          <span
                            className={`${
                              STATUS_OPTIONS.find(s => s.value === t.status)?.color
                            } px-3 py-1 rounded-full text-xs ${
                              STATUS_OPTIONS.find(s => s.value === t.status)?.text
                            }`}
                          >
                            {t.status}
                          </span>
                        </td>
                        <td className="p-4">{t.dueDate ? new Date(t.dueDate).toLocaleDateString('vi-VN') : ''}</td>
                        <td className="p-4">
                          {t.timelineStart && t.timelineEnd ? (
                            <span className="bg-gray-200 text-gray-800 px-3 py-1 rounded-full text-xs">
                              {new Date(t.timelineStart).toLocaleDateString('vi-VN')} -{' '}
                              {new Date(t.timelineEnd).toLocaleDateString('vi-VN')}
                            </span>
                          ) : ''}
                        </td>
                        <td className="p-4">{t.notes}</td>
                        <td className="p-4 flex gap-2">
                          <button
                            className="text-blue-600 hover:underline text-sm"
                            onClick={() => handleEditTask(t)}
                          >
                            Sửa
                          </button>
                          <button
                            className="text-red-500 hover:underline text-sm"
                            onClick={() => handleDeleteTask(t.id)}
                          >
                            Xoá
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </main>

      {/* Modal for Board */}
      {showBoardModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-6 w-full max-w-md">
            <h3 className="text-xl font-bold mb-4 text-gray-800">{editBoardId ? 'Sửa Board' : 'Thêm Board'}</h3>
            {boardFormError && (
              <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-3 mb-4 rounded-r-lg">
                {boardFormError}
              </div>
            )}
            <form onSubmit={editBoardId ? handleUpdateBoard : handleAddBoard} className="space-y-4">
              <input
                className="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-600"
                placeholder="Tên board"
                value={boardForm.name}
                onChange={e => {
                  setBoardForm({ name: e.target.value });
                  setBoardFormError('');
                }}
                required
              />
              <div className="flex gap-2">
                <button
                  className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition duration-200"
                  type="submit"
                >
                  {editBoardId ? 'Lưu' : 'Thêm'}
                </button>
                <button
                  className="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg hover:bg-gray-300 transition duration-200"
                  type="button"
                  onClick={() => setShowBoardModal(false)}
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal for Group */}
      {showGroupModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-6 w-full max-w-md">
            <h3 className="text-xl font-bold mb-4 text-gray-800">{editGroupId ? 'Sửa Group' : 'Thêm Group'}</h3>
            {groupFormError && (
              <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-3 mb-4 rounded-r-lg">
                {groupFormError}
              </div>
            )}
            <form onSubmit={editGroupId ? handleUpdateGroup : handleAddGroup} className="space-y-4">
              <input
                className="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-600"
                placeholder="Tên group"
                value={groupForm.name}
                onChange={e => {
                  setGroupForm({ name: e.target.value });
                  setGroupFormError('');
                }}
                required
              />
              <div className="flex gap-2">
                <button
                  className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition duration-200"
                  type="submit"
                >
                  {editGroupId ? 'Lưu' : 'Thêm'}
                </button>
                <button
                  className="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg hover:bg-gray-300 transition duration-200"
                  type="button"
                  onClick={() => setShowGroupModal(false)}
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal for Task */}
      {showTaskModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl p-6 w-full max-w-lg">
            <h3 className="text-xl font-bold mb-4 text-gray-800">{editTaskId ? 'Sửa Task' : 'Thêm Task'}</h3>
            {taskFormError && (
              <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-3 mb-4 rounded-r-lg">
                {taskFormError}
              </div>
            )}
            <form onSubmit={editTaskId ? handleUpdateTask : handleAddTask} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Tên task</label>
                <input
                  className="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-600"
                  placeholder="Nhập tên task"
                  value={editTaskId ? editTask.name : newTask.name}
                  onChange={e =>
                    editTaskId
                      ? setEditTask({ ...editTask, name: e.target.value })
                      : setNewTask({ ...newTask, name: e.target.value })
                  }
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Trạng thái</label>
                <select
                  className="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-600"
                  value={editTaskId ? editTask.status : newTask.status}
                  onChange={e =>
                    editTaskId
                      ? setEditTask({ ...editTask, status: e.target.value })
                      : setNewTask({ ...newTask, status: e.target.value })
                  }
                >
                  {STATUS_OPTIONS.map(opt => (
                    <option key={opt.value} value={opt.value}>
                      {opt.value}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Hạn chót</label>
                <input
                  type="date"
                  className="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-600"
                  value={editTaskId ? editTask.dueDate : newTask.dueDate}
                  onChange={e =>
                    editTaskId
                      ? setEditTask({ ...editTask, dueDate: e.target.value })
                      : setNewTask({ ...newTask, dueDate: e.target.value })
                  }
                  required
                />
              </div>
              <div className="flex gap-4">
                <div className="flex-1">
                  <label className="block text-sm font-medium text-gray-700">Timeline bắt đầu</label>
                  <input
                    type="date"
                    className="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-600"
                    value={editTaskId ? editTask.timelineStart : newTask.timelineStart}
                    onChange={e =>
                      editTaskId
                        ? setEditTask({ ...editTask, timelineStart: e.target.value })
                        : setNewTask({ ...newTask, timelineStart: e.target.value })
                    }
                    required
                  />
                </div>
                <div className="flex-1">
                  <label className="block text-sm font-medium text-gray-700">Timeline kết thúc</label>
                  <input
                    type="date"
                    className="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-600"
                    value={editTaskId ? editTask.timelineEnd : newTask.timelineEnd}
                    onChange={e =>
                      editTaskId
                        ? setEditTask({ ...editTask, timelineEnd: e.target.value })
                        : setNewTask({ ...newTask, timelineEnd: e.target.value })
                    }
                    required
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Ghi chú</label>
                <input
                  className="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-600"
                  placeholder="Nhập ghi chú"
                  value={editTaskId ? editTask.notes : newTask.notes}
                  onChange={e =>
                    editTaskId
                      ? setEditTask({ ...editTask, notes: e.target.value })
                      : setNewTask({ ...newTask, notes: e.target.value })
                  }
                />
              </div>
              <div className="flex gap-2">
                <button
                  className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition duration-200"
                  type="submit"
                >
                  {editTaskId ? 'Lưu' : 'Thêm'}
                </button>
                <button
                  className="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg hover:bg-gray-300 transition duration-200"
                  type="button"
                  onClick={() => setShowTaskModal(false)}
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default HomePage;