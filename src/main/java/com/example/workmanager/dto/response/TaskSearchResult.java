package com.example.workmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
@AllArgsConstructor
@Data
public class TaskSearchResult {
    private List<TaskResponse> tasks;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private int currentSize;
    private boolean hasMore;
    private boolean isFirst;
    private boolean isLast;


}