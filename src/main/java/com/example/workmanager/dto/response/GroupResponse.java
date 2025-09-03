package com.example.workmanager.dto.response;

import com.example.workmanager.model.Group;
import lombok.Data;
import lombok.Getter;

@Data
public class GroupResponse {

    private Integer id;

    private String name;

    private Integer boardId;

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
