package com.example.workmanager.controller;

import com.example.workmanager.dto.GroupResponse;
import com.example.workmanager.model.Board;
import com.example.workmanager.model.Group;
import com.example.workmanager.model.Role;
import com.example.workmanager.model.User;
import com.example.workmanager.model.UserRole;
import com.example.workmanager.repository.BoardRepository;
import com.example.workmanager.repository.GroupRepository;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
import com.example.workmanager.service.GroupService;
import com.example.workmanager.service.PermissionService;
import com.example.workmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class GroupControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private TaskService taskService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private Board testBoard;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();

        // Tạo test data
        setupTestData();
    }

    private void setupTestData() {
        // Tạo test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);

        // Tạo test board
        testBoard = new Board();
        testBoard.setName("Test Board");
        testBoard = boardRepository.save(testBoard);

        // Tạo test group
        testGroup = new Group();
        testGroup.setName("Test Group");
        testGroup.setBoard(testBoard);
        testGroup = groupRepository.save(testGroup);

        // Gán quyền ADMIN cho user trên board
        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setBoard(testBoard);
        userRole.setRole(Role.ADMIN);
        userRoleRepository.save(userRole);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateGroup_Success() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Test Group");
        request.put("boardId", testBoard.getId());

        // When & Then
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Test Group"))
                .andExpect(jsonPath("$.boardId").value(testBoard.getId()))
                .andExpect(jsonPath("$.boardName").value(testBoard.getName()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateGroup_MissingName() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("boardId", testBoard.getId());
        // Missing name

        // When & Then
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Missing 'name'")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateGroup_MissingBoardId() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Test Group");
        // Missing boardId

        // When & Then
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Missing 'name' or 'boardId'")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateGroup_InvalidBoardId() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Test Group");
        request.put("boardId", "invalid");

        // When & Then
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid 'boardId' format")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateGroup_NoPermission() throws Exception {
        // Given - Tạo user khác không có quyền
        User unauthorizedUser = new User();
        unauthorizedUser.setEmail("unauthorized@example.com");
        unauthorizedUser.setPassword("password123");
        unauthorizedUser.setName("Unauthorized User");
        unauthorizedUser = userRepository.save(unauthorizedUser);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Test Group");
        request.put("boardId", testBoard.getId());

        // When & Then
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bạn không có quyền tạo group trên board này")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetAllGroups_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].boardId").exists())
                .andExpect(jsonPath("$[0].boardName").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetGroupById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/groups/" + testGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testGroup.getId()))
                .andExpect(jsonPath("$.name").value(testGroup.getName()))
                .andExpect(jsonPath("$.boardId").value(testBoard.getId()))
                .andExpect(jsonPath("$.boardName").value(testBoard.getName()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetGroupById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/groups/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetGroupById_NoPermission() throws Exception {
        // Given - Tạo board khác mà user không có quyền
        Board unauthorizedBoard = new Board();
        unauthorizedBoard.setName("Unauthorized Board");
        unauthorizedBoard = boardRepository.save(unauthorizedBoard);

        Group unauthorizedGroup = new Group();
        unauthorizedGroup.setName("Unauthorized Group");
        unauthorizedGroup.setBoard(unauthorizedBoard);
        unauthorizedGroup = groupRepository.save(unauthorizedGroup);

        // When & Then
        mockMvc.perform(get("/api/groups/" + unauthorizedGroup.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bạn không có quyền xem group này")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateGroup_Success() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Group Name");

        // When & Then
        mockMvc.perform(put("/api/groups/" + testGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testGroup.getId()))
                .andExpect(jsonPath("$.name").value("Updated Group Name"))
                .andExpect(jsonPath("$.boardId").value(testBoard.getId()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateGroup_MissingName() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        // Missing name

        // When & Then
        mockMvc.perform(put("/api/groups/" + testGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Missing 'name'")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateGroup_NotFound() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Group Name");

        // When & Then
        mockMvc.perform(put("/api/groups/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateGroup_NoPermission() throws Exception {
        // Given - Tạo board khác mà user không có quyền
        Board unauthorizedBoard = new Board();
        unauthorizedBoard.setName("Unauthorized Board");
        unauthorizedBoard = boardRepository.save(unauthorizedBoard);

        Group unauthorizedGroup = new Group();
        unauthorizedGroup.setName("Unauthorized Group");
        unauthorizedGroup.setBoard(unauthorizedBoard);
        unauthorizedGroup = groupRepository.save(unauthorizedGroup);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Group Name");

        // When & Then
        mockMvc.perform(put("/api/groups/" + unauthorizedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bạn không có quyền sửa group này")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteGroup_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/groups/" + testGroup.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteGroup_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/groups/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteGroup_NoPermission() throws Exception {
        // Given - Tạo board khác mà user không có quyền
        Board unauthorizedBoard = new Board();
        unauthorizedBoard.setName("Unauthorized Board");
        unauthorizedBoard = boardRepository.save(unauthorizedBoard);

        Group unauthorizedGroup = new Group();
        unauthorizedGroup.setName("Unauthorized Group");
        unauthorizedGroup.setBoard(unauthorizedBoard);
        unauthorizedGroup = groupRepository.save(unauthorizedGroup);

        // When & Then
        mockMvc.perform(delete("/api/groups/" + unauthorizedGroup.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bạn không có quyền xoá group này")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetTasksByGroupId_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/groups/" + testGroup.getId() + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetTasksByGroupId_GroupNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/groups/99999/tasks"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetTasksByGroupId_NoPermission() throws Exception {
        // Given - Tạo board khác mà user không có quyền
        Board unauthorizedBoard = new Board();
        unauthorizedBoard.setName("Unauthorized Board");
        unauthorizedBoard = boardRepository.save(unauthorizedBoard);

        Group unauthorizedGroup = new Group();
        unauthorizedGroup.setName("Unauthorized Group");
        unauthorizedGroup.setBoard(unauthorizedBoard);
        unauthorizedGroup = groupRepository.save(unauthorizedGroup);

        // When & Then
        mockMvc.perform(get("/api/groups/" + unauthorizedGroup.getId() + "/tasks"))
                .andExpect(status().isForbidden());
    }
} 