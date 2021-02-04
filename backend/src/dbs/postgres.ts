import { Pool } from "pg";
import { Database, DatabaseType, DBConnection, DBUser } from "../db_connection";
import { DBServerConfigStanza } from "../db_wrangler";

export interface PostgresConfigStanza extends DBServerConfigStanza {
  type: "postgres";
  // TODO prune
  auth: {
    host?: string;
    port?: number;
    user: string;
    database?: string;
    password?: string;
    ssl?: boolean;
  };
}

export default class Postgres implements DBConnection {
  public readonly server: string;
  public readonly type: DatabaseType = "postgres";
  private readonly pool: Pool;

  public constructor(private readonly config: PostgresConfigStanza) {
    this.server = config.name;
    console.log(config.auth);
    this.pool = new Pool(config.auth);
  }

  public init(): Promise<void> {
    // Open and close a connection to make sure we can connect
    return this.pool.connect().then((client) => client.release());
  }

  public is_connected(): Promise<boolean> {
    return this.pool
      .connect()
      .then((client) => client.release())
      .then(() => true)
      .catch(() => false);
  }

  // See https://www.postgresql.org/docs/13/ddl-priv.html#PRIVILEGE-ABBREVS-TABLE
  private parse_acl(acl?: string) {
    // TODO need to pull default privileges from the server
    if (!acl) return [];
    // Remove the braces
    acl = acl.substring(1, acl.length - 1);
    
    const acls: string[] = acl.split(",");
// TODO need to actually figure out what the acls mean
    )

  }

  public async list_dbs(): Promise<Database[]> {
    return Promise.all(
      await this.pool
        .query("SELECT datname, datacl FROM pg_database WHERE datistemplate = false;")
        .then((result: { rows: Array<{ datname: string; datacl: string }> }) =>
          result.rows.map(({ datname, datacl}) => {
    return {
      type: "postgres",
      server: this.server,
      name: datname,
      users: await this.list_users(db_name),
    };
          }
        )
    );
  }

  // TODO probably need for querying things
  private get_db_conn(db_name: string): Pool {
    const config = this.config.auth;
    config.database = db_name;
    return new Pool(config);
  }

  public async get_db(db_name: string): Promise<Database> {
    return {
      type: "postgres",
      server: this.server,
      name: db_name,
      users: await this.list_users(db_name),
    };
  }

  // TODO relevant docs for the system schemas
  /**
   * https://www.postgresql.org/docs/current/information-schema.html
   * https://www.postgresql.org/docs/current/catalogs-overview.html
   */

  public list_users(db_name?: string): Promise<DBUser[]> {
    // Get only users with access to the specific db
    if (db_name)
      // TODO actually filter by access
      return new Promise<DBUser[]>((resolve) => resolve([]));
    // return this.pool
    //   .query("SELECT username from pg_catalog.pg_user;")
    //   .then((result: { rows: Array<{ username: string }> }) =>
    //     result.rows.map((row) => row.username)
    //   );
    // Get all users
    else
      return this.pool
        .query("SELECT username from pg_catalog.pg_user;")
        .then((result: { rows: Array<{ username: string }> }) =>
          result.rows.map((row) => {
            return {
              server: this.server,
              type: "postgres",
              user: row.username,
              roles: [], // TODO roles
              extra_data: null,
            };
          })
        );
    // TODO by db
    // TODO roles
    throw new Error("Method not implemented.");
  }

  public create_user_account(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>
  ): Promise<DBUser> {
    throw new Error("Method not implemented.");
  }

  public create_service_account(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>,
    db: string
  ): Promise<DBUser> {
    throw new Error("Method not implemented.");
  }

  public create_db(
    db_name: string,
    username: string,
    password: string
  ): Promise<{ db: Database; user: DBUser }> {
    throw new Error("Method not implemented.");
  }

  public delete_user(username: string, db: string): Promise<void> {
    throw new Error("Method not implemented.");
  }

  public delete_db(db_name: string): Promise<void> {
    throw new Error("Method not implemented.");
  }

  public update_user(
    username: string,
    db: string,
    password?: string,
    roles?: Array<{ db: string; role: string }>
  ): Promise<DBUser> {
    throw new Error("Method not implemented.");
  }

  public set_password(
    db_name: string,
    username: string,
    password: string
  ): Promise<DBUser> {
    throw new Error("Method not implemented.");
  }

  public close(): Promise<void> {
    return this.pool.end();
  }
}
