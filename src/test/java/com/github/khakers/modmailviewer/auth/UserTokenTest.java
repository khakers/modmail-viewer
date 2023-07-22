package com.github.khakers.modmailviewer.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserTokenTest {
    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();


    String test = """
            {
              "roles": [
                1083617364853141500
              ],
              "id": 403050000000000500,
              "avatar": null,
              "exp": 1678594623,
              "iat": 1678583823,
              "username": "TestUser",
              "discriminator": "1234"
            }
            """;

    @Test
    public void testDecode() throws JsonProcessingException {
        var decoded = objectMapper.readValue(test, UserToken.class);
        Assertions.assertArrayEquals(new long[]{1083617364853141500L}, decoded.discordRoles);
        Assertions.assertEquals(decoded.id, 403050000000000500L);
        Assertions.assertEquals(decoded.discriminator, "1234");
    }

}