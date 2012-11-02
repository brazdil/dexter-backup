#java -jar smali.jar -o classes.dex HelloWorld.smali
#zip HelloWorld.zip classes.dex
#adb push HelloWorld.zip /data/local
#adb shell dalvikvm -cp /data/local/HelloWorld.zip HelloWorld

.class public Lcom/example/Arithmetic;
.super Ljava/lang/Object;

.method public static main([Ljava/lang/String;)V
    .registers 3

    const v0, 0xDEC0DED 	# tainted
    const v1, 1234

    add-int v2, v0, v1

    return-void
.end method
