import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;

public class TeacherUI extends JFrame {
    // 1. Kitap Yönetimi
    private JTable tableKitaplar;
    private DefaultTableModel modelKitaplar;
    private JTextField txtArama, txtBaslik, txtYazar;
    private JComboBox<String> cmbKategori;

    // 2. Üye Yönetimi
    private JTable tableUyeler;
    private DefaultTableModel modelUyeler;
    private JTextField txtUyeKullanici, txtUyeSifre, txtUyeAdSoyad, txtUyeNo;
    private JComboBox<String> cmbUyeRol;

    // 3. Ödünç/İade Yönetimi
    private JTable tableIslemler;
    private DefaultTableModel modelIslemler;
    private JComboBox<String> cmbOduncKitap, cmbOduncUye;
    private JTextField txtGunSayisi;

    // 4. Raporlar & Karneler
    private JComboBox<String> cmbKarneUye;
    private JTable tableKarne;
    private DefaultTableModel modelKarne;

    // 5. Talepler Yönetimi (YENİ)
    private JTable tableTalepler;
    private DefaultTableModel modelTalepler;
    
    public TeacherUI() {
        setTitle("Öğretmen Paneli - Kütüphane Otomasyonu");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("📚 Kitap Yönetimi", createKitapYonetimiPanel());
        tabbedPane.addTab("👥 Üye İşlemleri", createUyeYonetimiPanel());
        tabbedPane.addTab("🔄 Ödünç/İade İşlemleri", createOduncPanel()); 
        tabbedPane.addTab("📊 Raporlar & Karneler", createRaporlarPanel()); 
        tabbedPane.addTab("📩 Gelen Talepler", createTaleplerPanel()); // YENİ SEKME EKLENDİ

        add(tabbedPane);
        
        kitaplariListele();
        uyeleriListele();
        islemleriListele();
        gelenTalepleriListele();
    }

    // ==========================================
    // 1. KİTAP YÖNETİMİ 
    // ==========================================
    private JPanel createKitapYonetimiPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlArama = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlArama.add(new JLabel("Kitap Ara:"));
        txtArama = new JTextField(30);
        pnlArama.add(txtArama);
        
