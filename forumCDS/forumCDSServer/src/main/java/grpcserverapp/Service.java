package grpcserverapp;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import servicestubs.*;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Service extends ForumGrpc.ForumImplBase {

    // < topicName, < userName, streamObs<ForumMessage> > >
    private ConcurrentMap<String, ConcurrentMap<String, StreamObserver<ForumMessage>>> topicsList = new ConcurrentHashMap<>();
    private ArrayList<ForumMessage> msgList = new ArrayList<ForumMessage>();
    public Service(int svcPort) {

        System.out.println("Service is available on port:" + svcPort);

        //add some default topics and users

//        topicsList.put("Football", new ConcurrentHashMap<>());
//        topicsList.put("Food", new ConcurrentHashMap<>());
//        topicsList.put("Music", new ConcurrentHashMap<>());
//
//        topicsList.get("Football").put("John", null);
//        topicsList.get("Football").put("Alice", null);
//        topicsList.get("Food").put("Alice", null);
//        topicsList.get("Music").put("John", null);

        // run a thread to send messages to all topics
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (msgList.isEmpty()) {
                    continue;
                }
                //send latest message to all users subscribed to the topic
                ForumMessage msg = msgList.get(0);
                msgList.remove(0);
                if (topicsList.containsKey(msg.getTopicName())) {
                    for (StreamObserver<ForumMessage> streamObserver : topicsList.get(msg.getTopicName()).values()) {
                        if (streamObserver != null) {
                            streamObserver.onNext(msg);
                        }
                    }
                }


            }
        }).start();


    }

    @Override
    public void topicSubscribe(SubscribeUnSubscribe request, StreamObserver<ForumMessage> responseObserver) {
        System.out.println("Received request to subscribe to topic: " + request.getTopicName());

        if(!topicsList.containsKey(request.getTopicName())){
            topicsList.put(request.getTopicName(), new ConcurrentHashMap<>());
        }
        topicsList.get(request.getTopicName()).put(request.getUsrName(), responseObserver);

        ForumMessage message = ForumMessage.newBuilder()
                .setFromUser(request.getUsrName())
                .setTopicName(request.getTopicName())
                .setTxtMsg(request.getUsrName() + " subscribed to topic: " + request.getTopicName())
                .build();

        msgList.add(message);


        //responseObserver.onCompleted();

    }

    @Override
    public void topicUnSubscribe(SubscribeUnSubscribe request, StreamObserver<Empty> responseObserver) {
        System.out.println("Received request to unsubscribe to topic: " + request.getTopicName());

        ForumMessage message = ForumMessage.newBuilder()
                .setFromUser(request.getUsrName())
                .setTopicName(request.getTopicName())
                .setTxtMsg(request.getUsrName() + " unsubscribed to topic: " + request.getTopicName())
                .build();

        msgList.add(message);

        // remove the user from the topic
        if(topicsList.containsKey(request.getTopicName())){
            topicsList.get(request.getTopicName()).remove(request.getUsrName());
        }

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllTopics(Empty request, StreamObserver<ExistingTopics> responseObserver) {
        System.out.println("Received request to get all topics");

        ExistingTopics.Builder topics = ExistingTopics.newBuilder();

        // get all topics from the topicsList
        topicsList.forEach((topic, users) -> {
            System.out.println("Topic: " + topic);
            topics.addTopicName(topic);
        });

        responseObserver.onNext(topics.build());
        responseObserver.onCompleted();
    }

    @Override
    public void publishMessage(ForumMessage request, StreamObserver<Empty> responseObserver) {
        System.out.println("Received request to publish message: " + request.getTxtMsg());

        if(!topicsList.containsKey(request.getTopicName())){
            responseObserver.onError(new Exception("Topic does not exist"));
            return;
        }

        if(!topicsList.get(request.getTopicName()).containsKey(request.getFromUser())){
            responseObserver.onError(new Exception("User is not subscribed to the topic"));
            return;
        }

        msgList.add(request);

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }


}
