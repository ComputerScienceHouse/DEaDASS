package api;

import api.model.Message;
import org.springframework.web.bind.annotation.*;

import api.model.CreateResponse;
import api.model.CreateRequest;
import dbconn.ManagerManager;

import java.util.Map;


@RestController
public class Controller {

    ManagerManager man; // TODO how is this being initialised?

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root() {
        return "Hello World!";
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Message dbCreate(@RequestBody CreateRequest db) {
        return man.request(db.getPoolID(), db.getName(), db.getPurpose(), db.getType());
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Message dbDelete(@RequestBody Map<String, String> body) {
        return man.delete(body.get("database"));
    }


    @RequestMapping(value = "/approve", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Message dbApprove(@RequestBody Map<String, String> body) {
        return man.approve(body.get("database"));
    }


    @RequestMapping(value = "/deny", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Message dbDeny(@RequestBody Map<String, String> body) {
        return man.deny(body.get("database"));
    }

}
