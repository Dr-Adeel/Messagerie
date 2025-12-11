package com.eilco.messagerie.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
      private String token;
      private Long id;
      private String username;
      private String firstName;
      private String lastName;
      @Builder.Default
    private String type = "Bearer";

      public JwtResponse(String accessToken, Long id, String username, String firstName, String lastName) {
            this.token = accessToken;
            this.id = id;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
      }
}
