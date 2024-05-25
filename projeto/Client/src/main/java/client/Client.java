package client;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import servicestubs.*;
import utils.Utils;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private static String svcIP = "localhost";
    private static int svcPort = 8000;
    private static ManagedChannel channel;
    private static FunctionalServiceGrpc.FunctionalServiceBlockingStub blockingStubFunctional;
    private static FunctionalServiceGrpc.FunctionalServiceStub noBlockStubFunctional;

    private static ManagementServiceGrpc.ManagementServiceBlockingStub blockingStubManagement;
    private static ManagementServiceGrpc.ManagementServiceStub noBlockStubManagement;

    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0];
                svcPort = Integer.parseInt(args[1]);
            }

            System.out.println("Possible IPs of the server:");
            ips();
            System.out.println("Enter the IP of the server:");
            Scanner scan = new Scanner(System.in);
            svcIP = scan.nextLine();

            System.out.println("connect to " + svcIP + ":" + svcPort);
            channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    // Channels are secure by default (via SSL/TLS).
                    // For the example we disable TLS to avoid
                    // needing certificates.
                    .usePlaintext()
                    .build();
            blockingStubFunctional = FunctionalServiceGrpc.newBlockingStub(channel);
            noBlockStubFunctional = FunctionalServiceGrpc.newStub(channel);

            blockingStubManagement = ManagementServiceGrpc.newBlockingStub(channel);
            noBlockStubManagement = ManagementServiceGrpc.newStub(channel);
            // Call service operations for example ping server
            boolean end = false;
            while (!end) {
                menu();
            }
        }
        catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }

    public static void ips() throws IOException, InterruptedException {
        String cfURL = "https://europe-west1-cn2324-t2-g11.cloudfunctions.net/cf-list-group-run-vms";
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cfURL))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() == 200) System.out.println(response.body());
    }

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }

    private static void menu(){
        Scanner scan = new Scanner(System.in);

        System.out.println("------------ Menu --------------");
        System.out.println("1. Upload image");
        System.out.println("2. Get image details by id");
        System.out.println("3. Get filenames of images by date and details");
        System.out.println("4. Download image by filename");
        System.out.println("5. (Management) Change number of server instances");
        System.out.println("6. (Management) Change number of image processing instances");
        System.out.println("99. Exit");
        System.out.println("-------------------------------");
        System.out.println("Select an option: ");
        int op = scan.nextInt();

        switch (op) {
            case 1:
                System.out.println("Upload image");
                uploadImageAsyncronous();
                break;
            case 2:
                System.out.println("Get image details by id");
                getImageDetailsById();
                break;
            case 3:
                System.out.println("Get filenames of images by date and label");
                getFilenamesByLabelAndDate();
                break;
            case 4:
                System.out.println("Download image by filename");
                downloadImageByFilename();
                break;
            case 5:
                System.out.println("(Management) Change number of server instances");
                changeNumberOfServerInstances();
                break;
            case 6:
                System.out.println("(Management) Change number of image processing instances");
                changeNumberOfImageProcessingInstances();
                break;
            case 99:
                System.out.println("Exit");
                System.exit(0);
            default:
                System.out.println("Invalid option");
                break;
        }


    }

    private static void changeNumberOfImageProcessingInstances() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter number of image processing instances: ");
        int num = scan.nextInt();

        StreamObserver<TextMessage> responseObserver = new StreamObserver<TextMessage>() {
            @Override
            public void onNext(TextMessage textMessage) {
                System.out.println("Message: " + textMessage.getTxt());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("All messages received");
            }
        };

        NumberOfInstances numberOfInstances = NumberOfInstances.newBuilder().setNumberOfInstances(num).build();

        noBlockStubManagement.changeNumberOfImageProcessingInstances(numberOfInstances, responseObserver);
    }

    private static void changeNumberOfServerInstances() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter number of server instances: ");
        int num = scan.nextInt();

        StreamObserver<TextMessage> responseObserver = new StreamObserver<TextMessage>() {
            @Override
            public void onNext(TextMessage textMessage) {
                System.out.println("Message: " + textMessage.getTxt());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("All messages received");
            }
        };

        NumberOfInstances numberOfInstances = NumberOfInstances.newBuilder().setNumberOfInstances(num).build();

        noBlockStubManagement.changeNumberOfServerInstances(numberOfInstances, responseObserver);
    }

    private static void getFilenamesByLabelAndDate() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter label: ");
        String label = scan.nextLine();

        System.out.println("Enter start date (dd/mm/yyyy): ");
        String date_start = scan.nextLine();
        int day_start = Integer.parseInt(date_start.substring(0, date_start.indexOf("/")));
        int month_start = Integer.parseInt(date_start.substring(date_start.indexOf("/") + 1, date_start.lastIndexOf("/")));
        int year_start = Integer.parseInt(date_start.substring(date_start.lastIndexOf("/") + 1));

        System.out.println("Enter end date (dd/mm/yyyy): ");
        String date_end = scan.nextLine();
        int day_end = Integer.parseInt(date_end.substring(0, date_end.indexOf("/")));
        int month_end = Integer.parseInt(date_end.substring(date_end.indexOf("/") + 1, date_end.lastIndexOf("/")));
        int year_end = Integer.parseInt(date_end.substring(date_end.lastIndexOf("/") + 1));

        StreamObserver<TextMessage> responseObserver = new StreamObserver<TextMessage>() {
            @Override
            public void onNext(TextMessage textMessage) {
                System.out.println("Image filename: " + textMessage.getTxt());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("All filenames received");
            }
        };

        PublishDate date1 = PublishDate.newBuilder()
                .setDay(day_start)
                .setMonth(month_start)
                .setYear(year_start)
                .build();
        PublishDate date2 = PublishDate.newBuilder()
                .setDay(day_end)
                .setMonth(month_end)
                .setYear(year_end)
                .build();

        Condition condition = Condition.newBuilder()
                .setLabel(label)
                .setDate1(date1)
                .setDate2(date2)
                .build();

        noBlockStubFunctional.getFilenamesByLabelAndDate(condition, responseObserver);
    }


    private static void downloadImageByFilename() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter image filename: ");
        String filename = scan.nextLine();

        StreamObserver<ImageBlock> responseObserver = new StreamObserver<ImageBlock>() {
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            @Override
            public void onNext(ImageBlock imageBlock) {
                try {
                    imageStream.write(imageBlock.getBlock().getImage().toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                String filePath = filename;
                System.out.println("Image received: " + filePath);
                BufferedImage image = null;
                try {
                    image = ImageIO.read(new ByteArrayInputStream(imageStream.toByteArray()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                displayImage(image);
                storeImage(image, filePath);
            }
        };

        noBlockStubFunctional.downloadImageByFilename(TextMessage.newBuilder().setTxt(filename).build(), responseObserver);
    }

    private static void storeImage(BufferedImage image, String filePath) {
        // create image file and stores it
        try {
            String type = filePath.substring(filePath.lastIndexOf(".") + 1);
            ImageIO.write(image, type, Files.newOutputStream(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void displayImage(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void getImageDetailsById() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter image id: ");
        String imageId = scan.nextLine();

        // Asynchronous non-blocking call
        StreamObserver<ImageDetails> responseObserver = new StreamObserver<ImageDetails>() {
            PublishDate date = PublishDate.newBuilder().build();
            ArrayList<ImageLabel> labels = new ArrayList<>();
            @Override
            public void onNext(ImageDetails imageDetails) {
                date = imageDetails.getDate();
                labels.addAll(imageDetails.getLabelsList());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("All topics received");
                int day = date.getDay();
                int month = date.getMonth();
                int year = date.getYear();
                System.out.println("Image date: " + day + "/" + month + "/" + year);
                System.out.println("Labels: ");
                for (ImageLabel label : labels) {
                    System.out.println(label.getName1() + " / " + label.getName2());
                }
            }
        };
        noBlockStubFunctional.getImageDetailsById(TextMessage.newBuilder().setTxt(imageId).build(), responseObserver);

    }


    private static void uploadImageAsyncronous() {
        StreamObserver<TextMessage> responseObserver = new StreamObserver<TextMessage>() {
            @Override
            public void onNext(TextMessage imageDetails) {
                System.out.println("Image uploaded: " + imageDetails.getTxt());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Image upload completed");

            }
        };

        StreamObserver<ImageBlock> requestObserver = noBlockStubFunctional.uploadImage(responseObserver);

        Scanner scan = new Scanner(System.in);
        System.out.println("Enter image path: ");
        String imagePath = scan.nextLine();
        if(imagePath.startsWith("\"") && imagePath.endsWith("\""))
            imagePath = imagePath.substring(1, imagePath.length() - 1);
//        String type = imagePath.substring(imagePath.lastIndexOf(".") + 1);
        System.out.println("Enter image name: ");
        String imageName = scan.nextLine();
//        imageName = imageName + "." + type;

        byte[] imageBytes = Utils.imageToBytes(imagePath);
        ByteString imageByteString = ByteString.copyFrom(imageBytes);

        // divide image into blocks
        int blockSize = 1024;
        int blockCount = (int) Math.ceil((double) imageBytes.length / blockSize);
        System.out.println("blockCount: " + blockCount);
        for (int i = 0; i < blockCount; i++) {
            int start = i * blockSize;
            int end = Math.min((i + 1) * blockSize, imageBytes.length);
            byte[] block = Arrays.copyOfRange(imageBytes, start, end);

            ImageBlock imageBlock = ImageBlock.newBuilder()
                    .setFilename(imageName)
                    .setBlock(
                            Block.newBuilder()
                                    .setImage(ByteString.copyFrom(block))
                                    .build()
                    )
                    .build();

            requestObserver.onNext(imageBlock);
        }

        requestObserver.onCompleted();

    }


}