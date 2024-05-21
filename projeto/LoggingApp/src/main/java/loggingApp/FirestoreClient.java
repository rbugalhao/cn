package loggingApp;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.pubsub.v1.PubsubMessage;

public class FirestoreClient {


    public static void insertDocument(Firestore db, String collectionName, LogObj logObj) throws Exception {
        CollectionReference colRef = db.collection(collectionName);
        DocumentReference docRef = colRef.document(logObj.id);
        ApiFuture<WriteResult> resultFut = docRef.set(logObj);
        WriteResult result = resultFut.get();
        System.out.println("Update time : " + result.getUpdateTime());
    }


}
