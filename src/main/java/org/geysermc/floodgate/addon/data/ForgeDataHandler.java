package org.geysermc.floodgate.addon.data;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.mixin.ConnectionMixin;
import org.geysermc.floodgate.mixin.ClientIntentionPacketMixinInterface;
import org.geysermc.floodgate.api.logger.FloodgateLogger;
import org.geysermc.floodgate.config.FloodgateConfig;
import org.geysermc.floodgate.player.FloodgateHandshakeHandler;
import org.geysermc.floodgate.player.FloodgateHandshakeHandler.HandshakeResult;

import org.slf4j.Logger;

import java.net.InetSocketAddress;

public final class ForgeDataHandler extends CommonDataHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final FloodgateLogger logger;
    private Connection networkManager;
    private FloodgatePlayer player;

    public ForgeDataHandler(
            FloodgateHandshakeHandler handshakeHandler,
            FloodgateConfig config,
            AttributeKey<String> kickMessageAttribute, FloodgateLogger logger) {
        super(handshakeHandler, config, kickMessageAttribute, new PacketBlocker());
        this.logger = logger;
    }

    @Override
    protected void setNewIp(Channel channel, InetSocketAddress newIp) {
        ((ConnectionMixin) this.networkManager).setAddress(newIp);
    }

    @Override
    protected Object setHostname(Object handshakePacket, String hostname) {
        // While it would be ideal to simply create a new handshake packet, the packet constructor
        // does not allow us to set the protocol version
        ((ClientIntentionPacketMixinInterface) handshakePacket).setAddress(hostname);
        return handshakePacket;
    }

    @Override
    protected boolean shouldRemoveHandler(HandshakeResult result) {
        player = result.getFloodgatePlayer();

        if (getKickMessage() != null) {
            // we also have to keep this handler if we want to kick then with a disconnect message
            return false;
        } else if (player == null) {
            // player is not a Floodgate player
            return true;
        }

        if (result.getResultType() == FloodgateHandshakeHandler.ResultType.SUCCESS) {
            logger.info("Floodgate player who is logged in as {} {} joined",
                    player.getCorrectUsername(), player.getCorrectUniqueId());
        }

        // Handler will be removed after the login hello packet is handled
        return false;
    }

    @Override
    protected boolean channelRead(Object packet) {
        if (packet instanceof ClientIntentionPacket intentionPacket) {
            ctx.pipeline().addAfter("splitter", "floodgate_packet_blocker", blocker);
            networkManager = (Connection) ctx.channel().pipeline().get("packet_handler");
            handle(packet, intentionPacket.hostName());
            return false;
        }
        return !checkAndHandleLogin(packet);
    }

    private boolean checkAndHandleLogin(Object packet) {
        if (packet instanceof ServerboundHelloPacket) {
            String kickMessage = getKickMessage();
            if (kickMessage != null) {
                networkManager.disconnect(Component.nullToEmpty(kickMessage));
                return true;
            }

            // we have to fake the offline player (login) cycle
            if (!(networkManager.getPacketListener() instanceof ServerLoginPacketListenerImpl packetListener)) {
                // player is not in the login state, abort
                ctx.pipeline().remove(this);
                return true;
            }

            GameProfile gameProfile = new GameProfile(player.getCorrectUniqueId(), player.getCorrectUsername());

            // Forge-specific implementation for handleHello method
            packetListener.handleHello(new ServerboundHelloPacket(gameProfile.getName(),gameProfile.getId()));

            ctx.pipeline().remove(this);
            return true;
        }
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        if (config.isDebug()) {
            LOGGER.error("Exception caught in ForgeDataHandler", cause);
        }
    }
}
