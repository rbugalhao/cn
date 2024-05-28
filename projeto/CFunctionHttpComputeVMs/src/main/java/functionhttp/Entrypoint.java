package functionhttp;

import com.google.cloud.compute.v1.*;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;


import java.io.BufferedWriter;
import java.util.ArrayList;

public class Entrypoint implements HttpFunction {


@Override
public void service(HttpRequest request, HttpResponse response) throws Exception {
    BufferedWriter writer = response.getWriter();
    String gcpProjetID=request.getFirstQueryParameter("prjid").orElse("cn2324-t2-g11");
    String zone = request.getFirstQueryParameter("zone").orElse("europe-southwest1-a");
    String instGroup = request.getFirstQueryParameter("group").orElse("instance-group-server-1");
    ArrayList<String> ipAddresses = new ArrayList<>();
    try (InstancesClient client = InstancesClient.create()) {
        for (Instance instance : client.list(gcpProjetID, zone).iterateAll()) {
            if (instance.getStatus().compareTo("RUNNING") == 0) {
                if (instance.getName().contains(instGroup)) {
                    String ip = instance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    ipAddresses.add(ip);
                }
            }
        }
    }
    writer.write(new Gson().toJson(ipAddresses.toArray()));
}


}
