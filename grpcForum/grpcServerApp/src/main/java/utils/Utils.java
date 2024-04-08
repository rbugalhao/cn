package utils;

import org.w3c.dom.Document;
import topic.TopicDatabase;
import user.User;
import user.UserDatabase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    public static final boolean READ = false;

    public static final boolean WRITE = true;

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


    public static boolean addMessage(String topicName, String fromUser, String txtMsg) {
        int topicId = TopicDatabase.getIdByTopicname(topicName);
        int userId = UserDatabase.getIdByUsername(fromUser);
        if(topicId == -1) {
            System.out.println("Topic not found");
            return false;
        }
        if(userId == -1) {
            System.out.println("User not found");
            return false;
        }

        // check if user is subscribed to topic
        if(!UserDatabase.checkTopicIsSubscribed(userId, topicId)) {
            System.out.println("User not subscribed to topic: " + topicName);
            return false;
        }

        TopicDatabase.createMessage(topicId, userId, fromUser, txtMsg);
        return true;
    }

    public static synchronized FileReader rw(boolean action, String path, Document doc, File xmlFile) throws TransformerException {
        // 0 - read, 1 - write
        if(action == WRITE) {
            editFile(doc, xmlFile);
            return null;
        }
        return readFile(path);
    }
    private static synchronized FileReader readFile(String file) {
        return new FileReader(file);
    }
    private static synchronized void editFile(Document doc, File xmlFile) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(source, result);
    }

    public static void createXMLFile(String filePath, String content) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filePath);
            writer.write(content);
            System.out.println("XML file created successfully at path: " + filePath);
        } catch (IOException e) {
            System.err.println("Error occurred while creating the XML file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error occurred while closing the file writer: " + e.getMessage());
                }
            }
        }
    }

}

