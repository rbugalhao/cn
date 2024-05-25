package server;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import servicestubs.*;




import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ServiceManagement extends ManagementServiceGrpc.ManagementServiceImplBase{

    public static Subscriber subscriber = null;
    private final String PROJECT_ID = "cn2324-t2-g11";
    private final String SERVER_INSTANCE_GROUP_ZONE = "europe-southwest1-a";
    private final String SERVER_INSTANCE_GROUP_NAME = "instance-group-server-1";
    private final String LABELS_INSTANCE_GROUP_ZONE = "europe-southwest1-a";
    private final String LABELS_INSTANCE_GROUP_NAME = "instance-group-labels-1";

    private final String TOPIC_NAME = "IMAGES";
    private final String SUBSCRIPTION_NAME = "subImages";

    public ServiceManagement(int svcPort) throws IOException {

        System.out.println("Service is available on port:" + svcPort);

    }


    @Override
    public void changeNumberOfServerInstances(NumberOfInstances request, StreamObserver<TextMessage> responseObserver) {
        int numberOfInstances = request.getNumberOfInstances();

        try {
            ComputeEngineClient.resizeManagedInstanceGroup(PROJECT_ID, SERVER_INSTANCE_GROUP_ZONE, SERVER_INSTANCE_GROUP_NAME, numberOfInstances);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        TextMessage reply = TextMessage.newBuilder().setTxt("Number of instances changed to " + numberOfInstances).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();

    }

    @Override
    public void changeNumberOfImageProcessingInstances(NumberOfInstances request, StreamObserver<TextMessage> responseObserver) {
        int numberOfInstances = request.getNumberOfInstances();

        try {
            ComputeEngineClient.resizeManagedInstanceGroup(PROJECT_ID, LABELS_INSTANCE_GROUP_ZONE, LABELS_INSTANCE_GROUP_NAME, numberOfInstances);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        TextMessage reply = TextMessage.newBuilder().setTxt("Number of instances changed to " + numberOfInstances).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();

    }

}
