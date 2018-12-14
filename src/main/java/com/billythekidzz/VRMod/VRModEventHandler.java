package com.billythekidzz.VRMod;

import CoroUtil.forge.CoroUtil;
import CoroUtil.util.CoroUtilAbility;
import CoroUtil.util.CoroUtilPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import weather2.Weather;
import weather2.api.WeatherDataHelper;
import weather2.api.WeatherUtilData;
import weather2.api.WindDataHelper;
import weather2.util.WeatherUtil;
import weather2.util.WindReader;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;

import java.util.ArrayList;

import static com.billythekidzz.VRMod.VRMod.instance;

public class VRModEventHandler {
    private enum Directions{
        SOUTH,
        NORTH,
        EAST,
        WEST;
    }
    private int tickCount = 0;
    private int windTickCount = 0;
    private Integer previousXPos;
    private Integer previousYPos;
    private Integer previousZPos;
    private String south = "11,15";
    private String east = "1,4,5";
    private String west = "9,12,13";
    private String north = "3,6,7";
    private int moveZTicks = 0;
    private int moveXTicks = 0;
    private int moveTicksNum = 10;
    private int speedValue = 0;
    private int footstepTick = 0;
    private int footstepTickValue = 20;
    private int walkSpeedValue = 100;
    private int runSpeedValue = 200;
    private int maxSpeed = 255;
    private float windSpeed;
    private BlockPos playerLocation;
    private World playerWorld;
    private ArrayList<String> direction = new ArrayList<>();
    private ArrayList<String> windDirection = new ArrayList<>();
    private ArrayList<Boolean> directionSprinting = new ArrayList<Boolean>(){{
        add(false);
        add(false);
        add(false);
        add(false);
    }};

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event){
        if(event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            player.setHealth(10);
        }
    }

    @SubscribeEvent
    public void playerUseItem(LivingEntityUseItemEvent.Finish event){
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (event.getItem() != null) {
                if (event.getItem().getItem() == Items.APPLE) {
                    VRMod.instance.getSocket().emit("scentCommand", "activate : scent = apple; duration = 10");
                }
                if (event.getItem().getItem() == Items.ROTTEN_FLESH) {
                    VRMod.instance.getSocket().emit("scentCommand", "activate : scent = apple; duration = 10");
                }
                if (event.getItem().getItem() == Items.COOKED_PORKCHOP) {
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15; speedvalue= 255");
                }
                if (event.getItem().getItem() == Items.COOKED_BEEF) {
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = all; speedvalue= 0");
                }
            }
        }
    }
    @SubscribeEvent
    public void explode(ExplosionEvent.Start event){
        if(!event.isCanceled()){
            VRMod.instance.getSocket().emit("floorCommand", "/home/hitldemo/Documents/TactaCage_MultiDirectionalFeedback/TestingProject/explosion.wav");
            tickCount = 150;
            //windTickCount = 200;
        }
    }

    @SubscribeEvent
    public void damage(LivingFallEvent event){
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();

        }
    }

    @SubscribeEvent
    public void livingUpdate(LivingEvent.LivingUpdateEvent event){
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if (player.getEntityWorld().isRemote) {
                playerLocation = player.getPosition();
                playerWorld = player.getEntityWorld();
                if (previousZPos == null) {
                    previousXPos = player.getPosition().getX();
                    previousYPos = player.getPosition().getY();
                    previousZPos = player.getPosition().getZ();
                } else {
                    if (previousZPos < player.getPosition().getZ()) {
                        //player.sendMessage(new TextComponentString("current z: " + player.getPosition().getZ()));
                        //player.sendMessage(new TextComponentString("previous z: " + previousZPos));
                        moveZTicks = moveTicksNum;
                        if (!direction.contains("south")) {
                            /*
                            if(direction.isEmpty()){
                                VRMod.instance.getSocket().emit("floorCommand", "/home/hitldemo/Documents/TactaCage_MultiDirectionalFeedback/TestingProject/footsteps-1.wav");
                            }
                            */
                            if(direction.contains("north")){
                                direction.remove("north");
                            }
                            direction.add("south");
                            //player.sendMessage(new TextComponentString(direction.toString()));
                            if(player.isSprinting()){
                                speedValue = runSpeedValue;
                                directionSprinting.set(Directions.SOUTH.ordinal(), true);
                            }
                            else{
                                directionSprinting.set(Directions.SOUTH.ordinal(), false);
                                speedValue = walkSpeedValue;
                            }
                            updateWind();
                            //VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= " + speedValue);
                        }
                    }
                    if (previousZPos > player.getPosition().getZ()) {
                        moveZTicks = moveTicksNum;
                        if (!direction.contains("north")) {
                            /*
                            if(direction.isEmpty()){
                                VRMod.instance.getSocket().emit("floorCommand", "/home/hitldemo/Documents/TactaCage_MultiDirectionalFeedback/TestingProject/footsteps-1.wav");
                            }
                            */
                            if(direction.contains("south")){
                                direction.remove("south");
                            }
                            direction.add("north");
                            //player.sendMessage(new TextComponentString(direction.toString()));
                            if(player.isSprinting()){
                                directionSprinting.set(Directions.NORTH.ordinal(), true);
                                speedValue = runSpeedValue;
                            }
                            else{
                                speedValue = walkSpeedValue;
                                directionSprinting.set(Directions.NORTH.ordinal(), false);
                            }
                            updateWind();
                            //VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= " + speedValue);
                        }
                    }
                    if (previousXPos < player.getPosition().getX()) {
                        moveXTicks = moveTicksNum;
                        if (!direction.contains("east")) {
                            /*
                            if(direction.isEmpty()){
                                VRMod.instance.getSocket().emit("floorCommand", "/home/hitldemo/Documents/TactaCage_MultiDirectionalFeedback/TestingProject/footsteps-1.wav");
                            }
                            */
                            if(direction.contains("west")){
                                direction.remove("west");
                            }
                            direction.add("east");
                            //player.sendMessage(new TextComponentString(direction.toString()));
                            if(player.isSprinting()){
                                speedValue = runSpeedValue;
                                directionSprinting.set(Directions.EAST.ordinal(), true);
                            }
                            else{
                                speedValue = walkSpeedValue;
                                directionSprinting.set(Directions.EAST.ordinal(), false);
                            }
                            updateWind();
                            //VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= " + speedValue);
                        }
                    }
                    if (previousXPos > player.getPosition().getX()) {
                        moveXTicks = moveTicksNum;
                        if (!direction.contains("west")) {
                            /*
                            if(direction.isEmpty()){
                                VRMod.instance.getSocket().emit("floorCommand", "/home/hitldemo/Documents/TactaCage_MultiDirectionalFeedback/TestingProject/footsteps-1.wav");
                            }
                            */
                            if(direction.contains("east")){
                                direction.remove("east");
                            }
                            direction.add("west");
                            //player.sendMessage(new TextComponentString(direction.toString()));
                            if(player.isSprinting()){
                                speedValue = runSpeedValue;
                                directionSprinting.set(Directions.WEST.ordinal(), true);
                            }
                            else{
                                speedValue = walkSpeedValue;
                                directionSprinting.set(Directions.WEST.ordinal(), false);
                            }
                            updateWind();
                            //VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= " + speedValue);
                        }
                    }
                    if(moveXTicks == 0) {
                        if (previousXPos == player.getPosition().getX()) {
                            if (direction.contains("west")) {
                                direction.remove("west");
                                updateWind();
                                /*
                                if(direction.isEmpty()){
                                    VRMod.instance.getSocket().emit("floorCommand", "stop");
                                }
                                */
                                //VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= 0");
                                //player.sendMessage(new TextComponentString(direction.toString()));
                            }
                            if (direction.contains("east")) {
                                direction.remove("east");
                                updateWind();
                                /*
                                if(direction.isEmpty()){
                                    VRMod.instance.getSocket().emit("floorCommand", "stop");
                                }
                                */
                                //VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= 0");
                                //player.sendMessage(new TextComponentString(direction.toString()));
                            }
                        }
                    }
                    if(moveZTicks == 0) {
                        if (previousZPos == player.getPosition().getZ()) {
                            if (direction.contains("north")) {
                                direction.remove("north");
                                updateWind();
                                /*
                                if(direction.isEmpty()){
                                    VRMod.instance.getSocket().emit("floorCommand", "stop");
                                }
                                */
                                //VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= 0");
                            }
                            if (direction.contains("south")) {
                                direction.remove("south");
                                updateWind();
                                /*
                                if(direction.isEmpty()){
                                    VRMod.instance.getSocket().emit("floorCommand", "stop");
                                }
                                */
                                //VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= 0");
                            }
                        }
                    }
                    previousXPos = player.getPosition().getX();
                    previousYPos = player.getPosition().getY();
                    previousZPos = player.getPosition().getZ();
                }
            }
        }
    }
    @SubscribeEvent
    public void explode(ExplosionEvent.Detonate event){

    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event){
        if(tickCount > 0){
            tickCount -= 1;
            if(tickCount == 0){
                VRMod.instance.getSocket().emit("floorCommand", "stop");
            }
        }
        if(moveXTicks > 0){
            moveXTicks -= 1;
        }
        if(moveZTicks > 0){
            moveZTicks -= 1;
        }
        /*
        if(footstepTick > 0){
            footstepTick -= 1;
            if(footstepTick == 0){
                VRMod.instance.getSocket().emit("floorCommand", "stop");
                footstepTick = 0;
            }
        }
        */
    }

    public void updateWind(){
        /*
        Minecraft.getMinecraft().player.sendChatMessage("Wind speed = " + windSpeed);
        if((Float) WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.EVENT) != null) {
            float windSpeed2 = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.EVENT) * maxSpeed;
            Minecraft.getMinecraft().player.sendChatMessage("Event Wind speed = " + windSpeed2);
        }
        if((Float) WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) != null) {
            float windSpeed3 = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
            Minecraft.getMinecraft().player.sendChatMessage("Priority Wind speed = " + windSpeed3);
        }
        if((Float) WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.GUST) != null) {
            float windSpeed4 = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.GUST) * maxSpeed;
            Minecraft.getMinecraft().player.sendChatMessage("Gust Wind speed = " + windSpeed4);
        }
        Minecraft.getMinecraft().player.sendChatMessage("Wind speed values = " + WindReader.WindType.values().toString());
        Minecraft.getMinecraft().player.sendChatMessage("Wind speed values = " + WindDataHelper.WindType.values().toString());
        */
        if(!direction.contains("north")){
            if(!windDirection.contains("north")) {
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= 0");
            }
            else{
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= " + windSpeed);
            }
        }
        else{
            if(directionSprinting.get(Directions.NORTH.ordinal()) == true || (windDirection.contains("south") && windSpeed > 50)) {
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= " + runSpeedValue);
            }
            else{
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= " + walkSpeedValue);
            }
        }
        if(!direction.contains("south")){
            if(!windDirection.contains("south")) {
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= 0");
            }
            else{
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= " + windSpeed);
            }
        }
        else{
            if(directionSprinting.get(Directions.SOUTH.ordinal()) == true || (windDirection.contains("north") && windSpeed > 50)) {
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= " + runSpeedValue);
            }
            else{
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= " + walkSpeedValue);
            }
        }
        if(!direction.contains("east")){
            if(!windDirection.contains("east")) {
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= 0");
            }
            else{
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= " + windSpeed);
            }
        }
        else{
            if(directionSprinting.get(Directions.EAST.ordinal()) == true || (windDirection.contains("west") && windSpeed > 50)) {
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= " + runSpeedValue);
            }
            else{
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= " + walkSpeedValue);
            }
        }
        if(!direction.contains("west")){
            if(!windDirection.contains("west")) {
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= 0");
            }
            else{
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= " + windSpeed);
            }
        }
        else{
            if(directionSprinting.get(Directions.WEST.ordinal()) == true || (windDirection.contains("east") && windSpeed > 50)) {
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= " + runSpeedValue);
            }
            else{
                VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= " + walkSpeedValue);
            }
        }
        //EntityPlayerSP player = Minecraft.getMinecraft().player;
        float windAngle = WindDataHelper.getWindAngle(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY);
        //Minecraft.getMinecraft().player.sendChatMessage("Wind angle = " + windAngle);
        //Minecraft.getMinecraft().player.sendChatMessage("Wind direction = " + windDirection);
        //Minecraft.getMinecraft().player.sendChatMessage("Footsteptick = " + footstepTick);
        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
        if(windSpeed > 255){
            windSpeed = 255;
        }
        if (windAngle > 337.5 || windAngle <= 22.5){
                if (!windDirection.contains("north")) {
                    windDirection.clear();
                    windDirection.add("north");
                    if(WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed > 50) {
                        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
                        if(windSpeed > 255){
                            windSpeed = 255;
                        }
                    }
                    else{
                        windSpeed = 0;
                    }
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= " + windSpeed);
            }
        }
        else if (windAngle > 22.5 && windAngle <= 67.5){
                if (!windDirection.contains("north") && !windDirection.contains("east")) {
                    windDirection.clear();
                    windDirection.add("north");
                    windDirection.add("east");
                    if(WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed > 50) {
                        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
                        if(windSpeed > 255){
                            windSpeed = 255;
                        }
                    }
                    else{
                        windSpeed = 0;
                    }
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= " + windSpeed);
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= " + windSpeed);
            }
        }
        else if (windAngle > 67.5 && windAngle <= 112.5){
                if (!windDirection.contains("east")) {
                    windDirection.clear();
                    windDirection.add("east");
                    if(WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed > 50) {
                        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
                        if(windSpeed > 255){
                            windSpeed = 255;
                        }
                    }
                    else{
                        windSpeed = 0;
                    }
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= " + windSpeed);
            }
        }
        else if (windAngle > 112.5 && windAngle <= 157.5){
                if (!windDirection.contains("south") && !windDirection.contains("east")) {
                    windDirection.clear();
                    windDirection.add("south");
                    windDirection.add("east");
                    if(WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed > 50) {
                        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
                        if(windSpeed > 255){
                            windSpeed = 255;
                        }
                    }
                    else{
                        windSpeed = 0;
                    }
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + east + "; speedvalue= " + windSpeed);
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= " + windSpeed);
                }
        }
        else if (windAngle > 157.5 && windAngle <= 202.5){
                if (!windDirection.contains("south")) {
                    windDirection.clear();
                    windDirection.add("south");
                    if(WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed > 50) {
                        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
                        if(windSpeed > 255){
                            windSpeed = 255;
                        }
                    }
                    else{
                        windSpeed = 0;
                    }
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= " + windSpeed);
                }
        }
        else if (windAngle > 202.5 && windAngle <= 247.5){
                if (!windDirection.contains("south") && !windDirection.contains("west")) {
                    windDirection.clear();
                    windDirection.add("south");
                    windDirection.add("west");
                    if(WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed > 50) {
                        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
                        if(windSpeed > 255){
                            windSpeed = 255;
                        }
                    }
                    else{
                        windSpeed = 0;
                    }
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= " + windSpeed);
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + south + "; speedvalue= " + windSpeed);
                }
        }
        else if (windAngle > 247.5 && windAngle <= 292.5){
                if (!windDirection.contains("west")) {
                    windDirection.clear();
                    windDirection.add("west");
                    if(WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed > 50) {
                        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
                        if(windSpeed > 255){
                            windSpeed = 255;
                        }
                    }
                    else{
                        windSpeed = 0;
                    }
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= " + windSpeed);
                }
        }
        else if (windAngle > 292.5 && windAngle <= 337.5){
                if (!windDirection.contains("north") && !windDirection.contains("west")) {
                    windDirection.clear();
                    windDirection.add("north");
                    windDirection.add("west");
                    if(WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed > 50) {
                        windSpeed = WindDataHelper.getWindSpeed(playerWorld, playerLocation, WindDataHelper.WindType.PRIORITY) * maxSpeed;
                        if(windSpeed > 255){
                            windSpeed = 255;
                        }
                    }
                    else{
                        windSpeed = 0;
                    }
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + west + "; speedvalue= " + windSpeed);
                    VRMod.instance.getSocket().emit("windCommand", "setfan: id = " + north + "; speedvalue= " + windSpeed);
                }
        }
    }
}
