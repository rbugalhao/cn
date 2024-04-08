package grpcserverapp;

import com.google.api.SystemParameterOrBuilder;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import servicestubs.*;
import topic.Topic;
import topic.TopicDatabase;
import user.UserDatabase;
import utils.Utils;


import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Service extends ForumGrpc.ForumImplBase {

    public Service(int svcPort) {
        System.out.println("Service is available on port:" + svcPort);
        createXMLFiles();
    }

    // create a folder xmlDB with a users.xml and topics.xml file, if they don't exist
    private void createXMLFiles() {
        File folder = new File("xmlDB");
        if (!folder.exists()) {
            folder.mkdir();
        }

        File users = new File("xmlDB/users.xml");
        if (!users.exists()) {
            Utils.createXMLFile("xmlDB/users.xml","<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><users current_id=\"0\"></users>");

        }

        File topics = new File("xmlDB/topics.xml");
        if (!topics.exists()) {
            Utils.createXMLFile("xmlDB/topics.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><topics current_id=\"0\"></topics>");

        }
    }
    @Override
    public void loginUser(User request, StreamObserver<Valid> responseObserver) {
        System.out.println("Received request to login user: " + request.getUsrName());
        boolean isValid = Utils.logUser(request.getUsrName(), request.getPassword());
        Valid valid = Valid.newBuilder().setValid(isValid).build();
        responseObserver.onNext(valid);
        responseObserver.onCompleted();
    }

    @Override
    public void registerUser(User request, StreamObserver<Valid> responseObserver) {
        System.out.println("Received request to register user: " + request.getUsrName());
        boolean reg = UserDatabase.addUser(request.getUsrName(), request.getPassword());
        Valid valid = Valid.newBuilder().setValid(reg).build();
        responseObserver.onNext(valid);
        responseObserver.onCompleted();
    }

    @Override
    public void topicSubscribe(SubscribeUnSubscribe request, StreamObserver<ForumMessage> responseObserver) {
        System.out.println("Received request to subscribe to topic: " + request.getTopicName());
        Utils.subscribeTopic(request.getUsrName(), request.getTopicName());

        ForumMessage message = ForumMessage.newBuilder()
                .setFromUser(request.getUsrName())
                .setTopicName(request.getTopicName())
                .setTxtMsg("Subscribed to topic: " + request.getTopicName())
                .build();
        responseObserver.onNext(message);
        responseObserver.onCompleted();
    }

    @Override
    public void topicUnSubscribe(SubscribeUnSubscribe request, StreamObserver<ForumMessage> responseObserver) {
        System.out.println("Received request to unsubscribe to topic: " + request.getTopicName());
        Utils.unsubscribeTopic(request.getUsrName(), request.getTopicName());

        ForumMessage message = ForumMessage.newBuilder()
                .setFromUser(request.getUsrName())
                .setTopicName(request.getTopicName())
                .setTxtMsg("Unubscribed to topic: " + request.getTopicName())
                .build();
        responseObserver.onNext(message);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllTopics(Empty request, StreamObserver<ExistingTopics> responseObserver) {
        System.out.println("Received request to get all topics");
        ExistingTopics.Builder topics = ExistingTopics.newBuilder();
        Topic[] topicsList = TopicDatabase.getAllTopics();

        if (topicsList==null) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
            return;
        }
        if (topicsList.length == 0) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
            return;
        }

        for (Topic topic : topicsList) {
            topics.addTopicName(topic.getTopicName());
        }
        responseObserver.onNext(topics.build());
        responseObserver.onCompleted();
    }

    @Override
    public void publishMessage(ForumMessage request, StreamObserver<Valid> responseObserver) {
        System.out.println("Received request to publish message: " + request.getTxtMsg());
        boolean val = Utils.addMessage(request.getTopicName(), request.getFromUser(), request.getTxtMsg());
        responseObserver.onNext(Valid.newBuilder().setValid(val).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getMessages(User request, StreamObserver<ForumMessage> responseObserver) {
        System.out.println("Received request to get messages for user: " + request.getUsrName());

        Topic[] topicsList = UserDatabase.getTopics(request.getUsrName());

        if (topicsList==null) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
            return;
        }
        if (topicsList.length == 0) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
            return;
        }

        for (Topic topic : topicsList) {
            ForumMessage[] messages = TopicDatabase.getMessages(topic.getId());

            if (messages==null) {
                continue;
            }

            for (ForumMessage message : messages) {
                responseObserver.onNext(message);
            }
        }
    }

    @Override
    public void getSubscribedTopics(User request, StreamObserver<ExistingTopics> responseObserver) {
        System.out.println("Received request to get my topics");
        ExistingTopics.Builder topics = ExistingTopics.newBuilder();
        Topic[] topicsList = UserDatabase.getTopics(request.getUsrName());

        if (topicsList==null) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
            return;
        }
        if (topicsList.length == 0) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
            return;
        }

        for (Topic topic : topicsList) {
            topics.addTopicName(topic.getTopicName());
        }
        responseObserver.onNext(topics.build());
        responseObserver.onCompleted();
    }


}
