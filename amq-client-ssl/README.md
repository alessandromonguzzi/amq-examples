# Create AMQ Broker with OpenShift operator and connect to it from outside using OpenShift routes.

## Install Operator from UI
Select Red Hat Integration - AMQ Broker operator from Operator Hub and install it in a specific project/namespace

## Create Brokers
1. Create secret containing broker's keystore and truststore:

```
keytool -genkey -alias broker -keyalg RSA -keystore broker.ks
keytool -export -alias broker -keystore broker.ks -file broker_cert.pem
keytool -import -alias broker -keystore client.ts -file broker_cert.pem
```

Take care of using the `broker.ks` and `client.ts` names or the operator will not be able to link them.
Take care of setting the CN value to the hostname of the machine you are running

2. Login to OpenShift and create the secret:

```
oc create secret generic amqbroker-tls-secret --from-file=broker.ks=broker.ks --from-file=client.ts=client.ts --from-literal=keyStorePassword=keystore --from-literal=trustStorePassword=keystore
```

3. Use the `yaml/broker.yaml` file to create two brokers in a cluster.

### Notes

```
     expose: true
      name: all
      port: 61617
      protocols: all
      sslEnabled: true
      sslProvider: JDK
      sslSecret: amqbroker-tls-secret
      verifyHost: false

```

- `expose: true` creates routes to expose the broker to the outside
- `verifyHost: false` avoids problems with CN field in the certificate
- `sslSecrect`: points to the secret created in point 2 above.

## Connect client
1. Provide the `client.ts` generated above in a location accessible to the client.

2. Run the `org.monguzzi.examples.BasicClient` providing as input parameter the broker URL with the following structure:

`<protocol>://<route>:<port>?<connectionProperties>`

Example:

```
tcp://my-broker-all-0-svc-rte-sftp.apps-crc.testing:443?useTopologyForLoadBalancing=false&sslEnabled=true&trustStorePath=src/main/resources/secrets/client.ts&trustStorePassword=keystore
```

### Notes

- `port` usuall is 80 for plain connection and 443 for SSL connection. This is due to the fact that the route is tunneling the traffic to the correct service and then to the pod where the broker resides
- `trustStorePath` points to the `client.ts` file containing the broker certificate.
- `trustStorePassword` is the password to access certificates in the `client.ts` file
