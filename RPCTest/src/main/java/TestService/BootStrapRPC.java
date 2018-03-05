package TestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by I075261 on 3/2/2018.
 */
public class BootStrapRPC {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootStrapRPC.class);

    public static void main(String[] args) {
        LOGGER.info("start server");
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
