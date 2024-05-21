package server;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.util.concurrent.ExecutionException;

public class FirestoreClient {


    public static DocumentSnapshot getContentById(Firestore db, String collectionName, String docId) throws ExecutionException, InterruptedException {

        DocumentReference docRef = db.collection(collectionName).document(docId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
//        document.get("ID");
        return document;
    }

}
