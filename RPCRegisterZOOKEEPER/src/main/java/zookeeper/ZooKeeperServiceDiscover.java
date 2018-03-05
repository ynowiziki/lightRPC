package zookeeper;

import RPCRegistry.ServiceDiscover;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by I075261 on 3/2/2018.
 */
public class ZooKeeperServiceDiscover implements ServiceDiscover {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscover.class);

    private String zkAddress;

    public ZooKeeperServiceDiscover(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    @Override
    public String discover(String serviceName) {
        ZkClient zkClient = new ZkClient(zkAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
        LOGGER.debug("connect zookeeper");
        try {
        // get service node
            String servicePath = Constant.ZK_REGISTRY_PATH + "/" + serviceName;
            if (!zkClient.exists(servicePath)) {
                throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
            }
            List<String> addressList = zkClient.getChildren(servicePath);
            if (addressList.isEmpty()) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }
            // get address node
            String address;
            int size = addressList.size();
            if (size == 1) {
                address = addressList.get(0);
                LOGGER.debug("get only address node: {}", address);
            } else {
                address = addressList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("get random address node: {}", address);
            }
            String addressPath = servicePath + "/" + address;
            return zkClient.readData(addressPath);
        } finally {
            zkClient.close();
    }
    }
}
