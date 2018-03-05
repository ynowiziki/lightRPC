package RPCServer;

import RPCRegistry.RPCRegistry;
import RPCUtil.RPCDecoder;
import RPCUtil.RPCEncoder;
import RPCUtil.RPCRequest;
import RPCUtil.RPCResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jdk.nashorn.internal.runtime.linker.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by I075261 on 3/1/2018.
 */
public class RPCServer implements ApplicationContextAware, InitializingBean{
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServer.class);

    private String serviceAddress;

    private Map<String, Object> handlerMap = new HashMap<>();

    private RPCRegistry serviceRegistry;

    public RPCServer(String serviceAddress, RPCRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RPCService.class);
        if (!serviceBeanMap.isEmpty()) {
            for(Object serviceBean : serviceBeanMap.values()) {
                RPCService rpcService = serviceBean.getClass().getAnnotation(RPCService.class);
                String serviceName = rpcService.value().getName();
                String serviceVersion = rpcService.version();
                if(!serviceVersion.isEmpty()) {
                    serviceName += '-' + serviceVersion;
                }
                handlerMap.put(serviceName, serviceBean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new RPCDecoder(RPCRequest.class)); // 解码 RPC 请求
                    pipeline.addLast(new RPCEncoder(RPCResponse.class)); // 编码 RPC 响应
                    pipeline.addLast(new RPCServerHandler(handlerMap)); // 处理 RPC 请求
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] address = serviceAddress.split(":");
            String ip = address[0];
            int port = Integer.parseInt(address[1]);

            ChannelFuture future = bootstrap.bind(ip, port).sync();

            // Register RPC service
            if (serviceRegistry != null) {
                for (String interfaceName : handlerMap.keySet()) {
                    serviceRegistry.register(interfaceName, serviceAddress);
                    LOGGER.debug("register service: {}", interfaceName, serviceAddress);
                }
            }
            LOGGER.debug("server started on port{}", port);

            future.channel().closeFuture().sync();

        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
