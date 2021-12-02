# XWIIMOTE
````
sudo apt-get install git bluez autoconf libudev-dev libtool libncurses5-dev -y
cd ~
git clone https://github.com/dvdhrm/xwiimote.git
cd xwiimote
./autogen.sh
make
sudo make install
````
##in .bashrc
````
export LD_LIBRARY_PATH=/usr/local/lib
````
## Python library
````
sudo apt-get install swig python-dev -y
cd ~
git clone https://github.com/dvdhrm/xwiimote-bindings
cd xwiimote-bindings
./autogen.sh --prefix=/usr 
sudo make
sudo make install
````

#PiGPIO
````
sudo apt-get install python3-pip -y
# not installing pip may leads to ModuleNotFoundError: No module named 'distutils.core'
````
````
wget https://github.com/joan2937/pigpio/archive/master.zip
unzip master.zip
cd pigpio-master
make
sudo make install
````
# mjpg_streamer

# Java 11 for ARM
(see: https://pi4j.com/documentation/java-installation/)
````
cd /usr/lib/jvm
sudo wget https://cdn.azul.com/zulu-embedded/bin/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf.tar.gz
sudo tar -xzvf zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf.tar.gz
sudo rm zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf.tar.gz

sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf/bin/java 1
sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/zulu11.41.75-ca-jdk11.0.8-linux_aarch32hf/bin/javac 1

sudo update-alternatives --config java
sudo update-alternatives --config javac
````

#Debug
````
sudo java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 -jar controller-1.0-SNAPSHOT-all.jar
````
