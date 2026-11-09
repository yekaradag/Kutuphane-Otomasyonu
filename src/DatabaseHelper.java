import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:kutuphane.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC"); 
            conn = DriverManager.getConnection(DB_URL);
        } catch (Exception e) {
            System.out.println("Veritabanı bağlantı hatası: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, role TEXT NOT NULL, student_no TEXT, name_surname TEXT NOT NULL);";
        String createBooksTable = "CREATE TABLE IF NOT EXISTS Books (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT NOT NULL, category TEXT NOT NULL, status TEXT NOT NULL);";
        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS Transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, book_id INTEGER, user_id INTEGER, borrow_date DATE DEFAULT CURRENT_DATE, return_date DATE, is_returned INTEGER DEFAULT 0, FOREIGN KEY(book_id) REFERENCES Books(id), FOREIGN KEY(user_id) REFERENCES Users(id));";
        
        // YENİ EKLENEN TALEP TABLOSU
        String createRequestsTable = "CREATE TABLE IF NOT EXISTS Requests (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, book_title TEXT NOT NULL, author TEXT NOT NULL, status TEXT DEFAULT 'Bekliyor', FOREIGN KEY(user_id) REFERENCES Users(id));";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createBooksTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createRequestsTable); // Talepler tablosunu çalıştır
            System.out.println("Veritabanı ve tablolar başarıyla oluşturuldu!");
        } catch (Exception e) {
            System.out.println("Tablo oluşturma hatası: " + e.getMessage());
        }
    }
}