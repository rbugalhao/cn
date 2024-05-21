package loggingApp;

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


        try {
            publish(pubsubMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        ackReplyConsumer.ack();

    }


    private void publish(PubsubMessage pubsubMessage) throws Exception {

        LogObj logObj = new LogObj();
        logObj.id = pubsubMessage.getMessageId();
        logObj.message = pubsubMessage.getData().toStringUtf8();
        logObj.attributes = pubsubMessage.getAttributesMap();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        logObj.date = formatter.parse(getCurrentDate());

        FirestoreClient.insertDocument(AppMain.db, AppMain.collectionName, logObj);

    }

    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date date = new java.util.Date();
        return formatter.format(date);
    }


}
