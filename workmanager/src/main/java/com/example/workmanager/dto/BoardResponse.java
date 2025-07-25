package com.example.workmanager.dto;

import com.example.workmanager.model.Board;
import java.util.List;
import java.util.stream.Collectors;

public class BoardResponse {
    private Integer id;
    private String name;
    private String description;
    private List<GroupResponse> groups;

    public BoardResponse(Board board) {
        this.id = board.getId();
        this.name = board.getName();
        this.description = board.getDescription();
        this.groups = board.getGroups() != null
                ? board.getGroups().stream().map(GroupResponse::new).collect(Collectors.toList())
                : null;
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<GroupResponse> getGroups() { return groups; }
}
