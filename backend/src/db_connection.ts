interface DBConnection {
  /**
   * Create a new database, using the db_name to create a new user
   * @param db_name  The name of the db to create
   * @param password  The password to create the new user with
   */
  create(db_name: string, username: string, password: string): void;

  /**
   * Delete a database
   * @param db_name  The database to be deleted
   */
  delete(db_name: string): void;

  /**
   * Reset a user's password
   * @param db_name  The database for which to reset the password
   * @param username  The user who's password is to be reset
   * @param password  The new password
   */
  set_password(db_name: string, username: string, password: string): void;

  /**
   * Close the database connection and deallocate resources
   */
  close(): void;
}

export type { DBConnection };
