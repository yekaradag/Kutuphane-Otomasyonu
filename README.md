# Kütüphane Otomasyonu

Bir köy okulunun kütüphanesi için geliştirilen, kitapları ve ödünç alma/iade işlemlerini dijital ortamda takip etmeyi amaçlayan bir Java uygulaması.

> **Durum:** Geliştirme aşamasında. Veritabanı katmanı tamamlandı; kullanıcı işlemleri ve arayüz üzerinde çalışılıyor.

## Kullanılan Teknolojiler

- **Java**
- **SQLite** (JDBC ile, `sqlite-jdbc` kütüphanesi)

## Veritabanı Tasarımı

Uygulama ilk çalıştırıldığında proje klasöründe `kutuphane.db` adlı yerel bir veritabanı oluşturur.

| Tablo | Açıklama |
| --- | --- |
| `Users` | Kullanıcılar ve rolleri (öğretmen / öğrenci), öğrenci numarası |
| `Books` | Kitap kataloğu: ad, yazar, kategori ve durum (rafta / ödünçte) |
| `Transactions` | Ödünç alma ve iade kayıtları; `Books` ve `Users` tablolarına foreign key ile bağlı |

## Planlanan Özellikler

- [x] Veritabanı ve tabloların oluşturulması
- [ ] Kullanıcı girişi (öğretmen / öğrenci rolleri)
- [ ] Kitap ekleme, arama ve listeleme
- [ ] Ödünç verme ve iade işlemleri
- [ ] Teslim tarihi geçen kitapların takibi
- [ ] Şifrelerin güvenli (hash'lenmiş) saklanması

## Çalıştırma

1. Projeyi klonlayın:
   ```bash
   git clone https://github.com/yekaradag/Kutuphane-Otomasyonu.git
   ```
2. `lib/sqlite-jdbc` dosyasını sınıf yoluna (classpath) ekleyin.
3. `src/Main.java` dosyasını çalıştırın.

## Geliştirici

**Yunus Emre Karadağ** · [GitHub](https://github.com/yekaradag)
