package com.example.playsymphonyapp.Editor;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public final class PcmMixer implements Closeable {
    public static final int RATE = 44100;

    public static final class Channel {
        public final File file;
        public final long startMs;
        public final float volume;
        public final float pan;
        public final boolean mute;
        public final boolean solo;

        public Channel(
                File file,
                long startMs,
                float volume,
                float pan,
                boolean mute,
                boolean solo
        ) {
            this.file = file;
            this.startMs = Math.max(0, startMs);
            this.volume = Math.max(0, Math.min(1, volume));
            this.pan = Math.max(-1, Math.min(1, pan));
            this.mute = mute;
            this.solo = solo;
        }
    }

    private final List<RandomAccessFile> files = new ArrayList<>();
    private byte[] input = new byte[0];
    private float[] sum = new float[0];

    public PcmMixer(List<Channel> channels) throws IOException {
        try {
            for (Channel channel : channels) {
                files.add(new RandomAccessFile(channel.file, "r"));
            }
        } catch (IOException error) {
            close();
            throw error;
        }
    }

    public static long durationFrames(List<Channel> channels) {
        long end = 0;

        for (Channel channel : channels) {
            end = Math.max(
                    end,
                    channel.startMs * RATE / 1000
                            + channel.file.length() / 4
            );
        }

        return end;
    }

    public short[] read(
            long frame,
            int frames,
            List<Channel> channels,
            float master
    ) throws IOException {
        if (channels.size() != files.size()) {
            throw new IllegalArgumentException("Channel list changed");
        }

        int samples = frames * 2;

        if (input.length < frames * 4) {
            input = new byte[frames * 4];
        }

        if (sum.length < samples) {
            sum = new float[samples];
        }

        java.util.Arrays.fill(sum, 0, samples, 0f);

        boolean hasSolo = false;

        for (Channel channel : channels) {
            if (channel.solo) hasSolo = true;
        }

        for (int index = 0; index < channels.size(); index++) {
            Channel channel = channels.get(index);

            if (channel.mute || (hasSolo && !channel.solo)) {
                continue;
            }

            RandomAccessFile file = files.get(index);

            long start = channel.startMs * RATE / 1000;
            long first = Math.max(frame, start);
            long last = Math.min(
                    frame + frames,
                    start + file.length() / 4
            );

            if (last <= first) continue;

            int count = (int) (last - first);
            int outputOffset = (int) (first - frame) * 2;

            file.seek((first - start) * 4);
            file.readFully(input, 0, count * 4);

            float left = channel.volume
                    * (channel.pan > 0 ? 1 - channel.pan : 1);

            float right = channel.volume
                    * (channel.pan < 0 ? 1 + channel.pan : 1);

            for (int i = 0; i < count * 2; i++) {
                int at = i * 2;

                short sample = (short) (
                        (input[at] & 255)
                                | (input[at + 1] << 8)
                );

                sum[outputOffset + i] += sample
                        * ((i & 1) == 0 ? left : right);
            }
        }

        short[] output = new short[samples];
        float gain = Math.max(0, Math.min(1, master));

        for (int i = 0; i < samples; i++) {
            output[i] = (short) Math.max(
                    -32768,
                    Math.min(32767, Math.round(sum[i] * gain))
            );
        }

        return output;
    }

    public static byte[] littleEndian(short[] samples) {
        byte[] bytes = new byte[samples.length * 2];

        for (int i = 0; i < samples.length; i++) {
            bytes[i * 2] = (byte) samples[i];
            bytes[i * 2 + 1] = (byte) (samples[i] >> 8);
        }

        return bytes;
    }

    @Override
    public void close() {
        for (RandomAccessFile file : files) {
            try {
                file.close();
            } catch (IOException ignored) {
            }
        }

        files.clear();
    }
}