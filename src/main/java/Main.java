import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;

public class Main {
    public static void main(String[] args ) {
        ZkClient zkClient = new ZkClient("127.0.0.1:2181",10000,10000,new SerializableSerializer());
        zkClient.delete("/zkpath");
        String path = zkClient.create("/zkpath","test", CreateMode.PERSISTENT);
        System.out.println("create path:" + path);

        String data  = zkClient.readData("/zkpath");

        System.out.println("data is :" + data);
    }
}
