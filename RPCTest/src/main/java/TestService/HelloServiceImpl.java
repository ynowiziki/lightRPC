package TestService;

/**
 * Created by I075261 on 3/2/2018.
 */

import RPCServer.RPCService;

@RPCService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "This is the RPC Test" + name;
    }

    @Override
    public String hello(Person person) {
       return  "User" + person.getFirstName() + " " + person.getLastName() + " start the service";
    }
}
