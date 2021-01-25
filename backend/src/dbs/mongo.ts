import DBConnection from "../db_connection";
import { MongoClient } from "mongodb";

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

  public create(
    db_name: string,
    username: string,
    password: string
  ): Promise<void> {
    // Check if the db is already in use
    return (
      this.client
        .db("admin")
        .admin()
        .listDatabases()
        .then((response: { databases: Array<{ name: string }> }) => {
          if (
            response.databases
              .map((db: { name: string }) => db.name)
              .includes(db_name)
          ) {
            throw `db {db_name} already exists`;
          }
        })
        // If it's not in use, we can create it
        .then(() => {
          this.client
            .db(db_name)
            .addUser(username, password, {
              roles: [{ role: "dbOwner", db: db_name }],
            })
            .catch((error) => {
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
            `Error setting password for {username} in mongo db {db_name}`
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
