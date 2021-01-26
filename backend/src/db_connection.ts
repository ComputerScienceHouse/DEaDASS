export interface DBConnection {
  /**
   * Initialise the database conection
   * @returns A promise. When resolved, the database connection will be valid
   */
  init(): Promise<void>;

  /**
   * Get a list of all the databases
   * @returns A promise resolving to a list of all dbs
   */
  list_dbs(): Promise<string[]>;

  /**
   * Get a list of all users
   * @returns A promise
   */
  list_users(): Promise<DBUser[]>;

  /**
   * Create a new user account for a person
   * @param username The csh username of the user
   * @param password The account password
   * @param roles The roles to grant the account initially
   */
  create_user_account(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>
  ): Promise<void>;

  /**
   * Create a new account for a service
   * @param username The username for the service (generally the service name)
   * @param password The account password
   * @param roles The roles to grant the account initially
   * @param db The database to create the account against
   * // TODO this ^ concept likely won't work for SQL, and will need to be reworked to store metadata somewhere
   */
  create_service_account(
    username: string,
    password: string,
    roles: Array<{ db: string; role: string }>,
    db: string
  ): Promise<void>;

  /**
   * Create a new database, using the db_name to create a new user
   * @param db_name  The name of the db to create
   * @param password  The password to create the new user with
   */
  create(db_name: string, username: string, password: string): Promise<void>;

  /**
   * Delete a database
   * @param db_name  The database to be deleted
   */
  delete(db_name: string): Promise<void>;

  /**
   * Reset a user's password
   * @param db_name  The database for which to reset the password
   * @param username  The user who's password is to be reset
   * @param password  The new password
   */
  set_password(
    db_name: string,
    username: string,
    password: string
  ): Promise<void>;

  /**
   * Close the database connection and deallocate resources
   */
  close(): Promise<void>;
}

export type DBUser = {
  user: string;
  roles: Array<{ role: string; db: string }>;
};
