./gradlew -x test :lbs-server:build
scp -P22 lbs-server/build/libs/lbs-server-1.0.jar lvpenyou.com:/home/xuduo
