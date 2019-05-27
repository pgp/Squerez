package it.pgp.squerez.commands;

import com.aelitis.azureus.core.dht.DHT;
import com.aelitis.azureus.core.dht.control.DHTControlStats;
import com.aelitis.azureus.core.dht.db.DHTDBStats;
import com.aelitis.azureus.core.dht.netcoords.DHTNetworkPosition;
import com.aelitis.azureus.core.dht.router.DHTRouterStats;
import com.aelitis.azureus.core.dht.transport.DHTTransport;
import com.aelitis.azureus.core.dht.transport.DHTTransportStats;
import com.aelitis.azureus.core.networkmanager.admin.NetworkAdmin;
import com.aelitis.azureus.core.peermanager.piecepicker.PiecePicker;
import com.aelitis.azureus.core.stats.AzureusCoreStats;
import com.aelitis.azureus.core.tag.Tag;
import com.aelitis.azureus.core.tag.TagManagerFactory;
import com.aelitis.azureus.core.tag.TagType;
import com.aelitis.azureus.plugins.dht.DHTPlugin;

import org.gudy.azureus2.core3.category.Category;
import org.gudy.azureus2.core3.disk.DiskManagerFileInfo;
import org.gudy.azureus2.core3.download.DownloadManager;
import org.gudy.azureus2.core3.download.DownloadManagerStats;
import org.gudy.azureus2.core3.global.GlobalManager;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.peer.PEPeer;
import org.gudy.azureus2.core3.peer.PEPeerManager;
import org.gudy.azureus2.core3.peer.PEPeerManagerStats;
import org.gudy.azureus2.core3.peer.PEPeerStats;
import org.gudy.azureus2.core3.peer.PEPiece;
import org.gudy.azureus2.core3.tracker.client.TRTrackerAnnouncer;
import org.gudy.azureus2.core3.tracker.client.TRTrackerScraperResponse;
import org.gudy.azureus2.core3.util.AEDiagnostics;
import org.gudy.azureus2.core3.util.DisplayFormatters;
import org.gudy.azureus2.core3.util.IndentWriter;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.ui.console.ConsoleInput;
import org.gudy.azureus2.ui.console.commands.IConsoleCommand;
import org.gudy.azureus2.ui.console.commands.TorrentFilter;

import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import it.pgp.squerez.MainActivity;
import it.pgp.squerez.enums.TorrentState;
import it.pgp.squerez.responses.TorrentStatus;

/**
 * @author PGP
 * adapted from {@link org.gudy.azureus2.ui.console.commands.Show}
 */
public class UIShowCommand extends IConsoleCommand {




    public UIShowCommand()
    {
        super("show", "sh");
    }

    public String getCommandDescriptions() {
        return("show [<various options>]\tsh\tShow info. Use without parameter to get a list of available options.");
    }

    public void printHelpExtra(PrintStream out, List args) {
        out.println("> -----");
        out.println("'show' options: ");
        out.println("<#>\t\t\t\tFurther info on a single torrent - args from [peers|pieces]+. Run 'show torrents' first for the number.");
        out.println("options\t\t\to\tShow list of options for 'set' (also available by 'set' without parameters).");
        out.println("files\t\t\tf\tShow list of files found from the 'add -f' command (also available by 'add -l')");
        out.println("dht\t\t\td\tShow distributed database statistics");
        out.println("nat\t\t\tn\tShow NAT status");
        out.println("stats [pattern] [on|off]\ts\tShow stats [with a given pattern] [turn averages on/off]");
        out.println("torrents [opts] [expr]\tt\tShow list of torrents. torrent options may be any (or none) of:");
        out.println("\t\ttransferring\tx\tShow only transferring torrents.");
        out.println("\t\tactive\t\ta\tShow only active torrents.");
        out.println("\t\tcomplete\tc\tShow only complete torrents.");
        out.println("\t\tincomplete\ti\tShow only incomplete torrents.");
        out.println("\t\tdead [days]\td [days]Show only dead torrents (complete and not uploaded for [days] (default 7) uptime (NOT elapsed)).");
        out.println("\te.g. show t a *Az* - shows all active torrents with 'Az' occurring in their name." );
        out.println("> -----");
    }

