package defaults;


import java.util.Optional;

public class Secrets {

    public static final String MANAGER_CONNECT_STRING = Optional.ofNullable(System.getenv("MANAGER_URI")).orElse("jdbc:postgresql://"),
            MANAGER_USER = Optional.ofNullable(System.getenv("MANAGER_USER")).orElse(""),
            MANAGER_PASSWORD = Optional.ofNullable(System.getenv("MANAGER_PASSWORD")).orElse("");


    public static final String POSTGRESS_CONNECT_STRING = Optional.ofNullable(System.getenv("POSTGRESS_URI")).orElse("jdbc:postgresql://"),
            POSTGRESS_USER = Optional.ofNullable(System.getenv("POSTGRESS_USER")).orElse(""),
            POSTGRESS_PASSWORD = Optional.ofNullable(System.getenv("POSTGRESS_PASSWORD")).orElse("");


    public static final String MYSQL_CONNECT_STRING = Optional.ofNullable(System.getenv("MYSQL_URI")).orElse(""),
            MYSQL_USER = Optional.ofNullable(System.getenv("MYSQL_USER")).orElse(""),
            MYSQL_PASSWORD = Optional.ofNullable(System.getenv("MYSQL_PASSWORD")).orElse("");


    public static final String MONGO_CONNECT_STRING = Optional.ofNullable(System.getenv("MONGO_URI")).orElse("mongodb://user:pass@server:port/db?ssl=true");


    // You can use your mail credentials for local development
    public static final String MAIL_HOST = Optional.ofNullable(System.getenv("MAIL_HOST")).orElse("mail.csh.rit.edu");
    public static final String MAIL_USERNAME = Optional.ofNullable(System.getenv("MAIL_USER")).orElse("");
    public static final String MAIL_PASSWORD = Optional.ofNullable(System.getenv("MAIL_PASSWORD")).orElse("");
    public static final String MAIL_ADMIN_UID = Optional.ofNullable(System.getenv("ADMIN_UID")).orElse("");


    public static final int DEFAULT_LIMIT = Integer.parseInt(Optional.ofNullable(System.getenv("DEFAULT-LIMIT")).orElse("5"));
    public static final String[] ORIGINS = Optional.ofNullable(System.getenv("ALLOWED_ORIGINS")).orElse("https://deadass.csh.rit.edu").split(",? ");
}
