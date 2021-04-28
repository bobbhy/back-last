package com.forumensak.api.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessagePayload {
    private String jwt;
    private String message;
    private long receiverId;
}
