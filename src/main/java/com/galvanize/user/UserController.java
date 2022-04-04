package com.galvanize.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Iterable<User> getAllUsers() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Object> saveNewUser(@RequestBody User user) {
        try {
            User savedUser = repository.save(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be saved", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneUser(@PathVariable long id) {
        try {
            User responseUser = repository.findById(id).get();
            return new ResponseEntity<>(responseUser, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("No such user exists", HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patchOneUser(@PathVariable long id, @RequestBody Map<String, String> user) {
        try {
            User oldUser = repository.findById(id).get();

            user.forEach((k, v) -> {
                // use reflection to get field k on manager and set it to value v
                Field field = ReflectionUtils.findField(User.class, k);
                field.setAccessible(true);
                ReflectionUtils.setField(field, oldUser, v);
            });


//            user.forEach( (k,v) -> {
//                switch (k) {
//                    case "email":
//                        oldUser.setEmail(v);
//                        break;
//                    case "password":
//                        oldUser.setPassword(v);
//                        break;
//                }
//            });

            User newUser = repository.save(oldUser);
            return new ResponseEntity<>(newUser, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("No such user", HttpStatus.BAD_REQUEST);
        }

    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOneUser(@PathVariable long id) {
        try {
            HashMap<String, Long> responseMap = new HashMap<>();

            repository.deleteById(id);

            responseMap.put("count", repository.count());
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("No valid user to delete", HttpStatus.BAD_REQUEST);
        }

    }


    @PostMapping("/authenticate")
    public ResponseEntity<Object> authenticateUser(@RequestBody User user) {

        User userToCheck = repository.getByEmail(user.getEmail());

//        System.out.println(user);

        if (userToCheck.getPassword().equals(user.getPassword())) {
            return new ResponseEntity<>(new AuthenticatedUser(true, userToCheck), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new AuthenticatedUser(false, null), HttpStatus.OK);
        }

    }


}
