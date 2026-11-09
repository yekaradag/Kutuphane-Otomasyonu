import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginUI extends JFrame {
    private JTextField txtKullanici;
    private JPasswordField txtSifre;
    private JButton btnGiris;

    public LoginUI() {
        setTitle("Köy Okulu Kütüphanesi - Giriş");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new GridLayout(4, 1, 10, 10)); 

        JPanel pnlUser = new JPanel();
        pnlUser.add(new JLabel("Kullanıcı Adı / Öğrenci No:"));
        txtKullanici = new JTextField(15);
        pnlUser.add(txtKullanici);

        JPanel pnlPass = new JPanel();
        pnlPass.add(new JLabel("Şifre:"));
        txtSifre = new JPasswordField(15);
        pnlPass.add(txtSifre);

        JPanel pnlBtn = new JPanel();
        btnGiris = new JButton("Giriş Yap");
        pnlBtn.add(btnGiris);

        add(new JLabel("Sisteme Hoş Geldiniz", SwingConstants.CENTER));
        add(pnlUser);
        add(pnlPass);
        add(pnlBtn);

        // Giriş Butonu Tıklanma Olayı
        btnGiris.addActionListener(e -> girisYap());
    }

    private void girisYap() {
        String kullanici = txtKullanici.getText();
        String sifre = new String(txtSifre.getPassword());

        // Hızlı test için Admin girişi
        if (kullanici.equals("admin") && sifre.equals("admin")) {
            new TeacherUI().setVisible(true); 
            this.dispose(); 
            return;
        }

        // Veritabanından gerçek kullanıcı kontrolü
        try (Connection conn = DatabaseHelper.connect()) {
            String query = "SELECT * FROM Users WHERE (username = ? OR student_no = ?) AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, kullanici);
            pstmt.setString(2, kullanici);
            pstmt.setString(3, sifre);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String rol = rs.getString("role");
                
                if (rol.equals("OGRETMEN")) {
                    new TeacherUI().setVisible(true); 
                    this.dispose();
                } else {
                    // İŞTE BURASI GÜNCELLENDİ: Öğrenci bilgileri çekilip kendi paneli açılıyor!
                    int userId = rs.getInt("id");
                    String adSoyad = rs.getString("name_surname");
                    new StudentUI(userId, adSoyad).setVisible(true);
                    this.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı Kullanıcı Adı veya Şifre!", "Giriş Başarısız", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Veritabanı Hatası: " + ex.getMessage());
        }
    }
}