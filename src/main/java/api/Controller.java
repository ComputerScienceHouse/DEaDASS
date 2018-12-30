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


    @RequestMapping(value="/databases", method = RequestMethod.GET, produces = "application/json")
    public String getDatabases() {
        return man.listDatabases();
    }


    @RequestMapping(value="/databases", method = RequestMethod.POST, produces = "application/json")
    public String createDatabase(@RequestBody Map<String, String> body) {
        return man.request(Integer.parseInt(body.get("pool_id")), body.get("db_name"),
                body.get("purpose"), Integer.parseInt(body.get("type"))).asJSON();
    }


    @RequestMapping(value = "/databases/{database}", method = RequestMethod.GET, produces = "application/json")
    public String getDatabase(@PathVariable(value = "database") String database) {
        return man.getDatabase(database);
    }


    @RequestMapping(value="/databases/{database}", method = RequestMethod.DELETE, produces = "application/json")
    public String deleteDatabase(@PathVariable(value = "database") String database) {
        return man.delete(database).asJSON();
    }


    @RequestMapping(value = "/databases/{database}/approval", method = RequestMethod.POST, produces = "application/json")
    public String approveDatabase(@PathVariable(value = "database") String database) {
        return man.approve(database).asJSON();
    }


    @RequestMapping(value = "/databases/{database}/approval", method = RequestMethod.DELETE, produces = "application/json")
    public String denyDatabase(@PathVariable(value = "database") String database) {
        return man.deny(database).asJSON();
    }


    // Database User routes
    @RequestMapping(value = "/databases/{database}/users", method = RequestMethod.GET, produces = "appliction/json")
    public String getUsers(@PathVariable(value = "database") String database) {
        return man.listUsers(database);
    }


    @RequestMapping(value = "/databases/{database}/users", method = RequestMethod.POST, produces = "application/json")
    public String createUser(@PathVariable(value = "database") String database, @RequestBody Map<String, String> body) {
        return man.createUser(database).asJSON(); // TODO args
    }


    @RequestMapping(value = "/databases/{database}/users/{username}", method = RequestMethod.GET, produces = "application/json")
    public String getUser(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        return man.getUser(database, username);
    }


    @RequestMapping(value = "/databases/{database}/users/{username}", method = RequestMethod.DELETE, produces = "application/json")
    public String deleteUser(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        return man.deleteUser(database, username).asJSON();
    }


    @RequestMapping(value = "/databases/{database}/users/{username}/password", method = RequestMethod.POST, produces = "application/json")
    public String resetPassword(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        return man.setPassword(database, username).asJSON();
    }


    @RequestMapping(value = "/pools", method = RequestMethod.GET, produces = "appliction/json")
    public String getPools() {
        return man.listPools();
    }


    @RequestMapping(value = "/pools", method = RequestMethod.POST, produces = "application/json")
    public String createPool(@RequestBody Map<String, String> body) {
        return man.createPool().asJSON(); // TODO args
    }


    @RequestMapping(value = "/pools/{id}", method = RequestMethod.GET, produces = "application/json")
    public String getPool(@PathVariable(value = "id") int id) {
        return man.getPool(id);
    }


    @RequestMapping(value = "/pools/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public String deletePool(@PathVariable(value = "id") int id) {
        return man.deletePool(id).asJSON();
    }


    // Pool User Routes - can't get specific user by pool since they are only unique by database.
    @RequestMapping(value = "/pools/{id}/users", method = RequestMethod.GET, produces = "appliction/json")
    public String getUsers(@PathVariable(value = "id") int poolID) {
        return man.listUsers(poolID);
    }


    // TODO PATCH?
}
