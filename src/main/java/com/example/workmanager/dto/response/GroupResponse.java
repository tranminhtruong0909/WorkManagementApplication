package com.example.workmanager.dto;

import com.example.workmanager.model.Group;
import lombok.Getter;

@Getter
public class GroupResponse {
    @Getter
    private Integer id;
    @Getter
    private String name;
    @Getter
    private Integer boardId;
    @Getter
    private String boardName;

    public GroupResponse(Integer id, String name, Integer boardId, String boardName) {
        this.id = id;
        this.name = name;
        this.boardId = boardId;
        this.boardName = boardName;
    }
    public GroupResponse(Group group) {
        this(
                group.getId(),
                group.getName(),
                group.getBoard().getId(),
                group.getBoard().getName()
        );
    }

}
