package utils;

import topic.TopicDatabase;
import user.User;
import user.UserDatabase;

public class Utils {
    public static void subscribeTopic(String usrName, String topicName) {

        int userId = UserDatabase.getIdByUsername(usrName);
        System.out.println("User id: " + userId);
        int topicId = TopicDatabase.getIdByTopicname(topicName);
        System.out.println("Topic id: " + topicId);
        boolean alreadySubscribed = false;
        if(topicId == -1) {
            TopicDatabase.createTopic(topicName);
            topicId = TopicDatabase.getIdByTopicname(topicName);
        }else alreadySubscribed = UserDatabase.checkTopicIsSubscribed(userId, topicId);
        System.out.println("Already subscribed: " + alreadySubscribed);
        if(!alreadySubscribed) {
            UserDatabase.addTopicToUser(userId, topicId);
            TopicDatabase.addUserToTopic(topicId, userId);
        }else {
            System.out.println("User already subscribed to topic: " + topicName);
        }
    }

    public static void unsubscribeTopic(String usrName, String topicName) {
        int userId = UserDatabase.getIdByUsername(usrName);
        int topicId = TopicDatabase.getIdByTopicname(topicName);
        boolean alreadySubscribed = UserDatabase.checkTopicIsSubscribed(userId, topicId);

        if(alreadySubscribed) {
            UserDatabase.removeTopicFromUser(userId, topicId);
            TopicDatabase.removeUserFromTopic(topicId, userId);
        }else {
            System.out.println("User not subscribed to topic: " + topicName);
        }
    }

    public static boolean logUser(String usrName, String password) {
        int userId = UserDatabase.getIdByUsername(usrName);
        if(userId == -1) {
            System.out.println("User not found");
            return false;
        }

        User user = UserDatabase.getUserById(userId);
        if(user.getPassword().equals(password)) {
            System.out.println("User logged in: " + usrName);
            return true;
        }

        System.out.println("Not logged in");
        return false;
    }


    public static void addMessage(String topicName, String fromUser, String txtMsg) {
        int topicId = TopicDatabase.getIdByTopicname(topicName);
        int userId = UserDatabase.getIdByUsername(fromUser);
        if(topicId == -1) {
            System.out.println("Topic not found");
            return;
        }
        if(userId == -1) {
            System.out.println("User not found");
            return;
        }

        TopicDatabase.createMessage(topicId, userId, fromUser, txtMsg);
    }
}
