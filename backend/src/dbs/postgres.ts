import { Pool } from "pg";
import { Database, DatabaseType, DBConnection, DBUser } from "../db_connection";
import { DBServerConfigStanza } from "../db_wrangler";

export interface PostgresConfigStanza extends DBServerConfigStanza {
  type: "postgres";
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

  public constructor(config: PostgresConfigStanza) {
    this.server = config.name;
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

  public list_dbs(): Promise<Database[]> {
    throw new Error("Method not implemented.");
  }

  public get_db(db_name: string): Promise<Database> {
    throw new Error("Method not implemented.");
  }

  public list_users(db_name?: string): Promise<DBUser[]> {
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
