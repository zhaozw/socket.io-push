package com.yy.misaka.server.demo.service;

import com.yy.misaka.server.demo.domain.User;
import com.yy.misaka.server.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRoom chatRoom;

    public User getByUid(String uid) {
        return userRepository.findByUid(uid);
    }

    public User modify(User user) {
        User indb = userRepository.findByUid(user.getUid());
        if (user.getNick() != null) {
            indb.setNick(user.getNick());
        }
        if (user.getPortrait() != null) {
            indb.setPortrait(user.getPortrait());
        }
        userRepository.save(indb);
        chatRoom.broadcastUsers();
        return indb;
    }
}
