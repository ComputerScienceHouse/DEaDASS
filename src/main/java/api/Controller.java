package api;

import api.model.Message;
import dbconn.ManagerManager;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * The spring route controller for the api.
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
@RestController
public class Controller {

    ManagerManager man = new ManagerManager();

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root() {
        return "Hello World!";
    }

    @RequestMapping(value="/message", method = RequestMethod.GET, produces = "application/json")
    public String messageTest() {
        return new Message("Testing message return", Message.Type.MESSAGE).asJSON();
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
    public String dbCreate(@RequestBody Map<String, String> body) {
        return man.request(Integer.parseInt(body.get("pool_id")), body.get("db_name"),
                body.get("purpose"), Integer.parseInt(body.get("type"))).asJSON();
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = "application/json")
    public String dbDelete(@RequestBody Map<String, String> body) {
        return man.delete(body.get("database")).asJSON();
    }


    @RequestMapping(value = "/approve", method = RequestMethod.POST, produces = "application/json")
    public String dbApprove(@RequestBody Map<String, String> body) {
        return man.approve(body.get("database")).asJSON();
    }


    @RequestMapping(value = "/deny", method = RequestMethod.POST, produces = "application/json")
    public String dbDeny(@RequestBody Map<String, String> body) {
        return man.deny(body.get("database")).asJSON();
    }

}
