package org.systemsoftware.mesosraspberry;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by anusha vijay
 *
 sudo ./mesos-master.sh --advertise_ip=54.67.39.153 --work_dir=/var/lib/mesos
 --zk=zk://ec2-54-67-39-153.us-west-1.compute.amazonaws.com:2181/mesos --quorum=1

 sudo ./mesos-slave.sh —-ip=“” —master=172.31.3.24:5050 --work_dir=/var/lib/mesos --containerizers=docker,mesos
 */

@Path("/launchMesos")
public class Launcher {

    String slaveLaunchCommand;
    String remoteSlaveLaunchCommand;
    String masterLaunchCommand;
    String dockerLaunchCommand;
    String slaveKillCommand;

    public Launcher() {

        this.slaveLaunchCommand = FrameworkStarter.ds.getMesosHome() + "/build/bin/mesos-slave.sh --master=" + FrameworkStarter.ds.getMesosIP() + ":5050  --work_dir=/var/lib/mesos --containerizers=docker,mesos";
        //this.slaveLaunchCommand = FrameworkStarter.ds.getMesosHome() + "/build/bin/mesos-slave.sh --master=" + FrameworkStarter.ds.getMesosIP() + ":5050 --ip="+FrameworkStarter.ds.getMesosIP()+" --work_dir=/var/lib/mesos --containerizers=docker,mesos";
        this.masterLaunchCommand = FrameworkStarter.ds.getMesosHome() + "/build/bin/mesos-master.sh --advertise_ip=" + FrameworkStarter.ds.getMesosIP() + " --work_dir=/var/lib/mesos --zk=zk://" + FrameworkStarter.ds.getMesosIP() + ":2181/mesos --quorum=1";
        this.dockerLaunchCommand = "docker run hello-world";
        this.slaveKillCommand = "pkill -USR1 mesos-slave";
        this.remoteSlaveLaunchCommand=FrameworkStarter.ds.getMesosHome()+"/build/bin/mesos-slave.sh --master=54.219.141.65:5050 --ip="+FrameworkStarter.ds.getSlaveIP()+" --work_dir=/var/lib/mesos --containerizers=docker,mesos";
        ///home/ubuntu/mesos-0.25.0/build/bin/mesos-slave.sh --master=54.219.141.65:5050 --ip=172.31.1.9 --work_dir=/var/lib/mesos --containerizers=docker,mesos
    }

    @Context
    private DataStore ds;

