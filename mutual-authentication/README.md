#Mutual authentication using certificates for AMQ Broker 7

## Setup certificates, keystores and truststores for client and broker
NOTE: be careful when you create the certificate for configuring the CN as the hostname you are using to run broker and clients (in this example localhost).

1. Using keytool, create a certificate for the broker:
```
keytool -genkey -alias broker -keyalg RSA -keystore broker.ks
```
2. Export the broker’s certificate so it can be shared with clients:
```
keytool -export -alias broker -keystore broker.ks -file broker.cert
```
3.Create a certificate/keystore for the client:
```
keytool -genkey -alias client -keyalg RSA -keystore client.ks
```
4. Create a truststore for the client, and import the broker’s certificate. This establishes that the client “trusts” the broker:
```
keytool -import -alias broker -keystore client.ts -file broker.cert
```
5. Export the client’s certificate so it can be shared with broker:
```
keytool -export -alias client -keystore client.ks -file client.cert
```
6. Create a truststore for the broker, and import the client’s certificate. This establishes that the broker “trusts” the client:
```
keytool -import -alias client -keystore broker.ts -file client.cert
```

## Configure broker to accept connections using certificates
In this example we configure them using the AMQP acceptor.
1. Copy the `broker.ks` and `broker.ts` in the broker's `etc` directory.
2. Edit the `etc/broker.xml` and update the `amqp` acceptor like the following:
```
<!-- AMQP Acceptor.  Listens on default AMQP port for AMQP traffic.-->
         <acceptor name="amqp">tcp://0.0.0.0:5672?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpLowCredits=300;amqpMinLargeMessageSize=102400;amqpDuplicateDetection=true;sslEnabled=true;keyStorePath=../etc/broker.ks;keyStorePassword=broker;needClientAuth=true;trustStorePath=../etc/broker.ts;trustStorePassword=broker</acceptor>
```
3. Save and start the broker
## Start consumer and producer using the provided keystores and truststores
1. consumer:
```
./bin/artemis consumer --destination secure_queue --message-count 1 --url "amqps://localhost:5672?transport.keyStoreLocation=client.ks&transport.keyStorePassword=client&transport.trustStoreLocation=client.ts&transport.trustStorePassword=client" --protocol amqp
```
2. producer:
```
./bin/artemis producer --destination secure_queue --message-count 1 --url "amqps://localhost:5672?transport.keyStoreLocation=client.ks&transport.keyStorePassword=client&transport.trustStoreLocation=client.ts&transport.trustStorePassword=client" --protocol amqp
```
