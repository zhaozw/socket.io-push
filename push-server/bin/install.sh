uname=`uname -a`;
echo $uname
if [[ $uname == *"ubuntu"* ]]
then
   echo "install ubuntu";
   sudo apt-get update
   curl -sL https://deb.nodesource.com/setup_5.x | sudo -E bash -
   sudo apt-get install -y nodejs
   sudo dpkg --configure -a
else
   echo "install on centos"
   curl --silent --location https://rpm.nodesource.com/setup_5.x | sudo bash -
   sudo yum -y update
   sudo yum -y install nodejs
fi

cd /usr/local/
sudo mkdir -p socket.io-push


sudo npm install -g socket.io-push #更新进程版本

sudo push-server -c 24 #重启/启动