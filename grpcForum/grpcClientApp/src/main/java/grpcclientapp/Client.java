package grpcclientapp;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import servicestubs.*;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Client {
    // generic ClientApp for Calling a grpc Service
    private static String svcIP = "localhost";
    private static int svcPort = 8000;
    private static ManagedChannel channel;
    private static ForumGrpc.ForumBlockingStub blockingStub;
    private static ForumGrpc.ForumStub noBlockStub;



    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0]; svcPort = Integer.parseInt(args[1]);
            }
            System.out.println("connect to " + svcIP + ":" + svcPort);
            channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    // Channels are secure by default (via SSL/TLS).
                    // For the example we disable TLS to avoid
                    // needing certificates.
                    .usePlaintext()
                    .build();
            blockingStub = ForumGrpc.newBlockingStub(channel);
            noBlockStub = ForumGrpc.newStub(channel);
            // Call service operations for example ping server
            boolean end = false;
            while (!end) {
                try {

                    User user = MenuInitial();
                    if (user == null) System.exit(0);

                    Menu(user);

                } catch (Exception ex) {
                    System.out.println("Execution call Error  !");
                    ex.printStackTrace();
                }
            }
            read("prima enter to end", new Scanner(System.in));
        } catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }

    private static User loginCall() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter your username:");
        String username = scan.nextLine();
        System.out.println("Enter your password:");
        String password = scan.nextLine();
        User user = User.newBuilder()
                .setUsrName(username)
                .setPassword(password)
                .build();
        Valid reply = blockingStub.loginUser(user);
        System.out.println("Server response: " + reply.getValid());
        if (reply.getValid()) return user;
        return null;
    }

    private static void registerCall() {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter your username:");
        String username = scan.nextLine();
        System.out.println("Enter your password:");
        String password = scan.nextLine();
        User user = User.newBuilder()
                .setUsrName(username)
                .setPassword(password)
                .build();
        Valid reply = blockingStub.registerUser(user);
        System.out.println("Server response: " + reply.getValid());
    }


    static void subscribeAsynchronousCall(User user) {
        // Asynchronous non-blocking call
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the topic you want to subscribe to:");
        String topic = scan.nextLine();
        SubscribeUnSubscribe request = SubscribeUnSubscribe.newBuilder()
                .setUsrName(user.getUsrName())
                .setTopicName(topic)
                .build();
        StreamObserver<ForumMessage> responseObserver = new StreamObserver<ForumMessage>() {
            @Override
            public void onNext(ForumMessage forumMessage) {
                System.out.println("Received message: " + forumMessage.getTxtMsg());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Subscription completed");
            }
        };
        noBlockStub.topicSubscribe(request, responseObserver);
    }

    static void unsubscribeAsynchronousCall(User user) {
        // Asynchronous non-blocking call
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the topic you want to unsubscribe from:");
        String topic = scan.nextLine();
        SubscribeUnSubscribe request = SubscribeUnSubscribe.newBuilder()
                .setUsrName(user.getUsrName())
                .setTopicName(topic)
                .build();
        StreamObserver<ForumMessage> responseObserver = new StreamObserver<ForumMessage>() {
            @Override
            public void onNext(ForumMessage forumMessage) {
                System.out.println("Received message: " + forumMessage.getTxtMsg());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Unsubscription completed");
            }
        };
        noBlockStub.topicUnSubscribe(request, responseObserver);
    }

    static void showAllTopicsAsynchronousCall() {
        // Asynchronous non-blocking call
        StreamObserver<ExistingTopics> responseObserver = new StreamObserver<ExistingTopics>() {
            @Override
            public void onNext(ExistingTopics existingTopics) {
                System.out.println("---- All topics ----");
                for(int i=0; i<existingTopics.getTopicNameCount(); i++) {
                    System.out.println(existingTopics.getTopicName(i));
                }
                System.out.println("-----------------------");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("All topics received");
            }
        };
        noBlockStub.getAllTopics(Empty.newBuilder().build(), responseObserver);
    }

    static void showMyTopicsAsynchronousCall(User user) {
        // Asynchronous non-blocking call


        StreamObserver<ExistingTopics> responseObserver = new StreamObserver<ExistingTopics>() {
            @Override
            public void onNext(ExistingTopics existingTopics) {
                System.out.println("---- My topics ----");
                for(int i=0; i<existingTopics.getTopicNameCount(); i++) {
                    System.out.println(existingTopics.getTopicName(i));
                }
                System.out.println("-----------------------");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("All topics received");
            }
        };
        noBlockStub.getSubscribedTopics(user, responseObserver);
    }

    static void showMessagesByUserTopicsAsynchronousCall(User user) {
        // Asynchronous non-blocking call
        StreamObserver<ForumMessage> responseObserver = new StreamObserver<ForumMessage>() {
            @Override
            public void onNext(ForumMessage forumMessage) {
                System.out.println("On " + forumMessage.getTopicName() + ", " + forumMessage.getFromUser() +" says: " + forumMessage.getTxtMsg());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("All messages received");
            }
        };
        noBlockStub.getMessages(user, responseObserver);
    }

    static void publishMessageAsynchronousCall(User user) {
        // Asynchronous non-blocking call
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the topic you want to publish to:");
        String topic = scan.nextLine();
        System.out.println("Enter the message you want to publish:");
        String message = scan.nextLine();
        ForumMessage request = ForumMessage.newBuilder()
                .setFromUser(user.getUsrName())
                .setTopicName(topic)
                .setTxtMsg(message)
                .build();
        StreamObserver<Valid> responseObserver = new StreamObserver<Valid>() {
            @Override
            public void onNext(Valid valid) {
                if (valid.getValid()) System.out.println("Message published");
                else System.out.println("Message not published");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Message publishing completed");
            }
        };
        noBlockStub.publishMessage(request, responseObserver);
    }

