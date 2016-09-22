package org.systemsoftware.mesosraspberry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anusha vijay
 */
public class DataStore {
    String mesosHome;
    String mesosIP;
    String slaveIP;



    Boolean masterStarted;
    Boolean slaveStarted;
    Boolean dockerStarted;
    List<String> nodesList = new ArrayList<String>();
    Process masterProcess;
    Process slaveProcess;
    Process dockerProcess;
    String dockerImageName;
    String dockerContainerID;
    String dockerSTATUS;
    String mesosIPPort;
    String mesosMasterID;
    String mesosSlaveID;
    Process slaveKillProcess;
    String remoteSlaveid;

    public String getSlaveIP() {
        return slaveIP;
    }

    public void setSlaveIP(String slaveIP) {
        this.slaveIP = slaveIP;
    }



    public String getRemoteSlaveid(){ return remoteSlaveid;}
    public void setRemoteSlaveid(String remoteSlaveid) {
        this.remoteSlaveid=remoteSlaveid;
    }
    public String getDockerContainerID(){
        return dockerContainerID;
    }

    public void setDockerContainerID(String dockerContainerID)
    {
        this.dockerContainerID=dockerContainerID;
    }
    public String getDockerSTATUS(){
        return dockerSTATUS;
    }

    public void setDockerSTATUS(String dockerSTATUS)
    {
        this.dockerSTATUS=dockerSTATUS;
    }
    public String getMesosSlaveID() {
        return mesosSlaveID;
    }

    public void setMesosSlaveID(String mesosSlaveID) {
        this.mesosSlaveID = mesosSlaveID;
    }

    public String getMesosMasterID() {
        return mesosMasterID;
    }

    public void setMesosMasterID(String mesosMasterID) {
        this.mesosMasterID = mesosMasterID;
    }

    public String getMesosIPPort() {return mesosIPPort;}

    public void setMesosIPPort(String mesosIPPort) { this.mesosIPPort = mesosIPPort; }

    public String getDockerImageName() { return dockerImageName; }

    public void setDockerImageName(String dockerImageName) { this.dockerImageName = dockerImageName;}

    public void setMasterProcess(Process masterProcess) {
        this.masterProcess = masterProcess;
    }

    public void setMesosIP(String mesosIP){ this.mesosIP=mesosIP.substring(0,mesosIP.indexOf(":"));}

    public String getMesosIP(){ return mesosIP;}

    public void setSlaveProcess(Process slaveProcess) {
        this.slaveProcess = slaveProcess;
    }

    public Process getMasterProcess() {
        return masterProcess;
    }

    public Process getSlaveProcess() { return slaveProcess; }

    public Process getDockerProcess() { return dockerProcess; }

    public void setDockerProcess(Process dockerProcess) {this.dockerProcess = dockerProcess;}

    public void setNodesList(List<String> nodesList) {
        this.nodesList = nodesList;
    }

    public List<String> getNodesList() {
        return nodesList;
    }

    public String getMesosHome() {
        return mesosHome;
    }

    public Boolean getMasterStarted() {
        return masterStarted;
    }

    public Boolean getSlaveStarted() { return slaveStarted; }

    public Boolean getDockerStarted() { return dockerStarted; }

    public void setMesosHome(String mesosHome) {
        this.mesosHome = mesosHome;
    }

    public void setMasterStarted(Boolean masterStarted) {
        this.masterStarted = masterStarted;
    }

    public void setSlaveStarted(Boolean slaveStarted) { this.slaveStarted = slaveStarted;}

    public void setDockerStarted(Boolean dockerStarted) {this.dockerStarted = dockerStarted;}

    public void setSlaveKillProcess(Process slaveKillProcess) {this.slaveKillProcess=slaveKillProcess;}

    public Process getSlaveKillProcess(){return slaveKillProcess;}
}
