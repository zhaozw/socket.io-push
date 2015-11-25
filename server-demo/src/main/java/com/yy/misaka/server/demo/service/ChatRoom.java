package com.yy.misaka.server.demo.service;

import com.yy.misaka.server.demo.domain.User;
import com.yy.misaka.server.demo.repository.UserRepository;
import com.yy.misaka.support.BroadcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ChatRoom {

    @Autowired
    private BroadcastService broadcastService;
    @Autowired
    private UserRepository userRepository;
    private Set<String> users = new HashSet<String>();

    public void add(String uid) {
        users.add(uid);
    }

    public void broadcastUsers() {
        broadcastService.broadcast("demo-server", "/userList", allUsers());
    }

    public List<User> allUsers() {
        return userRepository.findByUidIn(users);
    }
}
