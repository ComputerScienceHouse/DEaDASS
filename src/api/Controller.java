package api;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import api.model.CreateResponse;
import api.model.DBRequest;
import dbconn.ManagerManager;


@RestController
public class Controller {

    ManagerManager man; // TODO need to decide how to pull this in.

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root() {
        return "Hello World!";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
    public CreateResponse dbcreate(@RequestBody DBRequest db) {

        // TODO refactor request to handle allowing 5 databases without needing approval
        String pass = man.request(db.getUid(), db.getName(), db.getPurpose(), db.getType());

        if (pass == null)
            // DB requested
            return new CreateResponse("requested", "");
        else
            // DB created.
            return new CreateResponse("created", pass);
    }

}
