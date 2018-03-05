package client;

import RPCRegistry.*;
import RPCUtil.RPCRequest;
import RPCUtil.RPCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * Created by I075261 on 3/1/2018.
 */
public class RPCProxy  {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCProxy.class);

    private String serviceAddress;

    private ServiceDiscover serviceDiscover;

    public RPCProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RPCProxy(ServiceDiscover serviceDiscover) {
        this.serviceDiscover = serviceDiscover;
    }

    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建 RPC 请求对象并设置请求属性
                        RPCRequest request = new RPCRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setInterfaceName(method.getDeclaringClass().getName());
                        request.setServiceVersion(serviceVersion);
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        // 获取 RPC 服务地址
                        if (serviceDiscover != null) {
                            String serviceName = interfaceClass.getName();
                            if (!serviceVersion.isEmpty()) {
                                serviceName += "-" + serviceVersion;
                            }
                            serviceAddress = serviceDiscover.discover(serviceName);
                            LOGGER.debug("discover service: {} => {}", serviceName, serviceAddress);
                        }
                        if (serviceAddress.isEmpty()) {
                            throw new RuntimeException("server address is empty");
                        }
                        // 从 RPC 服务地址中解析主机名与端口号
                        String[] array = serviceAddress.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        // 创建 RPC 客户端对象并发送 RPC 请求
                        RPCClient client = new RPCClient(host, port);
                        long time = System.currentTimeMillis();
                        RPCResponse response = client.send(request);
                        LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);
                        if (response == null) {
                            throw new RuntimeException("response is null");
                        }
                        // 返回 RPC 响应结果
                        if (response.hasException()) {
                            throw response.getException();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }


}
