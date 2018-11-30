package api;

import org.springframework.web.bind.annotation.*;

import api.model.CreateResponse;
import api.model.CreateRequest;
import dbconn.ManagerManager;


@RestController
public class Controller {

    ManagerManager man; // TODO need to decide how to pull this in.

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root() {
        return "Hello World!";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public CreateResponse dbcreate(@RequestBody CreateRequest db) {

        // Request creation from the manager. If created, return the password, otherwise it's waiting for approval
        String password = man.request(db.getUid(), db.getName(), db.getPurpose(), db.getType());

        if (password.equals(""))
            return new CreateResponse("requested", ""); // DB requested
        else
            return new CreateResponse("created", password); // DB created.
    }

}
