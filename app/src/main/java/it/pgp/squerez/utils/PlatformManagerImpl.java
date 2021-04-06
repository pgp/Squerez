package it.pgp.squerez.utils;

import android.os.Environment;

import com.aelitis.azureus.core.AzureusCore;

import org.gudy.azureus2.core3.logging.LogEvent;
import org.gudy.azureus2.core3.logging.LogIDs;
import org.gudy.azureus2.core3.logging.Logger;
import org.gudy.azureus2.core3.util.AEMonitor;
import org.gudy.azureus2.platform.PlatformManager;
import org.gudy.azureus2.platform.PlatformManagerCapabilities;
import org.gudy.azureus2.platform.PlatformManagerListener;
import org.gudy.azureus2.platform.PlatformManagerPingCallback;
import org.gudy.azureus2.plugins.platform.PlatformManagerException;

import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;

import it.pgp.misc.ExitHandler;

/**
 * @author PGP
 * @created Mar 5, 2019
 */
public class PlatformManagerImpl implements PlatformManager
{
    private static final String ERR_UNSUPPORTED = "Unsupported capability called on platform manager";

    protected static PlatformManagerImpl singleton;

    protected static AEMonitor class_mon = new AEMonitor("PlatformManager");

    private final HashSet capabilitySet = new HashSet();


    public static final String BASE_RELPATH = "Squerez";
    public static final String DOWNLOADS_RELPATH = BASE_RELPATH+"/Downloads";
    public static final String USERDATA_RELPATH = BASE_RELPATH+"/userdata";
    public static final String TORRENTS_RELPATH = BASE_RELPATH+"/userdata/torrents";
    public static final String TMP_RELPATH = BASE_RELPATH+"/tmp";

    /**
     * Gets the platform manager singleton, which was already initialized
     */
    public static PlatformManagerImpl getSingleton() {
        return singleton;
    }

    static {
        initializeSingleton();
        ExitHandler.defaultHandler = ExitHandler.stdHandler; // force VM close on Android in order to terminate daemon threads
    }

    /**
     * Instantiates the singleton
     */
    private static void initializeSingleton() {
        try {
            class_mon.enter();
            singleton = new PlatformManagerImpl();
        } catch (Throwable e) {
            Logger.log(new LogEvent(LogIDs.CORE, "Failed to initialize platform manager"
                    + " for Unix Compatable OS", e));
        } finally {
            class_mon.exit();
        }
    }

