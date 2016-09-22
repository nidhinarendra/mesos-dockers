#!/usr/bin/env python

##Importing essential libraries

import os
import sys
import signal
import time
import logging
import uuid

import mesos.interface
from mesos.interface import Scheduler
from mesos.interface import mesos_pb2

import mesos.native
from mesos.native import MesosSchedulerDriver


logging.basicConfig(level=logging.INFO)
TASK_CPUS = 1
TASK_MEM = 200
TOTAL_TASKS = 2
Cpuoffer=0
Memoffer=0


def new_task(offer,name):
    task = mesos_pb2.TaskInfo()
    id=uuid.uuid4()
    task.task_id.value = str(id)
    task.slave_id.value = offer.slave_id.value
    task.name = name
    #task.command.value=cmd
    
    cpus = task.resources.add()
    cpus.name = "cpus"
    cpus.type = mesos_pb2.Value.SCALAR
    cpus.scalar.value = TASK_CPUS
    
    mem = task.resources.add()
    mem.name = "mem"
    mem.type = mesos_pb2.Value.SCALAR
    mem.scalar.value = TASK_MEM
    return task
def new_docker(offer,name):
    task= new_task(offer,name)
    
    container = mesos_pb2.ContainerInfo()
    container.type = 1
    
    docker = mesos_pb2.ContainerInfo.DockerInfo()
    docker.image = "busybox"
    docker.network = 3
    docker.force_pull_image = True
    
    container.docker.MergeFrom(docker)
    task.container.MergeFrom(container)
    
    return task
        
def max_tasks_to_run_with_offer( offer):
    logging.info("CPUs: %s MEM: %s",
                 offer.resources[0].scalar.value,
                 offer.resources[1].scalar.value)
    
    cpu_tasks = int(offer.resources[0].scalar.value/TASK_CPUS)
    mem_tasks = int(offer.resources[1].scalar.value/TASK_MEM)
            
    return cpu_tasks if cpu_tasks <= mem_tasks else mem_tasks
    
def shutdown(signal, frame):
    logging.info("Shutdown signal")
    driver.stop()
    time.sleep(5)
    sys.exit(0)
    
    
## Defining scheduler of docker_launch framework    
class LaunchDocker(Scheduler):
    def __init__(self, implicitAcknowledgements):
        self.implicitAcknowledgements = implicitAcknowledgements
        #self.executor = executor
        self.taskData = {}
        self.tasksLaunched = 0
        self.tasksFinished = 0
        self.messagesSent = 0
        self.imessagesReceived = 0
        self.runningTasks=0
        
    def registered(self,driver,frameworkID,masterInfo):
        print "Registered with Framework ID %s " % frameworkID.value
            
            
            
    def resourceOffers(self, driver, offers):
        logging.info("Recieved resource offers: %s",
                     [o.id.value for o in offers])
        
        tasks_to_start = TOTAL_TASKS - self.runningTasks
        for offer in offers:
            if TOTAL_TASKS <= self.runningTasks:
                driver.declineOffer(offer.id)
                logging.info("Declining Offer %s", offer.id)
                return
                    
        count_tasks = max_tasks_to_run_with_offer(offer)
        start_tasks = count_tasks if count_tasks <= tasks_to_start else tasks_to_start
        tasks_to_start -= start_tasks
        
        if start_tasks <= 0:
            logging.info("Declining Offer %s", offer.id)
            driver.declineOffer(offer.id)
            return
        
            logging.info("Starting %s tasks", start_tasks)
            tasks = []
            for i in range(start_tasks):
                task = new_docker(offer,"Docker Launch")
                
                logging.info("Added task %s "
                             "using offer %s.",
                             task.task_id.value,
                             offer.id.value)
                
                tasks.append(task)
                logging.info("Launching %s Tasks", len(tasks))
                driver.launchTasks(offer.id, tasks)
                            
      def statusUpdate(self, driver, update):
          logging.info("Task %s is in state %s" %
                       (update.task_id.value,
                        mesos_pb2.TaskState.Name(update.state)))
          
          if update.state == mesos_pb2.TASK_RUNNING:
              self.runningTasks += 1
              logging.info("Running tasks: %s", self.runningTasks)
              return
              
          if update.state != mesos_pb2.TASK_RUNNING or\
             update.state != mesos_pb2.TASK_STARTING or\
                             update.state != mesos_pb2.TASK_STAGING:
              self.runningTasks -= 1
              logging.info("Running tasks: %s", self.runningTasks)
              
          if update.state == mesos_pb2.TASK_KILLED or update.state == mesos_pb2.TASK_FAILED or update.state == mesos_pb2.TASK_ERROR or update.state == mesos_pb2.TASK_LOST:
              logging.info("Task needs to rescheduled")
              
                  
                  
if __name__ == '__main__':
    #executor = mesos_pb2.ExecutorInfo()
    #executor.executor_id.value = "Docker"
    161,1         83%
    framework = mesos_pb2.FrameworkInfo()
    framework.user = "" # Have Mesos fill in the current user.
    framework.name = "DockerLauncher"
    
    implicitAcknowledgements = 1
    
    framework.principal = "Launch-Docker-Framework"
    mesosScheduler = LaunchDocker(implicitAcknowledgements)
    driver = mesos.native.MesosSchedulerDriver(
        mesosScheduler,
        framework,
        "zk://172.31.3.24:2181/mesos") # I suppose here that mesos master url is local
    
    driver.run()
    logging.info("Listening for Ctrl-C")
    signal.signal(signal.SIGINT, shutdown)
    while True:
        time.sleep(5)
        sys.exit(0)
        
                                                                        
