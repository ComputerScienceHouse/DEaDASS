import Mongo from "./dbs/mongo";
import { Database, DatabaseType, DBConnection, DBUser } from "./db_connection";

export class DBWrangler {
  private readonly conns: DBConnection[];
  /**
   *
   * @param mongo mongodb connection to use
   */
  public constructor(private readonly mongo: Mongo) {
    this.conns = [this.mongo];
  }

  /**
   * Given a DatabaseType, return the connection for that db
   * @param type the database to select
   * @returns the requested DBConnection
   */
  private pick_db(type: DatabaseType): DBConnection {
    switch (type) {
      case "mongo":
        return this.mongo;
      default:
        throw `Unknown database '${<string>type}'`;
    }
  }

  /**
   * Initialise the database conection
   * @returns A promise. When resolved, the database connection will be valid
   */
  public async init(): Promise<void> {
    await Promise.all([this.mongo.init()]);
  }

  /**
   * Check if the db is connected
   */
  public is_connected(): { mongo: { isConnected: boolean } } {
    return {
      mongo: { isConnected: this.mongo.is_connected() },
    };
  }

  /**
   * Get a list of all the databases
   */
  // public list_dbs(): Promise<Array<{ type: DatabaseType; dbs: string[] }>>;
  public list_dbs(): Promise<Record<DatabaseType, string[]>>;
  /**
   * Get a list of databases
   * @param type get only databases of the specific type
   */
  public list_dbs(type: DatabaseType): Promise<string[]>;
  public async list_dbs(
    type?: DatabaseType
  ): Promise<Record<DatabaseType, string[]> | string[]> {
    if (type) return this.pick_db(type).list_dbs();

    return {
      mongo: await this.pick_db("mongo").list_dbs(),
    };
  }

  /**
   * Get details for a specific database
   */
  public get_db(type: DatabaseType, db_name: string): Promise<Database> {
    return this.pick_db(type).get_db(db_name);
  }

  /**
   * Get a list of all users
   */
  public list_users(): Promise<DBUser[]>;

  /**
   * Get a list of users
   * @param db_type limit the list to users of the specific db type
   */
  public list_users(db_type: DatabaseType): Promise<DBUser[]>;

  /**
   * Get a list of users
   * @param db_type limit the list to users of the specific db type
   * @param db_name limit the list to users with access to the given db
   */
  public list_users(db_type: DatabaseType, db_name: string): Promise<DBUser[]>;
  public list_users(
    db_type?: DatabaseType,
    db_name?: string
  ): Promise<DBUser[]> {
    if (db_type) {
      return this.pick_db(db_type).list_users(db_name);
    }
    return Promise.all([this.mongo.list_users()]).then((conns) => conns.flat());
  }

  /**
   * Create a new user account for a person
   * @param type the database type
   * @param username The csh username of the user
   * @param password The account password
   * @param roles The roles to grant the account initially
   * @return the created user
   */
  public create_user_account(
    type: DatabaseType,
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>
  ): Promise<DBUser> {
    return this.pick_db(type).create_user_account(username, password, roles);
  }

  /**
   * Create a new account for a service
   * @param type the database type
   * @param username The username for the service (generally the service name)
   * @param password The account password
   * @param roles The roles to grant the account initially
   * @param db The database to create the account against
   * @return the created user
   */
  public create_service_account(
    type: DatabaseType,
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>,
    db: string
  ): Promise<DBUser> {
    return this.pick_db(type).create_service_account(
      username,
      password,
      roles,
      db
    );
  }

  /**
   * Create a new database and service account
   * @param type the database type
   * @param db_name  The name of the db to create
   * @param username the name of the service account to create
   * @param password  The password to create the new user with
   * @returns the new database and user
   */
  public create_db(
    type: DatabaseType,
    db_name: string,
    username: string,
    password: string
  ): Promise<{ db: Database; user: DBUser }> {
    return this.pick_db(type).create_db(db_name, username, password);
  }

  /**
   * Delete a specific user
   * @param type the database type
   * @param username user to delete
   * @param db database the user is stored in
   */
  public delete_user(
    type: DatabaseType,
    username: string,
    db: string
  ): Promise<void> {
    return this.pick_db(type).delete_user(username, db);
  }

  /**
   * Delete a database
   * @param type the database type
   * @param db_name  The database to be deleted
   */
  public delete_db(type: DatabaseType, db_name: string): Promise<void> {
    return this.pick_db(type).delete_db(db_name);
  }

  /**
   * Update an existing user
   * @param type the database type
   * @param username user to update
   * @param db database the user is stored in
   * @param password (optional) new password to set
   * @param roles (optional) new roles to set
   */
  public update_user(
    type: DatabaseType,
    username: string,
    db: string,
    password?: string,
    roles?: Array<{ db: string; role: string }>
  ): Promise<DBUser> {
    return this.pick_db(type).update_user(username, db, password, roles);
  }

  /**
   * Reset a user's password
   * @param type the database type
   * @param db  The database for which to reset the password
   * @param username  The user who's password is to be reset
   * @param password  The new password
   */
  public set_password(
    type: DatabaseType,
    db: string,
    username: string,
    password: string
  ): Promise<DBUser> {
    return this.pick_db(type).set_password(username, db, password);
  }

  /**
   * Close the database connection and deallocate resources
   */
  public async close(): Promise<void> {
    await Promise.all(this.conns.map((conn) => conn.close()));
  }
}
