package user;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import topic.Topic;
import topic.TopicDatabase;
import utils.FileReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;

import static utils.Utils.*;

public class UserDatabase {

    private final static String USERS_FILE = "src/main/xml/users.xml";

    public static User getUserById(int userId) {
        try {
//            File xmlFile = new File(USERS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();

            FileReader fr = rw(READ, USERS_FILE, null, null);
            Document doc = fr.doc;

            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Node userNode = userList.item(i);

                if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) userNode;
                    int id = Integer.parseInt(userElement.getAttribute("id"));

                    if (id == userId) {
                        String username = userElement.getElementsByTagName("username").item(0).getTextContent();
                        String password = userElement.getElementsByTagName("password").item(0).getTextContent();
                        ArrayList<Topic> topics = getTopicsForUser(userElement);

                        return new User(id, username, password, topics);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<Topic> getTopicsForUser(Element userElement) {
        ArrayList<Topic> topics = new ArrayList<>();
        NodeList topicsList = userElement.getElementsByTagName("topic");
        for (int i = 0; i < topicsList.getLength(); i++) {
            Node topicNode = topicsList.item(i);
            if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                Element topicElement = (Element) topicNode;
                int topicId = Integer.parseInt(topicElement.getAttribute("id"));
                // Assuming you have a method to fetch topic details based on ID
                Topic topic = fetchTopicDetails(topicId);
                topics.add(topic);

            }
        }
        return topics;
    }

    private static Topic fetchTopicDetails(int topicId) {
        // get the topicname of the topic with the given ID
        String topicName = TopicDatabase.getTopicnameById(topicId);

        return new Topic(topicId, topicName);
    }

    public static boolean addUser(String username, String password) {
        try {
//            File xmlFile = new File(USERS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();

            FileReader fr = rw(READ, USERS_FILE, null, null);
            Document doc = fr.doc;

            // Get the root element (users)
            Element rootElement = doc.getDocumentElement();

            // Create a new user element
            Element newUser = doc.createElement("user");

            // Get the ID for the new user (assuming incrementing IDs)
            int newUserId = Integer.parseInt(rootElement.getAttribute("current_id"))+1;
            newUser.setAttribute("id", String.valueOf(newUserId));
            rootElement.setAttribute("current_id", String.valueOf(newUserId));

            // Create username element and set its value
            Element usernameElement = doc.createElement("username");
            usernameElement.appendChild(doc.createTextNode(username));
            newUser.appendChild(usernameElement);

            // Create password element and set its value
            Element passwordElement = doc.createElement("password");
            passwordElement.appendChild(doc.createTextNode(password));
            newUser.appendChild(passwordElement);

            Element topicsElement = doc.createElement("topics");
            newUser.appendChild(topicsElement);

            // Append the new user element to the root element
            rootElement.appendChild(newUser);

            // Write the updated XML file
//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            Transformer transformer = transformerFactory.newTransformer();
//            DOMSource source = new DOMSource(doc);
//            StreamResult result = new StreamResult(new File(USERS_FILE));
//            transformer.transform(source, result);
            rw(WRITE, null, doc, fr.xmlFile);

            System.out.println("New user added successfully.");

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addTopicToUser(int userId, int topicId) {
        try {
//            File xmlFile = new File(USERS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();

            FileReader fr = rw(READ, USERS_FILE, null, null);
            Document doc = fr.doc;

            // Find the user element with the given ID
            Element userElement = findUserById(doc, userId);

            if (userElement != null) {
                // Get the topics element under the user
                Element topicsElement = (Element) userElement.getElementsByTagName("topics").item(0);

                // Create a new topic element
                Element topicElement = doc.createElement("topic");
                topicElement.setAttribute("id", String.valueOf(topicId));

                // Append the topic element to the topics element
                topicsElement.appendChild(topicElement);

                // Write the updated XML file
//                TransformerFactory transformerFactory = TransformerFactory.newInstance();
//                Transformer transformer = transformerFactory.newTransformer();
//                DOMSource source = new DOMSource(doc);
//                StreamResult result = new StreamResult(new File(USERS_FILE));
//                transformer.transform(source, result);
                rw(WRITE, null, doc, fr.xmlFile);

                System.out.println("Topic added to user successfully.");
            } else {
                System.out.println("User with ID " + userId + " not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkTopicIsSubscribed(int userId, int topicId){
        try {
//            File xmlFile = new File(USERS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();

            FileReader fr = rw(READ, USERS_FILE, null, null);
            Document doc = fr.doc;

            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Node userNode = userList.item(i);

                if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) userNode;
                    int id = Integer.parseInt(userElement.getAttribute("id"));

                    if (id == userId) {
                        Element topics = (Element)userElement.getElementsByTagName("topics").item(0);
                        NodeList topicsList = topics.getElementsByTagName("topic");
                        for (int t = 0; t < topicsList.getLength(); t++){
                            Node topicNode = topicsList.item(t);
                            Element topicElement = (Element) topicNode;
                            if(Integer.parseInt(topicElement.getAttribute("id"))==topicId) return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Element findUserById(Document doc, int userId) {
        NodeList userList = doc.getElementsByTagName("user");
        for (int i = 0; i < userList.getLength(); i++) {
            Node userNode = userList.item(i);
            if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                Element userElement = (Element) userNode;
                int id = Integer.parseInt(userElement.getAttribute("id"));
                if (id == userId) {
                    return userElement;
                }
            }
        }
        return null;
    }

    public static int getIdByUsername(String name){

        try {
//            File xmlFile = new File(USERS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();

            FileReader fr = rw(READ, USERS_FILE, null, null);
            Document doc = fr.doc;

            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Node userNode = userList.item(i);

                if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) userNode;

                    String username = userElement.getElementsByTagName("username").item(0).getTextContent();

                    if(username.equals(name)){
                        return Integer.parseInt(userElement.getAttribute("id"));
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;

    }

    public static void removeTopicFromUser(int userId, int topicId) {
        try {
//            File xmlFile = new File(USERS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();

            FileReader fr = rw(READ, USERS_FILE, null, null);
            Document doc = fr.doc;

            // Find the user element with the given ID
            Element userElement = findUserById(doc, userId);

            if (userElement != null) {
                // Get the topics element under the user
                Element topicsElement = (Element) userElement.getElementsByTagName("topics").item(0);

                // Find the topic element with the given ID
                NodeList topicList = topicsElement.getElementsByTagName("topic");
                for (int i = 0; i < topicList.getLength(); i++) {
                    Node topicNode = topicList.item(i);
                    if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element topicElement = (Element) topicNode;
                        int id = Integer.parseInt(topicElement.getAttribute("id"));
                        if (id == topicId) {
                            topicsElement.removeChild(topicElement);
                            break;
                        }
                    }
                }

                // Write the updated XML file
//                TransformerFactory transformerFactory = TransformerFactory.newInstance();
//                Transformer transformer = transformerFactory.newTransformer();
//                DOMSource source = new DOMSource(doc);
//                StreamResult result = new StreamResult(new File(USERS_FILE));
//                transformer.transform(source, result);
                rw(WRITE, null, doc, fr.xmlFile);

                System.out.println("Topic removed from user successfully.");
            } else {
                System.out.println("User with ID " + userId + " not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Topic[] getTopics(String username) {
        int userId = getIdByUsername(username);
        if (userId == -1) {
            return null;
        }

        try {
//            File xmlFile = new File(USERS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();

            FileReader fr = rw(READ, USERS_FILE, null, null);
            Document doc = fr.doc;

            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Node userNode = userList.item(i);

                if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) userNode;
                    int id = Integer.parseInt(userElement.getAttribute("id"));

                    if (id == userId) {
                        return getTopicsForUser(userElement).toArray(new Topic[0]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