    @PUT
    @Path("remoteSlaveLaunch")
    @Consumes(MediaType.TEXT_PLAIN)
    public void remoteSlaveLaunch() {
        try {
                System.out.println(remoteSlaveLaunchCommand);
                Process p = Runtime.getRuntime().exec(remoteSlaveLaunchCommand);
                FrameworkStarter.ds.setSlaveProcess(p);
                FrameworkStarter.ds.setSlaveStarted(true);
                System.out.println("Launching remote Mesos Slave");

                try {
                    System.out.println("Sleeping for 20 seconds");
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //Building a URL
                //String remoteSlaveState = "http://54.153.35.164:5051/state";
                String remoteSlaveState = "http://"+FrameworkStarter.ds.getMesosIP()+":5051/state";
                JSONObject jsonObjRemoteS = SendInfo.getJSON(remoteSlaveState);
                String remoteSlaveID = jsonObjRemoteS.getString("id");
                System.out.println(remoteSlaveID);
                FrameworkStarter.ds.setRemoteSlaveid(remoteSlaveID);

                //Creating JSON body for POST request
                String jsonBody = "{\"mesos_master_id\":\"" + FrameworkStarter.ds.getMesosMasterID() +
                        "\",\"mesos_slave_id\":\"" + FrameworkStarter.ds.getRemoteSlaveid() + "\",\"STATUS\":\"ACTIVE\"}";

                String postURL="http://54.153.75.141:4000/slave";

                //String ServerOutput=SendInfo.postInfo(jsonBody,postURL);
                //System.out.println(ServerOutput);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    @PUT
    @Path("launchSlave")
    @Consumes(MediaType.TEXT_PLAIN)
    public void launchSlave() {
        try {
            System.out.println(slaveLaunchCommand);
            Process p = Runtime.getRuntime().exec(slaveLaunchCommand);
            FrameworkStarter.ds.setSlaveProcess(p);
            FrameworkStarter.ds.setSlaveStarted(true);
            System.out.println("Launching a Mesos Slave");

            try {
                System.out.println("Sleeping for 20 seconds");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //Building a URL
            //String slaveState = "http://54.219.141.65:5051/state";
            String slaveState = "http://"+FrameworkStarter.ds.getMesosIP()+":5051/state";
            JSONObject jsonObjSlave=SendInfo.getJSON(slaveState);
            String slaveID=jsonObjSlave.getString("id");
            FrameworkStarter.ds.setMesosSlaveID(slaveID);

            String jsonBody = "{\"mesos_master_ip\":\""+FrameworkStarter.ds.getMesosIP()+"\",\"mesos_master_id\":\"" + FrameworkStarter.ds.getMesosMasterID() + "\",\"mesos_slave_id\":\"" + FrameworkStarter.ds.getMesosSlaveID() + "\",\"STATUS\":\"ACTIVE\"}";
//            System.out.println(jsonBody);

            String postUrl="http://54.153.75.141:4000/slave";
            //String serverOutput=SendInfo.postInfo(jsonBody,postUrl);
            //System.out.println("Message from Server:"+serverOutput);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @PUT
    @Path("launchMaster")
    @Consumes(MediaType.TEXT_PLAIN)
    public void launchMaster() {
        try {
            System.out.println(masterLaunchCommand);
            Process p = Runtime.getRuntime().exec(masterLaunchCommand);
            FrameworkStarter.ds.setMasterProcess(p);
            FrameworkStarter.ds.setMasterStarted(true);
            System.out.println("Launching Mesos Master");

            try {
                System.out.println("Sleeping for 20 seconds");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Building a URL
            String masterState = "http://"+FrameworkStarter.ds.getMesosIPPort()+"/master/state";
            JSONObject jsonObjMaster = SendInfo.getJSON(masterState);
            String masterID = jsonObjMaster.getString("id");
            FrameworkStarter.ds.setMesosMasterID(masterID);
           System.out.println(masterID);

            String jsonBody = "{\"mesos_master_ip\":\""+FrameworkStarter.ds.getMesosIP()+"\",\"mesos_master_id\":" + "\"" + FrameworkStarter.ds.getMesosMasterID() + "\"}";
            String postUrl="http://54.153.75.141:4000/mesosmaster";
            //String serverOutput=SendInfo.postInfo(jsonBody,postUrl);
            //System.out.println("Message from Server:"+serverOutput);




//            StringBuilder result = new StringBuilder();
//            URL url = new URL(masterState);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String line;
//            while ((line = rd.readLine()) != null) {
//                result.append(line);
//            }
//            rd.close();
//
//            JSONObject jsonObjMaster = new JSONObject(result.toString());
//            String masterID = jsonObjMaster.getString("id");
//            FrameworkStarter.ds.setMesosMasterID(masterID);
//            System.out.println(masterID);
//            conn.disconnect();
//
//
//            URL urlServer = new URL("http://54.153.75.141:4000/mesosmaster");
//            HttpURLConnection Mastercon = (HttpURLConnection) urlServer.openConnection();
//            System.out.println("HTTP connection con worked");
//            System.out.println("This is the initial response code"+Mastercon.getResponseCode());
//            String input = "{\"mesos_master_ip\":\"54.219.141.65\",\"mesos_master_id\":" + "\"" + FrameworkStarter.ds.getMesosMasterID() + "\"}";
//            System.out.println(input);
//
//
//            Mastercon.setDoOutput(true);
//            Mastercon.setDoInput(true);
//            Mastercon.setRequestMethod("POST");
//            Mastercon.setRequestProperty("Content-Type", "application/json");
//
//            System.out.println("Content type worked");
//
//            //String input = "{\"mesos_master_ip\":\"54.219.141.65\",\"mesos_master_id\":" + "\"" + FrameworkStarter.ds.getMesosMasterID() + "\"}";
//
//            System.out.println(input);
//
//            OutputStream os = Mastercon.getOutputStream();
//            os.write(input.getBytes());
//            os.flush();
//
//            System.out.println("before getting RESPONSE CODE");
//
////            if (con.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
////                throw new RuntimeException("Failed : HTTP error code : "
////                        + con.getResponseCode());
////            }
//
//            System.out.println(Mastercon.getResponseCode() + "After getting RESPONSE CODE");
//
//            StringBuilder sb = new StringBuilder();
//            int HttpResult = Mastercon.getResponseCode();
//            if (HttpResult == HttpURLConnection.HTTP_OK) {
//                BufferedReader br = new BufferedReader(
//                        new InputStreamReader(Mastercon.getInputStream(), "utf-8"));
//                System.out.println("buffering thing");
//                String output = null;
//                while ((output = br.readLine()) != null) {
//                    sb.append(output + "\n");
//                }
//                br.close();
//                System.out.println("" + sb.toString());
//            } else {
//                System.out.println(Mastercon.getResponseMessage());
//            }
//            Mastercon.disconnect();


            /*OutputStreamWriter out=new OutputStreamWriter(httpCon.getOutputStream());
            out.write(FrameworkStarter.ds.getMesosMasterID());
            out.close();
            httpCon.getInputStream();
            System.out.println(url1.toString()+FrameworkStarter.ds.getMesosMasterID());*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PUT
    @Path("launchDocker")
    @Consumes(MediaType.TEXT_PLAIN)
    public void launchDocker() throws IOException {
        // This is the launching docker framework
        System.out.println("Here in launchDocker");
//        FrameworkStarter fw = new FrameworkStarter();
        System.out.println(FrameworkStarter.ds.getMesosIPPort());
        System.out.println(FrameworkStarter.ds.getDockerImageName());
        FrameworkStarter.launchDocker(FrameworkStarter.ds.getMesosIPPort(), FrameworkStarter.ds.getDockerImageName());
        // create framework object (Done)
        // call launch docker ( Done)
        // ( need to store task id in DS. Should be part of DockerPiScheduler) (done)
        // call PUT of Nidhi's machine with all the stored info.
        System.out.println("Launching Docker Process");
        System.out.println("Launching Docker Image Busybox");


        //Building a URL
        String dockerState = "http://"+FrameworkStarter.ds.getMesosIPPort()+"/master/tasks";
        JSONObject jsonObjDocker=SendInfo.getJSON(dockerState);

        JSONArray dockerArray=jsonObjDocker.getJSONArray("tasks");
        System.out.println("dockerArray is:"+ dockerArray.toString());
        JSONObject task1=dockerArray.getJSONObject(4);
        String dockerID=task1.getString("id");
        String dockerStatus=task1.getString("state");
        String dockerSlave=task1.getString("slave_id");
        FrameworkStarter.ds.setDockerContainerID(dockerID);
        FrameworkStarter.ds.setDockerSTATUS(dockerStatus);



        //String jsonBody = "{\"mesos_master_id\":\"" + FrameworkStarter.ds.getMesosMasterID() + "\",\"mesos_slave_id\":\"" + dockerSlave + "\",\"STATUS\":\"ACTIVE\"}";
//            System.out.println(jsonBody);
        String jsonBody = "{\"mesos_master_id\":\"" + FrameworkStarter.ds.getMesosMasterID() + "\",\"mesos_master_ip\":\""+FrameworkStarter.ds.getMesosIP()+"\",\"mesos_slave_id\":\"" + dockerSlave +
                "\",\"container_id\":\"" + FrameworkStarter.ds.dockerContainerID + "\",\"container_status\":\"" + FrameworkStarter.ds.getDockerSTATUS() + "\"}";

        System.out.println(jsonBody);
        String postUrl="http://54.153.75.141:4000/dockercontainer";
        String serverOutput=SendInfo.postInfo(jsonBody,postUrl);
        System.out.println("Message from Server:"+serverOutput);














        /*
        //This is the working PART

        Process p_cont = Runtime.getRuntime().exec("sudo docker ps -alq --no-trunc");

        try {
            System.out.println("Sleeping for 5 seconds");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        StringBuilder launchSB = new StringBuilder();

        BufferedReader launchBR = new BufferedReader(new
                InputStreamReader(p_cont.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p_cont.getErrorStream()));


        System.out.println("Here is the standard output of the command:\n");
        String dockerID = null;
        while ((dockerID = launchBR.readLine()) != null) {
            launchSB.append(dockerID + "\n");
            System.out.println(launchSB.toString());
        }
        dockerID = launchSB.toString();
        launchBR.close();
        FrameworkStarter.ds.setDockerContainerID(dockerID);
        System.out.println("This the getDockerContainerID From FW DS:" + FrameworkStarter.ds.getDockerContainerID());

        //Building a URL
        String dockerState = "http://54.219.141.65:4243/containers/json?limit=1";
        StringBuilder dockerState_SB = new StringBuilder();
        URL urldOCKERState = new URL(dockerState);

        System.out.println("SLAVE URL  ---------->" + dockerState.toString());

        HttpURLConnection Dockerconn = (HttpURLConnection) urldOCKERState.openConnection();
        Dockerconn.setRequestMethod("GET");
        System.out.println("doNE WITH ESTABLISHING A CONNECTION");
        BufferedReader rd = new BufferedReader(new InputStreamReader(Dockerconn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            dockerState_SB.append(line);
        }
        rd.close();
        System.out.println(dockerState_SB.toString());


        JSONArray jsonArrDocker = new JSONArray(dockerState_SB.toString());
        System.out.println("Printing JSON OBJECT-------->>" + jsonArrDocker);
        JSONObject jsonObjDocker = jsonArrDocker.getJSONObject(0);
        String status = jsonObjDocker.getString("State");
        FrameworkStarter.ds.setDockerSTATUS(status);
        System.out.println("The ID and Status is: " + FrameworkStarter.ds.dockerContainerID + "   and    " + FrameworkStarter.ds.getDockerSTATUS());


        URL urlServer = new URL("http://54.153.75.141:4000/dockercontainer");
        HttpURLConnection con = (HttpURLConnection) urlServer.openConnection();
        System.out.println("HTTP connection con worked");

        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        System.out.println("Content type worked");

        String input = "{\"mesos_master_id\":\"" + FrameworkStarter.ds.getMesosMasterID() + "\",\"mesos_slave_id\":\"" + FrameworkStarter.ds.getMesosSlaveID() +
                "\",\"Container_id\":\"" + FrameworkStarter.ds.dockerContainerID + "\",\"STATUS\":\"" + FrameworkStarter.ds.getDockerSTATUS() + "\"}";


        System.out.println(input);

        OutputStream os = con.getOutputStream();
        os.write(input.getBytes());
        os.flush();

        System.out.println("before getting RESPONSE CODE");
        System.out.println(con.getResponseCode() + "After getting RESPONSE CODE");

        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            System.out.println("buffering thing");
            String output = null;
            while ((output = br.readLine()) != null) {
                sb.append(output + "\n");
            }
            br.close();
            System.out.println("" + sb.toString());
        } else {
            System.out.println(con.getResponseMessage());
        }
        con.disconnect();
        Dockerconn.disconnect();
// Till here!!
*/
    }


  /*  @PUT
    @Path("docker")
    @Consumes(MediaType.TEXT_PLAIN)
    public void launchDocker_1(){
        try{
            Process p = Runtime.getRuntime().exec(dockerLaunchCommand);
            FrameworkStarter.ds.setDockerProcess(p);
            FrameworkStarter.ds.setDockerStarted(true);
            System.out.println("Launching Docker Hello-World");

            Process p_cont=Runtime.getRuntime().exec("sudo docker ps -alq --no-trunc");

            try {
                System.out.println("Sleeping for 5 seconds");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            StringBuilder launchSB=new StringBuilder();

            BufferedReader launchBR = new BufferedReader(new
                    InputStreamReader(p_cont.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p_cont.getErrorStream()));


            System.out.println("Here is the standard output of the command:\n");
            String dockerID = null;
            while ((dockerID = launchBR.readLine()) != null) {
                launchSB.append(dockerID+"\n");
                System.out.println(launchSB.toString());
            }
            dockerID=launchSB.toString();
            launchBR.close();
            FrameworkStarter.ds.setDockerContainerID(dockerID);
            System.out.println("This the getDockerContainerID From FW DS:"+FrameworkStarter.ds.getDockerContainerID());

            // read any errors from the attempted command
//            System.out.println("Here is the standard error of the command (if any):\n");
//            while ((dockerID = stdError.readLine()) != null) {
//                System.out.println(dockerID);
//            }

            //Executing "CONTAINER_ID=dockerID
            //String[] setDockerIDCommand = { "echo" };
            //Process setDockerID=Runtime.getRuntime().exec(setDockerIDCommand);
            //Process setDockerID=Runtime.getRuntime().exec("bin/bash CONTAINER_ID="+FrameworkStarter.ds.getDockerContainerID());

//            Process setDockerID_0 = new ProcessBuilder("CONTAINER_ID","=",FrameworkStarter.ds.getDockerContainerID()).start();
//            System.out.println(setDockerID_0);
//
//            try {
//                System.out.println("Sleeping for 5 seconds after assigning container id");
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            Process setDockerID=new ProcessBuilder("echo","$CONTAINER_ID").start();
//
//
//            try {
//                System.out.println("Sleeping for 5 seconds after asking to echo");
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            StringBuilder dockerID_sb=new StringBuilder();
//            BufferedReader dockerOutput = new BufferedReader(new
//                    InputStreamReader(setDockerID.getInputStream()));
//
//            BufferedReader dockerError = new BufferedReader(new
//                    InputStreamReader(setDockerID.getErrorStream()));
//
//            System.out.println("Here is the standard output of the command (if any) FOR ECHO :\n");
//            String Dockerline=null;
//            while ((Dockerline= dockerOutput.readLine()) != null) {
//                dockerID_sb.append(Dockerline);}
//                System.out.println(Dockerline.toString());
//
//
//
//            // read any errors from the attempted command
//            System.out.println("Here is the standard error of the command (if any):\n");
//
//            while ((Dockerline= dockerError.readLine()) != null) {
//                System.out.println(dockerID);}





//            Process dockerStatus=Runtime.getRuntime().exec("sudo docker inspect "+FrameworkStarter.ds.dockerContainerID);
//            System.out.println("printing command"+dockerStatus);
//
//            StringBuilder dockerStatus_SB = new StringBuilder();
//            BufferedReader dockerStatus_BR = new BufferedReader(new
//                    InputStreamReader(dockerStatus.getInputStream()));
//
//            BufferedReader dockerError_BR = new BufferedReader(new
//                    InputStreamReader(dockerStatus.getErrorStream()));
//
//            System.out.println("Here is the standard output of the command (if any):\n");
//            String Dockerline=null;
//            while ((Dockerline= dockerStatus_BR.readLine()) != null) {
//                dockerStatus_SB.append(Dockerline);
//                System.out.println(dockerStatus_SB.toString());}
//
//
//            // read any errors from the attempted command
//            System.out.println("Here is the standard error of the command (if any):\n");
//
//            while ((Dockerline= dockerError_BR.readLine()) != null) {
//                System.out.println(dockerID);}

            // Converting the output string into JSON object
//
//            StringBuilder Status_SB = new StringBuilder();
//
//            BufferedReader Status_BR = new BufferedReader(new
//                    InputStreamReader(dockerStatus.getInputStream()));
//            String result = null;
//            while ((result = dockerStatus_BR.readLine()) != null) {
//                Status_SB.append(result);
//                //System.out.println(Status_SB.toString());
//            }
//            Status_BR.close();



            //Building a URL
            String dockerState="http://54.219.141.65:4243/containers/json?limit=1";
            StringBuilder dockerState_SB = new StringBuilder();
            URL urldOCKERState = new URL(dockerState);

            System.out.println("SLAVE URL  ---------->"+dockerState.toString());

            HttpURLConnection Dockerconn = (HttpURLConnection) urldOCKERState.openConnection();
            Dockerconn.setRequestMethod("GET");
            System.out.println("doNE WITH ESTABLISHING A CONNECTION");
            BufferedReader rd = new BufferedReader(new InputStreamReader(Dockerconn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                dockerState_SB.append(line);
            }
            rd.close();
            System.out.println(dockerState_SB.toString());



            JSONArray jsonArrDocker = new JSONArray(dockerState_SB.toString());
            System.out.println("Printing JSON OBJECT-------->>"+jsonArrDocker);
            JSONObject jsonObjDocker=jsonArrDocker.getJSONObject(0);
            String status=jsonObjDocker.getString("State");
            FrameworkStarter.ds.setDockerSTATUS(status);
            System.out.println("The ID and Status is: "+FrameworkStarter.ds.dockerContainerID+"   and    "+FrameworkStarter.ds.getDockerSTATUS());


            URL urlServer = new URL("http://54.153.75.141:4000/dockercontainerregister");
            HttpURLConnection con = (HttpURLConnection) urlServer.openConnection();
            System.out.println("HTTP connection con worked");

            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            System.out.println("Content type worked");

            String input = "{\"mesos_master_id\":\""+FrameworkStarter.ds.getMesosMasterID()+"\",\"mesos_slave_id\":\""+FrameworkStarter.ds.getMesosSlaveID()+
                    "\",\"Container_id\":\""+FrameworkStarter.ds.dockerContainerID+"\",\"STATUS\":\""+FrameworkStarter.ds.getDockerSTATUS()+"\"}";


            System.out.println(input);

            OutputStream os = con.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            System.out.println("before getting RESPONSE CODE");
            System.out.println(con.getResponseCode()+"After getting RESPONSE CODE");

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                System.out.println("buffering thing");
                String output = null;
                while ((output = br.readLine()) != null) {
                    sb.append(output + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
            } else {
                System.out.println(con.getResponseMessage());
            }
            con.disconnect();
            Dockerconn.disconnect();



/*
StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                System.out.println("buffering thing");
                String output = null;
                while ((output = br.readLine()) != null) {
                    sb.append(output + "\n");
                }
                br.close();
                System.out.println("" + sb.toString());
 */


//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }

    // This part is to send Nidhi Docker ID and containers.
         /* This is the launching docker framework

        FrameworkStarter fw = new FrameworkStarter();
        fw.lauchDocker(FrameworkStarter.ds.getMesosIPPort(),FrameworkStarter.ds.getDockerImageName());
        // create framework object (Done)
        // call launch docker ( Done)
        // ( need to store task id in DS. Should be part of DockerPiScheduler) (done)
        // call PUT of Nidhi's machine with all the stored info.
        System.out.println("Launching Docker Process");
        System.out.println("Launching Docker Image Busybox");
        */


    @PUT
    @Path("killSlave")
    @Consumes(MediaType.TEXT_PLAIN)
    public void killSlave(){
        try {
            System.out.println(slaveKillCommand);
            Process p = Runtime.getRuntime().exec(slaveKillCommand);
            FrameworkStarter.ds.setSlaveProcess(p);
            System.out.println("Killing Mesos Slave with ID" +FrameworkStarter.ds.getMesosSlaveID());

            try {
                System.out.println("Sleeping for 10 seconds");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            URL serverUrl=new URL("http://54.153.75.141:4000/dockercontainerregister");
            HttpURLConnection httpCon = (HttpURLConnection) serverUrl.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("POST");
            httpCon.setRequestProperty("Content-Type", "application/json");

            String input = "{\"mesos_master_id\":\""+FrameworkStarter.ds.getMesosMasterID()+"\",\"mesos_slave_id\":\""+FrameworkStarter.ds.getMesosSlaveID()+"\",\"STATUS\":\"KILLED\"}";
            System.out.println(input);
            OutputStream os = httpCon.getOutputStream();
            os.write(input.getBytes());
            os.flush();

//            if (httpCon.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + httpCon.getResponseCode());
//            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (httpCon.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
            httpCon.disconnect();
            FrameworkStarter.ds.setMesosSlaveID("");



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
