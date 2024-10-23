package com.myname.mymodid.mixins.early;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.StartupQuery;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow
    private boolean serverRunning;

    @Shadow
    private long timeOfLastWarning;

    @Shadow
    private boolean serverStopped;

    @Shadow
    private boolean serverIsRunning;

    @Shadow
    private ServerStatusResponse field_147147_p; // Add a field for the server status object

    @Shadow
    protected abstract boolean startServer();

    @Shadow
    protected abstract void stopServer();

    @Shadow
    protected abstract void finalTick(CrashReport report);

    @Shadow
    protected abstract void tick();

    @Shadow
    protected abstract File getDataDirectory(); // Add this shadow method

    @Shadow
    protected abstract void systemExitNow(); // Add this shadow method

    @Shadow
    protected abstract CrashReport addServerInfoToCrashReport(CrashReport report); // Shadow this method for crash reports

    @Shadow(remap = false)
    protected abstract void func_147138_a(ServerStatusResponse p); // Shadow the method for handling server status

    /**
     * @author me
     * @reason reasons
     */
    @Overwrite
    public void run() {
        try {
            if (this.startServer()) {
                FMLCommonHandler.instance().handleServerStarted();
                this.func_147138_a(this.field_147147_p);

                // ------------------------------------------
                // Simplified game loop for max speed.
                while (this.serverRunning)
                {
                    this.tick();
                    this.serverIsRunning = true;
                }
                // ------------------------------------------

                FMLCommonHandler.instance().handleServerStopping();
                FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            } else {
                FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
                this.finalTick(null);
            }
        } catch (StartupQuery.AbortedException e) {
            // ignore silently
            FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
        } catch (Throwable throwable1) {
            CrashReport crashreport;

            if (throwable1 instanceof ReportedException) {
                crashreport = this.addServerInfoToCrashReport(((ReportedException) throwable1).getCrashReport());
            } else {
                crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
            }

            File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.saveToFile(file1)) {
            } else {
            }

            FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            this.finalTick(crashreport);
        } finally {
            try {
                this.stopServer();
                this.serverStopped = true;
            } catch (Throwable throwable) {
            } finally {
                FMLCommonHandler.instance().handleServerStopped();
                this.serverStopped = true;
                this.systemExitNow();
            }
        }
    }
}
