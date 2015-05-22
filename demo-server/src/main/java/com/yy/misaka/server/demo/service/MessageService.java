package com.yy.misaka.server.demo.service;

import com.yy.misaka.server.demo.domain.Message;
import com.yy.misaka.server.demo.domain.User;
import com.yy.misaka.server.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRoom chatRoom;
    @Autowired
    private BroadcastService broadcastService;

    public void enterRoom(String uid) {
        chatRoom.add(uid);
        User user = userRepository.findByUid(uid);
        if (user == null) {
            user = new User();
            user.setNick(uid);
            user.setUid(uid);
            userRepository.save(user);
        }
        chatRoom.broadcastUsers();
    }

    public void sendMessage(Message message) {
        message.setFromUser(userRepository.findByUid(message.getFromUid()));
        broadcastService.pushToUser(message.getToUid(), "demo-server", "/message", message);
    }

    public List<User> allUsers() {
        return chatRoom.allUsers();
    }
}
