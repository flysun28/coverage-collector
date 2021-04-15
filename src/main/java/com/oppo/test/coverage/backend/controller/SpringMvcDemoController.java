package com.oppo.test.coverage.backend.controller;

import esa.restlight.spring.shaded.org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/restlight/springmvc/users")
public class SpringMvcDemoController {

    private static final Map<String, User> USERS_MAP = new HashMap<>();

    static {
        USERS_MAP.put("1", new User("1", "LiMing", 25));
        USERS_MAP.put("2", new User("2", "LiSi", 36));
        USERS_MAP.put("3", new User("3", "WangWu", 31));
    }

    @GetMapping("/{id}")
    public User find(@PathVariable String id) {
        return USERS_MAP.get(id);
    }

    @PostMapping
    public void update(@RequestBody User user) {
        if (USERS_MAP.containsKey(user.getId())) {
            User userOriginal = USERS_MAP.get(user.getId());
            if (user.getName() != null) {
                userOriginal.setName(user.getName());
            }
            if (user.getAge() > 0) {
                userOriginal.setAge(user.getAge());
            }
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        USERS_MAP.remove(id);
    }

    @PutMapping
    public void add(@RequestBody User user) {
        USERS_MAP.put(user.getId(), user);
    }

    static class User {
        private String id;
        private String name;
        private int age;

        public User() {
        }

        public User(String id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
