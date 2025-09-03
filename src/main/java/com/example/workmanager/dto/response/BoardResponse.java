package com.example.workmanager.dto.response;

import com.example.workmanager.model.Board;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
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
}