        txtArama.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String aranan = txtArama.getText();
                TableRowSorter<DefaultTableModel> tr = new TableRowSorter<>(modelKitaplar);
                tableKitaplar.setRowSorter(tr);
                tr.setRowFilter(RowFilter.regexFilter("(?i)" + aranan)); 
            }
        });
        panel.add(pnlArama, BorderLayout.NORTH);

        String[] kolonlar = {"ID", "Başlık", "Yazar", "Kategori", "Durum"};
        modelKitaplar = new DefaultTableModel(kolonlar, 0);
        tableKitaplar = new JTable(modelKitaplar);
        panel.add(new JScrollPane(tableKitaplar), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel pnlForm = new JPanel(new FlowLayout());
        pnlForm.add(new JLabel("Başlık:")); txtBaslik = new JTextField(15); pnlForm.add(txtBaslik);
        pnlForm.add(new JLabel("Yazar:")); txtYazar = new JTextField(10); pnlForm.add(txtYazar);
        pnlForm.add(new JLabel("Kategori:")); cmbKategori = new JComboBox<>(new String[]{"Roman", "Bilim", "Tarih", "Edebiyat", "Çocuk"}); pnlForm.add(cmbKategori);

        JPanel pnlButonlar = new JPanel(new FlowLayout());
        JButton btnEkle = new JButton("Yeni Kitap Ekle"); JButton btnSil = new JButton("Seçili Kitabı Sil");
        pnlButonlar.add(btnEkle); pnlButonlar.add(btnSil);
        
        pnlAlt.add(pnlForm); pnlAlt.add(pnlButonlar); panel.add(pnlAlt, BorderLayout.SOUTH);

        btnEkle.addActionListener(e -> kitapEkle()); btnSil.addActionListener(e -> kitapSil());

        return panel;
    }

    private void kitaplariListele() {
        modelKitaplar.setRowCount(0); 
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM Books")) {
            while (rs.next()) { modelKitaplar.addRow(new Object[]{ rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("category"), rs.getString("status") }); }
        } catch (Exception ex) { }
        comboboxlariGuncelle();
    }

    private void kitapEkle() {
        String baslik = txtBaslik.getText().trim(); String yazar = txtYazar.getText().trim();
        String kategori = cmbKategori.getSelectedItem().toString();
        if (baslik.isEmpty() || yazar.isEmpty()) return;

        try (Connection conn = DatabaseHelper.connect()) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Books (title, author, category, status) VALUES (?, ?, ?, 'RAFTA')");
            pstmt.setString(1, baslik); pstmt.setString(2, yazar); pstmt.setString(3, kategori);
            pstmt.executeUpdate();
            txtBaslik.setText(""); txtYazar.setText("");
            kitaplariListele(); 
        } catch (Exception ex) { }
    }

    private void kitapSil() {
        int seciliSatir = tableKitaplar.getSelectedRow();
        if (seciliSatir == -1) return;
        int gercekSatir = tableKitaplar.convertRowIndexToModel(seciliSatir);
        int kitapId = (int) modelKitaplar.getValueAt(gercekSatir, 0);

        try (Connection conn = DatabaseHelper.connect()) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Books WHERE id = ?");
            pstmt.setInt(1, kitapId); pstmt.executeUpdate();
            kitaplariListele();
        } catch (Exception ex) { }
    }

    // ==========================================
    // 2. ÜYE YÖNETİMİ 
    // ==========================================
    private JPanel createUyeYonetimiPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] kolonlar = {"ID", "Kullanıcı Adı", "Ad Soyad", "Rol", "Öğrenci No"};
        modelUyeler = new DefaultTableModel(kolonlar, 0);
        tableUyeler = new JTable(modelUyeler);
        panel.add(new JScrollPane(tableUyeler), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel pnlForm = new JPanel(new FlowLayout());
        
        pnlForm.add(new JLabel("Kullanıcı Adı:")); txtUyeKullanici = new JTextField(8); pnlForm.add(txtUyeKullanici);
        pnlForm.add(new JLabel("Şifre:")); txtUyeSifre = new JTextField(8); pnlForm.add(txtUyeSifre);
        pnlForm.add(new JLabel("Ad Soyad:")); txtUyeAdSoyad = new JTextField(10); pnlForm.add(txtUyeAdSoyad);
        pnlForm.add(new JLabel("Rol:")); cmbUyeRol = new JComboBox<>(new String[]{"OGRENCI", "OGRETMEN"}); pnlForm.add(cmbUyeRol);
        pnlForm.add(new JLabel("Öğrenci No:")); txtUyeNo = new JTextField(6); pnlForm.add(txtUyeNo);

        JPanel pnlButonlar = new JPanel(new FlowLayout());
        JButton btnUyeEkle = new JButton("Yeni Üye Ekle"); JButton btnUyeSil = new JButton("Seçili Üyeyi Sil");
        pnlButonlar.add(btnUyeEkle); pnlButonlar.add(btnUyeSil);
        
        pnlAlt.add(pnlForm); pnlAlt.add(pnlButonlar); panel.add(pnlAlt, BorderLayout.SOUTH);

        cmbUyeRol.addActionListener(e -> {
            if (cmbUyeRol.getSelectedItem().equals("OGRETMEN")) { txtUyeNo.setText(""); txtUyeNo.setEnabled(false); } 
            else { txtUyeNo.setEnabled(true); }
        });

        btnUyeEkle.addActionListener(e -> uyeEkle()); btnUyeSil.addActionListener(e -> uyeSil());
        return panel;
    }

    private void uyeleriListele() {
        modelUyeler.setRowCount(0); 
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM Users")) {
            while (rs.next()) { modelUyeler.addRow(new Object[]{ rs.getInt("id"), rs.getString("username"), rs.getString("name_surname"), rs.getString("role"), rs.getString("student_no") }); }
        } catch (Exception ex) { }
        comboboxlariGuncelle();
    }

    private void uyeEkle() {
        String kullanici = txtUyeKullanici.getText().trim(); String sifre = txtUyeSifre.getText().trim();
        String adSoyad = txtUyeAdSoyad.getText().trim(); String rol = cmbUyeRol.getSelectedItem().toString();
        String ogrNo = txtUyeNo.getText().trim();
        if (kullanici.isEmpty() || sifre.isEmpty() || adSoyad.isEmpty()) return;

        try (Connection conn = DatabaseHelper.connect()) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Users (username, password, role, student_no, name_surname) VALUES (?, ?, ?, ?, ?)");
            pstmt.setString(1, kullanici); pstmt.setString(2, sifre); pstmt.setString(3, rol);
            pstmt.setString(4, ogrNo.isEmpty() ? null : ogrNo); pstmt.setString(5, adSoyad); pstmt.executeUpdate();
            txtUyeKullanici.setText(""); txtUyeSifre.setText(""); txtUyeAdSoyad.setText(""); txtUyeNo.setText("");
            uyeleriListele(); 
        } catch (Exception ex) { }
    }

    private void uyeSil() {
        int seciliSatir = tableUyeler.getSelectedRow();
        if (seciliSatir == -1) return;
        int uyeId = (int) modelUyeler.getValueAt(seciliSatir, 0);

        try (Connection conn = DatabaseHelper.connect()) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Users WHERE id = ?");
            pstmt.setInt(1, uyeId); pstmt.executeUpdate();
            uyeleriListele();
        } catch (Exception ex) { }
    }

    // ==========================================
    // 3. ÖDÜNÇ/İADE İŞLEMLERİ 
    // ==========================================
    private JPanel createOduncPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] kolonlar = {"İşlem ID", "Kitap Adı", "Üye Adı", "Veriliş Tarihi", "Son Teslim Tarihi", "Durum"};
        modelIslemler = new DefaultTableModel(kolonlar, 0);
        tableIslemler = new JTable(modelIslemler);
        
        tableIslemler.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String durum = (String) table.getModel().getValueAt(row, 5); 
                String teslimTarihiStr = (String) table.getModel().getValueAt(row, 4); 

                if (!isSelected) {
                    try {
                        LocalDate teslimTarihi = LocalDate.parse(teslimTarihiStr);
                        if (durum.equals("Bekliyor") && LocalDate.now().isAfter(teslimTarihi)) {
                            c.setBackground(new Color(255, 102, 102)); c.setForeground(Color.WHITE);
                        } else if (durum.equals("İade Edildi")) {
                            c.setBackground(new Color(204, 255, 204)); c.setForeground(Color.BLACK);
                        } else {
                            c.setBackground(Color.WHITE); c.setForeground(Color.BLACK);
                        }
                    } catch (Exception e) { c.setBackground(Color.WHITE); }
                }
                return c;
            }
        });
        
        panel.add(new JScrollPane(tableIslemler), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel pnlForm = new JPanel(new FlowLayout());
        cmbOduncUye = new JComboBox<>(); cmbOduncKitap = new JComboBox<>(); txtGunSayisi = new JTextField("15", 3); 

        pnlForm.add(new JLabel("Üye:")); pnlForm.add(cmbOduncUye);
        pnlForm.add(new JLabel("Müsait Kitaplar:")); pnlForm.add(cmbOduncKitap);
        pnlForm.add(new JLabel("Ödünç Süresi (Gün):")); pnlForm.add(txtGunSayisi);

        JPanel pnlButonlar = new JPanel(new FlowLayout());
        JButton btnOduncVer = new JButton("Kitabı Zimmetle"); JButton btnIadeAl = new JButton("Seçili Kitabı İade Al");
        pnlButonlar.add(btnOduncVer); pnlButonlar.add(btnIadeAl);
        
        pnlAlt.add(pnlForm); pnlAlt.add(pnlButonlar); panel.add(pnlAlt, BorderLayout.SOUTH);

        btnOduncVer.addActionListener(e -> oduncVer()); btnIadeAl.addActionListener(e -> iadeAl());
        return panel;
    }

    private void comboboxlariGuncelle() {
        if (cmbOduncUye == null || cmbOduncKitap == null || cmbKarneUye == null) return;
        cmbOduncUye.removeAllItems(); cmbOduncKitap.removeAllItems(); cmbKarneUye.removeAllItems();

        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rsKitap = stmt.executeQuery("SELECT id, title FROM Books WHERE status = 'RAFTA'");
            while (rsKitap.next()) { cmbOduncKitap.addItem(rsKitap.getInt("id") + " - " + rsKitap.getString("title")); }
            
            ResultSet rsUye = stmt.executeQuery("SELECT id, name_surname FROM Users WHERE role = 'OGRENCI'");
            while (rsUye.next()) { 
                String uyeData = rsUye.getInt("id") + " - " + rsUye.getString("name_surname");
                cmbOduncUye.addItem(uyeData); cmbKarneUye.addItem(uyeData); 
            }
        } catch (Exception ex) { }
    }

    private void islemleriListele() {
        modelIslemler.setRowCount(0);
        String sql = "SELECT t.id, b.title, u.name_surname, t.borrow_date, t.return_date, t.is_returned " +
                     "FROM Transactions t JOIN Books b ON t.book_id = b.id JOIN Users u ON t.user_id = u.id";
                     
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String durum = rs.getInt("is_returned") == 1 ? "İade Edildi" : "Bekliyor";
                modelIslemler.addRow(new Object[]{ rs.getInt("id"), rs.getString("title"), rs.getString("name_surname"), rs.getString("borrow_date"), rs.getString("return_date"), durum });
            }
        } catch (Exception ex) { }
    }

    private void oduncVer() {
        if (cmbOduncUye.getSelectedItem() == null || cmbOduncKitap.getSelectedItem() == null) return;
        int uyeId = Integer.parseInt(cmbOduncUye.getSelectedItem().toString().split(" - ")[0]);
        int kitapId = Integer.parseInt(cmbOduncKitap.getSelectedItem().toString().split(" - ")[0]);
        int gun = Integer.parseInt(txtGunSayisi.getText());
        String bugun = LocalDate.now().toString(); String teslimTarihi = LocalDate.now().plusDays(gun).toString();

        try (Connection conn = DatabaseHelper.connect()) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Transactions (book_id, user_id, borrow_date, return_date, is_returned) VALUES (?, ?, ?, ?, 0)");
            pstmt.setInt(1, kitapId); pstmt.setInt(2, uyeId); pstmt.setString(3, bugun); pstmt.setString(4, teslimTarihi); pstmt.executeUpdate();
            
            PreparedStatement pstmtUpdate = conn.prepareStatement("UPDATE Books SET status = 'ODUNCTE' WHERE id = ?");
            pstmtUpdate.setInt(1, kitapId); pstmtUpdate.executeUpdate();

            JOptionPane.showMessageDialog(this, "Kitap başarıyla zimmetlendi!");
            kitaplariListele(); islemleriListele();
        } catch (Exception ex) { }
    }

    private void iadeAl() {
        int seciliSatir = tableIslemler.getSelectedRow();
        if (seciliSatir == -1) return;
        if (modelIslemler.getValueAt(seciliSatir, 5).toString().equals("İade Edildi")) return;
        int islemId = (int) modelIslemler.getValueAt(seciliSatir, 0);

        try (Connection conn = DatabaseHelper.connect()) {
            PreparedStatement getBook = conn.prepareStatement("SELECT book_id FROM Transactions WHERE id = ?");
            getBook.setInt(1, islemId); int kitapId = getBook.executeQuery().getInt("book_id");

            PreparedStatement updateTrans = conn.prepareStatement("UPDATE Transactions SET is_returned = 1 WHERE id = ?");
            updateTrans.setInt(1, islemId); updateTrans.executeUpdate();

            PreparedStatement updateBook = conn.prepareStatement("UPDATE Books SET status = 'RAFTA' WHERE id = ?");
            updateBook.setInt(1, kitapId); updateBook.executeUpdate();

            JOptionPane.showMessageDialog(this, "Kitap başarıyla iade alındı!");
            kitaplariListele(); islemleriListele();
        } catch (Exception ex) { }
    }

    // ==========================================
    // 4. RAPORLAR & KARNELER
    // ==========================================
    private JPanel createRaporlarPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlUst = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnKitapKurdu = new JButton("👑 Ayın Kitap Kurdunu Hesapla");
        btnKitapKurdu.setFont(new Font("Arial", Font.BOLD, 14)); btnKitapKurdu.setBackground(new Color(255, 215, 0)); 
        pnlUst.add(btnKitapKurdu); panel.add(pnlUst, BorderLayout.NORTH);

        String[] kolonlar = {"Kitap Adı", "Alış Tarihi", "İade Tarihi", "Durum"};
        modelKarne = new DefaultTableModel(kolonlar, 0); tableKarne = new JTable(modelKarne);
        panel.add(new JScrollPane(tableKarne), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new FlowLayout());
        cmbKarneUye = new JComboBox<>(); JButton btnKarneGetir = new JButton("Geçmişi Görüntüle"); JButton btnDisaAktar = new JButton("Kuşeye Yazdır (.txt İndir)");

        pnlAlt.add(new JLabel("Öğrenci Seç:")); pnlAlt.add(cmbKarneUye); pnlAlt.add(btnKarneGetir); pnlAlt.add(btnDisaAktar);
        panel.add(pnlAlt, BorderLayout.SOUTH);

        btnKitapKurdu.addActionListener(e -> {
            String sql = "SELECT u.name_surname, COUNT(t.id) as okuma_sayisi FROM Transactions t JOIN Users u ON t.user_id = u.id WHERE strftime('%m', t.borrow_date) = strftime('%m', 'now') GROUP BY u.id ORDER BY okuma_sayisi DESC LIMIT 1";
            try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Bu Ayın Kitap Kurdu: " + rs.getString("name_surname") + "\nOkuduğu Kitap Sayısı: " + rs.getInt("okuma_sayisi"), "Tebrikler!", JOptionPane.INFORMATION_MESSAGE);
                } else { JOptionPane.showMessageDialog(this, "Bu ay henüz hiç kitap ödünç alınmamış."); }
            } catch (Exception ex) { }
        });

        btnKarneGetir.addActionListener(e -> {
            if (cmbKarneUye.getSelectedItem() == null) return;
            int uyeId = Integer.parseInt(cmbKarneUye.getSelectedItem().toString().split(" - ")[0]); modelKarne.setRowCount(0);
            String sql = "SELECT b.title, t.borrow_date, t.return_date, t.is_returned FROM Transactions t JOIN Books b ON t.book_id = b.id WHERE t.user_id = ?";
            try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, uyeId); ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String durum = rs.getInt("is_returned") == 1 ? "İade Edildi" : "Hala Kendisinde";
                    modelKarne.addRow(new Object[]{ rs.getString("title"), rs.getString("borrow_date"), rs.getString("return_date"), durum });
                }
            } catch (Exception ex) { }
        });

        btnDisaAktar.addActionListener(e -> {
            if (modelKarne.getRowCount() == 0) return;
            String ogrenciAdi = cmbKarneUye.getSelectedItem().toString().split(" - ")[1];
            try (PrintWriter writer = new PrintWriter(new FileWriter("Karne_" + ogrenciAdi.replace(" ", "_") + ".txt"))) {
                writer.println("=== KÖY OKULU KÜTÜPHANESİ ==="); writer.println("Öğrenci: " + ogrenciAdi); writer.println("Tarih: " + LocalDate.now()); writer.println("--------------------------------------------------");
                writer.printf("%-20s %-15s %-15s %-15s\n", "Kitap Adı", "Alış", "İade", "Durum"); writer.println("--------------------------------------------------");
                for (int i = 0; i < modelKarne.getRowCount(); i++) {
                    writer.printf("%-20s %-15s %-15s %-15s\n", modelKarne.getValueAt(i, 0), modelKarne.getValueAt(i, 1), modelKarne.getValueAt(i, 2), modelKarne.getValueAt(i, 3));
                }
                JOptionPane.showMessageDialog(this, "Öğrenci Karnesi başarıyla kaydedildi!");
            } catch (Exception ex) { }
        });

        return panel;
    }

    // ==========================================
    // 5. GELEN TALEPLER YÖNETİMİ (YENİ EKLENEN SEKME)
    // ==========================================
    private JPanel createTaleplerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] kolonlar = {"Talep ID", "Öğrenci Adı", "İstenen Kitap", "Yazar", "Durum"};
        modelTalepler = new DefaultTableModel(kolonlar, 0);
        tableTalepler = new JTable(modelTalepler);
        panel.add(new JScrollPane(tableTalepler), BorderLayout.CENTER);

        JPanel pnlAlt = new JPanel(new FlowLayout());
        JButton btnOnayla = new JButton("Onayla ve Kütüphaneye Ekle");
        btnOnayla.setBackground(new Color(144, 238, 144)); // Açık Yeşil
        
        JButton btnReddet = new JButton("Talebi Reddet");
        btnReddet.setBackground(new Color(255, 182, 193)); // Açık Kırmızı

        pnlAlt.add(btnOnayla);
        pnlAlt.add(btnReddet);
        panel.add(pnlAlt, BorderLayout.SOUTH);

        // --- BUTON OLAYLARI ---
        btnOnayla.addActionListener(e -> {
            int seciliSatir = tableTalepler.getSelectedRow();
            if (seciliSatir == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen onaylamak için bir talep seçin!");
                return;
            }
            String durum = modelTalepler.getValueAt(seciliSatir, 4).toString();
            if (!durum.equals("Bekliyor")) {
                JOptionPane.showMessageDialog(this, "Bu talep zaten işlem görmüş.");
                return;
            }

            int talepId = (int) modelTalepler.getValueAt(seciliSatir, 0);
            String kitapAdi = modelTalepler.getValueAt(seciliSatir, 2).toString();
            String yazar = modelTalepler.getValueAt(seciliSatir, 3).toString();

            try (Connection conn = DatabaseHelper.connect()) {
                // 1. Talebi onayla
                PreparedStatement updateReq = conn.prepareStatement("UPDATE Requests SET status = 'Onaylandı' WHERE id = ?");
                updateReq.setInt(1, talepId);
                updateReq.executeUpdate();

                // 2. Kitabı doğrudan kütüphaneye ekle
                PreparedStatement insertBook = conn.prepareStatement("INSERT INTO Books (title, author, category, status) VALUES (?, ?, 'Öğrenci Talebi', 'RAFTA')");
                insertBook.setString(1, kitapAdi);
                insertBook.setString(2, yazar);
                insertBook.executeUpdate();

                JOptionPane.showMessageDialog(this, "Talep onaylandı ve kitap kütüphaneye eklendi!");
                gelenTalepleriListele();
                kitaplariListele(); // Kitaplar listesini güncelle
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage()); }
        });

        btnReddet.addActionListener(e -> {
            int seciliSatir = tableTalepler.getSelectedRow();
            if (seciliSatir == -1) return;
            int talepId = (int) modelTalepler.getValueAt(seciliSatir, 0);

            try (Connection conn = DatabaseHelper.connect()) {
                PreparedStatement updateReq = conn.prepareStatement("UPDATE Requests SET status = 'Reddedildi' WHERE id = ?");
                updateReq.setInt(1, talepId);
                updateReq.executeUpdate();
                gelenTalepleriListele();
            } catch (Exception ex) { }
        });

        return panel;
    }

    private void gelenTalepleriListele() {
        modelTalepler.setRowCount(0);
        String sql = "SELECT r.id, u.name_surname, r.book_title, r.author, r.status " +
                     "FROM Requests r JOIN Users u ON r.user_id = u.id ORDER BY r.status DESC";
        try (Connection conn = DatabaseHelper.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelTalepler.addRow(new Object[]{ rs.getInt("id"), rs.getString("name_surname"), rs.getString("book_title"), rs.getString("author"), rs.getString("status") });
            }
        } catch (Exception ex) { }
    }
}