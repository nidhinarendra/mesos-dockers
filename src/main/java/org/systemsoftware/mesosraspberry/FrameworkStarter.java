package org.systemsoftware.mesosraspberry;

import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkInfo;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
//import org.json.*;


//Environment="MESOS_NATIVE_JAVA_LIBRARY=/usr/lib/libmesos.so";
/**
 * Created by anusha vijay
 */
public class FrameworkStarter {

    final public static DataStore ds = new DataStore();
    public static FrameworkInfo.Builder mesosFramework = null;
    public static DockerPIScheduler dockerScheduler;
    public static MesosSchedulerDriver driver = null;

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("Provide 4 args : IP:Port slaveIP dockerImageName MesosHome nodeList");
            System.exit(1);
        }

        int timeOut = 0;

        ds.setMesosHome(args[3]);
        ds.setMesosIP(args[0]);
        ds.setMesosIPPort(args[0]);
        ds.setDockerImageName(args[2]);
        ds.setSlaveIP(args[1]);

////         creating a framework object
        mesosFramework = FrameworkInfo.newBuilder()
                .setName("MesosOnRaspberry")
                .setUser("")
                .setFailoverTimeout(timeOut);

//         Not sure of this
        if (System.getenv("MESOS_CHECKPOINT") != null) {
            System.out.println("Enabling checkpoint for the framework");
            mesosFramework.setCheckpoint(true);
        }

        String dockerName = ds.getDockerImageName();
//
//         Create a scheduler object
       dockerScheduler = new DockerPIScheduler(dockerName);

        mesosFramework.setPrincipal("docker-pi-java");
        driver =new MesosSchedulerDriver(dockerScheduler, mesosFramework.build(), args[0]);

        //int status = driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1;

//         Ensure that the driver process terminates.
        //driver.stop();

        //System.exit(status);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // create a server object associate with a port
        System.out.println("Starting server on port 8081");
        Server appServer = new Server(8081);
        appServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                Launcher.class.getCanonicalName());

        try {
            appServer.start();
            appServer.join();
        } catch (Exception e) {
            System.out.println("Could not start server");
            e.printStackTrace();
        }
        finally {
            appServer.destroy();
            if(ds.getSlaveProcess() != null)
                ds.getSlaveProcess().destroy();
            if(ds.getMasterProcess() != null)
                ds.getMasterProcess().destroy();
        }

    }

    public static void launchDocker(String dockerName, String mesosIPPort){
        int timeOut = 0;
        // creating a framework object
//        FrameworkInfo.Builder mesosFramework = FrameworkInfo.newBuilder()
//                .setName("MesosOnRaspberry")
//                .setUser("")
//                .setFailoverTimeout(timeOut);
//
//        // Not sure of this
//        if (System.getenv("MESOS_CHECKPOINT") != null) {
//            System.out.println("Enabling checkpoint for the framework");
//            mesosFramework.setCheckpoint(true);
//        }
//
//        // Create a scheduler object
//        DockerPIScheduler dockerScheduler = new DockerPIScheduler(dockerName);
//
//        System.out.println("Inside Docker ");
//        mesosFramework.setPrincipal("docker-pi-java");
//        MesosSchedulerDriver driver = new MesosSchedulerDriver(dockerScheduler, mesosFramework.build(), mesosIPPort);

        int status = FrameworkStarter.driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1;

//         Ensure that the driver process terminates.
        try {
            System.out.println("Sleeping for 1 mins ");
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FrameworkStarter.driver.stop();

//        System.exit(status);
    }
}