    /**
     * Creates a new PlatformManager and initializes its capabilities
     */
    public PlatformManagerImpl() {
        capabilitySet.add(PlatformManagerCapabilities.GetUserDataDirectory);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#copyFilePermissions(java.lang.String, java.lang.String)
    public void copyFilePermissions(String from_file_name, String to_file_name)
            throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#createProcess(java.lang.String, boolean)
    public void createProcess(String command_line, boolean inherit_handles)
            throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#dispose()
    public void dispose() {
    }

    // @see org.gudy.azureus2.platform.PlatformManager#getApplicationCommandLine()
    public String getApplicationCommandLine() throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#getPlatformType()
    public int getPlatformType() {
        return PT_ANDROID;
    }

    // @see org.gudy.azureus2.platform.PlatformManager#getUserDataDirectory()
    public String getUserDataDirectory() throws PlatformManagerException {
        File dir = new File(Environment.getExternalStorageDirectory(),USERDATA_RELPATH); // requires runtime permissions
        if (!dir.exists()) {
            if(!dir.mkdirs()) throw new RuntimeException("Creating user folder requires storage permissions");
        }
        return dir.getAbsolutePath();
    }

    public String
    getComputerName()
    {
        String	host = System.getenv( "HOST" );

        if ( host != null && host.length() > 0 ){

            return( host );
        }

        return( null );
    }

    // @see org.gudy.azureus2.platform.PlatformManager#getVersion()
    public String getVersion() throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#hasCapability(org.gudy.azureus2.platform.PlatformManagerCapabilities)
    public boolean hasCapability(PlatformManagerCapabilities capability) {
        return capabilitySet.contains(capability);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#isApplicationRegistered()
    public boolean isApplicationRegistered() throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#performRecoverableFileDelete(java.lang.String)
    public void performRecoverableFileDelete(String file_name)
            throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#ping(java.net.InetAddress, java.net.InetAddress, org.gudy.azureus2.platform.PlatformManagerPingCallback)
    public void ping(InetAddress interface_address, InetAddress target,
                     PlatformManagerPingCallback callback) throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    public int
    getMaxOpenFiles()

            throws PlatformManagerException
    {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#registerApplication()
    public void registerApplication() throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#addListener(org.gudy.azureus2.platform.PlatformManagerListener)
    public void addListener(PlatformManagerListener listener) {
        // No Listener Functionality
    }

    // @see org.gudy.azureus2.platform.PlatformManager#removeListener(org.gudy.azureus2.platform.PlatformManagerListener)
    public void removeListener(PlatformManagerListener listener) {
        // No Listener Functionality
    }

    public File
    getVMOptionFile()

            throws PlatformManagerException
    {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    public String[]
    getExplicitVMOptions()

            throws PlatformManagerException
    {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    public void
    setExplicitVMOptions(
            String[]		options )

            throws PlatformManagerException
    {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    public boolean
    getRunAtLogin()

            throws PlatformManagerException
    {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    public void
    setRunAtLogin(
            boolean run )

            throws PlatformManagerException
    {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    public void
    startup(
            AzureusCore		azureus_core )

            throws PlatformManagerException
    {
    }

    public int
    getShutdownTypes()
    {
        return( 0 );
    }

    public void
    shutdown(
            int			type )

            throws PlatformManagerException
    {
        throw new PlatformManagerException("Unsupported capability called on platform manager");
    }

    public void
    setPreventComputerSleep(
            boolean		b )

            throws PlatformManagerException
    {
        throw new PlatformManagerException("Unsupported capability called on platform manager");
    }

    public boolean
    getPreventComputerSleep()
    {
        return( false );
    }

    // @see org.gudy.azureus2.platform.PlatformManager#setTCPTOSEnabled(boolean)
    public void setTCPTOSEnabled(boolean enabled) throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#testNativeAvailability(java.lang.String)
    public boolean testNativeAvailability(String name)
            throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#traceRoute(java.net.InetAddress, java.net.InetAddress, org.gudy.azureus2.platform.PlatformManagerPingCallback)
    public void traceRoute(InetAddress interface_address, InetAddress target,
                           PlatformManagerPingCallback callback) throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    private File getAccessibleDocLocation() {
        File dir = new File(Environment.getExternalStorageDirectory(),BASE_RELPATH);
        if(!dir.exists()) {
            if(!dir.mkdirs()) throw new RuntimeException("Storage permissions required");
        }
        return dir;
    }

    // @see org.gudy.azureus2.plugins.platform.PlatformManager#getLocation(long)
    public File getLocation(long location_id) throws PlatformManagerException {
        switch ((int)location_id) {
            case LOC_USER_DATA:
                return( new File( getUserDataDirectory() ));

            case LOC_DOCUMENTS:
                return getAccessibleDocLocation();

            case LOC_MUSIC:

            case LOC_VIDEO:

            default:
                return( null );
        }
    }

    // @see org.gudy.azureus2.plugins.platform.PlatformManager#isAdditionalFileTypeRegistered(java.lang.String, java.lang.String)
    public boolean isAdditionalFileTypeRegistered(String name, String type)
            throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.plugins.platform.PlatformManager#registerAdditionalFileType(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    public void registerAdditionalFileType(String name, String description,
                                           String type, String content_type) throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.plugins.platform.PlatformManager#showFile(java.lang.String)
    public void showFile(String file_name) throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.plugins.platform.PlatformManager#unregisterAdditionalFileType(java.lang.String, java.lang.String)
    public void unregisterAdditionalFileType(String name, String type)
            throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    // @see org.gudy.azureus2.platform.PlatformManager#getAzComputerID()
    public String getAzComputerID() throws PlatformManagerException {
        throw new PlatformManagerException(ERR_UNSUPPORTED);
    }

    public void requestUserAttention(int type, Object data) throws PlatformManagerException {
        throw new PlatformManagerException("Unsupported capability called on platform manager");
    }

    public Class<?>
    loadClass(
            ClassLoader	loader,
            String		class_name )

            throws PlatformManagerException
    {
        try{
            return( loader.loadClass( class_name ));

        }catch( Throwable e ){

            throw( new PlatformManagerException( "load of '" + class_name + "' failed", e ));
        }
    }
}
