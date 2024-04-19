package grpcclientapp;//package grpcclientapp;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import servicestubs.ForumMessage;
import storage.StorageOperations;

import java.io.IOException;

public class EvenNumbersStream implements StreamObserver<ForumMessage> {
    boolean completed=false;

    @Override
    public void onNext(ForumMessage forumMessage) {
        System.out.println("- On "+ forumMessage.getTopicName() + ", "+ forumMessage.getFromUser() +" says: " +forumMessage.getTxtMsg());

        try {
            downloadMessage(forumMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onError(Throwable throwable) {
        System.out.println("Completed with error:"+throwable.getMessage());
        completed=true;
    }
    @Override
    public void onCompleted() {
        System.out.println("Even numbers completed");
        completed=true;
    }

    public boolean isCompleted() {
        return completed;
    }

    private void downloadMessage(ForumMessage forumMessage) throws IOException {

        // check if msg has <texto>[;<bucketName>;<blobName>] format
        // part the message by the [; and ] separator
        String[] parts = forumMessage.getTxtMsg().split("\\[;");
        if (parts.length == 2) {
            String[] parts2 = parts[1].split(";");
            if (parts2.length == 2) {
                StorageOptions storageOptions = StorageOptions.getDefaultInstance();
                Storage storage = storageOptions.getService();
                String projID = storageOptions.getProjectId();
                if (projID != null) System.out.println("Current Project ID:" + projID);
                else {
                    System.out.println("The environment variable GOOGLE_APPLICATION_CREDENTIALS isn't well defined!!");
                    System.exit(-1);
                }
                StorageOperations soper = new StorageOperations(storage);
                String bucketName = parts2[0];
                String blobName = parts2[1].substring(0, parts2[1].length() - 1);
                System.out.println("Bucket Name: " + bucketName + " Blob Name: " + blobName);
                // call the storage operation to download the blob
                soper.downloadBlobFromBucket(bucketName, blobName);
            }
        }
    }
}
