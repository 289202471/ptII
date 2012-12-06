/**
 * 
 * This file is part of Modelica Development Tooling (MDT).
 * The Modelica Development Tooling (MDT) software is 
 * distributed under the conditions specified below.
 *
 * Copyright (c) 2005-2006,
 * The MDT Team: 
 * @author Adrian Pop [adrpo@ida.liu.se], 
 * @author Elmir Jagudin, 
 * @author Andreas Remar, 
 * @author Mana Mirzaei
 * Programming Environments Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 * 
 * All rights reserved.
 *
 * (The new BSD license, see also 
 *  http://www.opensource.org/licenses/bsd-license.php)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Authors nor the name of Linkopings University nor 
 *   the names of its contributors may be used to endorse or promote products 
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ptolemy.domains.openmodelica.lib.openmodelica.omc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import ptolemy.domains.openmodelica.lib.openmodelica.core.CompilerResult;
import ptolemy.domains.openmodelica.lib.openmodelica.core.ICompilerResult;
import ptolemy.domains.openmodelica.lib.openmodelica.core.ParseResults;
import ptolemy.domains.openmodelica.lib.openmodelica.core.compiler.ConnectException;
import ptolemy.domains.openmodelica.lib.openmodelica.core.compiler.IModelicaCompiler;
import ptolemy.domains.openmodelica.lib.openmodelica.omc.corba.OmcCommunication;
import ptolemy.domains.openmodelica.lib.openmodelica.omc.corba.OmcCommunicationHelper;
import ptolemy.domains.openmodelica.kernel.OpenModelicaDirector;

import org.omg.CORBA.ORB;

/**
 * The OMCProxy is the glue between the OpenModelica Compiler and MDT.
 * It uses the interactive API of OMC to get getInformation about classes
 * and to load classes into OMC.
 * 
 * @author Adrian Pop
 * @author Andreas Remar
 */

public class OMCProxy implements IModelicaCompiler {

    public OMCProxy() {      

    }

    /////////////////////////////////////////////////////////////////////////
    ////                            public variables                    ////
    
    
    public static String[] env = null;
    /** indicates if we've setup the communication with OMC */
    public boolean hasInitialized = false;
    /** the CORBA object */
    public OmcCommunication omcc;
    /** what Operating System we're running on */
    public static osType os;
    public static enum osType {
        UNIX, WINDOWS
    }
    
    public static File workDir = null;
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////                            public methods                      ////

    class OMCThread extends Thread {

        public OMCThread() {
            super("OpenModelica Interactive Compiler Thread");
        }

