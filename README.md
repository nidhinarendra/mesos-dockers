# mesos-dockers
There are a lot of IoTs communicating with the containers on which applications are running. When these containers fail due to any reason, the IoTs must be able to migrate from the failed container to another working container seamlessly. In order to do this we need to create a framework cMonitor.

A. Containers to run the applications.

B. Docker application to manage the containers.

C. Mesos to manage the docker containers and delegate tasks onto it.

D. Zookeeper to manage mesos masters.

E. Amazon web services in which a linux instance is created.

F. Flask web server installed on AWS.

G. Postgres database to maintain the identities of the mesos master and slave.

H. cMonitor framework.

I. Raspberry pi to install all of these components.
