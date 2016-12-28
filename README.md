# How it works
The manager pings MC-Api & Mojang every 60 seconds and converts their information to JSON formats that can be read by LaMetric time. These files are hosted at http://api.gamerking195.com/resources/ and are written to every 60 seconds. The lametric applications ping http://api.gamerking195.com/lametric/mojang.php & http://api.gamerking195.com/lametric/mineswine.php, every 60 seconds, which echo the contents of the respective JSON files. Lametric then takes the data from those two links to create the applications.

# Minecraft Status
The Minecraft status application for Lametric, http://apps.lametric.com/apps/minecraft_status/2484. This displays the status for all 10 Mojang services directly from the Mojang API at https://status.mojang.com/check. The status sight is pinged every 60 seconds.

# Mineswine Status
This app utilizes the server info in CSV format from https://us.mc-api.net/. The CSV format is used to avoid stupid JSON parsing and to get the data directly from a split. The direct URL read from is https://us.mc-api.net/v3/server/info/hub.mineswine.com/csv, and is pinged every 60 seconds.
