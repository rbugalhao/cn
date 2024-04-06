package grpcserverapp;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import servicestubs.*;


import java.util.Random;

public class Service extends ServiceGrpc.ServiceImplBase {

    public Service(int svcPort) {

        System.out.println("Service is available on port:" + svcPort);
    }

    @Override
    public void isAlive(ProtoVoid request, StreamObserver<TextMessage> responseObserver) {
        System.out.println("isAlive called!");
        responseObserver.onNext(TextMessage.newBuilder().setTxt("Service is alive").build());
        responseObserver.onCompleted();
    }

    @Override
    public void getEvenNumbers(IntNumber request, StreamObserver<IntNumber> responseObserver) {
        System.out.println("getEvenNumbers called!");
        if (request.getIntnumber() < 0) {
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT.withDescription("Number < 0 !")));
            return;
        }
        int count = 0;
        int evennumber = 2;
        for (; ; ) {

            responseObserver.onNext(IntNumber.newBuilder().setIntnumber(evennumber).build());
            evennumber += 2;
            count++;
            if (count == request.getIntnumber()) break;
            simulateExecutionTime();
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<IntNumber> addSeqOfNumbers(StreamObserver<IntNumber> responseObserver) {
        System.out.println("addSeqOfNumbers called! returned a stream to receive requests");
        return new StreamObserver<IntNumber>() {
            int soma = 0; // To accumulate values

            @Override
            public void onNext(IntNumber intNumber) {
                soma += intNumber.getIntnumber(); // Process request
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
                System.out.println("client completed requests -> complete response");
                responseObserver.onNext(IntNumber.newBuilder().setIntnumber(soma).build());
                responseObserver.onCompleted();
            }
        };
    }





    @Override
    public StreamObserver<AddOperands> multipleAdd(StreamObserver<AddResult> responseObserver) {
        System.out.println("multipleAdd called! returned a stream to receive requests");
        return new StreamObserver<AddOperands>() {
            @Override
            public void onNext(AddOperands addOperands) {
                System.out.println("Operands of ID=" + addOperands.getAddID());
                AddResult result = AddResult.newBuilder()
                        .setAddID(addOperands.getAddID())
                        .setResult(addOperands.getOp1() + addOperands.getOp2())
                        .build();
                simulateExecutionTime();
                System.out.println("  Result of ID=" + addOperands.getAddID() + " " + result.getResult());
                responseObserver.onNext(result);
            }
            @Override
            public void onError(Throwable throwable) {      }
            @Override
            public void onCompleted() {
                System.out.println("client completed requests -> completed responses");
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void findPrimes(IntervalNumbers request, StreamObserver<IntNumber> responseObserver) {
        //findPrimes implements contract
        long thId=Thread.currentThread().getId();
        System.out.println("Thread "+thId+" find primes for interval:["+request.getStart()+","+request.getEnd()+"]");

        for (int i = request.getStart(); i <= request.getEnd(); i++) {

            if (isPrime(i)){
                responseObserver.onNext(IntNumber.newBuilder().setIntnumber(i).build());
            }

        }

        responseObserver.onCompleted();

    }

    boolean isPrime(int num) {
        if (num <= 1) return false;
        if (num == 2 || num == 3) return true;
        if (num % 2 == 0) return false;
        for (int i=3; i <= Math.sqrt(num); i+=2) {
            if (num % i == 0) return false;
        }
        return true;
    }
    private void simulateExecutionTime() {
        try {
            // simulate processing time between 200ms and 3s
            Thread.sleep(new Random().nextInt(2800) + 200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
