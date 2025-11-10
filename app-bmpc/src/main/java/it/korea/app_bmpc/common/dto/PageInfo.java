package it.korea.app_bmpc.common.dto;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PageInfo {
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean isFirst;
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrevious;

    public static PageInfo of(Page<?> pageList) {
        return PageInfo.builder()
            .page(pageList.getNumber())
            .size(pageList.getSize())
            .totalPages(pageList.getTotalPages())
            .totalElements(pageList.getTotalElements())
            .isFirst(pageList.isFirst())
            .isLast(pageList.isLast())
            .hasNext(pageList.hasNext())
            .hasPrevious(pageList.hasPrevious())
            .build();
    }
}