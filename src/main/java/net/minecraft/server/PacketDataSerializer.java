package net.minecraft.server;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufProcessor;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import io.netty.util.ReferenceCounted;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

public class PacketDataSerializer extends ByteBuf {

    private final ByteBuf a;

    public PacketDataSerializer(ByteBuf bytebuf) {
        this.a = bytebuf;
    }

    public static int a(int i) {
        for (int j = 1; j < 5; ++j) {
            if ((i & -1 << j * 7) == 0) {
                return j;
            }
        }

        return 5;
    }

    public void a(byte[] abyte) {
        this.b(abyte.length);
        this.writeBytes(abyte);
    }

    // Paper start
    public byte[] a() {
        return readByteArray(Short.MAX_VALUE);
    }

    public byte[]readByteArray(int limit) {
        int len = this.e();
        if (len > limit) throw new DecoderException("The received a byte array longer than allowed " + len + " > " + limit);
        byte[] abyte = new byte[len];
    // Paper end

        this.readBytes(abyte);
        return abyte;
    }

    public BlockPosition c() {
        return BlockPosition.fromLong(this.readLong());
    }

    public void a(BlockPosition blockposition) {
        this.writeLong(blockposition.asLong());
    }

    public IChatBaseComponent d() throws IOException {
        return IChatBaseComponent.ChatSerializer.a(this.c(32767));
    }

    public void a(IChatBaseComponent ichatbasecomponent) throws IOException {
        this.a(IChatBaseComponent.ChatSerializer.a(ichatbasecomponent));
    }

    public <T extends Enum<T>> T a(Class<T> oclass) {
        return ((T[]) oclass.getEnumConstants())[this.e()]; // CraftBukkit - fix decompile error
    }

    public void a(Enum<?> oenum) {
        this.b(oenum.ordinal());
    }