        public void run() {
            File tmp[] = null;
            try {
                tmp = getOmcBinaryPaths();
            } catch (ConnectException e) {
                couldNotStartOMC = true;
                hasInitialized = false;
                return;
            }

            File omcBinary = tmp[0];
            final File workingDirectory = tmp[1];

            File f = new File(getPathToObject());
            /*
             * Delete old object reference file. We need to do this because we're
             * checking if the file exists to determine if the server has started
             * or not (further down). 
             */
            long lastModified = 0;
            if (f.exists()) {
                OpenModelicaDirector._ptLogger
                        .getInfo("Remember the creation time for old OMC object reference file.");
                lastModified = f.lastModified();
            }

            Process proc = null;
            StreamReaderThread outThread = null;
            StreamReaderThread errThread = null;
            String command[] = { omcBinary.getAbsolutePath(),
                    "+c=" + corbaSession, "+d=interactiveCorba" };
            ArrayList<String> both = new ArrayList<String>(command.length);
            Collections.addAll(both, command);

            String cmd[] = new String[both.size()];
            int nonNull = 0;
            for (int i = 0; i < both.size(); i++) {
                String str = (String) both.get(i);
                if (str != null) {
                    cmd[nonNull] = str;
                    nonNull++;
                }
            }
            String fullCMD = "";
            for (int i = 0; i < nonNull; i++)
                fullCMD += cmd[i] + " ";
            OpenModelicaDirector._ptLogger.getInfo("Running command: " + fullCMD);
            OpenModelicaDirector._ptLogger.getInfo("Setting working directory to: "
                    + workingDirectory.getAbsolutePath());

            try {
                /* prepare buffers for process output and error streams*/
                if (System.getenv("OPENMODELICAHOME") == null) {
                    Map<String, String> envMap = System.getenv();
                    Set<Entry<String, String>> entrySet = envMap.entrySet();
                    Collection<String> lst = new ArrayList<String>();
                    String x = "OPENMODELICAHOME="
                            + omcBinary.getParentFile().getParentFile()
                                    .getAbsolutePath();
                    lst.add(x);

                    if (System.getenv("OPENMODELICALIBRARY") == null) {
                        String y = "OPENMODELICALIBRARY="
                                + omcBinary.getParentFile().getParentFile()
                                        .getAbsolutePath() + "/lib/omlibrary";
                        lst.add(y);
                    }

                    Iterator<Entry<String, String>> i = entrySet.iterator();
                    while (i.hasNext()) {
                        Entry<String, String> z = i.next();
                        lst.add(z.getKey() + "=" + z.getValue());
                    }
                    env = lst.toArray(new String[lst.size()]);
                }
                proc = Runtime.getRuntime().exec(cmd, env, workingDirectory);
                workDir = workingDirectory;
                /*create thread for reading inputStream (process' stdout)*/
                outThread = new StreamReaderThread(proc.getInputStream(),
                        System.out);
                /*create thread for reading errorStream (process' stderr)*/
                errThread = new StreamReaderThread(proc.getErrorStream(),
                        System.err);
                /*start both threads*/
                outThread.start();
                errThread.start();
            } catch (IOException e) {
                OpenModelicaDirector._ptLogger.getInfo("Failed to run command: " + fullCMD);
                couldNotStartOMC = true;
                hasInitialized = false;
                return;
            }
            OpenModelicaDirector._ptLogger.getInfo("Command run successfully.");
            OpenModelicaDirector._ptLogger
                    .getInfo("Waiting for OMC CORBA object reference to appear on disk.");
            /*
             * Wait until the object exists on disk, but if it takes longer than
             * 10 seconds, abort. (Very arbitrary 10 seconds..)
             */
            int ticks = 0;
            while (true) {
                if (f.exists())
                    if (lastModified == 0
                            || (lastModified != 0 && f.lastModified() != lastModified))
                        break;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    /* ignore */
                }
                ticks++;
                /* If we've waited for around 5 seconds, abort the wait for OMC */
                if (ticks > 100) {
                    OpenModelicaDirector._ptLogger
                            .getInfo("The OMC Corba object reference file has not been modified in 100 seconds; we give up starting OMC.");
                    couldNotStartOMC = true;
                    hasInitialized = false;
                    return;
                }

            }
            OpenModelicaDirector._ptLogger.getInfo("OMC object reference found.");

