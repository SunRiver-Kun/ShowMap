防火墙可能会阻碍adb连接，甚至让studio无法启动！

安装Adb Interface
https://blog.csdn.net/u014159143/article/details/112094353
连接手机
https://blog.csdn.net/qq_43403062/article/details/111107957#:~:text=Android%20Studio%E5%A6%82%E4%BD%95%E8%BF%9E%E6%8E%A5%E6%89%8B%E6%9C%BA%E8%AE%BE%E5%A4%87%201%201.%E9%85%8D%E7%BD%AEadb%E7%8E%AF%E5%A2%83,2%202.%E9%85%8D%E7%BD%AEUSB%20Driver%203%203.%E5%AE%8C%E6%88%90%E8%BF%9E%E6%8E%A5
----------------------------------------------------------------
未配置key，在studio中的下面的Run中会有错误提示

修改key的信息为调试key（下面有生成调试key方法）
build.gradle中的android/signingConfigs/showmap/

AndroidManifest.xml
package和高德控制台那的一样
35行key那里的android:value改为你高德控制台那的key

生成调试Key	
https://blog.csdn.net/w47_csdn/article/details/87564029
https://lbs.amap.com/api/android-sdk/guide/create-project/get-key
https://www.oracle.com/java/technologies/downloads/#jdk18-windows
keytool来自jdk，添加到系统环境变量后可以直接用，
也可以直接从bin文件夹那拖到cmd里来。
密码1234567890，问题直接回车，最后输入 y 来确定 

cd .android
keytool -genkeypair -keyalg DSA  -alias showmap -keystore showmap
keytool -v -list -keystore showmap

* 获取发布SHA1	release key
public static String sHA1(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

String sha1 = sHA1(getApplicationContext());

