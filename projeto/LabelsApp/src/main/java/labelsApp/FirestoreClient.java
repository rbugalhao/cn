package labelsApp;

import children.Image;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class FirestoreClient {


    public static void insertDocument(Firestore db, String collectionName, Image image) throws Exception {
        CollectionReference colRef = db.collection(collectionName);
        DocumentReference docRef = colRef.document(image.blob);
        ApiFuture<WriteResult> resultFut = docRef.set(image);
        WriteResult result = resultFut.get();
        System.out.println("Update time : " + result.getUpdateTime());
    }


}
