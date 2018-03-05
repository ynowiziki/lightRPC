package RPCUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by I075261 on 2/28/2018.
 */
public class RPCEncoder extends  MessageToByteEncoder {
    private Class<?> genericClass;

    public RPCEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
