package com.etb.client.module.modules.movement;

import java.awt.Color;

import com.etb.client.utils.value.impl.NumberValue;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;

import com.etb.client.event.events.player.MotionEvent;
import com.etb.client.event.events.player.UpdateEvent;
import com.etb.client.module.Module;
import com.etb.client.utils.value.impl.EnumValue;

import net.minecraft.potion.Potion;

public class Speed extends Module {
    private int stage = 1, stageOG = 1;
    private double moveSpeed, lastDist, moveSpeedOG, lastDistOG;
    private EnumValue<Mode> mode = new EnumValue("Mode", Mode.HYPIXEL);


    public Speed() {
        super("Speed", Category.MOVEMENT, new Color(0, 255, 0, 255).getRGB());
        addValues(mode);
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null) return;
      //  mc.thePlayer.motionX = 0;
       // mc.thePlayer.motionZ = 0;
    }


    @Subscribe
    public void onUpdate(UpdateEvent event) {
        setSuffix(StringUtils.capitalize(mode.getValue().name().toLowerCase()));
        final boolean tick = mc.thePlayer.ticksExisted % 2 == 0;
        if (mode.getValue() == Mode.HYPIXEL) {
            lastDist = Math.sqrt(((mc.thePlayer.posX - mc.thePlayer.prevPosX) * (mc.thePlayer.posX - mc.thePlayer.prevPosX)) + ((mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (mc.thePlayer.posZ - mc.thePlayer.prevPosZ)));
        }
    }

    @Subscribe
    public void onMotion(MotionEvent event) {
        double forward = mc.thePlayer.movementInput.moveForward, strafe = mc.thePlayer.movementInput.moveStrafe, yaw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.isOnLiquid() || mc.thePlayer.isInLiquid()) return;
        switch (mode.getValue()) {
	        case HYPIXEL:
	        	switch (stage) {
	                case 0:
	                	mc.timer.timerSpeed = 1.05f;
	                    ++stage;
	                    lastDist = 0.0D;
	                    break;
	                case 2:
	                    double motionY = 0.4198;
	                    if ((mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F) && mc.thePlayer.onGround) {
	                        if (mc.thePlayer.isPotionActive(Potion.jump))
	                            motionY += ((mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
	                        event.setY(mc.thePlayer.motionY = motionY);
	                        moveSpeed *= 2.049;
	                    }
	                    break;
	                case 3:
	                    moveSpeed = lastDist - 0.7095 * (lastDist - getBaseMoveSpeed());
	                    break;
	                default:
	                    if ((mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, mc.thePlayer.motionY, 0.0D)).size() > 0 || mc.thePlayer.isCollidedVertically) && stage > 0) {
	                        stage = mc.thePlayer.moveForward == 0.0F && mc.thePlayer.moveStrafing == 0.0F ? 0 : 1;
	                    }
	                    moveSpeed = lastDist - lastDist / 159.0D;
	                    break;
            }
            moveSpeed = Math.max(moveSpeed, getBaseMoveSpeed());
            if (forward == 0.0F && strafe == 0.0F) {
            //    event.setX(0);
              //  event.setZ(0);
            }
            if (forward != 0 && strafe != 0) {
                forward = forward * Math.sin(Math.PI / 4);
                strafe = strafe * Math.cos(Math.PI / 4);
            }
           // event.setX((forward * moveSpeed * -Math.sin(Math.toRadians(yaw)) + strafe * moveSpeed * Math.cos(Math.toRadians(yaw))) * 0.8D);
           // event.setZ((forward * moveSpeed * Math.cos(Math.toRadians(yaw)) - strafe * moveSpeed * -Math.sin(Math.toRadians(yaw))) * 0.8D);
            ++stage;
            break;
        }
    }

    private void setMoveSpeed(final MotionEvent event, final double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            event.setX(forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw)));
            event.setZ(forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw)));
        }
    }

    private double getBaseMoveSpeed() {
        double baseSpeed = 0.272;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            final int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + (0.2 * amplifier);
        }
        return baseSpeed;
    }

    public enum Mode {
        HYPIXEL
    }
}
