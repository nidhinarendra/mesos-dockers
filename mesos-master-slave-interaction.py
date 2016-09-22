


if __name__ == '__main__':

    # lets say we have two slave s1 and s2 
    doc1 = start_docker(mesos_master, s1)
    if doc1 not None:
        response.put("Ec2master: master{} new slave started {}", doc1)

    doc2 = start_docker(mesos_master, s2)
    if doc2 not None:
        response.put("Ec2maste: master{} new slave started {}", doc2)

    sleep(100)

    flag = shutdown_docker(mesos_master, s2, doc2)

    if flag is True:
        response.put("Ec2master Slave {} died", s2)


    # another abitious things next step
    #doc3 = start_docker(mesos_master-2, s3)

    # Anush take care of : start_docker, shutdown_docker
    
