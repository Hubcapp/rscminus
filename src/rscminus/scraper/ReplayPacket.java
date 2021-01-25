/**
 * rscminus
 *
 * This file is part of rscminus.
 *
 * rscminus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * rscminus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with rscminus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * Authors: see <https://github.com/OrN/rscminus>
 */

package rscminus.scraper;

import rscminus.common.Logger;
import rscminus.common.MathUtil;
import rscminus.scraper.client.Class11;

import java.math.BigInteger;
import java.util.Comparator;

public class ReplayPacket {
    public int timestamp;
    public int opcode;
    public byte[] data;
    public boolean incoming;

    private static Class11 stringDecrypter = new Class11(new byte[]{(byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 21, (byte) 22, (byte) 22, (byte) 20, (byte) 22, (byte) 22, (byte) 22, (byte) 21, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 3, (byte) 8, (byte) 22, (byte) 16, (byte) 22, (byte) 16, (byte) 17, (byte) 7, (byte) 13, (byte) 13, (byte) 13, (byte) 16, (byte) 7, (byte) 10, (byte) 6, (byte) 16, (byte) 10, (byte) 11, (byte) 12, (byte) 12, (byte) 12, (byte) 12, (byte) 13, (byte) 13, (byte) 14, (byte) 14, (byte) 11, (byte) 14, (byte) 19, (byte) 15, (byte) 17, (byte) 8, (byte) 11, (byte) 9, (byte) 10, (byte) 10, (byte) 10, (byte) 10, (byte) 11, (byte) 10, (byte) 9, (byte) 7, (byte) 12, (byte) 11, (byte) 10, (byte) 10, (byte) 9, (byte) 10, (byte) 10, (byte) 12, (byte) 10, (byte) 9, (byte) 8, (byte) 12, (byte) 12, (byte) 9, (byte) 14, (byte) 8, (byte) 12, (byte) 17, (byte) 16, (byte) 17, (byte) 22, (byte) 13, (byte) 21, (byte) 4, (byte) 7, (byte) 6, (byte) 5, (byte) 3, (byte) 6, (byte) 6, (byte) 5, (byte) 4, (byte) 10, (byte) 7, (byte) 5, (byte) 6, (byte) 4, (byte) 4, (byte) 6, (byte) 10, (byte) 5, (byte) 4, (byte) 4, (byte) 5, (byte) 7, (byte) 6, (byte) 10, (byte) 6, (byte) 10, (byte) 22, (byte) 19, (byte) 22, (byte) 14, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 22, (byte) 21, (byte) 22, (byte) 21, (byte) 22, (byte) 22, (byte) 22, (byte) 21, (byte) 22, (byte) 22});
    private int m_position;
    private int m_bitmaskPosition;

    ReplayPacket() {
        m_position = 0;
        m_bitmaskPosition = 0;
    }

    public void startBitmask() {
        m_bitmaskPosition = m_position << 3;
    }

    public void endBitmask() {
        m_position = (m_bitmaskPosition + 7) >> 3;
    }

    public int readBitmask(int size) {
        int start = m_bitmaskPosition >> 3;
        int bitEnd = m_bitmaskPosition + size;
        int byteSize = ((bitEnd + 7) >> 3) - start;
        int offset = ((start + byteSize) << 3) - bitEnd;
        int bitmask = MathUtil.getBitmask(size);

        int ret = 0;
        for (int i = 0; i < byteSize; i++) {
            int dataOffset = start + (byteSize - i - 1);
            ret |= (data[dataOffset] & 0xFF) << (i << 3);
        }

        m_bitmaskPosition += size;
        return (ret >> offset) & bitmask;
    }

    public int tell() {
        return m_position;
    }

    public int tellBitmask() {
        return m_bitmaskPosition;
    }

    public void seek(int position) {
        m_position = position;
    }

    public void skip(int size) {
        m_position += size;
    }

    public void trim(int count) {
        int size = data.length - count;
        byte[] newData = new byte[size];
        System.arraycopy(data, 0, newData, 0, m_position);
        System.arraycopy(data, m_position + count, newData, m_position, data.length - m_position - count);
        data = newData;
    }

    public String readPaddedString() {
        if (readByte() == 0) {
            return readString();
        } else {
            throw new IllegalStateException("Padded String didn't begin with null byte!");
        }
    }

    public String readRSCString() {
        int length = readUnsignedByte();
        if (length >= 128) {
            m_position--;
            length = readUnsignedShort() - 32768;
        }
        byte[] byteData = new byte[length];
        int count = stringDecrypter.method240(data, 0, byteData, true, m_position, length);
        skip(count);
        return new String(byteData, 0, length);
    }

    public String readString() {
        int length = 0;
        if (data.length <= 1) {
            return "";
        }
        while (data[m_position + length] != '\0')
            length++;
        String ret;
        ret = new String(data, m_position, length);
        m_position += length + 1;
        return ret;
    }

    public BigInteger readUnsignedLong() {
        BigInteger bi = BigInteger.valueOf(readUnsignedInt()).shiftLeft(32);
        return bi.or(BigInteger.valueOf(readUnsignedInt()));
    }

    public long readUnsignedInt() {
        return (((long) readUnsignedByte()) << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | readUnsignedByte();
    }

    public int readUnsignedShort() {
        return (readUnsignedByte() << 8) | readUnsignedByte();
    }

    public long readUnsignedInt3() {
        if (data[m_position] >= 0) {
            return readUnsignedShort();
        } else {
            return readUnsignedInt() & Integer.MAX_VALUE;
        }
    }

    public int readUnsignedShortLE() {
        int a = readUnsignedByte();
        int b = readUnsignedByte() << 8;
        return b | a;
    }

    public byte readByte() {
        return data[m_position++];
    }

    public int readUnsignedByte() {
        return readByte() & 0xFF;
    }

    public void writeUnsignedByte(int value) {
        data[m_position++] = (byte) (value & 0xFF);
    }

    public void writeUnsignedShort(int value) {
        writeUnsignedByte(value >> 8);
        writeUnsignedByte(value);
    }

}

class ReplayPacketComparator implements Comparator<ReplayPacket> {

    @Override
    public int compare(ReplayPacket a, ReplayPacket b) {
        if (a == null && b != null) {
            return 15;
        } else if(b == null && a != null) {
            return -15;
        } else if (a == null && b == null) {
            return 0;
        }

        if (a.data == null && b.data != null) {
            return 15;
        } else if(b.data == null && a.data != null) {
            return -15;
        } else if (a.data == null && b.data == null) {
            return 0;
        }

        int offset = a.timestamp - b.timestamp;

        if (offset > 0) { // item a happened before item b
            offset = 10;
        } else if (offset < 0) { // item b happened before item a
            offset = -10;
        } else {
            int opcodeOffset = a.opcode - b.opcode;

            // Player coordinate MUST happen first, because so many opcodes send data as offsets of player coord
            if (a.opcode == 191 && b.opcode != 191) {
                offset = -5;
            } else if (b.opcode == 191 && a.opcode != 191) {
                offset = 5;
            } else {
                if (opcodeOffset > 0) {
                    offset = 5;
                } else if (opcodeOffset < 0) {
                    offset = -5;
                } else {
                    if (a.incoming == b.incoming) {
                        if (a.data.length > b.data.length) {
                            offset = -2;
                        } else if (b.data.length > a.data.length) {
                            offset = 2;
                        } else {
                            for (int i = 0; i < a.data.length; i++) {
                                if (a.data[i] != b.data[i]) {
                                    if (a.data[i] > b.data[i]) {
                                        offset = 1;
                                    } else {
                                        offset = -1;
                                    }
                                    break;
                                }
                            }
                            // Packets are truly equal if offset is still equal to zero after all the above.
                            // timestamp, opcode, incoming/outgoing, and data are all the same.
                        }

                    } else {
                        if (a.incoming) {
                            offset = 3;
                        } else {
                            offset = -3;
                        }
                    }
                }
            }
        }
        return offset;
    }
}