//    static void findPrimesAsynchronousCall() throws InterruptedException {
//        // Asynchronous non-blocking call
//        System.out.println("Lets find the primes between 1 and 500.");
//        EvenNumbersStream evenStream = new EvenNumbersStream();
//        noBlockStub.findPrimes(IntervalNumbers.newBuilder().setStart(1).setEnd(500).build(), evenStream);
//        while (!evenStream.isCompleted()) {
//            System.out.println("Continue working until receive all even numbers");
//            Thread.sleep(1000); // Simulate processing time (1 seg)
//        }
//    }
//
//
//    static void isAliveCall() {
//        //ping server
//        TextMessage reply = blockingStub.isAlive(ProtoVoid.newBuilder().build());
//        System.out.println("Ping server:" + reply.getTxt());
//    }
//
//    static void getNevenNumbersSynchronousCall() { // get N even numbers
//        //Synchronous blocking call
//        try {
//            int N = Integer.parseInt(read("How many even numbers?", new Scanner(System.in)));
//            IntNumber intNumber=IntNumber.newBuilder().setIntnumber(N).build();
//            Iterator<IntNumber> iterator = blockingStub.getEvenNumbers(intNumber);
//            while (iterator.hasNext()) {
//                System.out.println("more one even number: " + iterator.next().getIntnumber());
//            }
//        } catch(Exception ex) {
//            System.out.println("Synchronous call error: "+ex.getMessage());
//        }
//    }
//
//    static void getNevenNumbersAsynchronousCall() throws InterruptedException { // get N even numbers
//        // Asynchronous non blocking call
//        int N = Integer.parseInt(read("How many even numbers?", new Scanner(System.in)));
//        EvenNumbersStream evenStream = new EvenNumbersStream();
//        noBlockStub.getEvenNumbers(IntNumber.newBuilder().setIntnumber(N).build(), evenStream);
//        while (!evenStream.isCompleted()) {
//            System.out.println("Continue working until receive all even numbers");
//            Thread.sleep(1000); // Simulate processing time (1 seg)
//        }
//    }
//
//    static void addSequenceOfNumbersCall() {
//        // Add sequence of numbers
//        int N = Integer.parseInt(read("Enter N?", new Scanner(System.in)));
//        StreamObserver<IntNumber> streamNumbers = noBlockStub.addSeqOfNumbers(new StreamObserver<IntNumber>() {
//            @Override
//            public void onNext(IntNumber intNumber) {
//                System.out.println("Add total:" + intNumber.getIntnumber());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                System.out.println(throwable.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("Add sequence completed");
//            }
//        });
//        for (int i = 1; i <= N; i++) { // send N numbers
//            streamNumbers.onNext(IntNumber.newBuilder().setIntnumber(i).build());
//            System.out.println("sent number "+i);
//        }
//        streamNumbers.onCompleted();
//        // Note that client has sent all requests, but needs synchronization
//        // to terminate after get the final result
//    }
//
//    static void bidirectionalStreamingCall() {
//        // bidirectional streaming - Do multiple add operations
//        StreamObserver<AddOperands> streamDoAddOperations = noBlockStub.multipleAdd(new StreamObserver<AddResult>() {
//            @Override
//            public void onNext(AddResult addResult) {
//                System.out.println("Add Result ID(" + addResult.getAddID() + ")=" + addResult.getResult());
//            }
//            @Override
//            public void onError(Throwable throwable) {
//
//                System.out.println("onError:"+throwable.getMessage());
//            }
//            @Override
//            public void onCompleted() {
//                System.out.println("Add operations requests completed");
//            }
//        });
//        // Do a sequence of 20 add operations
//        for (int i = 0; i < 20; i++) {
//            int x = new Random().nextInt(10) + 1;
//            int y = new Random().nextInt(10) + 1;
//            System.out.println("Call to operation:(" + "ID" + i + "," + x + "," + y + ")");
//            AddOperands oper = AddOperands.newBuilder()
//                    .setAddID("ID" + i).setOp1(x).setOp2(y)
//                    .build();
//            streamDoAddOperations.onNext(oper);
//        }
//        streamDoAddOperations.onCompleted();
//        // Note that client has sent all requests, but needs synchronization
//        // to terminate after get all results
//    }


    private static void Menu(User user) {

        Scanner scan = new Scanner(System.in);
        for(;;) {
            System.out.println();
            System.out.println("===== MENU =====");
            System.out.println(" 1 - Case server stream: see all topics available");
            System.out.println(" 2 - Case server stream: see my topics");
            System.out.println(" 3 - Case server stream: see all messages");
            System.out.println(" 4 - Case Unary call: user subscribe to a topic");
            System.out.println(" 5 - Case Unary call: user unsubscribe to a topic");
            System.out.println(" 6 - Case Unary call: user publish a message");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option:");
            int op = scan.nextInt();
            System.out.println("================");
            switch (op) {
                case 1:showAllTopicsAsynchronousCall(); break;
                case 2:showMyTopicsAsynchronousCall(user); break;
                case 3:showMessagesByUserTopicsAsynchronousCall(user); break;
                case 4:subscribeAsynchronousCall(user); break;
                case 5:unsubscribeAsynchronousCall(user); break;
                case 6:publishMessageAsynchronousCall(user); break;
                case 99:System.out.println("Logging out...");return;
                default: System.out.println("Invalid option");
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }



    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }

    private static User MenuInitial() {
        User user = null;
        Scanner scan = new Scanner(System.in);
        for(;;) {
            System.out.println();
            System.out.println("===== MENU INITIAL =====");
            System.out.println(" 1 - LOGIN");
            System.out.println(" 2 - REGISTER");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an option:");
            int op = scan.nextInt();

            switch (op) {
                case 1:
                    user = loginCall();
                    if (user != null) return user;
                    break;
                case 2:
                    registerCall();
                    break;
                case 99:
                    System.exit(0);
                default:
                    System.out.println("Invalid option");
            }
        }

    }
}
