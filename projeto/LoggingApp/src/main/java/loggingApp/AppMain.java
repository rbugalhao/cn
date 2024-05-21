package loggingApp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.pubsub.v1.Subscriber;

import java.io.IOException;
import java.util.Scanner;

public class AppMain {

    private final static String PROJECT_ID = "cn2324-t2-g11";

    private final static String TOPIC_NAME = "IMAGES";
    private final static String SUBSCRIPTION_NAME = "subLog";

    private final static String dbName = "projeto";
    public final static String collectionName = "logs";
    public static Firestore db;

    public static void main(String[] args) throws IOException {
        PubSubClient.createSubscription(TOPIC_NAME, SUBSCRIPTION_NAME);
        Subscriber subscriber = PubSubClient.subscribeMessages(PROJECT_ID, SUBSCRIPTION_NAME);

        // innit firestore db
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId(dbName).setCredentials(credentials)
                .build();
        db = options.getService();

        Scanner scan = new Scanner(System.in);
        System.out.println("Write 'end' to exit");
        String input = scan.nextLine();
        while (!input.equals("end")) {
            input = scan.nextLine();
        }
        subscriber.stopAsync();
        subscriber.awaitTerminated();
        System.out.println("Exiting...");
    }

}
