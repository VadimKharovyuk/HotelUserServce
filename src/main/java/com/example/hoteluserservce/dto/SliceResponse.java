package com.example.hoteluserservce.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@Builder
public class SliceResponse<T> {
    private List<T> content;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;     // Главное для infinite scroll
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;


    public static <T> SliceResponse<T> from(Slice<T> slice) {
        return SliceResponse.<T>builder()
                .content(slice.getContent())
                .currentPage(slice.getNumber())
                .pageSize(slice.getSize())
                .hasNext(slice.hasNext())
                .hasPrevious(slice.hasPrevious())
                .isFirst(slice.isFirst())
                .isLast(slice.isLast())
                .build();
    }
}
