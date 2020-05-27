# Linux Phone Distro RSS Scraper

## What
Scraps information about new distro releases from various sites and creates simple RSS xml files. Optionally it automatically uploads these files via sftp.

## Why
Because it's more convenient to get a notification from your RSS reader app instead of checking manually for distro releases.

## Feeds
- [PureOS](http://linuxcommandlibrary.com/linuxphone/pureos/rss.xml)
- [postmarketOS](http://linuxcommandlibrary.com/linuxphone/postmarketos/rss.xml)
- [Ubuntu Touch by UBPorts](http://linuxcommandlibrary.com/linuxphone/ubuntu_touch/rss.xml)
- [Mobian](http://linuxcommandlibrary.com/linuxphone/mobian/rss.xml)
- [Fedora](http://linuxcommandlibrary.com/linuxphone/fedora/rss.xml)
- [Arch Linux ARM](http://linuxcommandlibrary.com/linuxphone/arch/rss.xml)
- [KDE Neon](http://linuxcommandlibrary.com/linuxphone/kde_neon/rss.xml)

Current RSS feeds get updated every hour and are hosted on my http://linuxcommandlibrary.com website.

## TODO:
- Add tests
- Make it more robust and catch exceptions
- Fix artifact build(somehow the manifest inside the jar is not the one I defined)

#### Missing distros:
- Manjaro ARM
- SailfishOS
- Maemo Leste
- Nemo Mobile
- NixOS
- LuneOS

## Contribution
I'm very happy about contributions. Just create an issue and we can discuss it.

## Acknowledgment:
Pine64, Purism, all the distros mentioned above, jcraft, jsoup, selenium