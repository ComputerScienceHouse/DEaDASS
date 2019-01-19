package api;

import api.model.Database;
import api.model.DatabaseType;
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
    public ResponseEntity<String> root() {
        return ResponseEntity.status(200).body("Hello World!");
    }


    @RequestMapping(value="/databases", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> getDatabases() {
        try {
            return ResponseEntity.status(200).body(man.listDatabases());
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        }
    }


    @RequestMapping(value="/databases", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Object> createDatabase(@RequestBody Map<String, String> body) {
        try {
            Database db = new Database(Integer.parseInt(body.get("pool_id")), body.get("db_name"),
                    body.get("purpose"), DatabaseType.fromString(body.get("type")));
            return ResponseEntity.status(201).body(man.request(db));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(wrapError(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(401).body(wrapError(e.getMessage()));
        }
    }


    @RequestMapping(value = "/databases/{database}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> getDatabase(@PathVariable(value = "database") String database) {
        try {
            return ResponseEntity.status(200).body(man.getDatabase(database));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(wrapError(e.getMessage()));
        }
    }


    @RequestMapping(value="/databases/{database}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Object> deleteDatabase(@PathVariable(value = "database") String database) {
        try {
            return ResponseEntity.status(200).body(man.delete(database));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(wrapError(e.getMessage()));
        }
    }


    @RequestMapping(value = "/databases/{database}/approval", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> checkPending(@PathVariable(value = "database") String database) {
        try {
            return ResponseEntity.status(200).body(man.isPending(database));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        }
    }


    // TODO: Consider merging into one PATCH route.
    @RequestMapping(value = "/databases/{database}/approval", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Object> approveDatabase(@PathVariable(value = "database") String database) {
        try {
            Map<String, Object> response = man.approve(database);
            if(response.containsKey("password"))
                return ResponseEntity.status(201).body(response);
            else
                return ResponseEntity.status(202).body(response);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(wrapError(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(401).body(wrapError(e.getMessage()));
        }
    }


    @RequestMapping(value = "/databases/{database}/approval", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Object> denyDatabase(@PathVariable(value = "database") String database) {
        try {
            return ResponseEntity.status(200).body(man.deny(database));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(wrapError(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(401).body(wrapError(e.getMessage()));
        }
    }


    // Database User routes
    @RequestMapping(value = "/databases/{database}/users", method = RequestMethod.GET, produces = "appliction/json")
    public ResponseEntity<Object> getUsers(@PathVariable(value = "database") String database) {
        return ResponseEntity.status(501).body(wrapError("Not yet implemented"));
    }


    @RequestMapping(value = "/databases/{database}/users", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Object> createUser(@PathVariable(value = "database") String database, @RequestBody Map<String, String> body) {
        return ResponseEntity.status(501).body(wrapError("Not yet implemented"));
    }


    @RequestMapping(value = "/databases/{database}/users/{username}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> getUser(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        return ResponseEntity.status(501).body(wrapError("Not yet implemented"));
    }


    @RequestMapping(value = "/databases/{database}/users/{username}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        return ResponseEntity.status(501).body(wrapError("Not yet implemented"));
    }


    @RequestMapping(value = "/databases/{database}/users/{username}/password", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Object> resetPassword(@PathVariable(value = "database") String database, @PathVariable(value = "username") String username) {
        try {
            Map<String, Object> message = new HashMap<String, Object>();
            message.put("password", man.setPassword(database, username));
            message.put("status", "success");
            return ResponseEntity.status(200).body(message);
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(wrapError(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError(e.getMessage()));
        }
    }


    @RequestMapping(value = "/pools", method = RequestMethod.GET, produces = "appliction/json")
    public ResponseEntity<Object> getPools() {
        try {
            return ResponseEntity.status(200).body(man.listPools());
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        }
    }


    @RequestMapping(value = "/pools", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Object> createPool(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(501).body(wrapError("Not yet implemented"));
    }


    @RequestMapping(value = "/pools/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> getPool(@PathVariable(value = "id") int id) {
        try {
            return ResponseEntity.status(200).body(man.getPool(id));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(wrapError("SQL Exception"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(wrapError(e.getMessage()));
        }
    }


    @RequestMapping(value = "/pools/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Object> deletePool(@PathVariable(value = "id") int id) {
        return ResponseEntity.status(501).body(wrapError("Not yet implemented"));
    }


    // Pool User Routes - can't get specific user by pool since they are only unique by database.
    @RequestMapping(value = "/pools/{id}/users", method = RequestMethod.GET, produces = "appliction/json")
    public ResponseEntity<Object> getUsers(@PathVariable(value = "id") int poolID) {
        return ResponseEntity.status(501).body(wrapError("Not yet implemented"));
    }


    /**
     * Wraps an error message in a map so it serialises to JSON error format
     * @param message the error message
     * @return A map that will serialise to JSON error format containing message as title
     */
    private Map<String, Object> wrapError(String message) {
        Map<String, String> error = new HashMap<String, String>();
        error.put("title", message);
        Map<String, String>[] array = new Map[]{error};
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("errors", array);
        map.put("status", "error");
        return map;
    }
}
