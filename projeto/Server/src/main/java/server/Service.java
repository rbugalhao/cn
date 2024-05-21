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

public class Service extends FunctionalServiceGrpc.FunctionalServiceImplBase {

    private StorageOperations soper;
    public static Subscriber subscriber = null;
    private final String PROJECT_ID = "cn2324-t2-g11";
    private final String STORAGE_BUCKET = "cn2324_projeto_g11_europa";
    private final String BLOB_PREFIX = "_id_";

    private final String TOPIC_NAME = "IMAGES";
    private final String SUBSCRIPTION_NAME = "subImages";

    private final static String dbName = "projeto";
    public final static String collectionName = "image-labels";
    public static Firestore db;

    public Service(int svcPort) throws IOException {

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
        ////////////
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId(dbName).setCredentials(credentials)
                .build();
        db = options.getService();

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
                    String id = BLOB_PREFIX + filePath;
                    responseObserver.onNext(TextMessage.newBuilder().setTxt(id).build());
                    responseObserver.onCompleted();
//                    displayImage(image);
                    // store in cloud storage
                    soper.uploadBlobToBucketImage(STORAGE_BUCKET, BLOB_PREFIX + filePath, image);
                    // send mensage to create labels
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

        DocumentSnapshot doc = null;
        try {
            doc = FirestoreClient.getContentById(db, collectionName, imageId);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date dateObj = doc.getDate("date");
        String date = formatter.format(dateObj);

        List<String> labels = (List<String>) doc.get("labels");
        List<String> translatedLabels = (List<String>) doc.get("translatedLabels");

        System.out.println("date: " + date);

        int day = Integer.parseInt(date.substring(0, date.indexOf("/")));
        int month = Integer.parseInt(date.substring(date.indexOf("/") + 1, date.lastIndexOf("/")));
        int year = Integer.parseInt(date.substring(date.lastIndexOf("/") + 1));

        System.out.println("Document data: " + doc.getData());

        ImageDetails.Builder imageDetails = ImageDetails.newBuilder();
        imageDetails.setDate(PublishDate.newBuilder().setDay(day).setMonth(month).setYear(year).build());

        for (int i = 0; i < labels.size(); i++) {
            imageDetails.addLabels(ImageLabel.newBuilder().setName1(labels.get(i)).setName2(translatedLabels.get(i)).build());
        }

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
            BufferedImage image = soper.downloadBlobFromBucket(STORAGE_BUCKET, filename);
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
        PublishDate date1 = request.getDate1();
        PublishDate date2 = request.getDate2();
        ArrayList<String> filenames = null;
        try {
            filenames = getFilenames(label, date1, date2);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        for (String filename : filenames) {
            responseObserver.onNext(TextMessage.newBuilder().setTxt(filename).build());
        }
        responseObserver.onCompleted();
    }

    private ArrayList<String> getFilenames(String label, PublishDate date1, PublishDate date2) throws ParseException {

        ArrayList<String> filenames = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date_i = formatter.parse(date1.getDay()+"/"+date1.getMonth()+"/"+date1.getYear());
        Date date_f = formatter.parse(date2.getDay()+"/"+date2.getMonth()+"/"+date2.getYear());

        // verificar labels
        Query query1 = db.collection(collectionName)
                .whereArrayContains("labels", label)
                .whereGreaterThanOrEqualTo("date", date_i)
                .whereLessThanOrEqualTo("date", date_f);

        ApiFuture<QuerySnapshot> querySnapshot1 = query1.get();
        QuerySnapshot queryResult1 = null;
        try {
            queryResult1 = querySnapshot1.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        for (QueryDocumentSnapshot document : queryResult1) {
            filenames.add(document.getId());
        }

        //verificar translatedLabels
        Query query2 = db.collection(collectionName)
                .whereArrayContains("translatedLabels", label)
                .whereGreaterThanOrEqualTo("date", date_i)
                .whereLessThanOrEqualTo("date", date_f);

        ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();
        QuerySnapshot queryResult2 = null;
        try {
            queryResult2 = querySnapshot2.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        for (QueryDocumentSnapshot document : queryResult2) {
            if (!filenames.contains(document.getId()))
                filenames.add(document.getId());
        }

//        filenames.add("image1.jpg");
//        filenames.add("image2.jpg");
        return filenames;
    }


}
