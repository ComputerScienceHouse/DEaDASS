import type { Database, DBConnection, DBRole, DBUser } from "../db_connection";
import { MongoClient } from "mongodb";

interface SystemUsersSchema {
  _id: unknown;
  user: string;
  db: string;
  roles: Array<{ role: string; db: string }>;
  customData: unknown;
}

export interface MongoDBUser extends DBUser {
  extra_data: { db: string };
}

function mongo_user_to_dbuser(mongo_user: SystemUsersSchema): MongoDBUser {
  return {
    type: "mongo",
    user: mongo_user.user,
    roles: mongo_user.roles,
    extra_data: { db: mongo_user.db },
  };
}

// eslint-disable-next-line @typescript-eslint/no-empty-function
function void_promise(): void {}

class Mongo implements DBConnection {
  private readonly client: MongoClient;

  /**
   * Instantiates a connection to the database
   * @param connection_string Mongodb connection string
   */
  public constructor(connection_string: string) {
    this.client = new MongoClient(connection_string);
  }

  public async init(): Promise<void> {
    await this.client.connect();
  }

  public is_connected(): boolean {
    return this.client.isConnected();
  }

  public list_dbs(): Promise<string[]> {
    return this.client
      .db("admin")
      .admin()
      .listDatabases()
      .then((response: { databases: Array<{ name: string }> }) =>
        response.databases.map((db: { name: string }) => db.name)
      );
  }

  public async get_db(db_name: string): Promise<Database> {
    return {
      type: "mongo",
      name: db_name,
      users: await this.list_users(db_name),
    };
  }

  public list_users(db_name?: string): Promise<MongoDBUser[]> {
    return this.client
      .db("admin")
      .collection("system.users")
      .find(db_name ? { "roles.db": db_name } : {})
      .map(mongo_user_to_dbuser)
      .toArray();
  }

  /**
   * Test if a user already exists
   * @param username
   * @param db the db to check against (generally 'admin' for people and a specific DB for service accounts)
   * @returns true if there's an existing user
   */
  private does_user_exist(username: string, db: string): Promise<boolean> {
    return this.client
      .db("admin")
      .collection("system.users")
      .findOne({ user: username, db: db })
      .then((result) => {
        if (result) {
          return true;
        } else {
          return false;
        }
      });
  }

  /**
   * Create a user
   * @param username the account username
   * @param password the user password
   * @param roles the user roles
   * @param db_name the database to create the user in
   * @returns the freshly created user
   */
  private create_user(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>,
    db_name: string
  ): Promise<MongoDBUser> {
    return this.does_user_exist(username, db_name).then((user_exists) => {
      if (roles.length == 0) {
        return Promise.reject("Creating a user without roles is disallowed");
      }
      if (!user_exists) {
        return this.client
          .db(db_name)
          .addUser(username, password, {
            roles: roles,
          })
          .then(() => this.get_user(username, db_name));
      } else throw "User already exists";
    });
  }

  public create_user_account(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>
  ): Promise<MongoDBUser> {
    return this.create_user(username, password, roles, "admin");
  }

  public create_service_account(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>,
    db: string
  ): Promise<MongoDBUser> {
    return this.create_user(username, password, roles, db);
  }

  /**
   * // TODO user data storage considerations
   * The schema I'm considering would separate csher's from services by
   * keeping users in admin, and services in the service db. This would
   * allow DEaDASS to figure out who has access to what from the mongo db
   * itself, and then all that needs to be stored elsewhere would be
   * pending accounts. I think I can tie custom data to users, and store
   * arbitrary things. It remains to be seen if the same is true for mysql
   * and postgres. It may be necessary to keep a DEaDASS db of all this
   * information, depending on how convenient those systems are. Also,
   * migration will take some effort.
   */
  public get_user(username: string, db: string): Promise<MongoDBUser> {
    return this.client
      .db("admin")
      .collection("system.users")
      .find({ user: username, db: db })
      .map(mongo_user_to_dbuser)
      .toArray()
      .then((users: MongoDBUser[]) => {
        if (users.length > 1) throw `Multiple users ${db}.${username}`;
        if (users.length == 0) throw `No user found`;
        return users[0];
      });
  }

  public create_db(
    db_name: string,
    username: string,
    password: string
  ): Promise<Database> {
    // Check if the db is already in use
    return (
      this.list_dbs()
        .then((names: string[]) => {
          if (names.includes(db_name)) {
            throw `db ${db_name} already exists`;
          }
        })
        // If it's not in use, we can create it
        .then(() => {
          // TODO refactor user creation, need to add roles to existing users and/or create user accounts. Do in higher level function?
          this.create_service_account(
            username,
            password,
            [{ role: "dbOwner", db: db_name }],
            db_name
          ).catch((error) => {
            console.error("Error creating new mongo user for db " + db_name);
            throw error;
          });
        })
        .then(() => this.get_db(db_name))
        .catch((error) => {
          console.error(error);
          throw error;
        })
    );
  }

  public delete_user(username: string, db: string): Promise<void> {
    return this.client.db(db).removeUser(username).then(void_promise);
  }

  public delete_db(db_name: string): Promise<void> {
    return this.client
      .db(db_name)
      .command({ dropAllUsersFromDatabase: 1 })
      .then(() => this.list_users(db_name))
      .then((users) =>
        // Remove roles from all users with access to this db
        Promise.all(
          users.map((user) =>
            this.update_user(
              user.user,
              user.extra_data.db,
              undefined,
              user.roles.filter((value: DBRole) => value.db !== db_name)
            )
          )
        )
      )
      .then(() => this.client.db(db_name).dropDatabase())
      .then(void_promise)
      .catch((error) => {
        console.error("Error deleting mongo db " + db_name);
        console.error(error);
        throw error;
      });
  }

  public update_user(
    username: string,
    db: string,
    password?: string,
    // TODO customData?: string,
    roles?: Array<{ db: string; role: string }>
  ): Promise<MongoDBUser> {
    const command: { [k: string]: unknown } = { updateUser: username };
    if (password) command.pwd = password;
    if (roles) command.roles = roles;
    return this.client
      .db(db)
      .command(command)
      .then(() => this.get_user(username, db));
  }

  public set_password(
    db_name: string,
    username: string,
    password: string
  ): Promise<MongoDBUser> {
    return this.update_user(username, db_name, password);
  }

  public async close(): Promise<void> {
    await this.client.close();
  }
}

export default Mongo;
