package RPCServer;

import RPCUtil.RPCRequest;
import RPCUtil.RPCResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by I075261 on 3/2/2018.
 */
public class RPCServerHandler extends SimpleChannelInboundHandler<RPCRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServerHandler.class);

    private final Map<String, Object> handlerMap;

    public RPCServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest request) throws Exception {
        RPCResponse response = new RPCResponse();
        response.setRequestId(request.getRequestId());
        try{
            Object result = handler(request);
            response.setResult(result);
        }catch (Exception e){
            LOGGER.error("handle request failure");
            response.setException(e);
        }

    }

    private Object handler(RPCRequest request) throws Exception{
        String serviceName = request.getInterfaceName();
        String serviceVersion = request.getServiceVersion();
        if (!serviceVersion.isEmpty()) {
            serviceName += "-" + serviceVersion;
        }
        Object serviceBean = handlerMap.get(serviceName);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
        }

        // construct method to be called
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] paramters = request.getParameters();

        // call cglib to invoke the method
        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
        return fastMethod.invoke(serviceBean, paramters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }

}
