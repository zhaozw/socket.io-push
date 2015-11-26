Android-Push-SDK
=========
Android客户端SDK

###AndroidManifest.xml

```
        可以单进程,也可以双进程
        <service android:name="com.yy.httpproxy.service.RemoteService" android:process=":push" android:enabled="true"/>

        自定义类YYNotificationReceiver,处理通知栏点击事件
        <receiver
            android:name=".YYNotificationReceiver"
            android:exported="true" >

            <!-- 这里com.xiaomi.mipushdemo.DemoMessageRreceiver改成app中定义的完整类名 -->
            <intent-filter>
                <action android:name="com.yy.httpproxy.service.RemoteService.INTENT" />
            </intent-filter>
     </receiver>
```     


