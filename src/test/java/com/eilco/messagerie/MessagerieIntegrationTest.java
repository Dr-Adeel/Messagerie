package com.eilco.messagerie;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.request.LoginRequest;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.request.UserRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MessagerieIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testFullFlowWithNotifications() throws Exception {
        // --- 1. Register User A (Alice) ---
        UserRequest aliceReq = UserRequest.builder()
                .username("alice_test")
                .password("password123")
                .firstName("Alice")
                .lastName("Test")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aliceReq)))
                .andExpect(status().isOk());

        // --- 2. Register User B (Bob) ---
        UserRequest bobReq = UserRequest.builder()
                .username("bob_test")
                .password("password123")
                .firstName("Bob")
                .lastName("Test")
                .build();

        MvcResult bobResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bobReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode bobNode = objectMapper.readTree(bobResult.getResponse().getContentAsString());
        Long bobId = bobNode.get("id").asLong();

        // --- 3. Login Alice ---
        LoginRequest loginAlice = new LoginRequest("alice_test", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAlice)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode tokenNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String tokenAlice = "Bearer " + tokenNode.get("token").asText();

        // --- 4. Create Group (Alice) ---
        GroupRequest groupReq = new GroupRequest();
        groupReq.setName("Test Group");

        MvcResult groupResult = mockMvc.perform(post("/api/groups/create")
                        .header(HttpHeaders.AUTHORIZATION, tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode groupNode = objectMapper.readTree(groupResult.getResponse().getContentAsString());
        Long groupId = groupNode.get("id").asLong();

        // --- 5. Add Member Bob to Group ---
        mockMvc.perform(post("/api/groups/" + groupId + "/add-member")
                        .param("username", "bob_test")
                        .header(HttpHeaders.AUTHORIZATION, tokenAlice))
                .andExpect(status().isOk());

        // --- 6. Send Private Message (Alice -> Bob) ---
        MessageRequest msgReq = new MessageRequest();
        msgReq.setReceiverUserId(bobId);
        msgReq.setContent("Hello Bob!");

        MvcResult messageResult = mockMvc.perform(post("/api/messages/send-private")
                        .header(HttpHeaders.AUTHORIZATION, tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello Bob!"))
                .andReturn();

        // Récupérer le messageId depuis la réponse
        JsonNode messageNode = objectMapper.readTree(messageResult.getResponse().getContentAsString());
        Long messageId = messageNode.get("id").asLong();

        // --- 7. Login Bob to check unread messages & notifications ---
        LoginRequest loginBob = new LoginRequest("bob_test", "password123");
        MvcResult loginResultBob = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBob)))
                .andExpect(status().isOk())
                .andReturn();
        String tokenBob = "Bearer " + objectMapper.readTree(loginResultBob.getResponse().getContentAsString())
                .get("token").asText();

        // --- 8. Check Unread Count for Bob ---
        mockMvc.perform(get("/api/messages/unread-count")
                        .header(HttpHeaders.AUTHORIZATION, tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));

        // --- 9. Check Private Notification for Bob ---
        MvcResult notifResult = mockMvc.perform(get("/api/notifications/unread")
                        .header(HttpHeaders.AUTHORIZATION, tokenBob))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode notifNode = objectMapper.readTree(notifResult.getResponse().getContentAsString());
        Assertions.assertEquals(1, notifNode.size());
        Assertions.assertEquals("PRIVATE_MESSAGE", notifNode.get(0).get("type").asText());
        // ✅ CORRECTION : messageId est un nombre (Long), pas le contenu du message
        Assertions.assertEquals(messageId, notifNode.get(0).get("messageId").asLong());
        Assertions.assertEquals(1L, notifNode.get(0).get("senderId").asLong());
        Assertions.assertEquals(2L, notifNode.get(0).get("recipientId").asLong());

        // --- 10. Send Group Message (Alice -> Test Group) ---
        MessageRequest groupMsgReq = new MessageRequest();
        groupMsgReq.setReceiverGroupId(groupId);
        groupMsgReq.setContent("Hello Group!");

        mockMvc.perform(post("/api/messages/send-group")
                        .header(HttpHeaders.AUTHORIZATION, tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupMsgReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello Group!"));

        // --- 11. Check Group Notification for Bob ---
        MvcResult groupNotifResult = mockMvc.perform(get("/api/notifications/unread")
                        .header(HttpHeaders.AUTHORIZATION, tokenBob))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode groupNotifNode = objectMapper.readTree(groupNotifResult.getResponse().getContentAsString());
        Assertions.assertEquals(2, groupNotifNode.size()); // Bob should now have 2 unread notifications
        boolean foundGroupNotif = false;
        for (JsonNode n : groupNotifNode) {
            if (n.get("type").asText().equals("GROUP_MESSAGE") &&
                    n.get("groupId").asLong() == groupId) {
                foundGroupNotif = true;
                break;
            }
        }
        Assertions.assertTrue(foundGroupNotif, "Group notification should exist for Bob");
    }

    @Test
    public void testErrorScenarios() throws Exception {
        // --- 1. Login with bad credentials (401) ---
        LoginRequest badLogin = new LoginRequest("non_existent_user", "wrong_password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());

        // --- 2. Register user for other tests ---
        UserRequest userReq = UserRequest.builder()
                .username("error_test_user")
                .password("password123")
                .firstName("Error")
                .lastName("Test")
                .build();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isOk());

        MvcResult loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("error_test_user", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        String token = "Bearer " + objectMapper.readTree(loginRes.getResponse().getContentAsString())
                .get("token").asText();

        // --- 3. Send message to non-existent user (404) ---
        MessageRequest msgReq = new MessageRequest();
        msgReq.setReceiverUserId(99999L);
        msgReq.setContent("Ghost message");

        mockMvc.perform(post("/api/messages/send-private")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isNotFound());

        // --- 4. Add Member to non-existent group (404) ---
        mockMvc.perform(post("/api/groups/99999/add-member")
                        .param("username", "error_test_user")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());
    }
}