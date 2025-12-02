package com.eilco.messagerie.models.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Long groupId;
    private String groupName;
}