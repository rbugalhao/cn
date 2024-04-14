package grpcclientapp;//package grpcclientapp;

import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import servicestubs.ForumMessage;

public class EvenNumbersStream implements StreamObserver<ForumMessage> {
    boolean completed=false;

    @Override
    public void onNext(ForumMessage forumMessage) {
        System.out.println("- On "+ forumMessage.getTopicName() + ", "+ forumMessage.getFromUser() +" says: " +forumMessage.getTxtMsg());
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
}
