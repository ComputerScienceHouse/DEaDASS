import type { DBConnection, DBUser } from "../db_connection";
import { MongoClient } from "mongodb";

interface SystemUsersSchema {
  _id: unknown;
  user: string;
  db: string;
  roles: Array<{ role: string; db: string }>;
  customData: unknown;
}

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

  public list_dbs(): Promise<string[]> {
    return this.client
      .db("admin")
      .admin()
      .listDatabases()
      .then((response: { databases: Array<{ name: string }> }) =>
        response.databases.map((db: { name: string }) => db.name)
      );
  }

  public list_users(): Promise<DBUser[]> {
    return this.client
      .db("admin")
      .collection("system.users")
      .find()
      .map((document: SystemUsersSchema) => {
        return {
          user: document.user,
          roles: document.roles,
        };
      })
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
   */
  private create_user(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>,
    db_name: string
  ): Promise<void> {
    return this.does_user_exist(username, db_name).then((user_exists) => {
      if (roles.length == 0) {
        return Promise.reject("Creating a user without roles is disallowed");
      }
      if (!user_exists) {
        return (
          this.client
            .db(db_name)
            .addUser(username, password, {
              roles: roles,
            })
            // eslint-disable-next-line @typescript-eslint/no-empty-function
            .then(() => {})
        );
      } else throw "User already exists";
    });
  }

  public create_user_account(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>
  ): Promise<void> {
    return this.create_user(username, password, roles, "admin");
  }

  public create_service_account(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>,
    db: string
  ): Promise<void> {
    return this.create_user(username, password, roles, db);
  }

  public create(
    db_name: string,
    username: string,
    password: string
  ): Promise<void> {
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
        .catch((error) => {
          console.error(error);
          throw error;
        })
    );
  }

  public delete(db_name: string): Promise<void> {
    return (
      this.client
        .db(db_name)
        .command({ dropAllUsersFromDatabase: 1 })
        .then(() => this.client.db(db_name).dropDatabase())
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        .then(() => {})
        .catch((error) => {
          console.error("Error deleting mongo db " + db_name);
          console.error(error);
          throw error;
        })
    );
  }

  public set_password(
    db_name: string,
    username: string,
    password: string
  ): Promise<void> {
    return (
      this.client
        .db(db_name)
        .command({ updateUser: username, pwd: password })
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        .then(() => {})
        .catch((error) => {
          console.error(
            `Error setting password for ${username} in mongo db ${db_name}`
          );
          console.error(error);
          throw error;
        })
    );
  }

  public async close(): Promise<void> {
    await this.client.close();
  }
}

export default Mongo;
