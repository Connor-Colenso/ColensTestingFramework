package com.myname.mymodid.mixins.early;

import com.myname.mymodid.MyMod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.StartupQuery;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static net.minecraft.server.MinecraftServer.getSystemTimeMillis;

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
                long i = getSystemTimeMillis();
                long l = 0L;
                this.func_147138_a(this.field_147147_p);

                while (this.serverRunning) {
                    if (!MyMod.magicStopTime) {
                        continue;
                    }
                    long j = getSystemTimeMillis();
                    long k = j - i;

                    // Remove the artificial cap
                    if (k < 0L) {
                        k = 0L;
                    }

                    l += k;
                    i = j;

                    // Directly tick as fast as the system allows
                    while (l > 0L) {
                        this.tick();
                        l -= 1L; // Decrement by 1 or a small value to ensure it keeps ticking
                    }

                    this.serverIsRunning = true;
                }

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
