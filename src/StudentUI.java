import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentUI extends JFrame {
    private int userId;
    private String adSoyad;

    // Katalog Bileşenleri
    private JTable tableKatalog;
    private DefaultTableModel modelKatalog;
    private JTextField txtArama;
    private JComboBox<String> cmbKategori;
    private JLabel lblDurum; 

    // Profil Bileşenleri
    private JTable tableProfil;
    private DefaultTableModel modelProfil;
    private JLabel lblOkunmaSayisi;

    // Talep Bileşenleri (YENİ)
    private JTable tableTalepler;
    private DefaultTableModel modelTalepler;
    private JTextField txtTalepKitap, txtTalepYazar;

    public StudentUI(int userId, String adSoyad) {
        this.userId = userId;
        this.adSoyad = adSoyad;

        setTitle("Öğrenci Paneli - Kütüphane Otomasyonu | Hoşgeldin, " + adSoyad);
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📖 Dijital Katalog", createKatalogPanel());
        tabbedPane.addTab("👤 Kişisel Profilim", createProfilPanel());
        tabbedPane.addTab("🙋‍♂️ Kitap Talep Et", createTalepPanel()); // YENİ SEKME

        add(tabbedPane);

        katalogListele();
        profilListele();
        talepleriListele();
        durumGuncelle();
    }

    private JPanel createKatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlUst = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlUst.add(new JLabel("Kitap Ara:"));
        txtArama = new JTextField(20);
        pnlUst.add(txtArama);
        pnlUst.add(new JLabel("Kategori:"));
        cmbKategori = new JComboBox<>(new String[]{"Tümü", "Roman", "Bilim", "Tarih", "Edebiyat", "Çocuk"});
        pnlUst.add(cmbKategori);
        panel.add(pnlUst, BorderLayout.NORTH);

        txtArama.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { filtrele(); } });
        cmbKategori.addActionListener(e -> filtrele());

        String[] kolonlar = {"Başlık", "Yazar", "Kategori", "Mevcudiyet Durumu"};
        modelKatalog = new DefaultTableModel(kolonlar, 0);
        tableKatalog = new JTable(modelKatalog);
        panel.add(new JScrollPane(tableKatalog), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblDurum = new JLabel("Toplam Kitap: 0 | Ödünçteki Kitap: 0");
        lblDurum.setFont(new Font("Arial", Font.BOLD, 12));
        pnlAlt.add(lblDurum);
        panel.add(pnlAlt, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProfilPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlUst = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblOkunmaSayisi = new JLabel("Bugüne kadar okuduğun toplam kitap sayısı: 0");
        lblOkunmaSayisi.setFont(new Font("Arial", Font.BOLD, 14));
        pnlUst.add(lblOkunmaSayisi);
        panel.add(pnlUst, BorderLayout.NORTH);

        String[] kolonlar = {"Kitap Adı", "Alış Tarihi", "Son Teslim Tarihi", "Durum"};
        modelProfil = new DefaultTableModel(kolonlar, 0);
        tableProfil = new JTable(modelProfil);
        panel.add(new JScrollPane(tableProfil), BorderLayout.CENTER);

        return panel;
    }

    // YENİ EKLENEN TALEP PANELİ
    private JPanel createTalepPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Üst Kısım: Form
        JPanel pnlForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlForm.add(new JLabel("Önerdiğin Kitap Adı:"));
        txtTalepKitap = new JTextField(15);
        pnlForm.add(txtTalepKitap);
        
        pnlForm.add(new JLabel("Yazarı:"));
        txtTalepYazar = new JTextField(15);
        pnlForm.add(txtTalepYazar);
        
        JButton btnTalepGonder = new JButton("Öğretmene Talep Gönder");
        pnlForm.add(btnTalepGonder);
        panel.add(pnlForm, BorderLayout.NORTH);

        // Orta Kısım: Geçmiş Taleplerim Tablosu
        String[] kolonlar = {"Talep Edilen Kitap", "Yazar", "Talep Durumu"};
        modelTalepler = new DefaultTableModel(kolonlar, 0);
        tableTalepler = new JTable(modelTalepler);
        panel.add(new JScrollPane(tableTalepler), BorderLayout.CENTER);

        // Buton Olayı
        btnTalepGonder.addActionListener(e -> talepGonder());

        return panel;
    }

    private void katalogListele() {
        modelKatalog.setRowCount(0);
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT title, author, category, status FROM Books")) {
            while (rs.next()) { modelKatalog.addRow(new Object[]{ rs.getString("title"), rs.getString("author"), rs.getString("category"), rs.getString("status") }); }
        } catch (Exception ex) { }
    }

    private void filtrele() {
        String aranan = txtArama.getText();
        String secilenKategori = cmbKategori.getSelectedItem().toString();
        TableRowSorter<DefaultTableModel> tr = new TableRowSorter<>(modelKatalog);
        tableKatalog.setRowSorter(tr);

        if (secilenKategori.equals("Tümü")) { tr.setRowFilter(RowFilter.regexFilter("(?i)" + aranan)); } 
        else { tr.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(RowFilter.regexFilter("(?i)" + aranan), RowFilter.regexFilter("(?i)" + secilenKategori, 2)))); }
    }

    private void profilListele() {
        modelProfil.setRowCount(0);
        int okunanSayisi = 0;
        String sql = "SELECT b.title, t.borrow_date, t.return_date, t.is_returned FROM Transactions t JOIN Books b ON t.book_id = b.id WHERE t.user_id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                okunanSayisi++;
                String durum = rs.getInt("is_returned") == 1 ? "İade Edildi" : "Hala Sende";
                modelProfil.addRow(new Object[]{ rs.getString("title"), rs.getString("borrow_date"), rs.getString("return_date"), durum });
            }
            lblOkunmaSayisi.setText("Bugüne kadar okuduğun toplam kitap sayısı: " + okunanSayisi);
        } catch (Exception ex) { }
    }

    // YENİ EKLENEN METOTLAR
    private void talepleriListele() {
        modelTalepler.setRowCount(0);
        String sql = "SELECT book_title, author, status FROM Requests WHERE user_id = ?";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                modelTalepler.addRow(new Object[]{ rs.getString("book_title"), rs.getString("author"), rs.getString("status") });
            }
        } catch (Exception ex) { }
    }

    private void talepGonder() {
        String kitap = txtTalepKitap.getText().trim();
        String yazar = txtTalepYazar.getText().trim();

        if (kitap.isEmpty() || yazar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen kitap adı ve yazar alanlarını doldurun!");
            return;
        }

        String sql = "INSERT INTO Requests (user_id, book_title, author) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.userId);
            pstmt.setString(2, kitap);
            pstmt.setString(3, yazar);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Kitap talebiniz öğretmene başarıyla iletildi!");
            txtTalepKitap.setText(""); txtTalepYazar.setText("");
            talepleriListele(); // Tabloyu güncelle
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage()); }
    }

    private void durumGuncelle() {
        int toplam = 0, odunc = 0;
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) AS sayi FROM Books");
            if (rs1.next()) toplam = rs1.getInt("sayi");

            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) AS sayi FROM Books WHERE status = 'ODUNCTE'");
            if (rs2.next()) odunc = rs2.getInt("sayi");

            lblDurum.setText("Toplam Kitap: " + toplam + " | Ödünçteki Kitap: " + odunc);
        } catch (Exception ex) { }
    }
}