# RETRO YILAN (ULTRA EVRİM) – AdMob app-ads.txt

Bu paket, mevcut `cezali-1genc/Retro-Snake` GitHub deposuna eklenmek üzere hazırlanmıştır.

## Dosyalar

- `app-ads.txt`: AdMob yetkili satırı. Depo kök dizinine ekleyin.
- `index.html`: İsteğe bağlı örnek GitHub Pages ana sayfası. Depoda zaten `index.html` varsa bununla DEĞİŞTİRMEYİN.
- `README-ADMOB.md`: Bu kurulum kılavuzu.

## 1. Dosyaları depoya ekleme

GitHub deposu:
https://github.com/cezali-1genc/Retro-Snake

`app-ads.txt` dosyasını depo kök dizinine (README.md ile aynı seviyeye) yükleyin.
Mevcut dosyaları silmeyin veya üzerine yazmayın.

## 2. GitHub Pages'i etkinleştirme

1. Depoda Settings > Pages bölümünü açın.
2. Build and deployment altında Source olarak `Deploy from a branch` seçin.
3. Branch olarak `main`, klasör olarak `/(root)` seçin.
4. Save'e basın.
5. Pages dağıtımının tamamlanmasını bekleyin.

Not: Depoda `main` yerine başka varsayılan dal kullanılıyorsa o dalı seçin.
Not: Pages için depo kökünde `index.html`, `index.md` veya `README.md` giriş dosyası bulunmalıdır. Depoda README.md zaten varsa örnek index.html zorunlu değildir.

## 3. Beklenen yayın adresi

Proje deposu için beklenen adres:

https://cezali-1genc.github.io/Retro-Snake/app-ads.txt

Bu adresin çalıştığı henüz doğrulanmış değildir. GitHub Pages'i etkinleştirdikten sonra tarayıcıda açın. Dosya açıldığında yalnızca şu satır görünmelidir:

google.com, pub-7323917953581312, DIRECT, f08c47fec0942fa0

## 4. Google Play ve AdMob

- Google Play Console'daki uygulama iletişim bilgileri / geliştirici web sitesi alanına, GitHub Pages site adresini ekleyin:
  https://cezali-1genc.github.io/Retro-Snake/
- AdMob uygulama kaydında geliştirici web sitesi alanının aynı siteyi gösterdiğini kontrol edin.
- AdMob'un app-ads.txt taramasını ve doğrulamasını bekleyin. Tarama birkaç gün sürebilir; en az 24 saat bekleyin ve AdMob'daki durumu tekrar kontrol edin.

ÖNEMLİ: Sadece dosyayı ZIP'e koymak veya GitHub deposuna yüklemek, dosyanın yayınlandığını ya da AdMob tarafından doğrulandığını göstermez. Pages dağıtımı ve canlı URL ayrıca kontrol edilmelidir.

## 5. AdMob kimlikleri (referans)

- Uygulama: RETRO YILAN (ULTRA EVRİM)
- AdMob App ID: `ca-app-pub-7323917953581312~1148549144`
- Ödüllü reklam birimi: `ca-app-pub-7323917953581312/6764224941`
- Publisher ID: `pub-7323917953581312`

Reklam App ID ve reklam birimi kimliği app-ads.txt satırına yazılmaz. app-ads.txt dosyasında Publisher ID kullanılır.
