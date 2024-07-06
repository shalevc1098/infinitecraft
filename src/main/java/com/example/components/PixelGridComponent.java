package com.example.components;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PixelGridComponent implements ComponentType<int[]> {
    @Nullable
    @Override
    public Codec<int[]> getCodec() {
        return Codec.INT.listOf().xmap(ints -> ints.stream().mapToInt(Integer::intValue).toArray(), ints -> Arrays.stream(ints).boxed().collect(Collectors.toList()));
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, int[]> getPacketCodec() {
        return new PacketCodec<PacketByteBuf, int[]>() {
            @Override
            public void encode(PacketByteBuf buf, int[] value) {
                buf.writeIntArray(value);
            }

            @Override
            public int[] decode(PacketByteBuf buf) {
                return buf.readIntArray();
            }
        };
    }
}