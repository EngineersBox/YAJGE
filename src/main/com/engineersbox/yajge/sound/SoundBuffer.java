package com.engineersbox.yajge.sound;

import com.engineersbox.yajge.resources.loader.ResourceLoader;
import com.engineersbox.yajge.util.AllocUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundBuffer {

    private final int bufferId;
    private ShortBuffer pcm = null;

    public SoundBuffer(final String file) {
        this.bufferId = alGenBuffers();
        try (final STBVorbisInfo info = STBVorbisInfo.malloc()) {
            final ShortBuffer pcm = readVorbis(file, info);
            alBufferData(this.bufferId, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
    }

    public int getBufferId() {
        return this.bufferId;
    }

    public void cleanup() {
        alDeleteBuffers(this.bufferId);
        AllocUtils.freeAll(this.pcm);
    }

    private ShortBuffer readVorbis(final String resource,
                                   final STBVorbisInfo info) {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer vorbis = ResourceLoader.ioResourceToByteBuffer(resource);
            final IntBuffer error = stack.mallocInt(1);
            final long decoder = STBVorbis.stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == NULL) {
                throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }

            STBVorbis.stb_vorbis_get_info(decoder, info);

            final int channels = info.channels();
            final int lengthSamples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            this.pcm = MemoryUtil.memAllocShort(lengthSamples);
            this.pcm.limit(STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, this.pcm) * channels);
            STBVorbis.stb_vorbis_close(decoder);
            return this.pcm;
        }
    }
}
