package labelsApp;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;

import java.io.IOException;
import java.util.ArrayList;
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

    }

}
