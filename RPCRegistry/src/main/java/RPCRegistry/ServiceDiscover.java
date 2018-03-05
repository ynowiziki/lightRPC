package RPCRegistry;

/**
 * Created by I075261 on 3/1/2018.
 */
public interface ServiceDiscover {

    /**
     * 根据服务名称查找服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    String discover(String serviceName);
}