    private static final int[] noTotalSwarmDataAvailable = {-1,-1};
    private int[] getTotalSwarmData(DownloadManager dm) {
        TRTrackerScraperResponse x = dm.getTrackerScrapeResponse();
        if(x != null && x.isValid())
            return new int[]{x.getSeeds(),x.getPeers()};
        else return noTotalSwarmDataAvailable;
    }

    public void execute(String commandName, ConsoleInput ci, List args) {
        if( args.isEmpty() )
        {
            printHelp(ci.out, args);
            return;
        }
        String subCommand = (String) args.remove(0);
        if (subCommand.equalsIgnoreCase("options") || subCommand.equalsIgnoreCase("o")) {
            ci.invokeCommand("set", null);
        } else if(subCommand.equalsIgnoreCase("files") || subCommand.equalsIgnoreCase("f")) {
            ci.invokeCommand("add", Arrays.asList("--list"));
        } else if (subCommand.equalsIgnoreCase("torrents") || subCommand.equalsIgnoreCase("t")) {

            ci.out.println("> -----");
            ci.torrents.clear();
            ci.torrents.addAll(ci.getGlobalManager().getDownloadManagers());
            Collections.sort(ci.torrents, new TorrentComparator());

            if (ci.torrents.isEmpty()) {
                ci.out.println("No Torrents");
                ci.out.println("> -----");
                return;
            }

            long totalReceived = 0;
            long totalSent = 0;
            long totalDiscarded = 0;
            int connectedSeeds = 0;
            int connectedPeers = 0;
            PEPeerManagerStats ps;
            boolean bShowOnlyActive = false;
            boolean bShowOnlyComplete = false;
            boolean bShowOnlyIncomplete = false;
            boolean bShowOnlyTransferring = false;
            int	bShowDeadForDays=0;

            for (ListIterator<String> iter = args.listIterator(); iter.hasNext();) {
                String arg = iter.next();
                if ("active".equalsIgnoreCase(arg) || "a".equalsIgnoreCase(arg)) {
                    bShowOnlyActive = true;
                    iter.remove();
                } else if ("complete".equalsIgnoreCase(arg) || "c".equalsIgnoreCase(arg)) {
                    bShowOnlyComplete = true;
                    iter.remove();
                } else if ("incomplete".equalsIgnoreCase(arg) || "i".equalsIgnoreCase(arg)) {
                    bShowOnlyIncomplete = true;
                    iter.remove();
                } else if ("transferring".equalsIgnoreCase(arg) || "x".equalsIgnoreCase(arg)) {
                    bShowOnlyTransferring = true;
                    bShowOnlyActive = true;
                    iter.remove();
                } else if ("dead".equalsIgnoreCase(arg) || "d".equalsIgnoreCase(arg)) {

                    iter.remove();

                    bShowDeadForDays = 7;	// default 1 week

                    if ( iter.hasNext()){

                        String days = iter.next();

                        try{
                            bShowDeadForDays = Integer.parseInt(days);

                            iter.remove();

                        }catch( Throwable e ){

                            iter.previous();
                        }
                    }
                }

            }

            Iterator torrent;
            if( args.size() > 0 )
            {
                List matchedTorrents = new TorrentFilter().getTorrents(ci.torrents, args);
                torrent = matchedTorrents.iterator();
            }
            else
                torrent = ci.torrents.iterator();

            List shown_torrents = new ArrayList();


            List<TorrentStatus> torrentStatuses = new ArrayList<>();
            int currentIdx = 1;
            while (torrent.hasNext()) {

                DownloadManager dm = (DownloadManager) torrent.next();

                DownloadManagerStats stats = dm.getStats();

                boolean bDownloadCompleted = stats.getDownloadCompleted(false) == 1000;
                boolean bCanShow = ((bShowOnlyComplete == bShowOnlyIncomplete) || (bDownloadCompleted && bShowOnlyComplete) || (!bDownloadCompleted && bShowOnlyIncomplete));

                if (bCanShow && bShowOnlyActive) {
                    int dmstate = dm.getState();
                    bCanShow = (dmstate == DownloadManager.STATE_SEEDING) || (dmstate == DownloadManager.STATE_DOWNLOADING) || (dmstate == DownloadManager.STATE_CHECKING) || (dmstate == DownloadManager.STATE_INITIALIZING) || (dmstate == DownloadManager.STATE_ALLOCATING);
                }

                if (bCanShow && bShowOnlyTransferring) {
                    try {
                        ps = dm.getPeerManager().getStats();
                        bCanShow = ps.getDataSendRate() > 0 || ps.getDataReceiveRate() > 0;
                    }
                    catch (Exception e) {}
                }

                if ( bCanShow && bShowDeadForDays > 0 ){

                    int dmstate = dm.getState();

                    bCanShow = false;

                    if ( 	dmstate == DownloadManager.STATE_SEEDING ||
                            ( bDownloadCompleted && ( dmstate == DownloadManager.STATE_QUEUED || dmstate == DownloadManager.STATE_STOPPED ))){

                        long seeding_secs = stats.getSecondsOnlySeeding();

                        long seeding_days = seeding_secs/(24*60*60);

                        if ( seeding_days >= bShowDeadForDays ){

                            int secs_since_last_up = stats.getTimeSinceLastDataSentInSeconds();

                            if ( secs_since_last_up == -1 ){

                                // never uploaded

                                bCanShow = true;

                            }else{

                                int days_since_last_up = secs_since_last_up/(24*60*60);

                                if ( days_since_last_up >= bShowDeadForDays ){

                                    bCanShow = true;
                                }
                            }
                        }
                    }
                }
                if (bCanShow) {

                    shown_torrents.add( dm );

                    DownloadManagerStats dmStats = dm.getStats();

                    try {
                        PEPeerManager pm = dm.getPeerManager();
                        ps = pm==null?null:pm.getStats();
                    } catch (Exception e) {
                        ps = null;
                    }
                    if (ps != null) {
                        totalReceived += dmStats.getTotalDataBytesReceived();
                        totalSent += dmStats.getTotalDataBytesSent();
                        totalDiscarded += ps.getTotalDiscarded();
                        connectedSeeds += dm.getNbSeeds();
                        connectedPeers += dm.getNbPeers();
                    }
                    ci.out.print(((shown_torrents.size() < 10) ? " " : "") + shown_torrents.size() + " ");
                    ci.out.println(getTorrentSummary(dm));
                    ci.out.println();

                    long completedBytes = dmStats.getDownloadCompletedBytes();
                    float completePercentage = dmStats.getDownloadCompleted(true)/10.0f;

                    int[] totalSwarmData = getTotalSwarmData(dm);

                    // BEGIN UI DATA FOR ADAPTER
                    TorrentStatus ts = new TorrentStatus(
                            currentIdx++,
                            dm.getTorrentFileName(),
                            dm.getAbsoluteSaveLocation().getAbsolutePath(),
                            completedBytes/1000000,
                            dm.getSize()/1000000,
                            completePercentage, // percentage in thousandths
                            dmStats.getDataReceiveRate(),
                            dmStats.getDataSendRate(),
                            totalSwarmData[0],
                            dm.getNbSeeds(),
                            totalSwarmData[1],
                            dm.getNbPeers(),
                            dmStats.getUploadRateLimitBytesPerSecond(),
                            dmStats.getDownloadRateLimitBytesPerSecond(),
                            TorrentState.fromVuzeState(dm.getState())
                    );
                    torrentStatuses.add(ts);
                    // END UI DATA FOR ADAPTER
                }
            }

            // update array adapter with TorrentStatus object just created
            if(MainActivity.mainActivity != null) MainActivity.mainActivity.runOnUiThread(()-> MainActivity.torrentAdapter.setNewContent(torrentStatuses));

            ci.torrents.clear();
            ci.torrents.addAll( shown_torrents );

            GlobalManager	gm = ci.getGlobalManager();

            ci.out.println("Total Speed (down/up): " + DisplayFormatters.formatByteCountToKiBEtcPerSec(gm.getStats().getDataReceiveRate() + gm.getStats().getProtocolReceiveRate() ) + " / " + DisplayFormatters.formatByteCountToKiBEtcPerSec(gm.getStats().getDataSendRate() + gm.getStats().getProtocolSendRate() ));

            ci.out.println("Transferred Volume (down/up/discarded): " + DisplayFormatters.formatByteCountToKiBEtc(totalReceived) + " / " + DisplayFormatters.formatByteCountToKiBEtc(totalSent) + " / " + DisplayFormatters.formatByteCountToKiBEtc(totalDiscarded));
            ci.out.println("Total Connected Peers (seeds/peers): " + connectedSeeds + " / " + connectedPeers);
            ci.out.println("> -----");
        } else if (subCommand.equalsIgnoreCase("dht") || subCommand.equalsIgnoreCase("d")) {

            showDHTStats( ci );

        } else if (subCommand.equalsIgnoreCase("nat") || subCommand.equalsIgnoreCase("n")) {

            IndentWriter	iw = new IndentWriter( new PrintWriter( ci.out ));

            iw.setForce( true );

            NetworkAdmin.getSingleton().logNATStatus( iw );

        } else if (subCommand.equalsIgnoreCase("stats") || subCommand.equalsIgnoreCase("s")) {

            String	pattern = AzureusCoreStats.ST_ALL;

            if( args.size() > 0 ){

                pattern = (String)args.get(0);

                if ( pattern.equals("*")){

                    pattern = ".*";
                }
            }

            if ( args.size() > 1 ){

                AzureusCoreStats.setEnableAverages(((String)args.get(1)).equalsIgnoreCase( "on" ));
            }

            java.util.Set	types = new HashSet();

            types.add( pattern );

            Map	reply = AzureusCoreStats.getStats( types );

            Iterator	it = reply.entrySet().iterator();

            List	lines = new ArrayList();

            while( it.hasNext()){

                Map.Entry	entry = (Map.Entry)it.next();

                lines.add( entry.getKey() + " -> " + entry.getValue());
            }

            Collections.sort( lines );

            for ( int i=0;i<lines.size();i++){

                ci.out.println( lines.get(i));
            }
        } else if (subCommand.equalsIgnoreCase("diag") || subCommand.equalsIgnoreCase("z")) {

            try{
                ci.out.println( "Writing diagnostics to file 'az.diag'" );

                FileWriter	fw = new FileWriter( "az.diag" );

                PrintWriter	pw = new PrintWriter( fw );

                AEDiagnostics.generateEvidence( pw );

                pw.flush();

                fw.close();

            }catch( Throwable e ){

                ci.out.println( e );
            }

        } else {
            if ((ci.torrents == null) || (ci.torrents != null) && ci.torrents.isEmpty()) {
                ci.out.println("> Command 'show': No torrents in list (try 'show torrents' first).");
                return;
            }
            try {
                int number = Integer.parseInt(subCommand);
                if ((number == 0) || (number > ci.torrents.size())) {
                    ci.out.println("> Command 'show': Torrent #" + number + " unknown.");
                    return;
                }
                DownloadManager dm = (DownloadManager) ci.torrents.get(number - 1);
                printTorrentDetails(ci.out, dm, number, args );
            }
            catch (Exception e) {
                ci.out.println("> Command 'show': Subcommand '" + subCommand + "' unknown.");
            }
        }
    }


