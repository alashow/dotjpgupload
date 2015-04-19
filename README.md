# dotjpgupload
Simple image uploader to server 

#Config

First need to set same password in `dotjpgupload/app/src/main/java/tm/veriloft/dotjpgupload/MainActivity.java` and `dotjpgupload/server/upload/index.php` files. 

You need config php uploader file for your needs. Default code is moves received file(image) to `../images` folder and inserts new filename to mysql database. You can change it or use another code for serverside.
Then upload `server` folder to your server. 
Also change your server url or ip in `dotjpgupload/app/src/main/java/tm/veriloft/dotjpgupload/MainActivity.java` file.
Then you need just run the app :)

#Screenshot
![Before upload](http://i.imgur.com/d9l3Nfb.jpg)
![Uploading progress](http://i.imgur.com/U8YHt9p.jpg)
![Success](http://i.imgur.com/JFhNbVH.jpg)
![Error](http://i.imgur.com/NvDMUiD.png)

## LICENSE
```
Copyright (C) 2015 Alashov Berkeli

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
