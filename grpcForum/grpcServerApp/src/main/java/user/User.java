package user;

import topic.Topic;

import java.util.ArrayList;

public class User {
    private int id;
    private String username;
    private String password;
    private ArrayList<Topic> topics;

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.topics = new ArrayList<>();
    }
    public User(int id, String username, String password, ArrayList<Topic> topics) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.topics = topics;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<Topic> getTopics() {
        return topics;
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        topics.remove(topic);
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", topicCount='" + topics.size() + '\'' +
                '}';
    }
}
