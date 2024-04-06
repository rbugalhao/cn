package topic;

public class Topic {

    private int id;
    private String topicname;

    public Topic(int id, String name) {
        this.id = id;
        this.topicname = name;
    }

    public Topic(int id) {
        this.id = id;
        this.topicname = "TBA";
    }

    public int getId() {
        return id;
    }

    public String getTopicName() {
        return topicname;
    }

    public void fetchTopicName(String name) {
        this.topicname = TopicDatabase.getTopicnameById(id);
    }


    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", name='" + topicname + '\'' +
                '}';
    }

}
