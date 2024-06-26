package server;

import com.google.cloud.BatchResult;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class StorageOperations {

    Storage storage = null;

    public StorageOperations(Storage storage) {
        this.storage = storage;
    }

//    StorageClass getStorageClass() {
//        Scanner scan = new Scanner(System.in);
//        StorageClass[] CLASSES = new StorageClass[]{
//                StorageClass.STANDARD,
//                StorageClass.NEARLINE,
//                StorageClass.COLDLINE,
//                StorageClass.ARCHIVE
//        };
//        int option;
//        do {
//            System.out.println("Options for Google Storage Class:");
//            System.out.println("1: STANDARD");
//            System.out.println("2: NEARLINE");
//            System.out.println("3: COLDLINE");
//            System.out.println("4: ARCHIVE");
//            System.out.print("Choose an option: ");
//            option = scan.nextInt();
//        } while (!(option > 0 && option <= 4));
//        return CLASSES[option - 1];
//    }
//
//    String getLocation() {
//        Scanner scan = new Scanner(System.in);
//        String[] LOCALS = new String[]{
//                "northamerica-northeast1", "us-central1", "us-east1", "us-east4", "us-west1", "us-west2",
//                "southamerica-east1",
//                "europe-north1", "europe-west1", "europe-west2", "europe-west3", "europe-west4", "europe-west6",
//                "asia-east1", "asia-east2", "asia-northeast1", "asia-south1", "asia-southeast1",
//                "australia-southeast1", "asia", "eu", "us", "EUR4","NAM4"
//        };
//        int option;
//        do {
//            System.out.println("Region names:");
//            System.out.println("North America:");
//            System.out.println("1:northamerica-northeast1	 (Montréal)");
//            System.out.println("2:us-central1	(Iowa)");
//            System.out.println("3:us-east1	(South Carolina)");
//            System.out.println("4: us-east4	(Northern Virginia)");
//            System.out.println("5:us-west1	(Oregon)");
//            System.out.println("6:us-west2	(Los Angeles)");
//            System.out.println("South America:");
//            System.out.println("7:southamerica-east1	(São Paulo)");
//            System.out.println("Europe");
//            System.out.println("8:europe-north1	(Finland)");
//            System.out.println("9:europe-west1	(Belgium)");
//            System.out.println("10:europe-west2	(London)");
//            System.out.println("11:europe-west3	(Frankfurt)");
//            System.out.println("12:europe-west4	(Netherlands)");
//            System.out.println("13:europe-west6	(Zürich)");
//            System.out.println("Asia:");
//            System.out.println("14:asia-east1	(Taiwan)");
//            System.out.println("15:asia-east2	(Hong Kong)");
//            System.out.println("16:asia-northeast1	(Tokyo)");
//            System.out.println("17:asia-south1	(Mumbai)");
//            System.out.println("18:asia-southeast1	(Singapore)");
//            System.out.println("Australia:");
//            System.out.println("19:australia-southeast1	(Sydney)");
//            System.out.println("Multi-regional locations:");
//            System.out.println("20:asia	(Data centers in Asia)");
//            System.out.println("21:eu	 (Data centers in the European Union1)");
//            System.out.println("22:us	 (Data centers in the United States)");
//            System.out.println("Dual-region locations:");
//            System.out.println("23: EUR4 	EUROPE-NORTH1 and EUROPE-WEST4");
//            System.out.println("24: NAM4 	US-CENTRAL1 and US-EAST1");
//            System.out.print("Choose an option: ");
//            option = scan.nextInt();
//        } while (!(option > 0 && option <= 24));
//        return LOCALS[option - 1];
//    }

//    public void createBucket() throws Exception {
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Enter the name of the Bucket? ");
//        String bucketName = scan.nextLine();
//        Bucket bucket = storage.create(
//                BucketInfo.newBuilder(bucketName)
//                        // See here for possible values: http://g.co/cloud/storage/docs/storage-classes
//                        .setStorageClass(getStorageClass())
//                        // Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
//                        .setLocation(getLocation())
//                        .build());
//    }
//
//    public void deleteBucket() throws Exception {
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Enter the name of the Bucket to delete? ");
//        String bucketName = scan.nextLine();
//        Bucket bucket = storage.get(bucketName);
//        bucket.delete();
//    }
//
//    public void listBuckets(String projID) {
//        System.out.println("Buckets in Project=" + projID + ":");
//        for (Bucket bucket : storage.list().iterateAll()) {
//            System.out.println("  " + bucket.toString());
//            for (Blob blob : bucket.list().iterateAll()) {
//                System.out.println("      " + blob.toString());
//            }
//
//        }
//    }
//
//    public void uploadBlobToBucket() throws Exception {
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Enter the name of the Bucket? ");
//        String bucketName = scan.nextLine();
//        System.out.println("Enter the name of the Blob? ");
//        String blobName = scan.nextLine();
//        System.out.println("Enter the pathname of the file to upload? ");
//        String absFileName = scan.nextLine();
//        Path uploadFrom = Paths.get(absFileName);
//        String contentType = Files.probeContentType(uploadFrom);
//        BlobId blobId = BlobId.of(bucketName, blobName);
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
//        if (Files.size(uploadFrom) > 1_000_000) {
//            // When content is not available or large (1MB or more) it is recommended
//            // to write it in chunks via the blob's channel writer.
//            try (WriteChannel writer = storage.writer(blobInfo)) {
//                byte[] buffer = new byte[1024];
//                try (InputStream input = Files.newInputStream(uploadFrom)) {
//                    int limit;
//                    while ((limit = input.read(buffer)) >= 0) {
//                        try {
//                            writer.write(ByteBuffer.wrap(buffer, 0, limit));
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                }
//            }
//        } else {
//            byte[] bytes = Files.readAllBytes(uploadFrom);
//            // create the blob in one request.
//            storage.create(blobInfo, bytes);
//        }
//        System.out.println("Blob " + blobName + " created in bucket " + bucketName);
//    }
//
//    public void uploadBlobToBucket(String bucketName, String blobName, byte[] bytes) throws IOException {
//        BlobId blobId = BlobId.of(bucketName, blobName);
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build();
//
//        // When content is not available or large (1MB or more) it is recommended
//        // to write it in chunks via the blob's channel writer.
//        try (WriteChannel writer = storage.writer(blobInfo)) {
//            ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, 1_000_000);
//            while (buffer.hasRemaining()) {
//                writer.write(buffer);
//            }
//        }
//
//        System.out.println("Blob " + blobName + " created in bucket " + bucketName);
//    }
//
//    public void uploadBlobToBucketImage(String bucketName, String blobName, BufferedImage bufferedImage) throws IOException {
//        // Convert BufferedImage to byte array
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        String formatName = blobName.substring(blobName.lastIndexOf('.') + 1);
//        ImageIO.write(bufferedImage, formatName, baos);
//        byte[] imageBytes = baos.toByteArray();
//
//        // Create BlobId and BlobInfo
//        BlobId blobId = BlobId.of(bucketName, blobName);
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/"+formatName).build();
//
//        if (imageBytes.length > 1_000_000) {
//            // Write image in chunks if the size is larger than 1MB
//            try (WriteChannel writer = storage.writer(blobInfo)) {
//                ByteBuffer buffer = ByteBuffer.wrap(imageBytes, 0, 1_000_000);
//                while (buffer.hasRemaining()) {
//                    writer.write(buffer);
//                }
//            }
//        } else {
//            // Create the blob in one request if the size is less than 1MB
//            storage.create(blobInfo, imageBytes);
//        }
//
//        System.out.println("Blob " + blobName + " created in bucket " + bucketName);
//    }
//
//    public void downloadBlobFromBucket() throws IOException {
//        Scanner scan = new Scanner(System.in);
//        System.out.println("The name of Bucket? ");
//        String bucketName = scan.nextLine();
//        System.out.println("The name of Blob? ");
//        String blobName = scan.nextLine();
//        System.out.println("What is the file pathname for downloading the Blob? ");
//        String absFileName = scan.nextLine();
//        Path downloadTo = Paths.get(absFileName);
//        //System.out.println("download to: "+downloadTo);
//        BlobId blobId = BlobId.of(bucketName, blobName);
//        Blob blob = storage.get(blobId);
//        if (blob == null) {
//            System.out.println("No such Blob exists !");
//            return;
//        }
//        PrintStream writeTo = new PrintStream(Files.newOutputStream(downloadTo));
//        if (blob.getSize() < 1_000_000) {
//            // Blob is small read all its content in one request
//            byte[] content = blob.getContent();
//            writeTo.write(content);
//        } else {
//            // When Blob size is big or unknown use the blob's channel reader.
//            try (ReadChannel reader = blob.reader()) {
//                WritableByteChannel channel = Channels.newChannel(writeTo);
//                ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
//                while (reader.read(bytes) > 0) {
//                    bytes.flip();
//                    channel.write(bytes);
//                    bytes.clear();
//                }
//            }
//        }
//        writeTo.close();
//        System.out.println("Blob " + blobName + " downloaded to " + downloadTo);
//    }

    public BufferedImage downloadBlobFromBucket(String bucketName, String blobName) throws IOException {
//        Scanner scan = new Scanner(System.in);
//        System.out.println("What is the file pathname for downloading the Blob? ");
//        String absFileName = scan.nextLine();
//        Path downloadTo = Paths.get(absFileName);
        //System.out.println("download to: "+downloadTo);
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            System.out.println("No such Blob exists !");
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (blob.getSize() < 1_000_000) {
            // Blob is small read all its content in one request
            byte[] content = blob.getContent();
            baos.write(content);
        } else {
            // When Blob size is big or unknown use the blob's channel reader.
            try (ReadChannel reader = blob.reader()) {
                WritableByteChannel channel = Channels.newChannel(baos);
                ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
                while (reader.read(bytes) > 0) {
                    bytes.flip();
                    channel.write(bytes);
                    bytes.clear();
                }
            }
        }
        return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }

    // TODO: Develop other Operations. Some of them in slides

