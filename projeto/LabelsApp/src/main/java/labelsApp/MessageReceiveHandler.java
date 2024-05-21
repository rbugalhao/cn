package labelsApp;

import children.Image;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class MessageReceiveHandler implements MessageReceiver {
    //        "gs://" + bucketName + "/" + blobName

    @Override
    public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
        System.out.println("\nMessage (Id:" + pubsubMessage.getMessageId()+" Data:"+pubsubMessage.getData().toStringUtf8()+")");
        //System.out.println("Message Data: " + pubsubMessage.getData().toStringUtf8());
        Map<String, String> atribs=pubsubMessage.getAttributesMap();
        for (String key : atribs.keySet())
            System.out.println("Msg Attribute:("+key+", "+atribs.get(key)+")");



        switch (pubsubMessage.getData().toStringUtf8()){
            case "PUBLISH":
                System.out.println("PUBLISH");
                publish(pubsubMessage);
                break;

            default:
                System.out.println("Invalid message");
                break;
        }

        ackReplyConsumer.ack();

    }


    private void publish(PubsubMessage pubsubMessage){

        Map<String, String> atribs=pubsubMessage.getAttributesMap();

        List<String> labels = null;
        try {
            labels = Utils.detectLabels("gs://" + atribs.get("bucket") + "/" + atribs.get("blob"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> translatedLabels = Utils.TranslateLabels(labels);

        labels.forEach(label -> {
            System.out.println("Label detected:" + label);
        });
        translatedLabels.forEach(label -> {
            System.out.println("Translated Label detected:" + label);
        });

        // add lables to firestore
        Image img = new Image();
        img.bucket = atribs.get("bucket");
        img.blob = atribs.get("blob");
        img.labels = labels;
        img.translatedLabels = translatedLabels;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            img.date = formatter.parse(getCurrentDate());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        try {
            FirestoreClient.insertDocument(AppMain.db, AppMain.collectionName, img);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date date = new java.util.Date();
        return formatter.format(date);
    }


}
