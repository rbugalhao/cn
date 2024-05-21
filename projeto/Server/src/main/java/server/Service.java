package server;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import servicestubs.*;
import servicestubs.Date;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Service extends FunctionalServiceGrpc.FunctionalServiceImplBase {

    private StorageOperations soper;
    private Subscriber subscriber = null;
    private final String PROJECT_ID = "cn2324-t2-g11";
    private final String STORAGE_BUCKET = "cn2324_projeto_g11_europa";
    private final String BLOB_PREFIX = "_id_";

    private final String TOPIC_NAME = "IMAGES";
    private final String SUBSCRIPTION_NAME = "subImages";

    public Service(int svcPort) {

        System.out.println("Service is available on port:" + svcPort);

        ////////////
        StorageOptions storageOptions = StorageOptions.getDefaultInstance();
        Storage storage = storageOptions.getService();
        String projID = storageOptions.getProjectId();
        if (projID != null) System.out.println("Current Project ID:" + projID);
        else {
            System.out.println("The environment variable GOOGLE_APPLICATION_CREDENTIALS isn't well defined!!");
            System.exit(-1);
        }
        soper = new StorageOperations(storage);
        ////////////
        PubSubClient.createNewTopic(TOPIC_NAME);
        PubSubClient.createSubscription(TOPIC_NAME, SUBSCRIPTION_NAME);
        subscriber = PubSubClient.subscribeMessages(PROJECT_ID, SUBSCRIPTION_NAME);
    }


    // recieves a stream of blocks (bytes) of an image, and shows it
    @Override
    public StreamObserver<ImageBlock> uploadImage(StreamObserver<TextMessage> responseObserver) {
        return new StreamObserver<ImageBlock>() {
            String filename;
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            int blockNumber = 0;
            @Override
            public void onNext(ImageBlock imageBlock) {

                filename = imageBlock.getFilename();
                ByteString imageBytes = imageBlock.getBlock().getImage();
                try {
                    imageStream.write(imageBytes.toByteArray());
                    blockNumber++;
                } catch (IOException e) {
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error receiving image: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                try {
                    String filePath = filename;
                    System.out.println("Image received: " + filePath);
                    System.out.println("Number of blocks: " + blockNumber);
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageStream.toByteArray()));
                    String id = filePath;
                    responseObserver.onNext(TextMessage.newBuilder().setTxt(id).build());
                    responseObserver.onCompleted();
//                    displayImage(image);
                    // store in cloud storage
                    soper.uploadBlobToBucketImage(STORAGE_BUCKET, BLOB_PREFIX + filePath, image);

                    Map<String, String> atribs = new HashMap<String, String>();
                    atribs.put("bucket", STORAGE_BUCKET);
                    atribs.put("blob", BLOB_PREFIX + filePath);
                    PubSubClient.publishMessage(TOPIC_NAME, MessageType.PUBLISH.toString(), atribs);
                } catch (Exception e) {
                    responseObserver.onError(e);
                }
            }
        };
    }




    public static void displayImage(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }


    @Override
    public void getImageDetailsById(TextMessage request, StreamObserver<ImageDetails> responseObserver) {
        System.out.println("getImageDetailsById called!");
        String imageId = request.getTxt();
        ImageDetails imageDetails = getImageDetails(imageId);
        responseObserver.onNext(imageDetails);
        responseObserver.onCompleted();
    }

    private ImageDetails getImageDetails(String imageId) {


        ImageDetails.Builder imageDetails = ImageDetails.newBuilder();
        imageDetails.setDate(Date.newBuilder().setDay(1).setMonth(1).setYear(2021).build());
        imageDetails.addLabels(ImageLabel.newBuilder().setName1("label1").setName2("label1_t").build());
        imageDetails.addLabels(ImageLabel.newBuilder().setName1("label2").setName2("label2_t").build());
        return imageDetails.build();
    }


    @Override
    public void downloadImageByFilename(TextMessage request, StreamObserver<ImageBlock> responseObserver) {
        System.out.println("downloadImageByFilename called!");
        String filename = request.getTxt();
        byte[] imageBytes = getImageBytes(filename);
        if (imageBytes == null) {
            responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription("Image not found")));
            return;
        }
        int blockSize = 1024;
        int blockNumber = 0;
        for (int i = 0; i < imageBytes.length; i += blockSize) {
            int end = Math.min(i + blockSize, imageBytes.length);
            byte[] block = new byte[end - i];
            System.arraycopy(imageBytes, i, block, 0, end - i);
            ImageBlock imageBlock = ImageBlock.newBuilder()
                    .setFilename(filename)
                    .setBlock(Block.newBuilder().setImage(ByteString.copyFrom(block)).build())
                    .build();
            responseObserver.onNext(imageBlock);
            blockNumber++;
        }
        responseObserver.onCompleted();
        System.out.println("Number of blocks: " + blockNumber);
    }

    private byte[] getImageBytes(String filename) {
        try {
//            BufferedImage image = ImageIO.read(new ByteArrayInputStream(Files.readAllBytes(Paths.get(filename))));
            BufferedImage image = soper.downloadBlobFromBucket(STORAGE_BUCKET, BLOB_PREFIX + filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String type = filename.substring(filename.lastIndexOf(".") + 1);
            ImageIO.write(image, type, baos);
            baos.flush();
            byte[] imageInBytes = baos.toByteArray();
            baos.close();
            return imageInBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void getFilenamesByLabelAndDate(Condition request, StreamObserver<TextMessage> responseObserver) {
        System.out.println("getFilenamesByLabelAndDate called!");
        String label = request.getLabel();
        Date date1 = request.getDate1();
        Date date2 = request.getDate2();
        ArrayList<String> filenames = getFilenames(label, date1, date2);
        for (String filename : filenames) {
            responseObserver.onNext(TextMessage.newBuilder().setTxt(filename).build());
        }
        responseObserver.onCompleted();
    }

    private ArrayList<String> getFilenames(String label, Date date1, Date date2) {
        ArrayList<String> filenames = new ArrayList<>();
        filenames.add("image1.jpg");
        filenames.add("image2.jpg");
        return filenames;
    }


}
