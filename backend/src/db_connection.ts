interface DBConnection {
  /**
   * Initialise the database conection
   * @returns A promise. When resolved, the database connection will be valid
   */
  init(): Promise<void>;

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

export default DBConnection;
