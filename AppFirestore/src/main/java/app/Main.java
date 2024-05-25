package app;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import csv.CsvHandler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {

    public static String dbName = "projeto";
    public static String collectionName = "image-labels";
    public static String pathCsv = "C:\\cnLab\\cn\\OcupacaoEspacosPublicos.csv";

    public static Firestore db;

    public static void main(String[] args) throws Exception {

        // innit firestore db
        GoogleCredentials credentials =
                GoogleCredentials.getApplicationDefault();
        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId(dbName).setCredentials(credentials)
                .build();
        db = options.getService();


        for(;;){
            menu();
        }



    }

    private static void uploadAllDocs(String pathCsv, Firestore db, String collectionName) throws Exception {
        CsvHandler.insertDocuments(pathCsv, db, collectionName);
    }

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }

    private static void menu() throws Exception {
        System.out.println("=========================================");
        System.out.println("1. Get content by doc id");
        System.out.println("2. Delete a doc field by doc id and field name");
        System.out.println("3. Get all docs with a specific \"freguesia\"");
        System.out.println("4. Get all docs with an");
        System.out.println("      id greater than \"value\",");
        System.out.println("      with a specific \"freguesia\",");
        System.out.println("      and with a specific \"tipo de evento\"");
        System.out.println("5. Get all docs with \"DATA_INICIO\" between \"31/01/2017\" and \"01/03/2017\"");
        System.out.println("6. Get all docs with \"DATA_INICIO\" greater \"31/01/2017\" and \"DATA_FIM\" less than \"01/03/2017\"");
        System.out.println("7. See all docs");
        System.out.println("8. Populate db with csv file");
        System.out.println("=========================================");
        Scanner input = new Scanner(System.in);
        int option = Integer.parseInt(read("Choose an option: ", input));

        switch (option) {
            case 1:
                // Get content by doc id
                getContentById();
                break;
            case 2:
                // Delete a doc field by doc id and field name
                deleteDocFieldByIdAndName();
                break;
            case 3:
                // Get all docs with a specific "freguesia"
                getDocsByFreguesia();
                break;
            case 4:
                // Get all docs with an
                //      id greater than "value",
                //      with a specific "freguesia",
                //      and with a specific "tipo de evento"
                getDocsVerySpecific();
                break;
            case 5:
                // Get all docs with "DATA_INICIO" between "31/01/2017" and "01/03/2017"
                getDocsByDataInicio();
                break;
            case 6:
                // Get all docs with "DATA_INICIO" greater "31/01/2017" and "DATA_FIM" less than "01/03/2017"
                getDocsByMonth();
                break;
            case 7:
                getAllDocs();
                break;
            case 8:
                uploadAllDocs(pathCsv, db, collectionName);
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }

    private static void getAllDocs() throws ExecutionException, InterruptedException {
        CollectionReference cref = db.collection(collectionName);
        Iterable<DocumentReference> allDocs = cref.listDocuments();
        for (DocumentReference docref : allDocs) {
            ApiFuture<DocumentSnapshot> docfut = docref.get();
            DocumentSnapshot doc = docfut.get();
            // Time at which this document was last updated
            Timestamp updateTime = doc.getUpdateTime();
            System.out.println(updateTime + ":doc:" + doc.getData());
        }
    }

    private static void getDocsByMonth() throws ParseException, ExecutionException, InterruptedException {
//        Scanner input = new Scanner(System.in);
//        String d1 = read("Enter the earliest date: ", input);
//        String d2 = read("Enter the latest date: ", input);
        // create datetime objects 31/01/2017 and 01/03/2017
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date1 = formatter.parse("31/01/2017");
        Date date2 = formatter.parse("01/03/2017");

        Query query = db.collection(collectionName)
                .whereGreaterThan("event.dtInicio", date1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            String id = String.valueOf(doc.get("ID"));
            System.out.println(":id:" + id);
            Query query1 = db.collection(collectionName)
                    .whereEqualTo("ID", Integer.parseInt(id))
                    .whereLessThan("event.dtFinal", date2);
            ApiFuture<QuerySnapshot> querySnapshot1 = query1.get();
            for (DocumentSnapshot doc1: querySnapshot1.get().getDocuments()) {
                System.out.println(":doc:" + doc1.getData());
            }

        }

        // Get docs by month
    }

    private static void getDocsByDataInicio() throws ExecutionException, InterruptedException, ParseException {
//        Scanner input = new Scanner(System.in);
//        String d1 = read("Enter the earliest date: ", input);
//        String d2 = read("Enter the latest date: ", input);
        // create datetime objects 31/01/2017 and 01/03/2017
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date1 = formatter.parse("31/01/2017");
        Date date2 = formatter.parse("01/03/2017");


        // Get docs by data inicio
        // Get docs very specific
        Query query = db.collection(collectionName)
                .whereGreaterThan("event.dtInicio", date1)
                .whereLessThan("event.dtInicio", date2);
        // retrieve query results asynchronously using query.get()
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.println("--------------------");
            showDocs(doc);
        }
    }

    private static void getDocsVerySpecific() throws ExecutionException, InterruptedException {
        Scanner input = new Scanner(System.in);
        String freguesia = read("Enter the freguesia: ", input);
        String tipo = read("Enter the tipo de evento: ", input);
        String value = read("Enter the value: ", input);
        // Get docs very specific
        Query query = db.collection(collectionName)
                .whereEqualTo("location.freguesia", freguesia)
                .whereEqualTo("event.tipo", tipo)
                .whereGreaterThan("ID", Integer.parseInt(value));
        // retrieve query results asynchronously using query.get()
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.println("--------------------");
            showDocs(doc);
        }
    }

    private static void getDocsByFreguesia() throws ExecutionException, InterruptedException {
        Scanner input = new Scanner(System.in);
        String freguesia = read("Enter the freguesia: ", input);
        // Get docs by freguesia
        Query query = db.collection(collectionName)
                .whereEqualTo("location.freguesia", freguesia);
        // retrieve query results asynchronously using query.get()
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot doc: querySnapshot.get().getDocuments()) {
            System.out.println("--------------------");
            showDocs(doc);
        }
    }

    private static void deleteDocFieldByIdAndName() throws ExecutionException, InterruptedException {
        Scanner input = new Scanner(System.in);
        String docId = read("Enter the document id: ", input);
        String fieldName = read("Enter the field name: ", input);
        // Delete doc field by doc id and field name
        DocumentReference docRef = db.collection(collectionName).document(docId);
        // apagar campo
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldName, FieldValue.delete());
        ApiFuture<WriteResult> writeResult = docRef.update(updates);
        System.out.println("Update time : " + writeResult.get());
    }

    private static void getContentById() throws ExecutionException, InterruptedException {
        Scanner input = new Scanner(System.in);
        String docId = read("Enter the document id: ", input);

        DocumentReference docRef = db.collection(collectionName).document(docId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        document.get("ID");
        showDocs(document);

    }

    private static void showDocs(DocumentSnapshot document) {
        System.out.println("DocID: " + document.getId());
        System.out.println("coord.X: " + document.get("location.coord.X"));
        System.out.println("coord.Y: " + document.get("location.coord.Y"));
        System.out.println("local: " + document.get("location.local"));
        System.out.println("Freguesia: " + document.get("location.freguesia"));
        System.out.println("EvtID: " + document.get("event.evtID"));
        System.out.println("Nome: " + document.get("event.nome"));
        System.out.println("Tipo: " + document.get("event.tipo"));
        System.out.println("Participantes: " + document.get("event.details.Participantes"));
        System.out.println("Custo: " + document.get("event.details.Custo"));
        System.out.println("Data Inicio: " + document.get("event.dtInicio"));
        System.out.println("Data Fim: " + document.get("event.dtFinal"));
        System.out.println("Licenciamento Code: " + document.get("event.licenciamento.code"));
        System.out.println("Licenciamento Date: " + document.get("event.licenciamento.dtLicenc"));
    }

}