//    private Bucket getBucket(String bucketName) {
//        return storage.get(bucketName);
//    }
//    public void deleteBlobFromBucket() {
//        Scanner scan = new Scanner(System.in);
//        System.out.println("The name of Bucket? ");
//        String bucketName = scan.nextLine();
//        Bucket bucket = getBucket(bucketName);
//        if (bucket == null) {
//            System.out.println("No such Bucket exists !");
//            return;
//        }
//        StorageBatch batch = storage.batch();
//        for (Blob blob : bucket.list().iterateAll()) {
//            final String blobName = blob.getName();
//            batch.delete(blob.getBlobId()).notify(
//                    new BatchResult.Callback<Boolean, StorageException>() {
//                        @Override
//                        public void success(Boolean aBoolean) {
//                            System.out.println(blobName+" was deleted");
//                        }
//                        @Override
//                        public void error(StorageException e) {
//                            System.out.println(blobName+" Error!!!");
//                        }
//                    }
//            );
//        }
//        batch.submit();
//    }
//
//    public void changeBlobPermission() {
//        Scanner scan = new Scanner(System.in);
//        System.out.println("The name of Bucket? ");
//        String bucketName = scan.nextLine();
//        System.out.println("The name of Blob? ");
//        String blobName = scan.nextLine();
//        BlobId blobId = BlobId.of(bucketName, blobName);
//        Blob blob = storage.get(blobId);
//        if (blob == null) {
//            System.out.println("No such Blob exists !");
//            return;
//        }
//        Acl.Entity aclEnt = Acl.User.ofAllUsers();
//        Acl.Role[] roles = Acl.Role.values();
//
//        System.out.println("Choose a permission:");
//        for (int i = 0; i < roles.length; i++) {
//            System.out.println(i + ": " + roles[i]);
//        }
//        int option = scan.nextInt();
//        if (option < 0 || option >= roles.length) {
//            System.out.println("Invalid option");
//            return;
//        }
//        Acl.Role role = roles[option];
//        Acl acl = Acl.newBuilder(aclEnt, role).build();
//        blob.createAcl(acl);
//        System.out.println("Blob " + blobName + " permissions changed");
//    }

    public boolean blobExists(String bucketName, String blobName) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);
        return blob != null;
    }
}
