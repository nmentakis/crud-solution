package com.galvanize.user;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository repository;

    @Test
    @Transactional
    @Rollback
    void getUsersReturnAllUsers() throws Exception {
        User testUserOne = new User();
        testUserOne.setEmail("john@example.com");
        testUserOne.setPassword("supersecretpassword");

        User testUserTwo = new User();
        testUserTwo.setEmail("eliza@example.com");
        testUserTwo.setPassword("othersecretpassword");

        User savedUserOne = repository.save(testUserOne);
        User savedUserTwo = repository.save(testUserTwo);

        this.mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", is("john@example.com")))
                .andExpect(jsonPath("$[0].id", is((int) savedUserOne.getId())))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[1].email", is("eliza@example.com")))
                .andExpect(jsonPath("$[1].id", is((int)savedUserTwo.getId())))
                .andExpect(jsonPath("$[1].id").isNumber());

    }

    @Test
    @Transactional
    @Rollback
    void postToUsersSavesToDatabase() throws Exception {

        this.mvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"john@example.com\",\"password\":\"something-secret\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.password").doesNotHaveJsonPath());

    }

    @Test
    @Transactional
    @Rollback
    void getToSpecificUserReturnsSingleUser() throws Exception {

        User testUserOne = new User();
        testUserOne.setEmail("john@example.com");
        testUserOne.setPassword("supersecretpassword");

        repository.save(testUserOne);

        String requestUrl = String.format("/users/%d", testUserOne.getId());

        this.mvc.perform(get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.password").doesNotHaveJsonPath());
    }



    @Test
    @Transactional
    @Rollback
    void patchWithSpecificValue() throws Exception {
        User testUserOne = new User();
        testUserOne.setEmail("john@example.com");
        testUserOne.setPassword("supersecretpassword");

        repository.save(testUserOne);

        String requestUrl = String.format("/users/%d", testUserOne.getId());

        this.mvc.perform(patch(requestUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\": \"alice@example.com\"}"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.password").doesNotHaveJsonPath());

        this.mvc.perform(patch(requestUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"john@example.com\",\"password\":\"1234\"}"))

                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.password").doesNotHaveJsonPath());



    }


    @Test
    @Transactional
    @Rollback
    void deleteOneUserReturnsCountOfTotal() throws Exception {
        User testUserOne = new User();
        testUserOne.setEmail("john@example.com");
        testUserOne.setPassword("supersecretpassword");

        User testUserTwo = new User();
        testUserTwo.setEmail("eliza@example.com");
        testUserTwo.setPassword("othersecretpassword");

        User savedUserOne = repository.save(testUserOne);
        User savedUserTwo = repository.save(testUserTwo);

        String requestUrl = String.format("/users/%d", testUserOne.getId());

        this.mvc.perform(delete(requestUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(1)));

    }



    @Test
    @Transactional
    @Rollback
    void authenticateReturnsCorrectValueForValidUser() throws Exception {
        User testUserOne = new User();
        testUserOne.setEmail("john@example.com");
        testUserOne.setPassword("supersecretpassword");

        User savedUserOne = repository.save(testUserOne);

        mvc.perform(post("/users/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"john@example.com\",\"password\":\"supersecretpassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated", is(true)))
                .andExpect(jsonPath("$.user.email", is("john@example.com")));

        mvc.perform(post("/users/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@example.com\",\"password\":\"incorrectpassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated", is(false)))
                .andExpect(jsonPath("$.user").doesNotHaveJsonPath());




    }
}
