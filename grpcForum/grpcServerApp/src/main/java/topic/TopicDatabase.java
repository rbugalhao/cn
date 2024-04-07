package topic;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import servicestubs.ForumMessage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

import utils.FileReader;

import static utils.Utils.*;

public class TopicDatabase {


    private final static String TOPICS_FILE = "src/main/xml/topics.xml";

//    private static synchronized FileReader rw(boolean action, String path, Document doc, File xmlFile) throws TransformerException {
//        // 0 - read, 1 - write
//        if(action == WRITE) {
//            editFile(doc, xmlFile);
//            return null;
//        }
//        return readFile(path);
//    }
//    private static synchronized FileReader readFile(String file) {
//        return new FileReader(file);
//    }
//    private static synchronized void editFile(Document doc, File xmlFile) throws TransformerException {
//        TransformerFactory transformerFactory = TransformerFactory.newInstance();
//        Transformer transformer = transformerFactory.newTransformer();
//        DOMSource source = new DOMSource(doc);
//        StreamResult result = new StreamResult(xmlFile);
//        transformer.transform(source, result);
//    }

    public static int getIdByTopicname(String name) {
        try {
//            File xmlFile = new File(TOPICS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();

            FileReader fr = rw(READ, TOPICS_FILE, null, null);
            Document doc = fr.doc;

            NodeList topicList = doc.getElementsByTagName("topic");

            for (int i = 0; i < topicList.getLength(); i++) {
                Node topicNode = topicList.item(i);

                if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element topicElement = (Element) topicNode;
                    String topicname = topicElement.getElementsByTagName("topicname").item(0).getTextContent();

                    if (topicname.equals(name)) {
                        return Integer.parseInt(topicElement.getAttribute("id"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
    }
        return -1;
    }

    public static String getTopicnameById(int id) {
        try {
//            File xmlFile = new File(TOPICS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();
            FileReader fr = rw(READ, TOPICS_FILE, null, null);
            Document doc = fr.doc;

            NodeList topicList = doc.getElementsByTagName("topic");

            for (int i = 0; i < topicList.getLength(); i++) {
                Node topicNode = topicList.item(i);

                if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element topicElement = (Element) topicNode;
                    int topicId = Integer.parseInt(topicElement.getAttribute("id"));

                    if (topicId == id) {
                        return topicElement.getElementsByTagName("topicname").item(0).getTextContent();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createTopic(String topicName) {
        try {
//            File xmlFile = new File(TOPICS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();
            FileReader fr = rw(READ, TOPICS_FILE, null, null);
            Document doc = fr.doc;

            Element root = doc.getDocumentElement();

            Element topic = doc.createElement("topic");

            int new_id = Integer.parseInt(root.getAttribute("current_id")) + 1;
            topic.setAttribute("id", String.valueOf(new_id));


            Element topicname = doc.createElement("topicname");
            topicname.appendChild(doc.createTextNode(topicName));
            topic.appendChild(topicname);

            Element subscribers = doc.createElement("subscribers");
            topic.appendChild(subscribers);

            Element messages = doc.createElement("messages");
            messages.setAttribute("current_id", "0");
            topic.appendChild(messages);

            root.setAttribute("current_id", String.valueOf(new_id));

            root.appendChild(topic);

//            editFile(doc, xmlFile);
            rw(WRITE, null, doc, fr.xmlFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void addUserToTopic(int topicId, int userId) {
        try {
//            File xmlFile = new File(TOPICS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();
            FileReader fr = rw(READ, TOPICS_FILE, null, null);
            Document doc = fr.doc;

            NodeList topicList = doc.getElementsByTagName("topic");

            for (int i = 0; i < topicList.getLength(); i++) {
                Node topicNode = topicList.item(i);

                if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element topicElement = (Element) topicNode;
                    int id = Integer.parseInt(topicElement.getAttribute("id"));

                    if (id == topicId) {
                        Element subscribers = (Element) topicElement.getElementsByTagName("subscribers").item(0);
                        Element subscriber = doc.createElement("subscriber");
                        subscriber.setAttribute("user_id", String.valueOf(userId));
                        subscribers.appendChild(subscriber);
                    }
                }
            }

//            editFile(doc, xmlFile);
            rw(WRITE, null, doc, fr.xmlFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void removeUserFromTopic(int topicId, int userId) {
        try {
//            File xmlFile = new File(TOPICS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();
            FileReader fr = rw(READ, TOPICS_FILE, null, null);
            Document doc = fr.doc;

            NodeList topicList = doc.getElementsByTagName("topic");

            for (int i = 0; i < topicList.getLength(); i++) {
                Node topicNode = topicList.item(i);

                if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element topicElement = (Element) topicNode;
                    int id = Integer.parseInt(topicElement.getAttribute("id"));

                    if (id == topicId) {
                        Element subscribers = (Element) topicElement.getElementsByTagName("subscribers").item(0);
                        NodeList subscriberList = subscribers.getElementsByTagName("subscriber");

                        for (int j = 0; j < subscriberList.getLength(); j++) {
                            Node subscriberNode = subscriberList.item(j);
                            if (subscriberNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element subscriberElement = (Element) subscriberNode;
                                int subId = Integer.parseInt(subscriberElement.getAttribute("user_id"));
                                if (subId == userId) {
                                    subscribers.removeChild(subscriberNode);
                                }
                            }
                        }
                    }
                }
            }

//            editFile(doc, xmlFile);
            rw(WRITE, null, doc, fr.xmlFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Topic[] getAllTopics() {
        try {
//            File xmlFile = new File(TOPICS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();
            FileReader fr = rw(READ, TOPICS_FILE, null, null);
            Document doc = fr.doc;

            NodeList topicList = doc.getElementsByTagName("topic");
            Topic[] topics = new Topic[topicList.getLength()];

            for (int i = 0; i < topicList.getLength(); i++) {
                Node topicNode = topicList.item(i);

                if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element topicElement = (Element) topicNode;
                    int id = Integer.parseInt(topicElement.getAttribute("id"));
                    String topicname = topicElement.getElementsByTagName("topicname").item(0).getTextContent();
                    topics[i] = new Topic(id, topicname);
                }
            }

            return topics;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ForumMessage[] getMessages(int id) {
        try {
//            File xmlFile = new File(TOPICS_FILE);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
//            doc.getDocumentElement().normalize();
            FileReader fr = rw(READ, TOPICS_FILE, null, null);
            Document doc = fr.doc;

            NodeList topicList = doc.getElementsByTagName("topic");

            for (int i = 0; i < topicList.getLength(); i++) {
                Node topicNode = topicList.item(i);

                if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element topicElement = (Element) topicNode;
                    int topicId = Integer.parseInt(topicElement.getAttribute("id"));

                    if (topicId == id) {
                        Element messagesElement = (Element) topicElement.getElementsByTagName("messages").item(0);
                        NodeList messagesList = messagesElement.getElementsByTagName("message");
                        ForumMessage[] messages = new ForumMessage[messagesList.getLength()];

                        for (int j = 0; j < messagesList.getLength(); j++) {
                            Node messageNode = messagesList.item(j);

                            if (messageNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element messageElement = (Element) messageNode;
                                String username = messageElement.getElementsByTagName("username").item(0).getTextContent();
                                String topicName = topicElement.getElementsByTagName("topicname").item(0).getTextContent();
                                String constent = messageElement.getElementsByTagName("content").item(0).getTextContent();

                                messages[j] = ForumMessage.newBuilder().setFromUser(username).setTopicName(topicName).setTxtMsg(constent).build();
                            }
                        }

                        return messages;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createMessage(int topicId, int userId, String fromUser, String txtMsg) {

        try {
            FileReader fr = rw(READ, TOPICS_FILE, null, null);
            Document doc = fr.doc;

            NodeList topicList = doc.getElementsByTagName("topic");

            for (int i = 0; i < topicList.getLength(); i++) {
                Node topicNode = topicList.item(i);

                if (topicNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element topicElement = (Element) topicNode;
                    int tId = Integer.parseInt(topicElement.getAttribute("id"));

                    if (tId == topicId) {
                        Element messagesElement = (Element) topicElement.getElementsByTagName("messages").item(0);
                        String current_id = messagesElement.getAttribute("current_id");
                        int new_id = Integer.parseInt(current_id) + 1;
                        messagesElement.setAttribute("current_id", String.valueOf(new_id));

                        Element message = doc.createElement("message");
                        message.setAttribute("id", String.valueOf(new_id));

                        Element user_id = doc.createElement("user_id");
                        user_id.setTextContent(String.valueOf(userId));
                        message.appendChild(user_id);

                        Element username = doc.createElement("username");
                        username.setTextContent(fromUser);
                        message.appendChild(username);

                        Element content = doc.createElement("content");
                        content.setTextContent(txtMsg);
                        message.appendChild(content);

                        messagesElement.appendChild(message);

                    }
                }
            }
            rw(WRITE, null, doc, fr.xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

