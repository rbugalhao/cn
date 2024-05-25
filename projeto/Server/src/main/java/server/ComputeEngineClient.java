package server;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class ComputeEngineClient {

    private static int Menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 0 - list VM instances");
            System.out.println(" 1 - Start a stopped VM");
            System.out.println(" 2 - Stop a running VM");
            System.out.println(" 3 - list Managed Instance Groups");
            System.out.println(" 4 - list VMs of Managed Instance Groups");
            System.out.println(" 5 - resize a Managed Instance Group");
            System.out.println(" 6 - list VMs of Managed Instance Groups with IP");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scan.nextInt();
        } while (!((op >= 0 && op <= 6) || op == 99));
        return op;
    }


    static void listVMInstances(String project, String zone) throws IOException {
        System.out.println("==== Listing VM instances at zone: " + zone);
        try (InstancesClient client = InstancesClient.create()) {
            for (Instance instance : client.list(project, zone).iterateAll()) {
                System.out.println("Name: " + instance.getName() + "  VMId:" + instance.getId());
                System.out.println("    Number of network interfaces: " + instance.getNetworkInterfacesCount());
                String ip = instance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                System.out.println("    IP: " + ip);
                System.out.println("    Status: " + instance.getStatus() + " : Last Start time: " + instance.getLastStartTimestamp());
            }
        }
    }

    static void startStoppedVM(String projectID, String zone, String instanceName) throws IOException, ExecutionException, InterruptedException {
        try (InstancesClient client = InstancesClient.create()) {
            StartInstanceRequest request =
                    StartInstanceRequest.newBuilder()
                            .setProject(projectID)
                            .setZone(zone)
                            .setInstance(instanceName)
                            .build();
            OperationFuture<Operation, Operation> fut = client.startAsync(request);
            while (!fut.isDone()) {
                System.out.println("waiting to start!");
                Thread.sleep(4 * 1000);
            }
            System.out.println("" + fut.get().getStatus());
        }
    }

    static void stopRunningVM(String projectID, String zone, String instanceName) throws IOException, ExecutionException, InterruptedException {
        try (InstancesClient client = InstancesClient.create()) {
            StopInstanceRequest request =
                    StopInstanceRequest.newBuilder()
                            .setProject(projectID)
                            .setZone(zone)
                            .setInstance(instanceName)
                            .build();
            OperationFuture<Operation, Operation> fut = client.stopAsync(request);
            while (!fut.isDone()) {
                System.out.println("waiting to stop!");
                Thread.sleep(4 * 1000);
            }
            System.out.println("" + fut.get().getStatus());
        }
    }


    static void listManagedInstanceGroups(String project, String zone) throws IOException {
        System.out.println("======= Listing of managed instance groups at zone: " + zone);
        try (InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create()) {
            for (InstanceGroupManager manager : managersClient.list(project, zone).iterateAll()) {
                System.out.println("Name: " + manager.getName());
                System.out.println("Template: " + manager.getInstanceTemplate());
            }
        }
    }

    static void listManagedInstanceGroupVMs(String projectId, String zone, String grpName) throws IOException {
        InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create();
        ListManagedInstancesInstanceGroupManagersRequest request =
                ListManagedInstancesInstanceGroupManagersRequest.newBuilder()
                        .setInstanceGroupManager(grpName)
                        .setProject(projectId)
                        .setReturnPartialSuccess(true)
                        .setZone(zone)
                        .build();
        System.out.println("Instances of instance group: " + grpName);
        for (ManagedInstance instance :
                managersClient.listManagedInstances(request).iterateAll()) {
            System.out.println(instance.getInstance() + " with STATUS = " + instance.getInstanceStatus());
        }
    }

    static void resizeManagedInstanceGroup(String project, String zone, String instanceGroupName, int newSize) throws IOException, InterruptedException, ExecutionException {
        System.out.println("================== Resizing instance group");
        InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create();
        OperationFuture<Operation, Operation> result = managersClient.resizeAsync(
                project,
                zone,
                instanceGroupName,
                newSize
        );
        Operation oper = result.get();
        System.out.println("Resizing with status " + oper.getStatus());
    }

    static void listIpInstancesFromGroup(String projectID, String zone, String groupName) throws IOException {
        System.out.println("==== Listing Running VM instances from instance group: " + groupName);
        try (InstancesClient client = InstancesClient.create()) {
            for (Instance curInst : client.list(projectID, zone).iterateAll()) {
                if (curInst.getName().contains(groupName)) {
                    System.out.println("Name: " + curInst.getName() + "  VMId:" + curInst.getId());
                    System.out.println("    Number of network interfaces: " + curInst.getNetworkInterfacesCount());
                    String ip = curInst.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    System.out.println("    IP: " + ip);
                    System.out.println("    Status: " + curInst.getStatus() + " : Last Start time: " + curInst.getLastStartTimestamp());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        if (args.length != 2) {
            System.out.println("Usage: java -jar ComputeEngineAPI<...>.jar projectID zone");
            //System.exit(-1);
        }
//        String projectID = args[0]; //"cn24-geral";
//        String zone = args[1];      // "europe-west1-b";
        String projectID = "cn2324-t2-g11"; //"cn24-geral";
        String zone ="europe-southwest1-a";      // "europe-west1-b";

        Scanner input = new Scanner(System.in);
        boolean end = false;
        while (!end) {
            int option = Menu();
            switch (option) {
                case 0:
                    listVMInstances(projectID, zone);
                    break;
                case 1:
                    System.out.println("instance name?");
                    String instanceNameToStart = input.nextLine();
                    startStoppedVM(projectID, zone, instanceNameToStart);
                    break;
                case 2:
                    System.out.println("instance name?");
                    String instanceNameToStop = input.nextLine();
                    stopRunningVM(projectID, zone, instanceNameToStop);
                    break;
                case 3:
                    listManagedInstanceGroups(projectID, zone);
                    break;
                case 4:
                    System.out.println("instance group name?");
                    String managedGroupNameList = input.nextLine();
                    listManagedInstanceGroupVMs(projectID, zone, managedGroupNameList);
                    break;
                case 5:
                    System.out.println("instance group name?");
                    String managedGroupNameResize = input.nextLine();
                    System.out.println("How many VMs in instance group " + managedGroupNameResize + " ?");
                    String newSize = input.nextLine();
                    resizeManagedInstanceGroup(projectID, zone, managedGroupNameResize, Integer.parseInt(newSize));
                    break;
                case 6:
                    System.out.println("managed instance group name?");
                    String managedGroupName = input.nextLine();
                    listIpInstancesFromGroup(projectID, zone, managedGroupName);
                    break;
                case 99:
                    end = true;
                    break;
            }
        }

    }


}
