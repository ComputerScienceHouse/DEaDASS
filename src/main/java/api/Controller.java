package api;

import api.model.Database;
import api.model.DatabaseType;
import api.model.JSONUtils;
import api.model.exception.BadRequestException;
import api.model.exception.NotFoundException;
import api.model.exception.SQLException;
import dbconn.ManagerManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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


    @RequestMapping(value="/databases", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getDatabases() {
        try {
            return ResponseEntity.status(200).body(JSONUtils.toJSON(man.listDatabases()));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        }
    }


    @RequestMapping(value="/databases", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> createDatabase(@RequestBody Map<String, String> body) {
        try {
            Database db = new Database(Integer.parseInt(body.get("pool_id")), body.get("db_name"),
                    body.get("purpose"), DatabaseType.fromString(body.get("type")));
            return ResponseEntity.status(201).body(man.request(db));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }


    @RequestMapping(value = "/databases/{database}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getDatabase(@PathVariable(value = "database") String database) {
        try {
            return ResponseEntity.status(200).body(man.getDatabase(database).asJSON());
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    @RequestMapping(value="/databases/{database}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<String> deleteDatabase(@PathVariable(value = "database") String database) {
        try {
            return ResponseEntity.status(200).body(man.delete(database));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    @RequestMapping(value = "/databases/{database}/approval", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> checkPending(@PathVariable(value = "database") String database) {
        try {
            return ResponseEntity.status(200).body(man.isPending(database));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        }
    }


    // TODO: Consider merging into one PATCH route.
    @RequestMapping(value = "/databases/{database}/approval", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> approveDatabase(@PathVariable(value = "database") String database) {
        try {
            String response = man.approve(database);
            if(response.contains("password"))
                return ResponseEntity.status(201).body(response);
            else
                return ResponseEntity.status(202).body(response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }


    @RequestMapping(value = "/databases/{database}/approval", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<String> denyDatabase(@PathVariable(value = "database") String database) {
        try {
            return ResponseEntity.status(200).body(man.deny(database));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }


    // Database User routes
    @RequestMapping(value = "/databases/{database}/users", method = RequestMethod.GET, produces = "appliction/json")
    public ResponseEntity<String> getUsers(@PathVariable(value = "database") String database) {
        return ResponseEntity.status(501).body("Not yet implemented");
    }


    @RequestMapping(value = "/databases/{database}/users", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> createUser(@PathVariable(value = "database") String database, @RequestBody Map<String, String> body) {
        return ResponseEntity.status(501).body("Not yet implemented");
    }


    @RequestMapping(value = "/databases/{database}/users/{username}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getUser(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        return ResponseEntity.status(501).body("Not yet implemented");
    }


    @RequestMapping(value = "/databases/{database}/users/{username}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<String> deleteUser(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        return ResponseEntity.status(501).body("Not yet implemented");
    }


    @RequestMapping(value = "/databases/{database}/users/{username}/password", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> resetPassword(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        Map<String, Object> message = new HashMap<String, Object>();
        message.put("status", "error");
        try {
            message.put("password", man.setPassword(database, username));
            message.put("status", "success");
            return ResponseEntity.status(200).body(JSONUtils.toJSON(message));
        } catch (NotFoundException e) {
            message.put("message", e.getMessage());
            return ResponseEntity.status(404).body(JSONUtils.toJSON(message));
        } catch (SQLException e) {
            e.printStackTrace();
            message.put("message", e.getMessage());
            return ResponseEntity.status(500).body(JSONUtils.toJSON(message));
        }
    }


    @RequestMapping(value = "/pools", method = RequestMethod.GET, produces = "appliction/json")
    public ResponseEntity<String> getPools() {
        try {
            return ResponseEntity.status(200).body(JSONUtils.toJSON(man.listPools()));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        }
    }


    @RequestMapping(value = "/pools", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> createPool(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(501).body("Not yet implemented");
    }


    @RequestMapping(value = "/pools/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getPool(@PathVariable(value = "id") int id) {
        try {
            return ResponseEntity.status(200).body(JSONUtils.toJSON(man.getPool(id)));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SQL Exception");
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    @RequestMapping(value = "/pools/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<String> deletePool(@PathVariable(value = "id") int id) {
        return ResponseEntity.status(501).body("Not yet implemented");
    }


    // Pool User Routes - can't get specific user by pool since they are only unique by database.
    @RequestMapping(value = "/pools/{id}/users", method = RequestMethod.GET, produces = "appliction/json")
    public ResponseEntity<String> getUsers(@PathVariable(value = "id") int poolID) {
        return ResponseEntity.status(501).body("Not yet implemented");
    }
}