            int omcExitCode = 0;
            try {
                /**wait for process to end*/
                omcExitCode = proc.waitFor();

                /**finish reading whatever's left in the buffers*/
                outThread.join();
                errThread.join();
            } catch (InterruptedException e) {
                OpenModelicaDirector._ptLogger.getSever("OpenModelica compiler interrupted:"
                        + e.getMessage() + " with code " + proc.exitValue());
                couldNotStartOMC = true;
                hasInitialized = false;
                return;
            }
            OpenModelicaDirector._ptLogger.getInfo("OpenModelica compiler exited with code: "
                    + proc.exitValue());
            couldNotStartOMC = true;
            hasInitialized = false;
        }
    };

   

    /** 
     * build the model
     *  @param className The name of the main class of the model
     *  @exception ConnectException If buildModel command couldn't 
     *  be sent to the OMC.
     */

    public CompilerResult buildModel(String className) throws ConnectException {
        CompilerResult result = sendCommand("buildModel(" + className + ")");
        return result;
    }

    /**
     * @return the name of the operating system. If an unknown os is found,
     * the default is Unix. 
     */
    public static osType getOs() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Linux")) {
            return osType.UNIX;
        } else if (osName.contains("Windows")) {

            return osType.WINDOWS;
        } else {
            /* If the OS is not GNU/Linux or Windows, default to Unix */
            return osType.UNIX;
        }
    }

    
    /**
     * Initialize the communication with the OMC
     * @throws ConnectException if we're unable to start communicating with
     * the server
     */
    public synchronized void init() throws ConnectException {
        /* 
         * Get type of operating system, used for finding object
         * reference and starting OMC if the reference is faulty 
         */
        os = getOs();

        /*set time format for the corbasession*/

        String strDate = "";
        Date date = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        strDate = timeFormat.format(date);
        corbaSession = strDate;

        /* See if an OMC server is already running */

        File f = new File(getPathToObject());
        String stringifiedObjectReference = null;
        if (!f.exists()) {
            /* If a server isn't running, start it */
            OpenModelicaDirector._ptLogger
                    .getInfo("No OMC object reference found, starting server.");
            startServer();
        } else
            OpenModelicaDirector._ptLogger
                    .getInfo("Old OMC CORBA object reference present, assuming OMC is running.");

        /* Read in the CORBA OMC object from a file on disk */
        stringifiedObjectReference = readObjectFromFile();

        /*
         * Setup up OMC object reference by initializing ORB and then
         * converting the string object to a real CORBA object.
         */
        setupOmcc(stringifiedObjectReference);
        hasInitialized = true;
    }

    /**
     * @author Adrian Pop [adrpo@ida.liu.se]
     * @param retval, the string returned by the OMC compiler
     * @return checks if the string is actually an error
     */
    public boolean isError(String retval) {
        if (retval == null)
            return false;
        /*
         * See if there were parse errors, an empty list {} also denotes error
         */
        return (retval.toLowerCase().contains("error"));
    }

    /**  
     *   Load models from the file.
     *  @param fname The file name.
     *  @exception ConnectException If loadFileInteractiveQualified command couldn't 
     *  be sent to the OMC.
     */

    public CompilerResult loadFile(String fname) throws ConnectException {
        CompilerResult result = sendCommand("loadFileInteractiveQualified(\"" + fname + "\")");
        return result;
    }

    /** Send a command to the OMC and gets the result string
     *  @param command The command which should be sent to the OMC.
     *  @return ICompilerResult
     *  @exception ConnectException If commands couldn't 
     *  be sent to the OMC.
     */

    public CompilerResult sendCommand(String command) throws ConnectException {

        String error = null;
        String[] retval = { "" };
        /* if we couldn't start OMC, don't even bother anymore!*/
        if (couldNotStartOMC)
            return (CompilerResult) CompilerResult.makeResult(retval, error);

        if (numberOfErrors > showMaxErrors)
            return (CompilerResult) CompilerResult.makeResult(retval, error);

        /* trim the start and end spaces*/
        command = command.trim();

        if (hasInitialized == false)
            init();

        try {

            /*
             * Fetches the error string from OMC. This should be called after an "Error"
             * is received. (Or whenever the queue of errors should be emptied.)
             */

            retval[0] = omcc.sendExpression(command);

            if (!command.equalsIgnoreCase("quit()"))
                error = omcc.sendExpression("getErrorString()");

            /* Make sure the error string isn't empty */

            if (error != null && error.length() > 2) {
                error = error.trim();
                error = error.substring(1, error.length() - 1);
            } else {
                error = null;
            }

            return (CompilerResult) CompilerResult.makeResult(retval, error);

        } catch (org.omg.CORBA.COMM_FAILURE x) {
            numberOfErrors++;
            /** lost connection to OMC or something */
            throw new ConnectException(
                    "Couldn't send command to the OpenModelica Compiler. Tried sending: "
                            + command);
        }
    }

    /** 
     * 
     * Leave and quit OpenModelica environment
     *  @exception ConnectException If quit command couldn't 
     *  be sent to OMC.
     */

    public void quit() throws ConnectException {

        if (hasInitialized = true) {
            sendCommand("quit()");
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                                protected class                  ////

    protected class LoadResult {
        long lastModification;
        ParseResults results;

        public LoadResult(ParseResults results, long lastModification) {
            this.results = results;
            this.lastModification = lastModification;
        }

    }

    /////////////////////////////////////////////////////////////////////////
    ////                           private methods                      ////

    private File findExecutableOnPath(String executableName) {
        String systemPath = System.getenv("PATH");
        /* try with small letters*/
        if (systemPath == null)
            systemPath = System.getenv("path");
        String[] pathDirs = systemPath.split(File.pathSeparator);

        File fullyQualifiedExecutable = null;
        for (String pathDir : pathDirs) {
            File file = new File(pathDir, executableName);
            if (file.isFile()) {
                fullyQualifiedExecutable = file;
                break;
            }
        }
        return fullyQualifiedExecutable;
    }

    /**
     * With the help of voodoo magic determines the path to the
     * omc binary that user (probably) wants to use and the working
     * directory of where that binary (most likely) should be started in
     * 
     * This will returns for example 'c:\openmodelica132\omc.exe'
     * or '/usr/local/share/openmodelica/omc' depending on
     * such factors as: OS type, environment variables settings,
     * where the first matching
     * binary found and the weather outside. 
     * 
     * @return full path to the omc binary and the working folder  
     */
    private File[] getOmcBinaryPaths() throws ConnectException {
        String binaryName = "omc";

        if (os == osType.WINDOWS) {
            binaryName += ".exe";
        }

        File omcBinary = null;
        File omcWorkingDirectory = null;
        File openModelicaHomeDirectory = null;
        /*
         * user specified that standard path to omc should be used,
         * try to determine the omc path via the OPENMODELICAHOME and
         * by checking in it's various subdirectory for the omc binary file
         */
        OpenModelicaDirector._ptLogger
                .getInfo("Using OPENMODELICAHOME environment variable to find omc-binary");

        /*
         * Standard path to omc (or omc.exe) binary is encoded in OPENMODELICAHOME
         * variable. If we don't find it just search the path!
         */
        String openModelicaHome = System.getenv("OPENMODELICAHOME");
        if (openModelicaHome == null) {
            OpenModelicaDirector._ptLogger
                    .getInfo("OPENMODELICAHOME environment variable is NULL, trying the PATH variable");
            File omc = findExecutableOnPath(binaryName);
            if (omc != null) {
                OpenModelicaDirector._ptLogger.getInfo("Found omc executable in the path here: "
                        + omc.getAbsolutePath());

                openModelicaHome = omc.getParentFile().getParentFile()
                        .getAbsolutePath();
            } else {
                final String m = "Environment variable OPENMODELICAHOME is not set and we could not find: "
                        + binaryName + " in the PATH";
                OpenModelicaDirector._ptLogger.getInfo(m);
                throw new ConnectException(m);
            }
        }

        openModelicaHomeDirectory = new File(openModelicaHome);

        /* the subdirectories where omc binary may be located, hurray for standards! */
        /* adrpo 2012-06-12 do not support the old ways! "/omc" and "Compiler/omc"*/
        String[] subdirs = { "bin" };

        for (String subdir : subdirs) {
            String path = openModelicaHomeDirectory.getAbsolutePath()
                    + File.separator;
            path += subdir.equals("") ? binaryName : subdir + File.separator
                    + binaryName;

            File file = new File(path);

            if (file.exists()) {
                omcBinary = file;
                OpenModelicaDirector._ptLogger.getInfo("Using omc-binary at '"
                        + omcBinary.getAbsolutePath() + "'");
                break;
            } else {

                OpenModelicaDirector._ptLogger.getInfo("No omc binary at: [" + path + "]");

            }
        }

        if (omcBinary == null) {
            OpenModelicaDirector._ptLogger
                    .getInfo("Could not find omc-binary on the OPENMODELICAHOME path or in the PATH variable");
            throw new ConnectException(
                    "Unable to start the OpenModelica Compiler, binary not found");
        }

        /*
         * always start in /tmp
         */

        /* set the working directory to temp/OpenModelica*/
        omcWorkingDirectory = new File(System.getProperty("java.io.tmpdir"));
        OpenModelicaDirector._ptLogger.getInfo("Using working directory '"
                + omcWorkingDirectory.getAbsolutePath() + "'");

        return new File[] { omcBinary, omcWorkingDirectory };
    }

    
    /**
     * @return Returns the path to the OMC CORBA object that is stored on disk.
     */
    private String getPathToObject() {

        String fileName = null;
        String temp = System.getProperty("java.io.tmpdir");

        /* This mirrors the way OMC creates the object file. */
        switch (os) {
        case UNIX:
            String username = System.getenv("USER");
            if (username == null) {
                username = "nobody";
            }
            if (corbaSession == null || corbaSession.equalsIgnoreCase(""))
                fileName = temp + "/openmodelica." + username + ".objid";
            else
                fileName = temp + "/openmodelica." + username + ".objid" + "."
                        + corbaSession;
            break;
        case WINDOWS:
            if (corbaSession == null || corbaSession.equalsIgnoreCase(""))
                fileName = temp + "openmodelica.objid";
            else
                fileName = temp + "openmodelica.objid" + "." + corbaSession;
            break;
        }
        OpenModelicaDirector._ptLogger.getInfo("Will look for OMC object reference in '" + fileName
                + "'.");

        return fileName;
    }

    /**
     * Reads in the OMC CORBA object reference from a file on disk.
     * @return the object reference as a <code>String</code>
     */

    private String readObjectFromFile() throws ConnectException {
        String path = getPathToObject();
        File f = new File(path);
        String stringifiedObjectReference = null;

        BufferedReader br = null;

        try {
            FileReader fr = new FileReader(f);
            br = new BufferedReader(fr);
            stringifiedObjectReference = br.readLine();
        } catch (FileNotFoundException e) {
            throw new ConnectException(
                    "The OpenModelica Compiler CORBA object path '" + path
                            + "' does not exist!");
        } catch (IOException e) {
            throw new ConnectException(
                    "Unable to read OpenModelica Compiler CORBA object from '"
                            + path + "'");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                System.err
                        .println("Very weird error indeed, IOException when closing BufferedReader for file '"
                                + path + "'.");
            }
        }

        return stringifiedObjectReference;
    }

    /**
     * Initializes an ORB, converts the stringified OMC object to a real
     * CORBA object, and then narrows that object to an OmcCommunication
     * object. 
     */
    private synchronized void setupOmcc(String stringifiedObjectReference) {
        /* Can't remember why this is needed. But it is. */
        String args[] = { null };

        /* set the CORBA read timeout to a larger value as we send huge amounts of data
         * from OMC to MDT
         */
        System.setProperty("com.sun.CORBA.transport.ORBTCPReadTimeouts",
                "1:60000:300:1");

        ORB orb;
        orb = ORB.init(args, null);

        /* Convert string to CORBA object. */
        org.omg.CORBA.Object obj = orb
                .string_to_object(stringifiedObjectReference);

        /* Convert object to OmcCommunication object. */
        omcc = OmcCommunicationHelper.narrow(obj);
    }

    /**
     * Start a new OMC server.
     */
    private synchronized void startServer() throws ConnectException {

        if (!fOMCThreadHasBeenScheduled) {
            //FIXME reinit server
            /* create the OMC thread */
            if (fOMCThread == null) {
                fOMCThread = new OMCThread();
            }
            fOMCThread.start();
            fOMCThreadHasBeenScheduled = true;
            /* wait for 10 seconds */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            ;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                        private variables                       ////

    private String corbaSession = null;
    /** indicates if we've give up to run OMC as it didn't wanted to start! */
    private boolean couldNotStartOMC = false;
    private OMCThread fOMCThread = null;
    private boolean fOMCThreadHasBeenScheduled = false;
    /** number of compiler errors to show */
    private int numberOfErrors = 0;
    /** number of compiler errors to show */
    private int showMaxErrors = 10;

}
