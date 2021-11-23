
./gradlew uploadArchives  
将插件发布到本地maven仓库
代码更改后直接执行此命令可以直接更新
删除生成的文件后，需要先注释掉classpath和apply，再执行此命令生成新的插件，否则报错

使用注意：
1.根目录build.gradle添加：
    //引入本地 Maven 仓库
    maven{
        url uri('assets-journalist-plugin')
    }
    
    classpath 'com.assetsjournalist:assetsplugin:1.0.0'
    
2.插件引用
    apply plugin: 'assetsplugin'
    