    public int e() {
        int i = 0;
        int j = 0;

        byte b0;

        do {
            b0 = this.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public long f() {
        long i = 0L;
        int j = 0;

        byte b0;

        do {
            b0 = this.readByte();
            i |= (long) (b0 & 127) << j++ * 7;
            if (j > 10) {
                throw new RuntimeException("VarLong too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    public void a(UUID uuid) {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
    }

    public UUID g() {
        return new UUID(this.readLong(), this.readLong());
    }

    public void b(int i) {
        while ((i & -128) != 0) {
            this.writeByte(i & 127 | 128);
            i >>>= 7;
        }

        this.writeByte(i);
    }

    public void b(long i) {
        while ((i & -128L) != 0L) {
            this.writeByte((int) (i & 127L) | 128);
            i >>>= 7;
        }

        this.writeByte((int) i);
    }

    public void a(NBTTagCompound nbttagcompound) {
        if (nbttagcompound == null) {
            this.writeByte(0);
        } else {
            try {
                NBTCompressedStreamTools.a(nbttagcompound, (DataOutput) (new ByteBufOutputStream(this)));
            } catch (Exception ioexception) { // CraftBukkit - IOException -> Exception
                throw new EncoderException(ioexception);
            }
        }

    }

    public NBTTagCompound h() throws IOException {
        int i = this.readerIndex();
        byte b0 = this.readByte();

        if (b0 == 0) {
            return null;
        } else {
            this.readerIndex(i);
            return NBTCompressedStreamTools.a((DataInput) (new ByteBufInputStream(this)), new NBTReadLimiter(2097152L));
        }
    }

    public void a(ItemStack itemstack) {
        if (itemstack == null || itemstack.getItem() == null) { // CraftBukkit - NPE fix itemstack.getItem()
            this.writeShort(-1);
        } else {
            this.writeShort(Item.getId(itemstack.getItem()));
            this.writeByte(itemstack.count);
            this.writeShort(itemstack.getData());
            NBTTagCompound nbttagcompound = null;

            if (itemstack.getItem().usesDurability() || itemstack.getItem().p()) {
                // Spigot start - filter
                itemstack = itemstack.cloneItemStack();
                CraftItemStack.setItemMeta(itemstack, CraftItemStack.getItemMeta(itemstack));
                // Spigot end
                nbttagcompound = itemstack.getTag();
            }

            this.a(nbttagcompound);
        }

    }

    public ItemStack i() throws IOException {
        ItemStack itemstack = null;
        short short0 = this.readShort();

        if (short0 >= 0) {
            byte b0 = this.readByte();
            short short1 = this.readShort();

            itemstack = new ItemStack(Item.getById(short0), b0, short1);
            itemstack.setTag(this.h());
            // CraftBukkit start
            if (itemstack.getTag() != null) {
                CraftItemStack.setItemMeta(itemstack, CraftItemStack.getItemMeta(itemstack));
            }
            // CraftBukkit end
        }

        return itemstack;
    }

    public String c(int i) {
        int j = this.e();

        if (j > i * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + i * 4 + ")");
        } else if (j < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            //FlamePaper NETTY
            String s = new String(io.netty.buffer.ByteBufUtil.getBytes(this, this.readerIndex(), j), Charset.forName("UTF-8"));
            this.readerIndex(readerIndex() + j);

            if (s.length() > i) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + j + " > " + i + ")");
            } else {
                return s;
            }
        }
    }

    public PacketDataSerializer a(String s) {
        byte[] abyte = s.getBytes(Charsets.UTF_8);

        if (abyte.length > 32767) {
            throw new EncoderException("String too big (was " + s.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            this.b(abyte.length);
            this.writeBytes(abyte);
            return this;
        }
    }
    //FlamePaper NETTY
    public int refCnt() {
        return a.refCnt();
    }

    public boolean release() {
        return a.release();
    }

    public boolean release(int decrement) {
        return a.release(decrement);
    }

    public int capacity() {
        return a.capacity();
    }

    public ByteBuf capacity(int newCapacity) {
        return a.capacity(newCapacity);
    }

    public int maxCapacity() {
        return a.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return a.alloc();
    }

    public ByteOrder order() {
        return a.order();
    }

    public ByteBuf order(ByteOrder endianness) {
        return a.order(endianness);
    }

    public ByteBuf unwrap() {
        return a.unwrap();
    }

    public boolean isDirect() {
        return a.isDirect();
    }

    public boolean isReadOnly() {
        return a.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return a.asReadOnly();
    }

    public int readerIndex() {
        return a.readerIndex();
    }

    public ByteBuf readerIndex(int readerIndex) {
        return a.readerIndex(readerIndex);
    }

    public int writerIndex() {
        return a.writerIndex();
    }

    public ByteBuf writerIndex(int writerIndex) {
        return a.writerIndex(writerIndex);
    }

    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return a.setIndex(readerIndex, writerIndex);
    }

    public int readableBytes() {
        return a.readableBytes();
    }

    public int writableBytes() {
        return a.writableBytes();
    }

    public int maxWritableBytes() {
        return a.maxWritableBytes();
    }

    public int maxFastWritableBytes() {
        return a.maxFastWritableBytes();
    }

    public boolean isReadable() {
        return a.isReadable();
    }

    public boolean isReadable(int size) {
        return a.isReadable(size);
    }

    public boolean isWritable() {
        return a.isWritable();
    }

    public boolean isWritable(int size) {
        return a.isWritable(size);
    }

    public ByteBuf clear() {
        return a.clear();
    }

    public ByteBuf markReaderIndex() {
        return a.markReaderIndex();
    }

    public ByteBuf resetReaderIndex() {
        return a.resetReaderIndex();
    }

    public ByteBuf markWriterIndex() {
        return a.markWriterIndex();
    }

    public ByteBuf resetWriterIndex() {
        return a.resetWriterIndex();
    }

    public ByteBuf discardReadBytes() {
        return a.discardReadBytes();
    }

    public ByteBuf discardSomeReadBytes() {
        return a.discardSomeReadBytes();
    }

    public ByteBuf ensureWritable(int minWritableBytes) {
        return a.ensureWritable(minWritableBytes);
    }

    public int ensureWritable(int minWritableBytes, boolean force) {
        return a.ensureWritable(minWritableBytes, force);
    }

    public boolean getBoolean(int index) {
        return a.getBoolean(index);
    }

    public byte getByte(int index) {
        return a.getByte(index);
    }

    public short getUnsignedByte(int index) {
        return a.getUnsignedByte(index);
    }

    public short getShort(int index) {
        return a.getShort(index);
    }

    public short getShortLE(int index) {
        return a.getShortLE(index);
    }

    public int getUnsignedShort(int index) {
        return a.getUnsignedShort(index);
    }

    public int getUnsignedShortLE(int index) {
        return a.getUnsignedShortLE(index);
    }

    public int getMedium(int index) {
        return a.getMedium(index);
    }

    public int getMediumLE(int index) {
        return a.getMediumLE(index);
    }

    public int getUnsignedMedium(int index) {
        return a.getUnsignedMedium(index);
    }

    public int getUnsignedMediumLE(int index) {
        return a.getUnsignedMediumLE(index);
    }

    public int getInt(int index) {
        return a.getInt(index);
    }

    public int getIntLE(int index) {
        return a.getIntLE(index);
    }

    public long getUnsignedInt(int index) {
        return a.getUnsignedInt(index);
    }

    public long getUnsignedIntLE(int index) {
        return a.getUnsignedIntLE(index);
    }

    public long getLong(int index) {
        return a.getLong(index);
    }

    public long getLongLE(int index) {
        return a.getLongLE(index);
    }

    public char getChar(int index) {
        return a.getChar(index);
    }

    public float getFloat(int index) {
        return a.getFloat(index);
    }

    public float getFloatLE(int index) {
        return a.getFloatLE(index);
    }

    public double getDouble(int index) {
        return a.getDouble(index);
    }

    public double getDoubleLE(int index) {
        return a.getDoubleLE(index);
    }

    public ByteBuf getBytes(int index, ByteBuf dst) {
        return a.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return a.getBytes(index, dst, length);
    }

    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return a.getBytes(index, dst, dstIndex, length);
    }

    public ByteBuf getBytes(int index, byte[] dst) {
        return a.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return a.getBytes(index, dst, dstIndex, length);
    }

    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return a.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return a.getBytes(index, out, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return a.getBytes(index, out, length);
    }

    public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return a.getBytes(index, out, position, length);
    }

    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return a.getCharSequence(index, length, charset);
    }

    public ByteBuf setBoolean(int index, boolean value) {
        return a.setBoolean(index, value);
    }

    public ByteBuf setByte(int index, int value) {
        return a.setByte(index, value);
    }

    public ByteBuf setShort(int index, int value) {
        return a.setShort(index, value);
    }

    public ByteBuf setShortLE(int index, int value) {
        return a.setShortLE(index, value);
    }

    public ByteBuf setMedium(int index, int value) {
        return a.setMedium(index, value);
    }

    public ByteBuf setMediumLE(int index, int value) {
        return a.setMediumLE(index, value);
    }

    public ByteBuf setInt(int index, int value) {
        return a.setInt(index, value);
    }

    public ByteBuf setIntLE(int index, int value) {
        return a.setIntLE(index, value);
    }

    public ByteBuf setLong(int index, long value) {
        return a.setLong(index, value);
    }

    public ByteBuf setLongLE(int index, long value) {
        return a.setLongLE(index, value);
    }

    public ByteBuf setChar(int index, int value) {
        return a.setChar(index, value);
    }

    public ByteBuf setFloat(int index, float value) {
        return a.setFloat(index, value);
    }

    public ByteBuf setFloatLE(int index, float value) {
        return a.setFloatLE(index, value);
    }

    public ByteBuf setDouble(int index, double value) {
        return a.setDouble(index, value);
    }

    public ByteBuf setDoubleLE(int index, double value) {
        return a.setDoubleLE(index, value);
    }

    public ByteBuf setBytes(int index, ByteBuf src) {
        return a.setBytes(index, src);
    }

    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return a.setBytes(index, src, length);
    }

    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return a.setBytes(index, src, srcIndex, length);
    }

    public ByteBuf setBytes(int index, byte[] src) {
        return a.setBytes(index, src);
    }

    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return a.setBytes(index, src, srcIndex, length);
    }

    public ByteBuf setBytes(int index, ByteBuffer src) {
        return a.setBytes(index, src);
    }

    public int setBytes(int index, InputStream in, int length) throws IOException {
        return a.setBytes(index, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return a.setBytes(index, in, length);
    }

    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        return a.setBytes(index, in, position, length);
    }

    public ByteBuf setZero(int index, int length) {
        return a.setZero(index, length);
    }

    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return a.setCharSequence(index, sequence, charset);
    }

    public boolean readBoolean() {
        return a.readBoolean();
    }

    public byte readByte() {
        return a.readByte();
    }

    public short readUnsignedByte() {
        return a.readUnsignedByte();
    }

    public short readShort() {
        return a.readShort();
    }

    public short readShortLE() {
        return a.readShortLE();
    }

    public int readUnsignedShort() {
        return a.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return a.readUnsignedShortLE();
    }

    public int readMedium() {
        return a.readMedium();
    }

    public int readMediumLE() {
        return a.readMediumLE();
    }

    public int readUnsignedMedium() {
        return a.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return a.readUnsignedMediumLE();
    }

    public int readInt() {
        return a.readInt();
    }

    public int readIntLE() {
        return a.readIntLE();
    }

    public long readUnsignedInt() {
        return a.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return a.readUnsignedIntLE();
    }

    public long readLong() {
        return a.readLong();
    }

    public long readLongLE() {
        return a.readLongLE();
    }

    public char readChar() {
        return a.readChar();
    }

    public float readFloat() {
        return a.readFloat();
    }

    public float readFloatLE() {
        return a.readFloatLE();
    }

    public double readDouble() {
        return a.readDouble();
    }

    public double readDoubleLE() {
        return a.readDoubleLE();
    }

    public ByteBuf readBytes(int length) {
        return a.readBytes(length);
    }

    public ByteBuf readSlice(int length) {
        return a.readSlice(length);
    }

    public ByteBuf readRetainedSlice(int length) {
        return a.readRetainedSlice(length);
    }

    public ByteBuf readBytes(ByteBuf dst) {
        return a.readBytes(dst);
    }

    public ByteBuf readBytes(ByteBuf dst, int length) {
        return a.readBytes(dst, length);
    }

    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return a.readBytes(dst, dstIndex, length);
    }

    public ByteBuf readBytes(byte[] dst) {
        return a.readBytes(dst);
    }

    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return a.readBytes(dst, dstIndex, length);
    }

    public ByteBuf readBytes(ByteBuffer dst) {
        return a.readBytes(dst);
    }

    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return a.readBytes(out, length);
    }

    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return a.readBytes(out, length);
    }

    public CharSequence readCharSequence(int length, Charset charset) {
        return a.readCharSequence(length, charset);
    }

    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return a.readBytes(out, position, length);
    }

    public ByteBuf skipBytes(int length) {
        return a.skipBytes(length);
    }

    public ByteBuf writeBoolean(boolean value) {
        return a.writeBoolean(value);
    }

    public ByteBuf writeByte(int value) {
        return a.writeByte(value);
    }

    public ByteBuf writeShort(int value) {
        return a.writeShort(value);
    }

    public ByteBuf writeShortLE(int value) {
        return a.writeShortLE(value);
    }

    public ByteBuf writeMedium(int value) {
        return a.writeMedium(value);
    }

    public ByteBuf writeMediumLE(int value) {
        return a.writeMediumLE(value);
    }

    public ByteBuf writeInt(int value) {
        return a.writeInt(value);
    }

    public ByteBuf writeIntLE(int value) {
        return a.writeIntLE(value);
    }

    public ByteBuf writeLong(long value) {
        return a.writeLong(value);
    }

    public ByteBuf writeLongLE(long value) {
        return a.writeLongLE(value);
    }

    public ByteBuf writeChar(int value) {
        return a.writeChar(value);
    }

    public ByteBuf writeFloat(float value) {
        return a.writeFloat(value);
    }

    public ByteBuf writeFloatLE(float value) {
        return a.writeFloatLE(value);
    }

    public ByteBuf writeDouble(double value) {
        return a.writeDouble(value);
    }

    public ByteBuf writeDoubleLE(double value) {
        return a.writeDoubleLE(value);
    }

    public ByteBuf writeBytes(ByteBuf src) {
        return a.writeBytes(src);
    }

    public ByteBuf writeBytes(ByteBuf src, int length) {
        return a.writeBytes(src, length);
    }

    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return a.writeBytes(src, srcIndex, length);
    }

    public ByteBuf writeBytes(byte[] src) {
        return a.writeBytes(src);
    }

    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return a.writeBytes(src, srcIndex, length);
    }

    public ByteBuf writeBytes(ByteBuffer src) {
        return a.writeBytes(src);
    }

    public int writeBytes(InputStream in, int length) throws IOException {
        return a.writeBytes(in, length);
    }

    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return a.writeBytes(in, length);
    }

    public int writeBytes(FileChannel in, long position, int length) throws IOException {
        return a.writeBytes(in, position, length);
    }

    public ByteBuf writeZero(int length) {
        return a.writeZero(length);
    }

    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return a.writeCharSequence(sequence, charset);
    }

    public int indexOf(int fromIndex, int toIndex, byte value) {
        return a.indexOf(fromIndex, toIndex, value);
    }

    public int bytesBefore(byte value) {
        return a.bytesBefore(value);
    }

    public int bytesBefore(int length, byte value) {
        return a.bytesBefore(length, value);
    }

    public int bytesBefore(int index, int length, byte value) {
        return a.bytesBefore(index, length, value);
    }

    public int forEachByte(ByteProcessor processor) {
        return a.forEachByte(processor);
    }

    public int forEachByte(int index, int length, ByteProcessor processor) {
        return a.forEachByte(index, length, processor);
    }

    public int forEachByteDesc(ByteProcessor processor) {
        return a.forEachByteDesc(processor);
    }

    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return a.forEachByteDesc(index, length, processor);
    }

    public ByteBuf copy() {
        return a.copy();
    }

    public ByteBuf copy(int index, int length) {
        return a.copy(index, length);
    }

    public ByteBuf slice() {
        return a.slice();
    }

    public ByteBuf retainedSlice() {
        return a.retainedSlice();
    }

    public ByteBuf slice(int index, int length) {
        return a.slice(index, length);
    }

    public ByteBuf retainedSlice(int index, int length) {
        return a.retainedSlice(index, length);
    }

    public ByteBuf duplicate() {
        return a.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return a.retainedDuplicate();
    }

    public int nioBufferCount() {
        return a.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return a.nioBuffer();
    }

    public ByteBuffer nioBuffer(int index, int length) {
        return a.nioBuffer(index, length);
    }

    public ByteBuffer internalNioBuffer(int index, int length) {
        return a.internalNioBuffer(index, length);
    }

    public ByteBuffer[] nioBuffers() {
        return a.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int index, int length) {
        return a.nioBuffers(index, length);
    }

    public boolean hasArray() {
        return a.hasArray();
    }

    public byte[] array() {
        return a.array();
    }

    public int arrayOffset() {
        return a.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return a.hasMemoryAddress();
    }

    public long memoryAddress() {
        return a.memoryAddress();
    }

    public boolean isContiguous() {
        return a.isContiguous();
    }

    public ByteBuf asByteBuf() {
        return a.asByteBuf();
    }

    public String toString(Charset charset) {
        return a.toString(charset);
    }

    public String toString(int index, int length, Charset charset) {
        return a.toString(index, length, charset);
    }

    public int hashCode() {
        return a.hashCode();
    }

    public boolean equals(Object obj) {
        return a.equals(obj);
    }

    public int compareTo(ByteBuf buffer) {
        return a.compareTo(buffer);
    }

    public String toString() {
        return a.toString();
    }

    public ByteBuf retain(int increment) {
        return a.retain(increment);
    }

    public ByteBuf retain() {
        return a.retain();
    }

    public ByteBuf touch() {
        return a.touch();
    }

    public ByteBuf touch(Object hint) {
        return a.touch(hint);
    }
}
