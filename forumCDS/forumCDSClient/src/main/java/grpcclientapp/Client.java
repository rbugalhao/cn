package grpcclientapp;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import servicestubs.*;

import java.util.Scanner;

public class Client {
    // generic ClientApp for Calling a grpc Service
    private static String svcIP = "localhost";
    private static int svcPort = 8000;
    private static ManagedChannel channel;
    private static ForumGrpc.ForumBlockingStub blockingStub;
    private static ForumGrpc.ForumStub noBlockStub;

    private static String userName="";


    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0]; svcPort = Integer.parseInt(args[1]);
            }
            System.out.println("connect to " + svcIP + ":" + svcPort);
            channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    // Channels are secure by default (via SSL/TLS).
                    // For the example we disable TLS to avoid
                    // needing certificates .
                    .usePlaintext()
                    .build();
            blockingStub = ForumGrpc.newBlockingStub(channel);
            noBlockStub = ForumGrpc.newStub(channel);
            // Call service operations for example ping server
            Scanner scan = new Scanner(System.in);

            userName = read("Enter your user name:", scan);

            boolean end = false;
            while (true) {
                try {

                    Menu();

                } catch (Exception ex) {
                    System.out.println("Execution call Error  !");
                    ex.printStackTrace();
                }
            }
//            read("prima enter to end", new Scanner(System.in));
        } catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }



    static void subscribeAsynchronousCall() {
        // Asynchronous non-blocking call
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the topic you want to subscribe to:");
        String topic = scan.nextLine().trim();
        // Asynchronous non blocking call
        EvenNumbersStream evenStream = new EvenNumbersStream();
        noBlockStub.topicSubscribe(SubscribeUnSubscribe.newBuilder().setUsrName(userName).setTopicName(topic).build(), evenStream);

    }

    static void unsubscribeAsynchronousCall() {
        // Asynchronous non-blocking call
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the topic you want to unsubscribe from:");
        String topic = scan.nextLine().trim();
        SubscribeUnSubscribe request = SubscribeUnSubscribe.newBuilder()
                .setUsrName(userName)
                .setTopicName(topic)
                .build();

        noBlockStub.topicUnSubscribe(request, new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty empty) {
                System.out.println("Unsubscribed from topic: " + topic);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Unsubscription completed");
            }
        });
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





    static void publishMessageAsynchronousCall() {
        // Asynchronous non-blocking call
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the topic you want to publish to:");
        String topic = scan.nextLine().trim();
        System.out.println("Enter the message you want to publish:");
        String message = scan.nextLine().trim();
        ForumMessage request = ForumMessage.newBuilder()
                .setFromUser(userName)
                .setTopicName(topic)
                .setTxtMsg(message)
                .build();
        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty empty) {
                System.out.println("Message published");
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


    private static void Menu() {

        Scanner scan = new Scanner(System.in);
        for(;;) {
            System.out.println();
            System.out.println("===== MENU =====");
            System.out.println(" 1 - Case server stream: see all topics available");
            System.out.println(" 2 - Case Unary call: user subscribe to a topic");
            System.out.println(" 3 - Case Unary call: user unsubscribe to a topic");
            System.out.println(" 4 - Case Unary call: user publish a message");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option:");
            int op = scan.nextInt();
            System.out.println("================");
            switch (op) {
                case 1:showAllTopicsAsynchronousCall(); break;
                case 2:subscribeAsynchronousCall(); break;
                case 3:unsubscribeAsynchronousCall(); break;
                case 4:publishMessageAsynchronousCall(); break;
                case 99:System.out.println("Exiting...");System.exit(0);
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


}
