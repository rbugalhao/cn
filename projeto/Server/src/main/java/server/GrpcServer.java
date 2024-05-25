package server;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GrpcServer {

    private static int svcPort = 8000;


    public static void main(String[] args) {
        try {

            if (args.length > 0) svcPort = Integer.parseInt(args[0]);
            io.grpc.Server svc = ServerBuilder.forPort(svcPort)
                    // Add one or more services.
                    // The Server can host many services in same TCP/IP port
                    .addService(new Service(svcPort))
                    .addService(new ServiceManagement(svcPort))
                    .build();
            svc.start();
            System.out.println("Server started on port " + svcPort);

            // Java virtual machine shutdown hook
            // to capture normal or abnormal exits
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(svc));
            // Waits for the server to become terminated
            svc.awaitTermination();
            server.Service.subscriber.stopAsync();
            server.ServiceManagement.subscriber.stopAsync();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}