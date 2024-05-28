package server;


import com.google.api.core.ApiFuture;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class PubSubClient {

    private final static String PROJECT_ID = "cn2324-t2-g11";


    // Menu options
//    public static void listTopics() throws IOException {
//        TopicAdminClient topicAdmin = TopicAdminClient.create();
//        TopicAdminClient.ListTopicsPagedResponse res = topicAdmin.listTopics(ProjectName.of(PROJECT_ID));
//        for (Topic top : res.iterateAll()) {
//            System.out.println("Topic Name=" + top.getName());
//        }
//        topicAdmin.close();
//    }
//
//    public static void createNewTopic(String topicID) {
//        TopicAdminClient topicAdmin = null;
//        try {
//            topicAdmin = TopicAdminClient.create();
//        } catch (IOException e) {
//            System.out.println("Error creating TopicAdminClient");
//        }
//        //ProjectTopicName projTopName=ProjectTopicName.of(PROJECT_ID, topicName);
//        TopicName projTopName = TopicName.ofProjectTopicName(PROJECT_ID, topicID);
//        // check if topic exists
//        TopicAdminClient.ListTopicsPagedResponse list = topicAdmin.listTopics(ProjectName.of(PROJECT_ID));
//        boolean exists = false;
//        for (Topic top : list.iterateAll()) {
//            if (top.getName().equals(projTopName.toString())) {
//                exists = true;
//                break;
//            }
//        }
//        if (!exists){
//            topicAdmin.createTopic(projTopName);
//            topicAdmin.close();
//        }
//
//    }

    public static void publishMessage(String topicID, String msg, Map<String, String> atribs) throws Exception {
        TopicName topicName = TopicName.ofProjectTopicName(PROJECT_ID, topicID);
        Publisher publisher = Publisher.newBuilder(topicName).build();
        ByteString msgData = ByteString.copyFromUtf8(msg);

//        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
//            .setData(msgData)
//            .putAttributes("key1", "value1")
//            .build();

        PubsubMessage.Builder builder = PubsubMessage.newBuilder()
            .setData(msgData);
        for (String key : atribs.keySet())
            builder.putAttributes(key, atribs.get(key));
        PubsubMessage pubsubMessage = builder.build();


        ApiFuture<String> future = publisher.publish(pubsubMessage);
        String msgID = future.get();
        System.out.println("Message Published with ID=" + msgID);
        publisher.shutdown();
    }

//    public static void publishMultipleMessages(String topicID, int numMsg, String msgPrefix) throws Exception {
//        TopicName topicName = TopicName.ofProjectTopicName(PROJECT_ID, topicID);
//        Publisher publisher = Publisher.newBuilder(topicName).build();
//        for (int i = 0; i < numMsg; i++) {
//            ByteString msgData = ByteString.copyFromUtf8("Multiple:" + msgPrefix + (i + 1));
//            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
//                .setData(msgData)
//                .build();
//            ApiFuture<String> future = publisher.publish(pubsubMessage);
//            String msgID = future.get();
//            System.out.println("Message Published with ID=" + msgID);
//        }
//        publisher.shutdown();
//    }
//
//    public static void listExistingSubscriptions() throws IOException {
//        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create();
//        SubscriptionAdminClient.ListSubscriptionsPagedResponse list = subscriptionAdminClient.listSubscriptions(ProjectName.of(PROJECT_ID));
//        System.out.println("Existing Subscriptions:");
//        for (Subscription sub : list.iterateAll()) {
//            System.out.println(sub.getName() + " (Topic Name:" + sub.getTopic() + ")");
//        }
//        subscriptionAdminClient.close();
//    }

    public static void createSubscription(String topicID, String subscriptionID) {
        TopicName topicName = TopicName.of(PROJECT_ID, topicID);
        SubscriptionName subscriptionName = SubscriptionName.of(
            PROJECT_ID, subscriptionID);
        SubscriptionAdminClient subscriptionAdminClient = null;
        try {
            subscriptionAdminClient = SubscriptionAdminClient.create();
            PushConfig pConfig = PushConfig.getDefaultInstance();
            // check if sub exists
            SubscriptionAdminClient.ListSubscriptionsPagedResponse list = subscriptionAdminClient.listSubscriptions(ProjectName.of(PROJECT_ID));
            boolean exists = false;
            for (Subscription sub : list.iterateAll()) {
                if (sub.getName().equals(subscriptionName.toString())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                subscriptionAdminClient.createSubscription(subscriptionName, topicName, pConfig, 0);
                subscriptionAdminClient.close();
            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("Error creating SubscriptionAdminClient");
        }

    }

    public static Subscriber subscribeMessages(String projectID, String subscriptionID) {
        ProjectSubscriptionName projSubscriptionName = ProjectSubscriptionName.of(
            projectID, subscriptionID);
        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(1) // um só thread no handler
                .build();
        Subscriber subscriber =
            Subscriber.newBuilder(projSubscriptionName, new MessageReceiveHandler())
                .build();
        subscriber.startAsync().awaitRunning();
        return subscriber;
    }


}
