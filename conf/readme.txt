一、申请MDM Vendor网页及证书生成：
http://www.softhinker.com/in-the-news/iosmdmvendorcsrsigning
http://ysmanse.tistory.com/archive/20130415

二、代码文件相关（WebRoot/mdmtool）：
1、MDMPush.p12 是用于MDM命令推送的文件（apple后台生成）；
2、edumdm.crt 是服务器SSL证书；
3、edumdmnopass.key 是服务器SSL对应的秘钥edumdm.key生成的无需验证的秘钥；
4、ca-bundle.pem 是服务器SSL根证书（在startssl中下载的）；
5、edumdm.mobileconfig 是模板文件。

三、相关的openssl命令：
1、将.crt和.key转化为.p12证书（mobileconfig配置文件中需要的p12文件）
  openssl pkcs12 -export -out edumdm.p12 -inkey edumdm.key -in edumdm.crt
2、将.key转化为.key.unsecure
  openssl rsa -in edumdm.key -out edumdm.key.unsecure
3、openssl对mobileconfig文件签名：
  openssl smime -sign -in edumdm.mobileconfig -out signed.mobileconfig -signer edumdm.crt -inkey edumdmnopass.key -certfile ca-bundle.pem -outform der -nodetach
