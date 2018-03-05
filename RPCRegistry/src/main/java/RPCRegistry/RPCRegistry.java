package RPCRegistry;

/**
 * Created by I075261 on 3/1/2018.
 */
public interface RPCRegistry {
    /**
     * 注册服务名称与服务地址
     *
     * @param serviceName    服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress);
}
