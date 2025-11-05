package it.korea.app_bmpc.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewRecommendEvent {
    private final String userId;
    private final String message;
}
