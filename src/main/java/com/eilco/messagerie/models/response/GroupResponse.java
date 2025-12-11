package com.eilco.messagerie.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {

    private Long id;
    private String name;

    private Long creatorId;
    private String creatorUsername;

    private List<UserResponse> members;

    private int memberCount;
}