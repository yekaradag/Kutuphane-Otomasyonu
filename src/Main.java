import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 1. Önce veritabanı altyapısını hazırla
        DatabaseHelper.initializeDatabase();
        
        // 2. Arayüzü başlat
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginUI loginEkrani = new LoginUI();
                loginEkrani.setVisible(true); // Pencereyi görünür yap
            }
        });
    }
}
