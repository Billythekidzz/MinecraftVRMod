package com.billythekidzz.VRMod;

import com.billythekidzz.VRMod.proxy.CommonProxy;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;

@Mod(modid = VRMod.MODID, name = VRMod.NAME, version = VRMod.VERSION)
public class VRMod
{
    private Socket socket = null;
    public static final String MODID = "vrmod";
    public static final String NAME = "VR Mod";
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide = "com.billythekidzz.VRMod.proxy.ClientProxy", serverSide = "com.billythekidzz.VRMod.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static VRMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLInitializationEvent event){
        try {
            socket = IO.socket("http://132.181.65.31:3000/");
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        MinecraftForge.EVENT_BUS.register(new VRModEventHandler());
        proxy.postInit(event);
    }

    public Socket getSocket(){
        return socket;
    }
}