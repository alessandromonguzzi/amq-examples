# Using queue with a filter over a cluster of two brokers
In this examples three consumers connect to a cluster composed by two active brokers. The first two consumers connect to the broker named master01, while the third consumer connects to the broker named master02.

A single producer connected to the broker master01 produces 30 messages that contain different values for the filtering property. Each consumer receives 10 of these messages that match its specified filter.

## Broker setup
Start two brokers using the `broker.xml` files contained in the `master01` and `master02` directories.
The two brokers form a cluster listening for incoming connection at `tcp://localhost:61616` and `tcp://localhost:61617`.

## Client start
Run the `FilteringClient` example and check the output of the three consumers.

### Note
If you want to make every consumers to point to a specific broker, just change the `jndi.properties` accordingly.