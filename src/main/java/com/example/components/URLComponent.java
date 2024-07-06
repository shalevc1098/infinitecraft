package com.example.components;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class URLComponent implements ComponentType<String> {
    @Nullable
    @Override
    public Codec<String> getCodec() {
        return Codec.STRING;
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, String> getPacketCodec() {
        return new PacketCodec<PacketByteBuf, String>() {
            @Override
            public void encode(PacketByteBuf buf, String value) {
                buf.writeString(value);
            }

            @Override
            public String decode(PacketByteBuf buf) {
                return buf.readString();
            }
        };
    }
}