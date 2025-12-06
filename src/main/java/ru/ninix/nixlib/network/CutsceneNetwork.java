package ru.ninix.nixlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import ru.ninix.nixlib.NixLib;
import ru.ninix.nixlib.client.cutscene.ClientCutsceneManager;
import ru.ninix.nixlib.cutscene.CutsceneManager;

public class CutsceneNetwork {


    public record PlayCutscenePacket(String json) implements CustomPacketPayload {
        public static final Type<PlayCutscenePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NixLib.MODID, "play_cutscene"));
        public static final StreamCodec<ByteBuf, PlayCutscenePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayCutscenePacket::json,
            PlayCutscenePacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AddKeyframePacket(int durationTicks, float fov, float roll) implements CustomPacketPayload {
        public static final Type<AddKeyframePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NixLib.MODID, "add_keyframe"));
        public static final StreamCodec<ByteBuf, AddKeyframePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AddKeyframePacket::durationTicks,
            ByteBufCodecs.FLOAT, AddKeyframePacket::fov,
            ByteBufCodecs.FLOAT, AddKeyframePacket::roll,
            AddKeyframePacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }


    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToClient(
            PlayCutscenePacket.TYPE,
            PlayCutscenePacket.CODEC,
            (packet, context) -> {
                context.enqueueWork(() -> ClientCutsceneManager.startPlayback(packet.json()));
            }
        );

        registrar.playToServer(
            AddKeyframePacket.TYPE,
            AddKeyframePacket.CODEC,
            (packet, context) -> {
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        CutsceneManager.addKeyframeToBuffer(player, packet.durationTicks(), packet.fov(), packet.roll());
                    }
                });
            }
        );
    }

    public static void sendToPlayer(ServerPlayer player, String cutsceneJson) {
        PacketDistributor.sendToPlayer(player, new PlayCutscenePacket(cutsceneJson));
    }

    public static void sendToServer(int duration, float fov, float roll) {
        PacketDistributor.sendToServer(new AddKeyframePacket(duration, fov, roll));
    }
}
