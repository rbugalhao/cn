package server;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;

import java.util.Map;

public class MessageReceiveHandler implements MessageReceiver {

    @Override
    public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
        System.out.println("Message (Id:" + pubsubMessage.getMessageId()+" Data:"+pubsubMessage.getData().toStringUtf8()+")");
        //System.out.println("Message Data: " + pubsubMessage.getData().toStringUtf8());
        Map<String, String> atribs=pubsubMessage.getAttributesMap();
        for (String key : atribs.keySet())
            System.out.println("Msg Attribute:("+key+", "+atribs.get(key)+")");
        ackReplyConsumer.ack();

    }
}