    /**
     * prints out the full details of a particular torrent
     * @param out
     * @param dm
     * @param torrentNum
     */
    private static void printTorrentDetails( PrintStream out, DownloadManager dm, int torrentNum, List<String> args)
    {
        String name = dm.getDisplayName();
        if (name == null)
            name = "?";
        out.println("> -----");
        out.println("Info on Torrent #" + torrentNum + " (" + name + ")");
        out.println("- General Info -");
        String[] health = { "- no info -", "stopped", "no remote connections", "no tracker", "OK", "ko" };
        try {
            out.println("Health: " + health[dm.getHealthStatus()]);
        } catch (Exception e) {
            out.println("Health: " + health[0]);
        }
        out.println("State: " + dm.getState());
        if (dm.getState() == DownloadManager.STATE_ERROR)
            out.println("Error: " + dm.getErrorDetails());
        out.println("Hash: " + TorrentUtils.nicePrintTorrentHash(dm.getTorrent(), true));
        out.println("- Torrent file -");
        out.println("Torrent Filename: " + dm.getTorrentFileName());
        out.println("Saving to: " + dm.getSaveLocation());
        out.println("Created By: " + dm.getTorrentCreatedBy());
        out.println("Comment: " + dm.getTorrentComment());
        Category cat = dm.getDownloadState().getCategory();
        if (cat != null){
            out.println("Category: " + cat.getName());
        }
        List<Tag> tags = TagManagerFactory.getTagManager().getTagsForTaggable( TagType.TT_DOWNLOAD_MANUAL, dm );
        String tags_str;
        if ( tags.size() == 0 ){
            tags_str = "None";
        }else{
            tags_str = "";
            for ( Tag t: tags ){
                tags_str += (tags_str.length()==0?"":",") + t.getTagName(true);
            }
        }
        out.println("Tags: " + tags_str);
        out.println("- Tracker Info -");
        TRTrackerAnnouncer trackerclient = dm.getTrackerClient();
        if (trackerclient != null) {
            out.println("URL: " + trackerclient.getTrackerURL());
            String timestr;
            try {
                int time = trackerclient.getTimeUntilNextUpdate();
                if (time < 0) {
                    timestr = MessageText.getString("GeneralView.label.updatein.querying");
                } else {
                    int minutes = time / 60;
                    int seconds = time % 60;
                    String strSeconds = "" + seconds;
                    if (seconds < 10) {
                        strSeconds = "0" + seconds; //$NON-NLS-1$
                    }
                    timestr = minutes + ":" + strSeconds;
                }
            } catch (Exception e) {
                timestr = "unknown";
            }
            out.println("Time till next Update: " + timestr);
            out.println("Status: " + trackerclient.getStatusString());
        } else
            out.println("  Not available");

        out.println("- Files Info -");
        DiskManagerFileInfo[] files = dm.getDiskManagerFileInfo();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                out.print(((i < 9) ? "   " : "  ") + (i + 1)
                        + " (");
                String tmp = ">";
                if (files[i].getPriority()>0)
                    tmp = "+";
                if (files[i].isSkipped())
                    tmp = "!";
                out.print(tmp + ") ");
                if (files[i] != null) {
                    long fLen = files[i].getLength();
                    if (fLen > 0) {
                        DecimalFormat df = new DecimalFormat("000.0%");
                        out.print(df.format(files[i].getDownloaded() * 1.0
                                / fLen));
                        out.println("\t" + files[i].getFile(true).getName());
                    } else
                        out.println("Info not available.");
                } else
                    out.println("Info not available.");
            }
        } else{
            out.println("  Info not available.");
        }

        for ( String arg: args ){

            arg = arg.toLowerCase();

            if ( arg.startsWith( "pie" )){

                out.println( "Pieces" );

                PEPeerManager pm = dm.getPeerManager();

                if ( pm != null ){

                    PiecePicker picker = pm.getPiecePicker();

                    PEPiece[] pieces = pm.getPieces();

                    String	line = "";

                    for (int i=0;i<pieces.length;i++){

                        String str = picker.getPieceString( i );

                        line += (line.length()==0?(i + " "):",") + str;

                        PEPiece piece = pieces[i];

                        if ( piece != null ){

                            line += ":" + piece.getString();
                        }

                        if ( (i+1)%10 == 0 ){

                            out.println( line );

                            line = "";
                        }
                    }

                    if ( line.length() > 0 ){

                        out.println( line );
                    }
                }
            }else if ( arg.startsWith( "pee" )){

                out.println( "Peers" );

                PEPeerManager pm =dm.getPeerManager();

                if ( pm != null ){

                    List<PEPeer> peers = pm.getPeers();

                    out.println( "\tConnected to " + peers.size() + " peers" );

                    for ( PEPeer peer: peers ){

                        PEPeerStats stats = peer.getStats();

                        System.out.println( "\t\t" + peer.getIp() +
                                ", in=" + ( peer.isIncoming()?"Y":"N" ) +
                                ", prot=" + peer.getProtocol() +
                                ", choked=" + ( peer.isChokingMe()?"Y":"N" ) +
                                ", up=" + DisplayFormatters.formatByteCountToKiBEtcPerSec( stats.getDataSendRate() + stats.getProtocolSendRate()) +
                                ", down=" + DisplayFormatters.formatByteCountToKiBEtcPerSec( stats.getDataReceiveRate() + stats.getProtocolReceiveRate()) +
                                ", in_req=" + peer.getIncomingRequestCount() +
                                ", out_req=" + peer.getOutgoingRequestCount());
                    }
                }
            }
        }

        out.println("> -----");
    }

    protected void
    showDHTStats(
            ConsoleInput	ci )
    {
        try{
            PluginInterface	def = ci.azureus_core.getPluginManager().getDefaultPluginInterface();

            PluginInterface dht_pi =
                    def.getPluginManager().getPluginInterfaceByClass(DHTPlugin.class );

            if ( dht_pi == null ){

                ci.out.println( "\tDHT isn't present" );

                return;
            }

            DHTPlugin	dht_plugin = (DHTPlugin)dht_pi.getPlugin();

            if ( dht_plugin.getStatus() != DHTPlugin.STATUS_RUNNING ){

                ci.out.println( "\tDHT isn't running yet (disabled or initialising)" );

                return;
            }

            DHT[]	dhts = dht_plugin.getDHTs();

            for (int i=0;i<dhts.length;i++){

                if ( i > 0 ){
                    ci.out.println();
                }

                DHT	dht = dhts[i];

                DHTTransport transport = dht.getTransport();

                DHTTransportStats	t_stats = transport.getStats();
                DHTDBStats			d_stats	= dht.getDataBase().getStats();
                DHTControlStats		c_stats = dht.getControl().getStats();
                DHTRouterStats		r_stats = dht.getRouter().getStats();

                long[]	rs = r_stats.getStats();

                DHTNetworkPosition[]	nps = transport.getLocalContact().getNetworkPositions();

                String	np_str = "";

                for (int j=0;j<nps.length;j++){
                    np_str += (j==0?"":",") + nps[j];
                }

                ci.out.println( 	"DHT:ip=" + transport.getLocalContact().getAddress() +
                        ",net=" + transport.getNetwork() +
                        ",prot=V" + transport.getProtocolVersion() +
                        ",np=" + np_str +
                        ",sleeping=" + dht.isSleeping());

                ci.out.println(
                        "Router" +
                                ":nodes=" + rs[DHTRouterStats.ST_NODES] +
                                ",leaves=" + rs[DHTRouterStats.ST_LEAVES] +
                                ",contacts=" + rs[DHTRouterStats.ST_CONTACTS] +
                                ",replacement=" + rs[DHTRouterStats.ST_REPLACEMENTS] +
                                ",live=" + rs[DHTRouterStats.ST_CONTACTS_LIVE] +
                                ",unknown=" + rs[DHTRouterStats.ST_CONTACTS_UNKNOWN] +
                                ",failing=" + rs[DHTRouterStats.ST_CONTACTS_DEAD]);

                ci.out.println(
                        "Transport" +
                                ":" + t_stats.getString());

                int[]	dbv_details = d_stats.getValueDetails();

                ci.out.println(
                        "Control:dht=" + c_stats.getEstimatedDHTSize() +
                                ", Database:keys=" + d_stats.getKeyCount() +
                                ",vals=" + dbv_details[DHTDBStats.VD_VALUE_COUNT]+
                                ",loc=" + dbv_details[DHTDBStats.VD_LOCAL_SIZE]+
                                ",dir=" + dbv_details[DHTDBStats.VD_DIRECT_SIZE]+
                                ",ind=" + dbv_details[DHTDBStats.VD_INDIRECT_SIZE]+
                                ",div_f=" + dbv_details[DHTDBStats.VD_DIV_FREQ]+
                                ",div_s=" + dbv_details[DHTDBStats.VD_DIV_SIZE] );

                dht.getRouter().print();
            }

        }catch( Throwable e ){

            e.printStackTrace( ci.out );
        }
    }
}
