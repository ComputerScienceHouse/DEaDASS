import Mongo, { MongoConfigStanza } from "./dbs/mongo";
import Postgres, { PostgresConfigStanza } from "./dbs/postgres";
import { Database, DBConnection, DBUser } from "./db_connection";

export interface DBServerConfigStanza {
  type: string;
  name: string;
}

export type ServerStatus = { isConnected: boolean };

class MapWithMap<K, V> extends Map<K, V> {
  // eslint is disagreeing with prettier here, so we need the disable
  // eslint-disable-next-line @typescript-eslint/type-annotation-spacing
  public map<T>(mapfunc: (value: V, key: K, map: Map<K, V>) => T): T[] {
    const results: T[] = [];
    this.forEach((value, key, map) => results.push(mapfunc(value, key, map)));
    return results;
  }
}

export class DBWrangler {
  private readonly conns: MapWithMap<string, DBConnection> = new MapWithMap<
    string,
    DBConnection
  >();

  public constructor(config_stanza: DBServerConfigStanza[]) {
    config_stanza.forEach((dbstanza) => {
      switch (dbstanza.type) {
        case "mongo":
          this.conns.set(dbstanza.name, new Mongo(<MongoConfigStanza>dbstanza));
          break;
        case "postgres":
          this.conns.set(
            dbstanza.name,
            new Postgres(<PostgresConfigStanza>dbstanza)
          );
          break;
        default:
          console.error(
            `Unrecongised db config for '${dbstanza.name}' of type '${dbstanza.type}'`
          );
      }
    });
  }

  /**
   * Given a database name, return the connection for that db
   * @param server the database to select
   * @returns the requested DBConnection
   */
  private pick_db(server: string): DBConnection {
    const retval = this.conns.get(server);
    if (retval) return retval;
    throw `Unknown database '${server}'`;
  }

  /**
   * Initialise the database conection
   * @return A promise. When resolved, all the server connections will be valid
   */
  public async init(): Promise<void> {
    await Promise.all(this.conns.map((conn) => conn.init()));
  }

  /**
   * Check if the db is connected
   */
  public async is_connected(): Promise<MapWithMap<string, ServerStatus>> {
    return new MapWithMap<string, ServerStatus>(
      await Promise.all(
        this.conns.map((conn, server) =>
          conn.is_connected().then((val): [string, ServerStatus] => {
            return [
              server,
              {
                isConnected: val,
              },
            ];
          })
        )
      )
    );
  }

  /**
   * Get a list of all the databases
   */
  // public list_dbs(): Promise<Array<{ type: DatabaseType; dbs: string[] }>>;
  public list_dbs(): Promise<Database[]>;
  /**
   * Get a list of databases
   * @param server get only databases from the specified server
   */
  public list_dbs(server: string): Promise<Database[]>;
  public list_dbs(server?: string): Promise<Database[]> {
    if (server) return this.pick_db(server).list_dbs();
    return Promise.all(this.conns.map((conn) => conn.list_dbs())).then((dbs) =>
      dbs.flat()
    );
  }

  /**
   * Get details for a specific database
   * @param server where the db is hosted
   * @param db_name the specific db
   */
  public get_db(server: string, db_name: string): Promise<Database> {
    return this.pick_db(server).get_db(db_name);
  }

  /**
   * Get a list of all users
   */
  public list_users(): Promise<DBUser[]>;

  /**
   * Get a list of users
   * @param server limit the list to users of the specific server
   */
  public list_users(server: string): Promise<DBUser[]>;

  /**
   * Get a list of users
   * @param server limit the list to users of the specific server
   * @param db_name limit the list to users with access to the given db
   */
  public list_users(server: string, db_name: string): Promise<DBUser[]>;
  public list_users(server?: string, db_name?: string): Promise<DBUser[]> {
    if (server) {
      return this.pick_db(server).list_users(db_name);
    }
    return Promise.all(
      this.conns.map((conn) => conn.list_users())
    ).then((conns) => conns.flat());
  }

  /**
   * Create a new user account for a person
   * @param server the server to create the account on
   * @param username The csh username of the user
   * @param password The account password
   * @param roles The roles to grant the account initially
   * @return the created user
   */
  public create_user_account(
    server: string,
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>
  ): Promise<DBUser> {
    return this.pick_db(server).create_user_account(username, password, roles);
  }

  /**
   * Create a new account for a service
   * @param server where the account should be created
   * @param username The username for the service (generally the service name)
   * @param password The account password
   * @param roles The roles to grant the account initially
   * @param db The database to create the account against
   * @return the created user
   */
  public create_service_account(
    server: string,
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>,
    db: string
  ): Promise<DBUser> {
    return this.pick_db(server).create_service_account(
      username,
      password,
      roles,
      db
    );
  }

  /**
   * Create a new database and service account
   * @param server where to create the db
   * @param db_name  The name of the db to create
   * @param username the name of the service account to create
   * @param password  The password to create the new user with
   * @returns the new database and user
   */
  public create_db(
    server: string,
    db_name: string,
    username: string,
    password: string
  ): Promise<{ db: Database; user: DBUser }> {
    return this.pick_db(server).create_db(db_name, username, password);
  }

  /**
   * Delete a specific user
   * @param server the server the account is hosted on
   * @param username user to delete
   * @param db database the user is stored in
   */
  public delete_user(
    server: string,
    username: string,
    db: string
  ): Promise<void> {
    return this.pick_db(server).delete_user(username, db);
  }

  /**
   * Delete a database
   * @param server where the db is hosted
   * @param db_name  The database to be deleted
   */
  public delete_db(server: string, db_name: string): Promise<void> {
    return this.pick_db(server).delete_db(db_name);
  }

  /**
   * Update an existing user
   * @param server the server the user exists on
   * @param username user to update
   * @param db database the user is stored in
   * @param password (optional) new password to set
   * @param roles (optional) new roles to set
   */
  public update_user(
    server: string,
    username: string,
    db: string,
    password?: string,
    roles?: Array<{ db: string; role: string }>
  ): Promise<DBUser> {
    return this.pick_db(server).update_user(username, db, password, roles);
  }

  /**
   * Reset a user's password
   * @param server the server the user exists on
   * @param db  The database for which to reset the password
   * @param username  The user who's password is to be reset
   * @param password  The new password
   */
  public set_password(
    server: string,
    db: string,
    username: string,
    password: string
  ): Promise<DBUser> {
    return this.pick_db(server).set_password(username, db, password);
  }

  /**
   * Close the database connections and deallocate resources
   */
  public async close(): Promise<void> {
    await Promise.all(this.conns.map((conn) => conn.close()));
  }
}
