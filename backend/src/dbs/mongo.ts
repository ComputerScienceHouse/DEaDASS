import { DBConnection } from "../db_connection";
import { MongoClient } from "mongodb";

class Mongo implements DBConnection {
  private readonly client: MongoClient;

  /**
   * Instantiates a connection to the database
   * @param connection_string Mongodb connection string
   */
  public constructor(connection_string: string) {
    this.client = new MongoClient(connection_string);
    this.client
      .connect()
      .then(() => console.log("Connected to mongo"))
      .catch((error: Error) => {
        console.error("Error connecting to mongo");
        console.error(error);
        throw error;
      });

    this.client.on("error", (error: Error) => {
      console.error("Error connecting to mongo");
      console.error(error);
      throw error;
    });
  }

  public create(db_name: string, username: string, password: string): void {
    this.client
      .db(db_name)
      .addUser(
        username,
        password,
        { roles: [{ role: "dbOwner", db: db_name }] },
        (error) => {
          if (error) {
            console.error("Error creating new mongo user for db " + db_name);
            console.error(error);
            throw error;
          }
        }
      );
  }

  public delete(db_name: string): void {
    this.client
      .db(db_name)
      .dropDatabase()
      .catch((error) => {
        console.error("Error deleting mongo db " + db_name);
        console.error(error);
        throw error;
      });
  }

  public set_password(
    db_name: string,
    username: string,
    password: string
  ): void {
    this.client.db(db_name).removeUser(username, (error) => {
      if (error) {
        console.error("Error removing mongo user for db " + db_name);
        console.error(error);
        throw error;
      }
    });

    this.client
      .db(db_name)
      .addUser(
        username,
        password,
        { roles: [{ role: "dbOwner", db: db_name }] },
        (error) => {
          if (error) {
            console.error("Error recreating mongo user for db " + db_name);
            console.error(error);
            throw error;
          }
        }
      );
  }

  public close(): void {
    this.client.close().catch((error: Error) => {
      console.error("Error closing mongo");
      console.error(error);
      throw error;
    });
  }
}

module.exports = Mongo;
